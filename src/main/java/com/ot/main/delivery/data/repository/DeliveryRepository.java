package com.ot.main.delivery.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.main.delivery.data.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {


}
