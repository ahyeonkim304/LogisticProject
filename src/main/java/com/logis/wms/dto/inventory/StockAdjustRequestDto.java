package com.logis.wms.dto.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockAdjustRequestDto {
    private String productSku;
    private int quantity;
    private String reason;
}
