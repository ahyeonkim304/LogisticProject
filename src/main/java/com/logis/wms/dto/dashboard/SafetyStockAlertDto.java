package com.logis.wms.dto.dashboard;

import lombok.Getter;

@Getter
public class SafetyStockAlertDto {
    private final Long productId;
    private final String productName;
    private final String sku;
    private final long currentStock;
    private final int safetyStock;
    private final int deficit;

    public SafetyStockAlertDto(Long productId, String productName, String sku, Long currentStock, Integer safetyStock) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.currentStock = currentStock != null ? currentStock : 0L;
        this.safetyStock = safetyStock != null ? safetyStock : 0;
        this.deficit = this.safetyStock - (int) this.currentStock;
    }
}
