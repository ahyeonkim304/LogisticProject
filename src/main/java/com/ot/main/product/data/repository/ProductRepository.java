package com.ot.main.product.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.main.product.data.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
	List<Product> findByProductCodeContainingOrNameContaining(String ProductCode, String name);
	List<Product> findByProductCodeContaining(String ProductCode);
	List<Product> findByNameContaining(String name);
	// add 
//	Product getByProductCode(String productCode);
	
	List<Product> findAllByOrderByProductCodeDesc();
	// 정렬 
	//List<Product> findAllByOrderBy

}
