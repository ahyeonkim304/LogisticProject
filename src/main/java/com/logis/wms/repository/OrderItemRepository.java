package com.logis.wms.repository;

import com.logis.wms.dto.dashboard.TopProductDto;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.enums.OrderStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT new com.logis.wms.dto.dashboard.TopProductDto(oi.product.name, oi.product.sku, SUM(oi.quantity)) " +
           "FROM OrderItem oi WHERE oi.order.status IN :statuses AND oi.order.deleted = false " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.sku ORDER BY SUM(oi.quantity) DESC")
    List<TopProductDto> findTopOutboundProducts(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT new com.logis.wms.dto.dashboard.TopProductDto(oi.product.name, oi.product.sku, SUM(oi.quantity)) " +
           "FROM OrderItem oi WHERE oi.order.status IN :statuses AND oi.order.deleted = false " +
           "AND oi.order.account.id = :accountId " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.sku ORDER BY SUM(oi.quantity) DESC")
    List<TopProductDto> findTopOutboundProductsByAccount(@Param("statuses") List<OrderStatus> statuses,
                                                         @Param("accountId") Long accountId,
                                                         Pageable pageable);
}
