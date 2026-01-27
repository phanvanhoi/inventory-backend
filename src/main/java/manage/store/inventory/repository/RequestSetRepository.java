package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.entity.RequestSet;
import manage.store.inventory.entity.enums.RequestSetStatus;

public interface RequestSetRepository extends JpaRepository<RequestSet, Long> {

    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllSets();

    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.status = :status
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByStatus(@Param("status") String status);

    List<RequestSet> findByStatus(RequestSetStatus status);

    @Query("SELECT rs FROM RequestSet rs WHERE rs.status = 'PENDING'")
    List<RequestSet> findPendingApproval();

    @Query("SELECT rs FROM RequestSet rs WHERE rs.status = 'APPROVED'")
    List<RequestSet> findApprovedSets();

    // Alias cho findAllByStatus để sử dụng với String
    default List<RequestSetListDTO> findByStatus(String status) {
        return findAllByStatus(status);
    }

    // Đếm số request sets mà user đã tạo
    @Query("SELECT COUNT(rs) FROM RequestSet rs WHERE rs.createdByUser.userId = :userId")
    Long countByCreatedByUserId(@Param("userId") Long userId);

    // Lấy request sets theo nhiều status
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.status IN (:statuses)
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByStatuses(@Param("statuses") List<String> statuses);

    // Lấy request sets theo user (cho USER/PURCHASER chỉ xem của mình)
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.created_by = :userId
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllSetsByCreatedBy(@Param("userId") Long userId);

    // Lấy request sets theo user và status
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.created_by = :userId AND rs.status = :status
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByCreatedByAndStatus(@Param("userId") Long userId, @Param("status") String status);

    // Lấy request sets theo user và nhiều status
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.created_by = :userId AND rs.status IN (:statuses)
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByCreatedByAndStatuses(@Param("userId") Long userId, @Param("statuses") List<String> statuses);

    // Lấy tất cả request sets sắp xếp theo tên người tạo (cho ADMIN)
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY u.full_name ASC, rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllSetsOrderByCreatorName();

    // Lấy request sets theo status sắp xếp theo tên người tạo (cho ADMIN)
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.status = :status
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY u.full_name ASC, rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByStatusOrderByCreatorName(@Param("status") String status);

    // Lấy request sets theo nhiều status sắp xếp theo tên người tạo (cho ADMIN)
    @Query(
            value = """
                SELECT
                    rs.set_id AS setId,
                    rs.set_name AS setName,
                    rs.description AS description,
                    rs.status AS status,
                    rs.created_by AS createdBy,
                    u.full_name AS createdByName,
                    rs.created_at AS createdAt,
                    rs.submitted_at AS submittedAt,
                    COUNT(ir.request_id) AS requestCount,
                    GROUP_CONCAT(DISTINCT ir.request_type ORDER BY ir.request_id SEPARATOR ',') AS requestTypes,
                    GROUP_CONCAT(DISTINCT p.product_name ORDER BY p.product_name SEPARATOR ',') AS productNames
                FROM request_sets rs
                LEFT JOIN users u ON u.user_id = rs.created_by
                LEFT JOIN inventory_requests ir ON ir.set_id = rs.set_id
                LEFT JOIN products p ON p.product_id = ir.product_id
                WHERE rs.status IN (:statuses)
                GROUP BY rs.set_id, rs.set_name, rs.description, rs.status, rs.created_by, u.full_name, rs.created_at, rs.submitted_at
                ORDER BY u.full_name ASC, rs.created_at DESC
            """,
            nativeQuery = true
    )
    List<RequestSetListDTO> findAllByStatusesOrderByCreatorName(@Param("statuses") List<String> statuses);
}
