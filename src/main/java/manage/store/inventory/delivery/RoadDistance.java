package manage.store.inventory.delivery;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

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
}
