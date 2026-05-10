package com.ot.main.productmanagement.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.main.productmanagement.data.entity.ProductManagement;



public interface ProductManagementRepository extends JpaRepository<ProductManagement, Long> {

	Optional<ProductManagement> findById(String productCode);

	ProductManagement findByProductCode(String productCode);
	
}
