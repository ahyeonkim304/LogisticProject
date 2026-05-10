package com.ot.main.productmanagement.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductManagementSelectListResponseDTO {
	   private Long id;  
	   private String productCode;
	   private Integer leadTime;
	   private String name;
	   private Integer productStock;
	   private Integer safetyStock;
	
}
