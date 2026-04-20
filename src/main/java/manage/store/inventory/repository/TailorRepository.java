package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Tailor;
import manage.store.inventory.entity.enums.TailorType;

@Repository
public interface TailorRepository extends JpaRepository<Tailor, Long> {

    List<Tailor> findByActiveTrue();

    List<Tailor> findByType(TailorType type);

    List<Tailor> findByActiveTrueAndType(Boolean active, TailorType type);
}
