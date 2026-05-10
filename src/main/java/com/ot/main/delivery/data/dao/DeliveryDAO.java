package com.ot.main.delivery.data.dao;

import java.util.List;

import com.ot.main.delivery.data.entity.Delivery;

public interface DeliveryDAO  {

	//create
	Delivery createDelivery(Delivery delivery);
	
	//update
	Delivery updateDelivery(Long id, String productCode, boolean outStatus,
			Integer outStock);
	
	//List
    List<Delivery> selectDeliveryList();
}
