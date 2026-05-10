package com.ot.main.productmanagement.data.dto;

import com.ot.main.productmanagement.data.entity.ProductManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagementCompareResponseDTO {

	private Long id;
	private String productCode;
	
	public ProductManagement fill(ProductManagement productManagement) {
		productManagement.setId(this.id);
		productManagement.setProductCode(this.productCode);
		return productManagement;
	}
	
}
