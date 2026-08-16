package com.logis.wms.service;

import com.logis.wms.dto.dashboard.DashboardDto;
import com.logis.wms.dto.inventory.InventoryLogDto;
import com.logis.wms.enums.MovementType;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.repository.InventoryLogRepository;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.OrderRepository;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final OrderRepository orderRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryRepository inventoryRepository;

    // 주문 상태별 카운트·총 재고·입출고 합계·최근 로그를 집계하여 반환한다
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
}
