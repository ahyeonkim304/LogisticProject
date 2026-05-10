package com.ot.main.delivery.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ot.main.delivery.data.dao.DeliveryDAO;
import com.ot.main.delivery.data.dto.DeliveryCreateRequestDTO;
import com.ot.main.delivery.data.dto.DeliveryCreateResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryListResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryUpdateResponseDTO;
import com.ot.main.delivery.data.entity.Delivery;
import com.ot.main.delivery.service.DeliveryService;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectListResponseDTO;
import com.ot.main.productmanagement.data.entity.ProductManagement;

@Service
public class DeliveryServiceImpl implements DeliveryService {

//	@Autowired
	private DeliveryDAO deliveryDAO;

	@Autowired
	public DeliveryServiceImpl(DeliveryDAO deliveryDAO) {
		this.deliveryDAO = deliveryDAO;
	}

	// create
	@Override
	public DeliveryCreateResponseDTO createDelivery(DeliveryCreateRequestDTO deliveryRequestDTO) {

		System.out.println("== Service //" + deliveryRequestDTO.toString() + "=="); // test 후 삭제

		// 송장번호 10자리랜덤 생성
		Random random = new Random();
		Integer n = random.nextInt(1234567899);
		Integer trackingNumber = n;

		// 주문 내역 배송 DB에 넣기 (DTO -> 엔티티)
		Delivery delivery = new Delivery();
		delivery.setAddress(deliveryRequestDTO.getAddress());
		delivery.setHp1(deliveryRequestDTO.getHp1());
		delivery.setHp2(deliveryRequestDTO.getHp2());
		delivery.setHp3(deliveryRequestDTO.getHp3());
		delivery.setProductName(deliveryRequestDTO.getProductName());
		delivery.setStatusDelivery("배송준비중");
		delivery.setStockCount(deliveryRequestDTO.getStockCount()); // naming?
		delivery.setTrackingNumber(trackingNumber);
		delivery.setUserName(deliveryRequestDTO.getUserName());
		delivery.setZipcode(deliveryRequestDTO.getZipcode());
		delivery.setProductCode(deliveryRequestDTO.getProductCode());

		System.out.println("== Service :: delivery //" + deliveryRequestDTO.toString() + "=="); // test 후 삭제

		Delivery succesCreateDelivery = deliveryDAO.createDelivery(delivery);

		// 엔티티 -> DTO
		DeliveryCreateResponseDTO deliveryCreateResponseDTO = new DeliveryCreateResponseDTO();
		deliveryCreateResponseDTO.setAddress(succesCreateDelivery.getAddress());
		deliveryCreateResponseDTO.setHp1(succesCreateDelivery.getHp1());
		deliveryCreateResponseDTO.setHp2(succesCreateDelivery.getHp2());
		deliveryCreateResponseDTO.setHp3(succesCreateDelivery.getHp3());
		deliveryCreateResponseDTO.setProductName(succesCreateDelivery.getProductName());
		deliveryCreateResponseDTO.setStatusDelivery(succesCreateDelivery.getStatusDelivery());
		deliveryCreateResponseDTO.setStockCount(succesCreateDelivery.getStockCount());
		deliveryCreateResponseDTO.setTrackingNumber(succesCreateDelivery.getTrackingNumber());
		deliveryCreateResponseDTO.setUserName(succesCreateDelivery.getUserName());
		deliveryCreateResponseDTO.setZipcode(succesCreateDelivery.getZipcode());
		deliveryCreateResponseDTO.setProductCode(succesCreateDelivery.getProductCode());

		return deliveryCreateResponseDTO;
	}

	@Override
	public DeliveryUpdateResponseDTO updateDelivery(Long id, String productCode, boolean outStatus,
			Integer outStock) {

		// 운송장 번호 조회 및 배송 상테 변경
		Delivery success = deliveryDAO.updateDelivery(id, productCode, outStatus, outStock);
		System.out.println("=================successUpdateDelivery : " + success + "=================");
		
		// 엔티티 -> DTO
		DeliveryUpdateResponseDTO successUpdateDelivery = new DeliveryUpdateResponseDTO();

		
		successUpdateDelivery.setId(success.getId());
		successUpdateDelivery.setAddress(success.getAddress());
		successUpdateDelivery.setHp1(success.getHp1());
		successUpdateDelivery.setHp2(success.getHp2());
		successUpdateDelivery.setHp3(success.getHp3());
		successUpdateDelivery.setProductName(success.getProductName());
		successUpdateDelivery.setStatusDelivery(success.getStatusDelivery());
		successUpdateDelivery.setStockCount(success.getStockCount());
		successUpdateDelivery.setTrackingNumber(success.getTrackingNumber());
		successUpdateDelivery.setUserName(success.getUserName());
		successUpdateDelivery.setZipcode(success.getZipcode());
		successUpdateDelivery.setProductCode(success.getProductCode());

		System.out.println("=================succesUpdateDelivery : " + successUpdateDelivery + "=================");

		
		return successUpdateDelivery;
	}

	@Override
	public List<DeliveryListResponseDTO> selectDeliverlist() {
		List<Delivery> List = deliveryDAO.selectDeliveryList();
		
		List<DeliveryListResponseDTO> deliveryList = List.stream().map(
				delivery -> new DeliveryListResponseDTO(
						delivery.getId(),
						delivery.getTrackingNumber(),
						delivery.getUserName(),
						delivery.getHp1(),
						delivery.getHp2(),
						delivery.getHp3(),
						delivery.getAddress(),
						delivery.getZipcode(),
						delivery.getProductName(),
						delivery.getStockCount(),						
						delivery.getStatusDelivery(),
						delivery.getProductCode())).collect(Collectors.toList());
	
		return deliveryList;
	}

}
