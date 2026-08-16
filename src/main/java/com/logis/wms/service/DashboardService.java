package com.logis.wms.service;

import com.logis.wms.dto.dashboard.DashboardDto;
import com.logis.wms.dto.dashboard.SafetyStockAlertDto;
import com.logis.wms.dto.dashboard.TopProductDto;
import com.logis.wms.dto.dashboard.ZoneCapacityDto;
import com.logis.wms.dto.inventory.InventoryLogDto;
import com.logis.wms.enums.MovementType;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.repository.InventoryLogRepository;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.LocationRepository;
import com.logis.wms.repository.OrderItemRepository;
import com.logis.wms.repository.OrderRepository;
import com.logis.wms.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<OrderStatus> OUTBOUND_STATUSES =
        List.of(OrderStatus.OUTBOUND_COMPLETED, OrderStatus.SHIPPED);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final ProductRepository productRepository;

    public DashboardDto getDashboard(Long accountId) {
        Map<OrderStatus, Long> orderCountByStatus = Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(
                        s -> s,
                        s -> (accountId != null)
                                ? orderRepository.countByAccount_IdAndStatusAndDeletedFalse(accountId, s)
                                : orderRepository.countByStatusAndDeletedFalse(s)));

        Long totalStock = inventoryRepository.sumTotalQuantity();
        Long totalInbound = inventoryLogRepository.sumTotalByType(MovementType.INBOUND);
        Long totalOutbound = inventoryLogRepository.sumTotalByType(MovementType.OUTBOUND);
        return DashboardDto.builder()
                .orderCountByStatus(orderCountByStatus)
                .totalStock(totalStock != null ? totalStock : 0L)
                .totalInbound(totalInbound != null ? totalInbound : 0L)
                .totalOutbound(totalOutbound != null ? totalOutbound : 0L)
                .recentLogs(inventoryLogRepository.findTop10ByOrderByCreatedAtDesc().stream()
                        .map(InventoryLogDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<TopProductDto> getTopOutboundProducts(Long accountId) {
        PageRequest top5 = PageRequest.of(0, 5);
        if (accountId != null) {
            return orderItemRepository.findTopOutboundProductsByAccount(OUTBOUND_STATUSES, accountId, top5);
        }
        return orderItemRepository.findTopOutboundProducts(OUTBOUND_STATUSES, top5);
    }

    public List<ZoneCapacityDto> getZoneCapacity(Long accountId) {
        List<Object[]> rows = (accountId != null)
            ? locationRepository.findZoneCapacityRawByAccount(accountId)
            : locationRepository.findZoneCapacityRaw();
        return rows.stream().map(r -> new ZoneCapacityDto(
            (String) r[0],
            toLong(r[1]),
            toLong(r[2])
        )).collect(Collectors.toList());
    }

    public List<SafetyStockAlertDto> getSafetyStockAlerts(Long accountId) {
        if (accountId != null) {
            return productRepository.findSafetyStockAlertsByAccount(accountId);
        }
        return productRepository.findSafetyStockAlerts();
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long) return (Long) val;
        if (val instanceof BigInteger) return ((BigInteger) val).longValue();
        if (val instanceof BigDecimal) return ((BigDecimal) val).longValue();
        if (val instanceof Number) return ((Number) val).longValue();
        return 0L;
    }
}
