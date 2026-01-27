package manage.store.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import manage.store.inventory.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
