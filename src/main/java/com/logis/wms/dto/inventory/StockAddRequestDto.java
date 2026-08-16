package com.logis.wms.dto.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockAddRequestDto {
    private String productSku;
    private int quantity;
    private Long locationId;
    private String memo;
}
