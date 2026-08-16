package com.logis.wms.dto.location;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationInventoryDetailDto {

    private Long   locationId;
    private String code;
    private String name;
    private String type;
    private String typeName;
    private List<String> breadcrumb;
    private int skuCount;
    private int totalQty;
    private int lowStockCount;
    private List<SkuRowDto> items;

    @Getter
    @Builder
    public static class SkuRowDto {
        private String sku;
        private String productName;
        private int    qty;
        private String status;
        private String locationCode;
        private List<String> locationPath;
        private List<String> locationPathTypes;
    }
}
