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

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import manage.store.inventory.exception.BusinessException;

@Component
public class RoadDistance {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public int[][] meters(List<DeliveryStop> stops, String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException("Chưa cấu hình nguồn km đường bộ.");
        }
        for (DeliveryStop stop : stops) {
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                throw new BusinessException("Điểm " + stop.getStopName() + " chưa có tọa độ.");
            }
        }
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            if (i > 0) {
                path.append(';');
            }
            path.append(stops.get(i).getLongitude()).append(',').append(stops.get(i).getLatitude());
        }
        String root = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        URI uri = URI.create(root + "/table/v1/driving/" + path + "?annotations=distance");
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new BusinessException("Nguồn km đường bộ trả lỗi " + response.statusCode());
            }
            JsonNode distances = objectMapper.readTree(response.body()).path("distances");
            int n = stops.size();
            int[][] matrix = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    JsonNode cell = distances.get(i).get(j);
                    if (cell == null || cell.isNull()) {
                        throw new BusinessException("Không có đường bộ giữa " + stops.get(i).getStopName()
                                + " và " + stops.get(j).getStopName());
                    }
                    matrix[i][j] = (int) Math.round(cell.asDouble());
                }
            }
            return matrix;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Không tính được km đường bộ: " + ex.getMessage());
        }
    }

    public List<List<Double>> line(List<DeliveryStop> stops, String baseUrl) {
        List<List<Double>> points = new ArrayList<>();
        if (baseUrl == null || baseUrl.isBlank() || stops.size() < 2) {
            return points;
        }
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getLatitude() == null || stops.get(i).getLongitude() == null) {
                return points;
            }
            if (i > 0) {
                path.append(';');
            }
            path.append(stops.get(i).getLongitude()).append(',').append(stops.get(i).getLatitude());
        }
        String root = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        URI uri = URI.create(root + "/route/v1/driving/" + path + "?overview=full&geometries=geojson");
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(20)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                return points;
            }
            JsonNode coordinates = objectMapper.readTree(response.body()).path("routes").path(0).path("geometry").path("coordinates");
            if (!coordinates.isArray()) {
                return points;
            }
            for (JsonNode pair : coordinates) {
                points.add(List.of(pair.get(1).asDouble(), pair.get(0).asDouble()));
            }
            return points;
        } catch (Exception ex) {
            return points;
        }
    }

    public List<Map<String, Object>> geocode(String query) {
        List<Map<String, Object>> found = new ArrayList<>();
        if (query == null || query.trim().length() < 3) {
            return found;
        }
        String url = "https://nominatim.openstreetmap.org/search?format=json&countrycodes=vn&accept-language=vi&limit=5&q="
                + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "HangFashionDelivery/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                return found;
            }
            for (JsonNode node : objectMapper.readTree(response.body())) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", node.path("display_name").asText());
                row.put("latitude", node.path("lat").asDouble());
                row.put("longitude", node.path("lon").asDouble());
                found.add(row);
            }
            return found;
        } catch (Exception ex) {
            return found;
        }
    }
}
