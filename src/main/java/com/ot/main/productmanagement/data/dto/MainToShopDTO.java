package com.ot.main.productmanagement.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainToShopDTO {
	
	private String productcode;
	
	private String productstock;
}
