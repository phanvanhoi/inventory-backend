package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Invoice;
import manage.store.inventory.entity.enums.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByOrderOrderIdOrderByIssuedDateDesc(Long orderId);

    List<Invoice> findByStatus(InvoiceStatus status);
}
