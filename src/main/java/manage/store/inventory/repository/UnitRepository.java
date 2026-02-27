package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByUnitNameContainingIgnoreCase(String namePart);
}
