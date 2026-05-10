package com.ot.main.product.data.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequestDto {
	
	private String productCode;
	
	private Integer safetyStock;
	
	private String name;
	
	private String image;
	
	private Integer productStock;

	private Integer leadTime;

}
