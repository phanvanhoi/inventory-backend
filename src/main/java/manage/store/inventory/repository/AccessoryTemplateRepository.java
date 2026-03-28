package manage.store.inventory.repository;

import manage.store.inventory.entity.AccessoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessoryTemplateRepository extends JpaRepository<AccessoryTemplate, Long> {

    List<AccessoryTemplate> findByDeletedAtIsNullOrderByCreatedAtDesc();
}
