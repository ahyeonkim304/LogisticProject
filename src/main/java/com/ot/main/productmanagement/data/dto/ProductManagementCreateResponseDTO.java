package com.ot.main.productmanagement.data.dto;

import lombok.Data;

@Data
public class ProductManagementCreateResponseDTO {
	   private Long id;  
	
	   private String productCode;
	      
	   private String name;
	   
	   private Integer safetyStock;
	   
	   private Integer productStock;
	   
	   private Integer leadTime;
}
