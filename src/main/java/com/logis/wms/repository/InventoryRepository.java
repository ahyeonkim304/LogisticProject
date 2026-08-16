package com.logis.wms.repository;

import com.logis.wms.entity.Inventory;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProduct_IdAndLocation_Id(Long productId, Long locationId);
    List<Inventory> findByProduct_IdOrderByIdAsc(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.quantity > 0 ORDER BY i.id ASC")
    List<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId")
    Long sumQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(i.quantity) FROM Inventory i")
    Long sumTotalQuantity();

    boolean existsByLocation_IdAndQuantityGreaterThan(Long locationId, int quantity);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.location " +
           "WHERE i.location.id IN :ids AND i.quantity > 0")
    List<Inventory> findByLocationIdsWithProducts(@Param("ids") List<Long> ids);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.location " +
           "WHERE i.quantity > 0 ORDER BY i.product.sku ASC")
    List<Inventory> findAllWithDetails();

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product JOIN FETCH i.location l " +
           "WHERE i.quantity > 0 AND l.account.id = :accountId ORDER BY i.product.sku ASC")
    List<Inventory> findAllWithDetailsByAccount(@Param("accountId") Long accountId);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH i.location l " +
           "WHERE (UPPER(p.sku) LIKE UPPER(CONCAT('%',:kw,'%')) " +
           "    OR UPPER(p.name) LIKE UPPER(CONCAT('%',:kw,'%'))) " +
           "AND i.quantity > 0")
    List<Inventory> searchByKeyword(@Param("kw") String keyword);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.product p JOIN FETCH i.location l " +
           "WHERE (UPPER(p.sku) LIKE UPPER(CONCAT('%',:kw,'%')) " +
           "    OR UPPER(p.name) LIKE UPPER(CONCAT('%',:kw,'%'))) " +
           "AND i.quantity > 0 AND l.account.id = :accountId")
    List<Inventory> searchByKeywordAndAccount(@Param("kw") String keyword,
                                              @Param("accountId") Long accountId);
}
