package com.ot.main.productmanagement.data.dao;

import java.util.List;

import com.ot.main.productmanagement.data.dto.ProductManagementCompareResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectListResponseDTO;
import com.ot.main.productmanagement.data.entity.ProductManagement;

public interface ProductManagementDAO {
	
	// create
	public ProductManagement createStock(ProductManagement productManagement);
	
	// Update
	public ProductManagement modifyInStock(String productCode, boolean InStatus, Integer InStock);
	
	// Update
	public ProductManagement modifyOutStock(String productCode, boolean OutStatus, Integer OutStock);

	// LOOK UP STOCK DETAIL
	public ProductManagement selectOneStock(Long id);
	
	//LOOK UP STOCK
	public List<ProductManagement> selectStockList( );
	
	//compare
	public ProductManagement compareStockAndSafetyStock(String productCode);

	// DELETE STOCK
	public String deleteStock(Long id);
}
