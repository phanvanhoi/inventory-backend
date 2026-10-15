package manage.store.inventory.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import manage.store.inventory.exception.BusinessException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RoadDistanceTest {

    @Test
    void styleUrlsDropTheApiKey() {
        String rewritten = RoadDistance.rewriteStyle(
                "{\"sprite\":\"https://tiles.goong.io/assets/sprites/goong?api_key=secret\","
                        + "\"tiles\":[\"https://tiles.goong.io/sources/goong/{z}/{x}/{y}.pbf?api_key=secret\"]}");
        assertFalse(rewritten.contains("secret"));
        assertFalse(rewritten.contains("tiles.goong.io"));
        assertEquals(true, rewritten.contains("/api/delivery/basemap/proxy/assets/sprites/goong"));
        assertEquals(true, rewritten.contains("/api/delivery/basemap/proxy/sources/goong/{z}/{x}/{y}.pbf"));
    }

    @Test
    void encodedPolylineBecomesLatitudeFirst() {
        var pairs = RoadDistance.decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@");
        assertEquals(3, pairs.size());
        assertEquals(38.5, pairs.get(0).get(0), 0.00001);
        assertEquals(-120.2, pairs.get(0).get(1), 0.00001);
        assertEquals(40.7, pairs.get(1).get(0), 0.00001);
        assertEquals(-120.95, pairs.get(1).get(1), 0.00001);
        assertEquals(43.252, pairs.get(2).get(0), 0.00001);
        assertEquals(-126.453, pairs.get(2).get(1), 0.00001);
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
        String safe = RoadDistance.safeAssetPath("/assets/sprites/goong.png?api_key=secret&foo=bar");
        assertFalse(safe.contains("secret"));
        assertFalse(safe.contains("api_key"));
        assertEquals(true, safe.contains("foo=bar"));
        assertEquals("/sources/goong/1/2/3.pbf", RoadDistance.safeAssetPath("/sources/goong/1/2/3.pbf"));
        assertEquals("/tiles/composite/12/3252/1803.pbf", RoadDistance.safeAssetPath("/tiles/composite/12/3252/1803.pbf"));
        assertEquals("/sprite@2x.png", RoadDistance.safeAssetPath("/sprite@2x.png"));
        assertEquals("/fonts/Roboto%20Regular/0-255.pbf", RoadDistance.safeAssetPath("/fonts/Roboto Regular/0-255.pbf"));
    }

    @Test
    void tileJsonUsesThePageScheme() {
        String rewritten = RoadDistance.rewriteStyle(
                "{\"tiles\":[\"https://tiles.goong.io/tiles/composite/{z}/{x}/{y}.pbf?api_key=secret\"]}");
        String pointed = RoadDistance.pointAtProxy(rewritten, "adspyvn.com");
        assertFalse(pointed.contains("secret"));
        assertFalse(pointed.contains("tiles.goong.io"));
        assertFalse(pointed.contains("http://"));
        assertEquals(true, pointed.contains("//adspyvn.com/api/delivery/basemap/proxy/tiles/composite/{z}/{x}/{y}.pbf"));
        String relative = RoadDistance.pointAtProxy(rewritten, "bad host");
        assertEquals(true, relative.startsWith("{\"tiles\":[\"/api/delivery/basemap/proxy/tiles/"));
    }
}
