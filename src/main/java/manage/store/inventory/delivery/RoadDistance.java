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

    private static final Pattern VIETMAP_URL = Pattern.compile("https://maps\\.vietmap\\.vn(/[^\"\\\\]*)");
    private static final String ROOT = "https://maps.vietmap.vn";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public int[][] meters(List<DeliveryStop> stops, String apiKey) {
        requireKey(apiKey);
        for (DeliveryStop stop : stops) {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                throw new BusinessException("Điểm " + stop.getStopName() + " chưa có tọa độ.");
            }
        }
        StringBuilder url = new StringBuilder(ROOT + "/api/matrix/v4?apikey=" + enc(apiKey) + "&annotation=distance&vehicle=car");
        for (DeliveryStop stop : stops) {
            url.append("&point=").append(stop.getLatitude()).append(',').append(stop.getLongitude());
        }
        JsonNode body = read(url.toString());
        if (!"OK".equals(body.path("code").asText())) {
            throw new BusinessException("Vietmap không tính được km: " + body.path("messages").asText(""));
        }
        JsonNode distances = body.path("distances");
        int n = stops.size();
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                JsonNode cell = distances.path(i).path(j);
                if (cell.isMissingNode() || cell.isNull()) {
                    throw new BusinessException("Không có đường bộ giữa " + stops.get(i).getStopName()
                            + " và " + stops.get(j).getStopName());
                }
                matrix[i][j] = (int) Math.round(cell.asDouble());
            }
        }
        return matrix;
    }

    public List<List<Double>> line(List<DeliveryStop> stops, String apiKey) {
        List<List<Double>> points = new ArrayList<>();
        if (apiKey == null || apiKey.isBlank() || stops.size() < 2) {
            return points;
        }
        StringBuilder url = new StringBuilder(ROOT + "/api/route/v4?apikey=" + enc(apiKey) + "&vehicle=car&points_encoded=false");
        for (DeliveryStop stop : stops) {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                return points;
            }
            url.append("&point=").append(stop.getLatitude()).append(',').append(stop.getLongitude());
        }
        try {
            JsonNode body = read(url.toString());
            return latLngPairs(body.path("paths").path(0).path("points"));
        } catch (RuntimeException ex) {
            return points;
        }
    }

    public List<Map<String, Object>> geocode(String query, String focus, String apiKey) {
        List<Map<String, Object>> found = new ArrayList<>();
        if (query == null || query.trim().length() < 2 || apiKey == null || apiKey.isBlank()) {
            return found;
        }
        StringBuilder url = new StringBuilder(ROOT + "/api/autocomplete/v4?apikey=" + enc(apiKey)
                + "&display_type=5&text=" + enc(query.trim()));
        if (focus != null && focus.contains(",")) {
            url.append("&focus=").append(enc(focus.trim()));
        }
        try {
            JsonNode body = read(url.toString());
            JsonNode rows = body.isArray() ? body : body.path("data");
            int count = 0;
            for (JsonNode node : rows) {
                if (count++ >= 6) {
                    break;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", node.path("display").asText());
                row.put("name", node.path("name").asText());
                row.put("refId", node.path("ref_id").asText());
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
            throw new BusinessException("Thiếu mã địa chỉ Vietmap.");
        }
        JsonNode node = read(ROOT + "/api/place/v4?apikey=" + enc(apiKey) + "&refid=" + enc(refId.trim()));
        if (!node.hasNonNull("lat") || !node.hasNonNull("lng")) {
            throw new BusinessException("Vietmap không trả tọa độ cho địa chỉ này.");
        }
        return addressRow(node);
    }

    public Map<String, Object> reverse(double latitude, double longitude, String apiKey) {
        requireKey(apiKey);
        JsonNode body = read(ROOT + "/api/reverse/v4?apikey=" + enc(apiKey)
                + "&display_type=5&lat=" + latitude + "&lng=" + longitude);
        JsonNode node = body.isArray() ? body.path(0) : body.path("data").isArray() ? body.path("data").path(0) : body;
        if (!node.hasNonNull("display") && !node.hasNonNull("lat")) {
            throw new BusinessException("Không đọc được địa chỉ tại điểm đã ghim.");
        }
        Map<String, Object> row = addressRow(node);
        row.putIfAbsent("latitude", latitude);
        row.putIfAbsent("longitude", longitude);
        return row;
    }

    public String style(String apiKey) {
        requireKey(apiKey);
        HttpResponse<String> response = send(ROOT + "/maps/styles/tm/style.json?apikey=" + enc(apiKey));
        if (response.statusCode() >= 300) {
            throw new BusinessException("Không tải được bản đồ Vietmap (" + response.statusCode() + ").");
        }
        return rewriteStyle(response.body());
    }

    public HttpResponse<byte[]> asset(String pathAndQuery, String apiKey) {
        requireKey(apiKey);
        if (pathAndQuery == null || !pathAndQuery.startsWith("/maps/") || pathAndQuery.contains("..")) {
            throw new BusinessException("Đường dẫn bản đồ không hợp lệ.");
        }
        String safe = pathAndQuery.replace(" ", "%20");
        String joiner = safe.contains("?") ? "&" : "?";
        URI uri = URI.create(ROOT + safe + joiner + "apikey=" + enc(apiKey));
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Không tải được mảnh bản đồ Vietmap.");
        }
    }

    static String rewriteStyle(String json) {
        Matcher matcher = VIETMAP_URL.matcher(json);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            String path = matcher.group(1);
            int query = path.indexOf('?');
            if (query >= 0) {
                String base = path.substring(0, query);
                StringBuilder kept = new StringBuilder();
                for (String part : path.substring(query + 1).split("&")) {
                    if (part.isEmpty() || part.startsWith("apikey=")) {
                        continue;
                    }
                    if (!kept.isEmpty()) {
                        kept.append('&');
                    }
                    kept.append(part);
                }
                path = kept.isEmpty() ? base : base + "?" + kept;
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement("/api/delivery/basemap/proxy" + path));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    static List<List<Double>> latLngPairs(JsonNode points) {
        List<List<Double>> pairs = new ArrayList<>();
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

    private Map<String, Object> addressRow(JsonNode node) {
        String display = node.path("display").asText("");
        String name = node.path("name").asText("");
        if (name.isBlank()) {
            name = node.path("address").asText("");
        }
        if (name.isBlank() && !display.isBlank()) {
            int comma = display.indexOf(',');
            name = comma > 0 ? display.substring(0, comma).trim() : display;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", display.isBlank() ? name : display);
        row.put("name", name);
        if (node.hasNonNull("lat") && node.hasNonNull("lng")) {
            row.put("latitude", node.get("lat").asDouble());
            row.put("longitude", node.get("lng").asDouble());
        }
        return row;
    }

    private JsonNode read(String url) {
        HttpResponse<String> response = send(url);
        if (response.statusCode() >= 300) {
            throw new BusinessException("Vietmap trả lỗi " + response.statusCode() + ".");
        }
        try {
            return objectMapper.readTree(response.body());
        } catch (Exception ex) {
            throw new BusinessException("Vietmap trả dữ liệu không đọc được.");
        }
    }

    private HttpResponse<String> send(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).GET().build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Không kết nối được Vietmap: " + ex.getMessage());
        }
    }

    private static void requireKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException("Chưa có khóa Vietmap. Mở Cài đặt và dán khóa từ maps.vietmap.vn.");
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
