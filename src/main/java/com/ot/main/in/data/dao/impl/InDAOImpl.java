package com.ot.main.in.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.in.data.dao.InDAO;
import com.ot.main.in.data.entity.In;
import com.ot.main.in.data.repository.InRepository;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.data.repository.ProductRepository;

@Component
public class InDAOImpl implements InDAO{
	
	private InRepository inRepository;
	
	private ProductRepository productRepository;
	
	@Autowired 
	public InDAOImpl(InRepository inRepository, ProductRepository productRepository) {
		this.inRepository = inRepository;
		this.productRepository = productRepository;
	}
	
	@Override
	public In insertIn(In in, String productCode) {
		Product product = productRepository.getById(productCode);
		in.setProduct(product);
	
		In savedIn = inRepository.save(in);
		
		return savedIn;
	}

	@Override
	public List<In> findAllIn() throws Exception {
		Optional<List<In>> inListSelectAll = Optional.ofNullable(inRepository.findAll());
		List<In> inList = inListSelectAll.get();
		
		return inList;
	}

	@Override
	public In updateIn(In in) throws Exception {
		Optional<In> selectedIn = inRepository.findById(in.getId());
		In updatedIn;
		if(selectedIn.isPresent()) {
			In requestIn = selectedIn.get();
			requestIn.setInStock(in.getInStock());
			requestIn.setInStatus(in.isInStatus());
			requestIn.setInComplete_at(in.getInComplete_at());
			updatedIn = inRepository.save(requestIn);
			
		} else {
			updatedIn = null;
		}
		
		return updatedIn;
	}

	@Override
	public String deleteIn(Long id) throws Exception {
		String result;
		Optional<In> selectedIn = inRepository.findById(id);
		if(selectedIn.isPresent()) {
			In requestIn = selectedIn.get();
			inRepository.delete(requestIn);
			result = "상품이 삭제가 잘 되었습니다.";
		} else {
			result = "상품이 삭제가 안 되었습니다.";
		}
		
		return result;
	}
	

}
