package com.logis.wms.dto.dashboard;

import com.logis.wms.dto.inventory.InventoryLogDto;
import com.logis.wms.enums.OrderStatus;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardDto {

    private final Map<OrderStatus, Long> orderCountByStatus;
    private final long totalStock;
    private final long totalInbound;
    private final long totalOutbound;
    private final List<InventoryLogDto> recentLogs;

    public long getOrderCount(OrderStatus status) {
        if (orderCountByStatus == null) return 0L;
        return orderCountByStatus.getOrDefault(status, 0L);
    }
}
