package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.UnitEmployee;

public interface UnitEmployeeRepository extends JpaRepository<UnitEmployee, Long> {

    List<UnitEmployee> findByUnitIdOrderByFullName(Long unitId);
}
