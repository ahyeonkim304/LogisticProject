package com.ot.main.delivery.service;

import java.util.ArrayList;
import java.util.List;

import com.ot.main.delivery.data.dto.DeliveryCreateRequestDTO;
import com.ot.main.delivery.data.dto.DeliveryCreateResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryListResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryUpdateResponseDTO;

/**
 * 배송(Delivery) 도메인 서비스 계약.
 *
 *  표준 기능 (배송은 도메인 특성상 일괄등록은 미제공)
 *   1) 리스트 조회        : selectDeliverlist
 *   2) 단건 등록 (자동)   : createDelivery (쇼핑 주문 수신 시 자동 생성)
 *   3) 단건 수정 (상태)   : updateDelivery
 *   6) 내역 다운로드      : exportDeliveriesByIds
 */
public interface DeliveryService  {

	/** 1) 리스트 조회 */
	List<DeliveryListResponseDTO> selectDeliverlist();

	/** 2) 단건 등록 (쇼핑 주문 수신 시 호출) */
	DeliveryCreateResponseDTO createDelivery(DeliveryCreateRequestDTO deliveryRequestDTO);

	/** 3) 단건 수정 (배송 상태 변경) */
	DeliveryUpdateResponseDTO updateDelivery(Long id, String productCode,
	                                         boolean outStatus, Integer outStock);

	/** 6) 내역 다운로드 — 선택된 배송 ID 목록만 */
	default List<DeliveryListResponseDTO> exportDeliveriesByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return new ArrayList<>();
		List<DeliveryListResponseDTO> result = new ArrayList<>();
		for (DeliveryListResponseDTO d : selectDeliverlist()) {
			if (ids.contains(d.getId())) result.add(d);
		}
		return result;
	}
}
