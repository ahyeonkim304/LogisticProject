package com.ot.main.product.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.product.data.dao.ProductDAO;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.data.repository.ProductRepository;

@Component
public class ProductDAOImpl implements ProductDAO {
	
	private ProductRepository productRepository;
	
	@Autowired 
	public ProductDAOImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	/*
	@Override
	public List<Product> findAllProduct() {
		List<Product> productSelectAllResponseDtoList = productRepository.findAll();
		return productSelectAllResponseDtoList;
	}
	*/
	
	@Override
	public List<Product> findAllProduct() {
		List<Product> productSelectAllResponseDtoList = productRepository.findAllByOrderByProductCodeDesc();
		return productSelectAllResponseDtoList;
	}
	
	
	@Override
	public Product insertProduct(Product product) {
		Product savedProduct = productRepository.save(product);
		
		return savedProduct;
	}

	@Override
	public Product updateProduct(Product product) throws Exception{
		Optional<Product> selectedProduct = productRepository.findById(product.getProductCode());
		
		Product updatedProduct;
		
		if(selectedProduct.isPresent()) {
			Product requestProduct = selectedProduct.get();
			requestProduct.setName(product.getName());
			requestProduct.setImage(product.getImage());
			requestProduct.setLeadTime(product.getLeadTime());
			requestProduct.setProductStock(product.getProductStock());
			requestProduct.setSafetyStock(product.getSafetyStock());
			requestProduct.setUpdated_at(product.getUpdated_at());
			
			updatedProduct = productRepository.save(requestProduct);
			
		} else {
			updatedProduct = new Product();
		}
		
		return updatedProduct;
	}

	@Override
	public void deleteProduct(String productCode) throws Exception {
		
		Optional<Product> selectedProduct = productRepository.findById(productCode);
		 if(selectedProduct.isPresent()){
	            Product product = selectedProduct.get();
	            System.out.println(product);
	            productRepository.delete(product);
	            
	            
	     } else {
	    	 	System.out.println("해당하는 상품코드의 상품이 없습니다.");
	    	 	//throw new Exception();
	     }
	   
		
	}
	
	
	// 상품 검색 (상품 코드 or 이름)
	@Override
	public List<Product> findByProductCodeContainingOrNameContaining(String searchKeyword) {
		List<Product> searchProductList = productRepository.findByProductCodeContainingOrNameContaining(searchKeyword, searchKeyword);
		
		return searchProductList;
	}

	
	// 상품 검색 (상품 코드)
	@Override
	public List<Product> findByProductCodeContaining(String searchKeyword) {
		List<Product> searchProductList = productRepository.findByProductCodeContaining(searchKeyword);
		return searchProductList;
	}
	
	// 상품 검색 (이름)
	@Override
	public List<Product> findByNameContaining(String searchKeyword) {
		List<Product> searchProductList = productRepository.findByNameContaining(searchKeyword);
		return searchProductList;
	}


	
	
	
	

	

}
