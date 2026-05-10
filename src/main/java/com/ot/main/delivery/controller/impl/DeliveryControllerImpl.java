package com.ot.main.delivery.controller.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.delivery.controller.DeliveryController;
import com.ot.main.delivery.data.dto.DeliveryCreateRequestDTO;
import com.ot.main.delivery.data.dto.DeliveryCreateResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryListResponseDTO;
import com.ot.main.delivery.data.dto.DeliveryUpdateResponseDTO;
import com.ot.main.delivery.service.DeliveryService;

/**
 * 배송(Delivery) REST/MVC 컨트롤러.
 *
 *  Base URL : /api/v1/main-fulfillment
 *
 *  엔드포인트 요약
 *   GET  /selectDeliverylist          : 배송 목록 조회 화면
 *   POST /createDelivery              : 단건 등록 (REST, 쇼핑에서 호출)
 *   PUT  /updateDelivery              : 배송 상태 변경 (REST)
 *   GET  /downloadDeliveryTemplate    : 양식 다운로드
 *   GET  /downloadDeliverySelected    : 선택 배송 내역 다운로드
 *   GET  /updateByIn                  : (참고) 입고 → 재고변경 화면
 *   GET  /updateByOut                 : (참고) 출고 → 재고변경 화면
 */
@Controller
@RequestMapping(value = "/api/v1/main-fulfillment")
public class DeliveryControllerImpl implements DeliveryController {

	private final DeliveryService deliveryService;

	@Autowired
	public DeliveryControllerImpl(DeliveryService deliveryService) {
		this.deliveryService = deliveryService;
	}

	/* ============================================================
	 *  1) 배송 목록 조회
	 * ============================================================ */
	@Override
	@GetMapping("/selectDeliverylist")
	public ModelAndView selectDeliverlist() {
		List<DeliveryListResponseDTO> delivery = deliveryService.selectDeliverlist();
		ModelAndView mav = new ModelAndView();
		mav.addObject("deliveryList", delivery);
		mav.setViewName("delivery/delivery_List");
		return mav;
	}

	/* ============================================================
	 *  2) 단건 등록 (REST)
	 *     쇼핑 주문 수신 시 자동 호출
	 * ============================================================ */
	@PostMapping("/createDelivery")
	@ResponseBody
	@Override
	public ResponseEntity<DeliveryCreateResponseDTO> createDelivery(DeliveryCreateRequestDTO dto) {
		DeliveryCreateResponseDTO result = deliveryService.createDelivery(dto);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/* ============================================================
	 *  3) 배송 상태 변경 (REST)
	 *     "배송준비중" → "배송시작" 처럼 상태 갱신
	 * ============================================================ */
	@PutMapping("/updateDelivery")
	@ResponseBody
	@Override
	public ResponseEntity<DeliveryUpdateResponseDTO> updateDelivery(Long id, String productCode,
			boolean outStatus, Integer outStock) {
		DeliveryUpdateResponseDTO result = deliveryService.updateDelivery(id, productCode, outStatus, outStock);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/* ============================================================
	 *  6-1) 양식 다운로드 (참고용)
	 * ============================================================ */
	@Override
	@GetMapping("/downloadDeliveryTemplate")
	public void downloadTemplate(HttpServletResponse response) throws Exception {
		String[] headers = { "userName", "hp1", "hp2", "hp3", "address", "zipcode",
		                     "productCode", "productName", "stockCount" };
		List<String[]> sample = new ArrayList<>();
		sample.add(new String[] { "홍길동", "010", "1234", "5678",
				"서울시 종로구 광화문", "03171", "P0001", "샘플 상품", "1" });

		CsvUtil.writeCsvResponse(response, "delivery_template.csv", headers, sample);
	}

	/* ============================================================
	 *  6-2) 내역 다운로드 — 선택된 배송 ID 목록만
	 * ============================================================ */
	@Override
	@GetMapping("/downloadDeliverySelected")
	public void downloadSelected(@RequestParam("ids") List<Long> ids,
	                              HttpServletResponse response) throws Exception {

		List<DeliveryListResponseDTO> list = deliveryService.exportDeliveriesByIds(ids);

		String[] headers = { "id", "trackingNumber", "userName", "phone",
		                     "address", "zipcode", "productCode", "productName",
		                     "stockCount", "statusDelivery" };
		List<String[]> rows = new ArrayList<>();
		for (DeliveryListResponseDTO d : list) {
			rows.add(new String[] {
					String.valueOf(d.getId()),
					String.valueOf(d.getTrackingNumber()),
					nullSafe(d.getUserName()),
					nullSafe(d.getHp1()) + "-" + nullSafe(d.getHp2()) + "-" + nullSafe(d.getHp3()),
					nullSafe(d.getAddress()),
					nullSafe(d.getZipcode()),
					nullSafe(d.getProductCode()),
					nullSafe(d.getProductName()),
					String.valueOf(d.getStockCount()),
					nullSafe(d.getStatusDelivery())
			});
		}

		CsvUtil.writeCsvResponse(response, "delivery_export_" + currentDateString() + ".csv", headers, rows);
	}

	/* ============================================================
	 *  (참고) 입고 → 재고 변경 화면
	 * ============================================================ */
	@Override
	@GetMapping("/updateByIn")
	public ModelAndView updateByIn() {
		return new ModelAndView("productManagement/productManagement_updateByIn");
	}

	/* ============================================================
	 *  (참고) 출고 → 재고 변경 화면
	 * ============================================================ */
	@Override
	@GetMapping("/updateByOut")
	public ModelAndView updateByOut() {
		return new ModelAndView("productManagement/productManagement_updateByOut");
	}

	// ---------- helper ----------
	private static String nullSafe(String s) { return s == null ? "" : s; }
	private static String currentDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
}
