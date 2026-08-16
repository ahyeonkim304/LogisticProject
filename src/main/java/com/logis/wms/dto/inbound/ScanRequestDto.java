package com.logis.wms.dto.inbound;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScanRequestDto {
    private String barcode;
    private Double width;
    private Double depth;
    private Double height;
    private Double weight;
    private Integer volumeDivisor;
    private Long palletId;
}
