package com.logis.wms.dto.pallet;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PalletCreateDto {
    private String code;
    private Long locationId;
}
