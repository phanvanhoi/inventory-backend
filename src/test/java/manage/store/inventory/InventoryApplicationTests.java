package manage.store.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "test")
class InventoryApplicationTests {

	@Test
	void contextLoads() {
	}

}
