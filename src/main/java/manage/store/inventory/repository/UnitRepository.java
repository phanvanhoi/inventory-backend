package manage.store.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Unit;

public interface UnitRepository extends JpaRepository<Unit, Long> {
}
