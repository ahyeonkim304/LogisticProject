package com.logis.wms.repository;

import com.logis.wms.entity.Order;
import com.logis.wms.enums.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNoAndDeletedFalse(String orderNo);

    Page<Order> findByDeletedFalse(Pageable pageable);
    List<Order> findByDeletedFalse();
    Page<Order> findByStatusAndDeletedFalse(OrderStatus status, Pageable pageable);
    List<Order> findByStatusAndDeletedFalse(OrderStatus status);
    long countByStatusAndDeletedFalse(OrderStatus status);

    Page<Order> findByAccount_IdAndDeletedFalse(Long accountId, Pageable pageable);
    Page<Order> findByAccount_IdAndStatusAndDeletedFalse(Long accountId, OrderStatus status, Pageable pageable);
    List<Order> findByAccount_IdAndStatusAndDeletedFalse(Long accountId, OrderStatus status);
    long countByAccount_IdAndStatusAndDeletedFalse(Long accountId, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.deleted = false " +
           "AND (:accountId IS NULL OR o.account.id = :accountId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:kw IS NULL OR UPPER(o.orderNo) LIKE :kw OR UPPER(o.customerName) LIKE :kw)")
    Page<Order> searchOrders(@Param("kw") String kw,
                             @Param("accountId") Long accountId,
                             @Param("status") OrderStatus status,
                             Pageable pageable);
}
