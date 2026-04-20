package manage.store.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c " +
           "LEFT JOIN FETCH c.unit " +
           "LEFT JOIN FETCH c.parentCustomer " +
           "ORDER BY c.createdAt DESC")
    List<Customer> findAllWithRelations();

    @Query("SELECT c FROM Customer c " +
           "LEFT JOIN FETCH c.unit " +
           "LEFT JOIN FETCH c.parentCustomer " +
           "WHERE c.customerId = :id")
    Optional<Customer> findByIdWithRelations(@Param("id") Long id);

    List<Customer> findByUnitUnitId(Long unitId);

    List<Customer> findByContractYear(Integer year);

    List<Customer> findByParentCustomerCustomerId(Long parentId);

    @Query("SELECT c FROM Customer c WHERE c.parentCustomer IS NULL")
    List<Customer> findAllRootCustomers();

    long countBySeedSource(String seedSource);
}
