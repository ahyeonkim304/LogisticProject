package com.logis.wms.dto.inventory;

import com.logis.wms.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationInventoryDto {
    private final Long locationId;
    private final String code;
    private final String name;
    private final LocationType type;
    private final int quantity;
}
