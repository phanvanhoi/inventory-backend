package manage.store.inventory.delivery;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import manage.store.inventory.exception.BusinessException;

@Component
public class RoadDistance {

    private static final Pattern TILE_URL = Pattern.compile("https://tiles\\.goong\\.io(/[^\"\\\\]*)");
    private static final String API_ROOT = "https://rsapi.goong.io";
    private static final String TILE_ROOT = "https://tiles.goong.io";
    private static final String STYLE_PATH = "/assets/goong_map_web.json";
    private static final Pattern QUERY_KEY = Pattern.compile("[A-Za-z0-9_-]{1,40}");
    private static final Pattern QUERY_VALUE = Pattern.compile("[A-Za-z0-9._~%-]{0,80}");
    private static final Pattern ASSET_PATH = Pattern.compile("/[A-Za-z0-9._~%,{}@\\-/]+");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private String styleCache;
    private String styleCacheKey;
    private long styleCacheUntil;

    public int[][] meters(List<DeliveryStop> stops, String apiKey) {
        return meters(stops, apiKey, API_ROOT);
    }

    public int[][] meters(List<DeliveryStop> stops, String apiKey, String root) {
        requireKey(apiKey);
        for (DeliveryStop stop : stops) {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                throw new BusinessException("Điểm " + stop.getStopName() + " chưa có tọa độ.");
            }
        }
        int n = stops.size();
        String points = joinPoints(stops);
        String url = matrixRoot(root) + "/v2/distancematrix?origins=" + enc(points)
                + "&destinations=" + enc(points) + "&vehicle=car&api_key=" + enc(apiKey);
        JsonNode body = read(url);
        JsonNode rows = body.path("rows");
        if (!rows.isArray() || rows.size() < n) {
            String status = body.path("status").asText("");
            throw new BusinessException("Goong không tính được km" + (status.isBlank() ? "." : ": " + status));
        }
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            JsonNode elements = rows.path(i).path("elements");
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                JsonNode element = elements.path(j);
                JsonNode value = element.path("distance").path("value");
                if (!"OK".equals(element.path("status").asText()) || !value.isNumber()) {
                    throw new BusinessException("Không có đường bộ giữa " + stops.get(i).getStopName()
                            + " và " + stops.get(j).getStopName());
                }
                matrix[i][j] = (int) Math.round(value.asDouble());
            }
        }
        return matrix;
    }

    public List<List<Double>> line(List<DeliveryStop> stops, String apiKey) {
        List<List<Double>> points = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank() || stops.size() < 2) {
            return points;
        }
        try {
            for (int i = 0; i < stops.size() - 1; i++) {
                DeliveryStop from = stops.get(i);
                DeliveryStop to = stops.get(i + 1);
                if (from.getLatitude() == null || from.getLongitude() == null
                        || to.getLatitude() == null || to.getLongitude() == null) {
                    return List.of();
                }
                String url = API_ROOT + "/v2/direction?origin=" + from.getLatitude() + "," + from.getLongitude()
                        + "&destination=" + to.getLatitude() + "," + to.getLongitude()
                        + "&vehicle=car&api_key=" + enc(apiKey);
                JsonNode encoded = read(url).path("routes").path(0).path("overview_polyline").path("points");
                if (!encoded.isTextual() || encoded.asText().isBlank()) {
                    return List.of();
                }
                List<List<Double>> leg = decodePolyline(encoded.asText());
                if (leg.isEmpty()) {
                    return List.of();
                }
                if (!points.isEmpty() && leg.size() > 1) {
                    leg = leg.subList(1, leg.size());
                }
                points.addAll(leg);
            }
            return points;
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    public List<Map<String, Object>> geocode(String query, String focus, String apiKey) {
        List<Map<String, Object>> found = new ArrayList<>();
        if (query == null || query.trim().length() < 2 || apiKey == null || apiKey.isBlank()) {
            return found;
        }
        StringBuilder url = new StringBuilder(API_ROOT + "/v2/place/autocomplete?api_key=" + enc(apiKey)
                + "&limit=6&input=" + enc(query.trim()));
        if (focus != null && focus.contains(",")) {
            url.append("&location=").append(enc(focus.trim()));
        }
        try {
            JsonNode rows = read(url.toString()).path("predictions");
            if (!rows.isArray()) {
                return found;
            }
            for (JsonNode node : rows) {
                if (found.size() >= 6) {
                    break;
                }
                String placeId = node.path("place_id").asText("");
                if (placeId.isBlank()) {
                    continue;
                }
                Map<String, Object> row = placeRow(node);
                row.put("refId", placeId);
                found.add(row);
            }
            return found;
        } catch (RuntimeException ex) {
            return found;
        }
    }

    public Map<String, Object> place(String refId, String apiKey) {
        requireKey(apiKey);
        if (refId == null || refId.isBlank()) {
            throw new BusinessException("Thiếu mã địa chỉ Goong.");
        }
        JsonNode body = read(API_ROOT + "/v2/place/detail?api_key=" + enc(apiKey) + "&place_id=" + enc(refId.trim()));
        JsonNode result = body.path("result");
        Map<String, Object> row = placeRow(result);
        if (!row.containsKey("latitude") || !row.containsKey("longitude")) {
            throw new BusinessException("Goong không trả tọa độ cho địa chỉ này.");
        }
        return row;
    }

    public Map<String, Object> reverse(double latitude, double longitude, String apiKey) {
        requireKey(apiKey);
        JsonNode body = read(API_ROOT + "/v2/geocode?api_key=" + enc(apiKey)
                + "&limit=1&latlng=" + latitude + "," + longitude);
        JsonNode node = body.path("results").path(0);
        if (!node.isObject() || node.isEmpty()) {
            throw new BusinessException("Không đọc được địa chỉ tại điểm đã ghim.");
        }
        Map<String, Object> row = placeRow(node);
        row.putIfAbsent("latitude", latitude);
        row.putIfAbsent("longitude", longitude);
        return row;
    }

    public String style(String apiKey) {
        requireKey(apiKey);
        synchronized (this) {
            if (styleCache != null && apiKey.equals(styleCacheKey) && System.currentTimeMillis() < styleCacheUntil) {
                return styleCache;
            }
        }
        HttpResponse<String> response = send(TILE_ROOT + STYLE_PATH + "?api_key=" + enc(apiKey));
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new BusinessException("Goong từ chối khóa bản đồ. Dán khóa Map tiles từ account.goong.io.");
        }
        if (response.statusCode() >= 300) {
            throw new BusinessException("Goong trả lỗi " + response.statusCode() + ".");
        }
        String rewritten = rewriteStyle(response.body()).replace(apiKey, "");
        if (!rewritten.contains("/api/delivery/basemap/proxy/")) {
            throw new BusinessException("Goong không trả kiểu bản đồ.");
        }
        try {
            objectMapper.readTree(rewritten);
        } catch (Exception ex) {
            throw new BusinessException("Goong trả kiểu bản đồ không đọc được.");
        }
        synchronized (this) {
            styleCache = rewritten;
            styleCacheKey = apiKey;
            styleCacheUntil = System.currentTimeMillis() + 3_600_000L;
        }
        return rewritten;
    }

    public AssetFile asset(String pathAndQuery, String apiKey) {
        requireKey(apiKey);
        String safe = safeAssetPath(pathAndQuery);
        String joiner = safe.contains("?") ? "&" : "?";
        URI uri = URI.create(TILE_ROOT + safe + joiner + "api_key=" + enc(apiKey));
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            String type = response.headers().firstValue("content-type").orElse("");
            byte[] body = response.body() == null ? new byte[0] : response.body();
            String pathOnly = safe.contains("?") ? safe.substring(0, safe.indexOf('?')) : safe;
            if (response.statusCode() < 300 && (pathOnly.endsWith(".json") || type.contains("json"))) {
                body = rewriteStyle(new String(body, StandardCharsets.UTF_8)).replace(apiKey, "").getBytes(StandardCharsets.UTF_8);
                if (!type.contains("json")) {
                    type = "application/json; charset=utf-8";
                }
            }
            return new AssetFile(response.statusCode(), type, body);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Không tải được mảnh bản đồ Goong.");
        }
    }

    public record AssetFile(int statusCode, String contentType, byte[] body) {
    }

    static String pointAtProxy(String json, String host) {
        return json.replace("/api/delivery/basemap/proxy", proxyRoot(host));
    }

    private static String proxyRoot(String host) {
        if (host == null || !host.matches("[A-Za-z0-9.:_-]+")) {
            return "/api/delivery/basemap/proxy";
        }
        return "//" + host + "/api/delivery/basemap/proxy";
    }

    static String rewriteStyle(String json) {
        Matcher matcher = TILE_URL.matcher(json);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(out, Matcher.quoteReplacement("/api/delivery/basemap/proxy" + stripKey(matcher.group(1))));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    static List<List<Double>> decodePolyline(String encoded) {
        List<List<Double>> pairs = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;
        while (index < encoded.length()) {
            int[] latStep = polylineStep(encoded, index);
            index = latStep[0];
            lat += latStep[1];
            int[] lngStep = polylineStep(encoded, index);
            index = lngStep[0];
            lng += lngStep[1];
            pairs.add(List.of(lat / 1e5, lng / 1e5));
        }
        return pairs;
    }

    static List<List<Double>> latLngPairs(JsonNode points) {
        List<List<Double>> pairs = new ArrayList<>();
        if (points.isObject()) {
            points = points.path("coordinates");
        }
        if (!points.isArray()) {
            return pairs;
        }
        for (JsonNode pair : points) {
            if (!pair.isArray() || pair.size() < 2) {
                continue;
            }
            double first = pair.get(0).asDouble();
            double second = pair.get(1).asDouble();
            if (Math.abs(first) > 90) {
                pairs.add(List.of(second, first));
            } else {
                pairs.add(List.of(first, second));
            }
        }
        return pairs;
    }

    private static int[] polylineStep(String encoded, int index) {
        int shift = 0;
        int result = 0;
        int piece;
        do {
            piece = encoded.charAt(index++) - 63;
            result |= (piece & 0x1f) << shift;
            shift += 5;
        } while (piece >= 0x20);
        int delta = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
        return new int[] { index, delta };
    }

    private Map<String, Object> placeRow(JsonNode node) {
        String label = firstText(node, "formatted_address", "description", "display");
        String name = node.path("name").asText("");
        if (name.isBlank()) {
            name = node.path("structured_formatting").path("main_text").asText("");
        }
        if (name.isBlank() && !label.isBlank()) {
            int comma = label.indexOf(',');
            name = comma > 0 ? label.substring(0, comma).trim() : label;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label.isBlank() ? name : label);
        row.put("name", name);
        JsonNode location = node.path("geometry").path("location");
        if (location.hasNonNull("lat") && location.hasNonNull("lng")) {
            row.put("latitude", location.get("lat").asDouble());
            row.put("longitude", location.get("lng").asDouble());
        }
        return row;
    }

    private static String firstText(JsonNode node, String... names) {
        for (String name : names) {
            String text = node.path(name).asText("");
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String joinPoints(List<DeliveryStop> stops) {
        StringBuilder points = new StringBuilder();
        for (DeliveryStop stop : stops) {
            if (!points.isEmpty()) {
                points.append('|');
            }
            points.append(stop.getLatitude()).append(',').append(stop.getLongitude());
        }
        return points.toString();
    }

    private JsonNode read(String url) {
        HttpResponse<String> response = send(url);
        if (response.statusCode() >= 300) {
            throw new BusinessException("Goong trả lỗi " + response.statusCode() + ".");
        }
        try {
            return objectMapper.readTree(response.body());
        } catch (Exception ex) {
            throw new BusinessException("Goong trả dữ liệu không đọc được.");
        }
    }

    private HttpResponse<String> send(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Không kết nối được Goong.");
        }
    }

    static String safeAssetPath(String pathAndQuery) {
        if (pathAndQuery == null || pathAndQuery.contains("..") || pathAndQuery.contains("\\") || pathAndQuery.contains("//")) {
            throw new BusinessException("Đường dẫn bản đồ không hợp lệ.");
        }
        String path = pathAndQuery;
        String query = "";
        int split = pathAndQuery.indexOf('?');
        if (split >= 0) {
            path = pathAndQuery.substring(0, split);
            query = pathAndQuery.substring(split + 1);
        }
        path = path.replace(" ", "%20");
        if (!allowedAsset(path)) {
            throw new BusinessException("Đường dẫn bản đồ không hợp lệ.");
        }
        String filtered = filterQuery(query);
        return filtered.isEmpty() ? path : path + "?" + filtered;
    }

    private static boolean allowedAsset(String path) {
        if (!ASSET_PATH.matcher(path).matches()) {
            return false;
        }
        return path.startsWith("/assets/")
                || path.startsWith("/sources/")
                || path.startsWith("/maps/")
                || path.startsWith("/tiles/")
                || path.startsWith("/fonts/")
                || "/sprite.json".equals(path)
                || "/sprite.png".equals(path)
                || "/sprite@2x.json".equals(path)
                || "/sprite@2x.png".equals(path);
    }

    private static String filterQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        StringBuilder kept = new StringBuilder();
        for (String part : query.split("&")) {
            int eq = part.indexOf('=');
            String key = eq >= 0 ? part.substring(0, eq) : part;
            String value = eq >= 0 ? part.substring(eq + 1) : "";
            if (key.isEmpty() || "apikey".equalsIgnoreCase(key) || "api_key".equalsIgnoreCase(key)) {
                continue;
            }
            if (!QUERY_KEY.matcher(key).matches() || !QUERY_VALUE.matcher(value).matches()) {
                continue;
            }
            if (!kept.isEmpty()) {
                kept.append('&');
            }
            kept.append(key);
            if (eq >= 0) {
                kept.append('=').append(value);
            }
        }
        return kept.toString();
    }

    private static String stripKey(String path) {
        int query = path.indexOf('?');
        if (query < 0) {
            return path;
        }
        String filtered = filterQuery(path.substring(query + 1));
        return filtered.isEmpty() ? path.substring(0, query) : path.substring(0, query) + "?" + filtered;
    }

    private static String matrixRoot(String root) {
        if (root == null || root.isBlank()) {
            return API_ROOT;
        }
        String trimmed = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
        if (!trimmed.equals(API_ROOT) && !trimmed.startsWith(API_ROOT + "/")) {
            throw new BusinessException("Nguồn km phải là rsapi.goong.io.");
        }
        return trimmed;
    }

    private static void requireKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("Chưa có khóa Goong. Mở Cài đặt và dán khóa từ account.goong.io.");
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
