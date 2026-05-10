package com.ot.main.product.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequestDto {

	private String productCode;
	
	private Integer safetyStock;
	
	private String image;
	
	private String name;
	
	private Integer productStock;
	
	private Integer leadTime;

	
}
