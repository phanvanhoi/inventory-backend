package manage.store.inventory.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import manage.store.inventory.exception.BusinessException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RoadDistanceTest {

    @Test
    void streetStyleUsesRasterPictures() {
        String style = RoadDistance.streetStyle();
        assertEquals(true, style.contains("\"type\":\"raster\""));
        assertEquals(true, style.contains("/api/delivery/basemap/proxy/maps/tiles/tm/{z}/{x}/{y}.png"));
        assertFalse(style.contains(".pbf"));
        assertFalse(style.contains("apikey"));
    }

    @Test
    void styleUrlsDropTheApiKey() {
        String rewritten = RoadDistance.rewriteStyle(
                "{\"sprite\":\"https://maps.vietmap.vn/maps/styles/tm/sprite?apikey=secret\","
                        + "\"tiles\":[\"https://maps.vietmap.vn/maps/tiles/tm/{z}/{x}/{y}.pbf?apikey=secret\"]}");
        assertFalse(rewritten.contains("secret"));
        assertFalse(rewritten.contains("maps.vietmap.vn"));
        assertEquals(true, rewritten.contains("/api/delivery/basemap/proxy/maps/styles/tm/sprite"));
        assertEquals(true, rewritten.contains("/api/delivery/basemap/proxy/maps/tiles/tm/{z}/{x}/{y}.pbf"));
    }

    @Test
    void geoJsonLineBecomesLatitudeFirst() throws Exception {
        var points = new ObjectMapper().readTree(
                "{\"type\":\"LineString\",\"coordinates\":[[105.79,21.03],[105.80,21.04]]}");
        var pairs = RoadDistance.latLngPairs(points);
        assertEquals(2, pairs.size());
        assertEquals(21.03, pairs.get(0).get(0));
        assertEquals(105.79, pairs.get(0).get(1));
        assertEquals(21.04, pairs.get(1).get(0));
        assertEquals(105.80, pairs.get(1).get(1));
    }

    @Test
    void routePointsBecomeLatitudeFirst() throws Exception {
        var points = new ObjectMapper().readTree("[[106.67,10.75],[10.76,106.68]]");
        var pairs = RoadDistance.latLngPairs(points);
        assertEquals(10.75, pairs.get(0).get(0));
        assertEquals(106.67, pairs.get(0).get(1));
        assertEquals(10.76, pairs.get(1).get(0));
        assertEquals(106.68, pairs.get(1).get(1));
    }

    @Test
    void proxyRejectsParentSegmentsAndForeignPaths() {
        assertThrows(BusinessException.class, () -> RoadDistance.safeAssetPath("/maps/../secret"));
        assertThrows(BusinessException.class, () -> RoadDistance.safeAssetPath("/api/route/v4"));
        assertThrows(BusinessException.class, () -> RoadDistance.safeAssetPath("/maps/tiles/../style.json"));
        String safe = RoadDistance.safeAssetPath("/maps/styles/tm/sprite.png?apikey=secret&foo=bar");
        assertFalse(safe.contains("secret"));
        assertFalse(safe.contains("apikey"));
        assertEquals(true, safe.contains("foo=bar"));
        assertEquals("/maps/tiles/tm/1/2/3.pbf", RoadDistance.safeAssetPath("/maps/tiles/tm/1/2/3.pbf"));
    }
}
