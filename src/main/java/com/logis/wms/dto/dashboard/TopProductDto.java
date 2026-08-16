package com.logis.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopProductDto {
    private String productName;
    private String sku;
    private Long totalQty;
}
