package manage.store.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);
}
