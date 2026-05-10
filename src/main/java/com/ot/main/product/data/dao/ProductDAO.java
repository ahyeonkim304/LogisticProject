package com.ot.main.product.data.dao;

import java.util.List;

import com.ot.main.product.data.entity.Product;

public interface ProductDAO {
	
	Product insertProduct(Product product);
	
	List<Product> findAllProduct();
	
	Product updateProduct(Product product) throws Exception;
	
	void deleteProduct(String productCode) throws Exception;
	
	List<Product> findByProductCodeContainingOrNameContaining(String searchKeyword);
	
	List<Product> findByProductCodeContaining(String searchKeyword);
	
	List<Product> findByNameContaining(String searchKeyword);
	
}
