package manage.store.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByPositionCode(String positionCode);
}
