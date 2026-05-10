package com.ot.main.delivery.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.delivery.data.dao.DeliveryDAO;
import com.ot.main.delivery.data.entity.Delivery;
import com.ot.main.delivery.data.repository.DeliveryRepository;
import com.ot.main.out.data.entity.Out;
import com.ot.main.out.data.repository.OutRepository;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.data.repository.ProductRepository;
import com.ot.main.productmanagement.data.entity.ProductManagement;
import com.ot.main.productmanagement.data.repository.ProductManagementRepository;

@Component
public class DeliveryDAOImpl implements DeliveryDAO {

	private final DeliveryRepository deliveryRepository;

	@Autowired
	public DeliveryDAOImpl(DeliveryRepository deliveryRepository) {
		this.deliveryRepository = deliveryRepository;
	}

	// create
	@Override
	public Delivery createDelivery(Delivery delivery) {
		System.out.println("== DAO :: delivery //" + delivery.toString() + "=="); // test 후 삭제

		Delivery saveDelivery = deliveryRepository.save(delivery);
		System.out.println("== DAO :: saveDelivery : " + saveDelivery + "=="); // test 후 삭제
		return saveDelivery;
	}

	@Override
	public Delivery updateDelivery(Long id, String productCode, boolean outStatus, Integer outStock) {

		// id로 배송 1개 조회
		Optional<Delivery> updateDelivery = deliveryRepository.findById(id);

		Delivery saveDelivery = new Delivery();

		//outStatus == true 이면 출고 완료 상태
		if (updateDelivery.isPresent() && outStatus == true) {
			Delivery selectONE = updateDelivery.get();

			// Delivery 배송 상태 및 OutStock으로 수량 변경
			selectONE.setStatusDelivery("배송시작");
			selectONE.setStockCount(outStock);

			saveDelivery = deliveryRepository.save(selectONE);
			System.out.println("== DAO :: saveDelivery : " + saveDelivery.toString() + "=="); // test 후 삭제

		}
	
		return saveDelivery;

	}

	//List
	@Override
	public List<Delivery> selectDeliveryList() {
		
		List<Delivery> deliveryList = deliveryRepository.findAll();
		System.err.println("======= deliveryList :" + deliveryList + "=====================");
		
		return deliveryList;
	}
}
