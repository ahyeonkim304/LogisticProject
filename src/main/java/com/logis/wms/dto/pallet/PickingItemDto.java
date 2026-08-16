package com.logis.wms.dto.pallet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PickingItemDto {
    private final String productSku;
    private final String productName;
    private final String locationCode;
    private final String locationName;
    private final int quantity;
}
