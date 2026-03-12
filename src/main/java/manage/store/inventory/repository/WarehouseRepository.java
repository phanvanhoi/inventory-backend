package manage.store.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByIsDefaultTrue();
}
