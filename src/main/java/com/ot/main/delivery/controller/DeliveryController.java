package com.ot.main.delivery.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.delivery.data.dto.DeliveryCreateRequestDTO;
import com.ot.main.delivery.data.dto.DeliveryCreateResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryUpdateResponseDTO;

/**
 * 배송(Delivery) 컨트롤러 계약.
 *
 *  배송 도메인은 일괄 등록이 의미가 없어서(쇼핑 주문에서 자동 생성) 제외.
 *  표준 기능 중 조회/등록/수정/내역다운로드만 제공.
 */
public interface DeliveryController {

	/** 1) 배송 목록 조회 */
	public ModelAndView selectDeliverlist();

	/** 2) 단건 등록 (REST, 쇼핑에서 호출) */
	ResponseEntity<DeliveryCreateResponseDTO> createDelivery(@RequestBody DeliveryCreateRequestDTO deliveryRequestDTO);

	/** 3) 단건 수정 (배송 상태 변경) */
	ResponseEntity<DeliveryUpdateResponseDTO> updateDelivery(@RequestParam Long id,
			@RequestParam String productCode, @RequestParam boolean outStatus, @RequestParam Integer outStock);

	/** 6-1) 양식 다운로드 (배송 모듈은 일괄등록 미제공이지만 컬럼 참고용으로 제공) */
	void downloadTemplate(HttpServletResponse response) throws Exception;

	/** 6-2) 내역 다운로드 (선택된 배송 ID 목록만) */
	void downloadSelected(@RequestParam("ids") List<Long> ids,
	                      HttpServletResponse response) throws Exception;

	/** 입고 → 재고 변경 화면 */
	public ModelAndView updateByIn();

	/** 출고 → 재고 변경 화면 */
	public ModelAndView updateByOut();
}
