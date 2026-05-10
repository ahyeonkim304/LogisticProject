package com.ot.main.out.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.out.data.dao.OutDAO;
import com.ot.main.out.data.entity.Out;
import com.ot.main.out.data.repository.OutRepository;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.data.repository.ProductRepository;


@Component
public class OutDAOImpl implements OutDAO{
	
	private OutRepository outRepository;
	
	private ProductRepository productRepository;
	
	@Autowired 
	public OutDAOImpl(OutRepository outRepository, ProductRepository productRepository) {
		this.outRepository = outRepository;
		this.productRepository = productRepository;
	}

	@Override
	public Out insertOut(Out out, String productCode) {
		Product product = productRepository.getById(productCode);
		
		out.setProduct(product);
	
		Out savedOut = outRepository.save(out);
		
		return savedOut;
	}

	@Override
	public List<Out> findAllout() {
		Optional<List<Out>> outListSelectAll = Optional.ofNullable(outRepository.findAll());
		List<Out> outList = outListSelectAll.get();
		
		return outList;
	}

	@Override
	public Out updateOut(Out out) {
		Optional<Out> selectedOut = outRepository.findById(out.getId());
		Out updatedOut;
		if(selectedOut.isPresent()) {
			Out requestOut = selectedOut.get();
			requestOut.setOutStock(out.getOutStock());
			requestOut.setOutComplete_at(out.getOutComplete_at());
			updatedOut = outRepository.save(requestOut);
			
		} else {
			updatedOut = null;
		}
		
		return updatedOut;
	}

	@Override
	public String deleteOut(Long id) {
		String result;
		Optional<Out> selectedOut = outRepository.findById(id);
		if(selectedOut.isPresent()) {
			Out requestOut = selectedOut.get();
			outRepository.delete(requestOut);
			result = "상품이 삭제가 잘 되었습니다.";
		} else {
			result = "상품이 삭제가 안 되었습니다.";
		}
		
		return result;
	}
	
	
	
	

}
