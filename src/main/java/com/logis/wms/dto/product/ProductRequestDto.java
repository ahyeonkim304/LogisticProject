package com.logis.wms.dto.product;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {

    private Long accountId;

    @NotBlank(message = "SKU는 필수입니다.")
    private String sku;

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    private String category;

    private String unit;

    private String description;

    @Min(value = 0, message = "안전 재고는 0 이상이어야 합니다.")
    private Integer safetyStock = 0;
}
