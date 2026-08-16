package com.logis.wms.repository;

import com.logis.wms.entity.InventoryLog;
import com.logis.wms.enums.MovementType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findTop10ByOrderByCreatedAtDesc();
    List<InventoryLog> findTop30ByOrderByCreatedAtDesc();
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT SUM(l.quantity) FROM InventoryLog l WHERE l.product.id = :productId AND l.type = :type")
    Long sumByProductIdAndType(@Param("productId") Long productId, @Param("type") MovementType type);

    @Query("SELECT SUM(l.quantity) FROM InventoryLog l WHERE l.type = :type")
    Long sumTotalByType(@Param("type") MovementType type);

    @Query(value =
           "SELECT l FROM InventoryLog l " +
           "JOIN FETCH l.product p LEFT JOIN FETCH l.location loc LEFT JOIN FETCH p.account " +
           "WHERE (:kw IS NULL OR UPPER(p.sku) LIKE :kw OR UPPER(p.name) LIKE :kw) " +
           "AND (:accId IS NULL OR (p.account IS NOT NULL AND p.account.id = :accId)) " +
           "AND (:st IS NULL OR l.createdAt >= :st) " +
           "AND (:et IS NULL OR l.createdAt <= :et)",
           countQuery =
           "SELECT COUNT(l) FROM InventoryLog l JOIN l.product p " +
           "WHERE (:kw IS NULL OR UPPER(p.sku) LIKE :kw OR UPPER(p.name) LIKE :kw) " +
           "AND (:accId IS NULL OR (p.account IS NOT NULL AND p.account.id = :accId)) " +
           "AND (:st IS NULL OR l.createdAt >= :st) " +
           "AND (:et IS NULL OR l.createdAt <= :et)")
    Page<InventoryLog> searchHistory(@Param("kw")    String kw,
                                     @Param("accId") Long accId,
                                     @Param("st")    LocalDateTime st,
                                     @Param("et")    LocalDateTime et,
                                     Pageable pageable);
}
