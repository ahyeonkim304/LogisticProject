package com.logis.wms.repository;

import com.logis.wms.entity.OrderMeasurement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMeasurementRepository extends JpaRepository<OrderMeasurement, Long> {

    Optional<OrderMeasurement> findByOrderId(Long orderId);
}
