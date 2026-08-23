package com.logis.wms.dto.product;

import com.logis.wms.entity.Product;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private final Long id;
    private final Long accountId;
    private final String accountName;
    private final String sku;
    private final String name;
    private final Integer safetyStock;
    private int currentStock;
    private long locationCount;

    private ProductResponseDto(Product p) {
        this.id = p.getId();
        this.accountId = p.getAccount() != null ? p.getAccount().getId() : null;
        this.accountName = p.getAccount() != null
                ? (p.getAccount().getCompanyName() != null && !p.getAccount().getCompanyName().isBlank()
                   ? p.getAccount().getCompanyName()
                   : p.getAccount().getUsername())
                : null;
        this.sku = p.getSku();
        this.name = p.getName();
        this.safetyStock = p.getSafetyStock() != null ? p.getSafetyStock() : 0;
    }

    public static ProductResponseDto of(Product p, int currentStock) {
        ProductResponseDto dto = new ProductResponseDto(p);
        dto.currentStock = currentStock;
        return dto;
    }

    public static ProductResponseDto of(Product p, int currentStock, long locationCount) {
        ProductResponseDto dto = new ProductResponseDto(p);
        dto.currentStock = currentStock;
        dto.locationCount = locationCount;
        return dto;
    }

    public static ProductResponseDto of(Product p) {
        return new ProductResponseDto(p);
    }
}
