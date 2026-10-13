package manage.store.inventory.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class RoadDistanceTest {

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
    void routePointsBecomeLatitudeFirst() throws Exception {
        var points = new ObjectMapper().readTree("[[106.67,10.75],[10.76,106.68]]");
        var pairs = RoadDistance.latLngPairs(points);
        assertEquals(10.75, pairs.get(0).get(0));
        assertEquals(106.67, pairs.get(0).get(1));
        assertEquals(10.76, pairs.get(1).get(0));
        assertEquals(106.68, pairs.get(1).get(1));
    }
}
