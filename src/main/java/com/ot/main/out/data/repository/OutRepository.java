package com.ot.main.out.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ot.main.out.data.entity.Out;


public interface OutRepository extends JpaRepository<Out, Long> {

	
//	@Query(value = "SELECT * FROM f_out WHERE productcode IN (SELECT productcode FROM f_product WHERE productcode = ?1)", nativeQuery = true)
//	public Out selectOneByProductCode(String productCode);

	
	
	//Out findByProduct_ProductCode(String productCode);
    //Out findByProductProductCode(String productCode);
	//Out findByProductproductcode(String productCode);
//	Out findByProductCode(String productCode);

}
