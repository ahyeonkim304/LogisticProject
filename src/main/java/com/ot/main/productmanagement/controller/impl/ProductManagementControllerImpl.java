package com.ot.main.productmanagement.controller.impl;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.delivery.data.dto.DeliveryCreateRequestDTO;
import com.ot.main.delivery.service.DeliveryService;
import com.ot.main.productmanagement.controller.ProductManagementController;
import com.ot.main.productmanagement.data.dto.MainToShopDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementCreateResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectListResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectOneResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementUpdateResponseDTO;
import com.ot.main.productmanagement.data.dto.ShopToMainResponseDTO;
import com.ot.main.productmanagement.service.ProductManagementService;

/**
 * 재고(ProductManagement) REST/MVC 컨트롤러.
 *
 *  Base URL : /api/v1/main-fulfillment
 *
 *  엔드포인트 요약
 *   GET  /lookUpStock                 : 재고 리스트 조회 화면
 *   GET  /selectStockDetail           : 재고 상세 조회 화면
 *   GET  /goToCreateStock             : 재고 생성 화면 진입
 *   POST /createStock                 : 재고 단건 등록
 *   POST /deleteStock                 : 재고 단건 삭제
 *   PUT  /modifyInStock               : 입고 처리 (재고 +)
 *   PUT  /modifyOutStock              : 출고 처리 (재고 −)
 *   POST /bulkUploadStocks            : 일괄 등록 (CSV)
 *   GET  /downloadStockTemplate       : 양식 다운로드 (CSV)
 *   GET  /downloadStockSelected       : 선택 재고 내역 다운로드 (CSV)
 *   GET  /compareStockAndSafetyStock  : 안전재고-보유재고 비교
 *   POST /productManagement/mainToShop : 메인 → 쇼핑 통신
 *   POST /productManagement/shopToMain : 쇼핑 → 메인 응답 수신
 */
@Controller
@RequestMapping(value = "/api/v1/main-fulfillment")
public class ProductManagementControllerImpl implements ProductManagementController {

	private final ProductManagementService productManagementService;
	private final DeliveryService deliveryService;

	@Autowired
	public ProductManagementControllerImpl(ProductManagementService productManagementService,
	                                       DeliveryService deliveryService) {
		this.productManagementService = productManagementService;
		this.deliveryService = deliveryService;
	}

	/* ============================================================
	 *  1) 리스트 조회
	 * ============================================================ */
	@Override
	@GetMapping("/lookUpStock")
	public ModelAndView selectStockList() {
		List<ProductManagementSelectListResponseDTO> stockList = productManagementService.selectStockList();
		ModelAndView mav = new ModelAndView();
		mav.addObject("stockList", stockList);
		mav.setViewName("productManagement/productManagement_List");
		return mav;
	}

	/* ============================================================
	 *  단건 상세 조회
	 * ============================================================ */
	@Override
	@GetMapping("/selectStockDetail")
	public ModelAndView selectStockDetail(@RequestParam Long id) {
		ProductManagementSelectOneResponseDTO oneStock = productManagementService.selectStockDetail(id);
		ModelAndView mav = new ModelAndView();
		mav.addObject("oneStock", oneStock);
		mav.setViewName("productManagement/productManagement_Detail");
		return mav;
	}

	/* (참고) 등록 화면 진입 */
	@Override
	@GetMapping("/goToCreateStock")
	public ModelAndView goToCreateStock() {
		return new ModelAndView("productManagement/productManagement_create");
	}

	/* ============================================================
	 *  2) 단건 등록 (재고 생성)
	 * ============================================================ */
	@Override
	@PostMapping("/createStock")
	public ModelAndView createStock(@RequestParam String productCode) {
		ProductManagementCreateResponseDTO created = productManagementService.createStock(productCode);
		ModelAndView mav = new ModelAndView();
		mav.addObject("createProduct", created);
		mav.setViewName("redirect:/api/v1/main-fulfillment/lookUpStock");
		return mav;
	}

	/* ============================================================
	 *  3-1) 입고 처리 (REST)
	 * ============================================================ */
	@Override
	@PutMapping("/modifyInStock")
	@ResponseBody
	public ResponseEntity<ProductManagementUpdateResponseDTO> modifyInStock(
			@RequestParam String productCode, boolean inStatus, Integer inStock) {
		ProductManagementUpdateResponseDTO updated = productManagementService.modifyInStock(productCode, inStatus, inStock);
		return ResponseEntity.status(HttpStatus.OK).body(updated);
	}

	/* ============================================================
	 *  3-2) 출고 처리 (REST)
	 * ============================================================ */
	@Override
	@PutMapping("/modifyOutStock")
	@ResponseBody
	public ResponseEntity<ProductManagementUpdateResponseDTO> modifyOutStock(
			@RequestParam String productCode, boolean outStatus, Integer outStock) {
		ProductManagementUpdateResponseDTO updated = productManagementService.modifyOutStock(productCode, outStatus, outStock);
		return ResponseEntity.status(HttpStatus.OK).body(updated);
	}

	/* ============================================================
	 *  4) 단건 삭제
	 * ============================================================ */
	@Override
	@PostMapping("/deleteStock")
	public ModelAndView deleteStock(@RequestParam Long id) {
		productManagementService.deleteStock(id);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/lookUpStock");
	}

	/* ============================================================
	 *  5) 일괄 등록 (CSV 업로드)
	 *     CSV 헤더 : productCode
	 * ============================================================ */
	@Override
	@PostMapping("/bulkUploadStocks")
	public ModelAndView bulkUploadStocks(@RequestParam("file") MultipartFile file) {
		try {
			int n = productManagementService.bulkInsertStocks(file);
			System.out.println("재고 일괄 등록 완료 : " + n + " 건");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ModelAndView("redirect:/api/v1/main-fulfillment/lookUpStock");
	}

	/* ============================================================
	 *  6-1) 양식 다운로드
	 * ============================================================ */
	@Override
	@GetMapping("/downloadStockTemplate")
	public void downloadTemplate(HttpServletResponse response) throws Exception {
		String[] headers = { "productCode" };
		List<String[]> sample = new ArrayList<>();
		sample.add(new String[] { "P0001" });

		CsvUtil.writeCsvResponse(response, "stock_template.csv", headers, sample);
	}

	/* ============================================================
	 *  6-2) 내역 다운로드 — 선택된 재고 ID 목록만
	 * ============================================================ */
	@Override
	@GetMapping("/downloadStockSelected")
	public void downloadSelected(@RequestParam("ids") List<Long> ids,
	                              HttpServletResponse response) throws Exception {

		List<ProductManagementSelectListResponseDTO> list = productManagementService.exportStocksByIds(ids);

		String[] headers = { "id", "productCode", "name", "productStock", "safetyStock", "leadTime" };
		List<String[]> rows = new ArrayList<>();
		for (ProductManagementSelectListResponseDTO s : list) {
			rows.add(new String[] {
					String.valueOf(s.getId()),
					nullSafe(s.getProductCode()),
					nullSafe(s.getName()),
					String.valueOf(s.getProductStock()),
					String.valueOf(s.getSafetyStock()),
					String.valueOf(s.getLeadTime())
			});
		}

		CsvUtil.writeCsvResponse(response, "stock_export_" + currentDateString() + ".csv", headers, rows);
	}

	/* ============================================================
	 *  안전재고 vs 보유재고 비교
	 * ============================================================ */
	@Override
	@GetMapping("/compareStockAndSafetyStock")
	@ResponseBody
	public void compareStockAndSafetyStock(@RequestParam String productCode) {
		productManagementService.compareStockAndSafetyStock(productCode);
	}

	/* ============================================================
	 *  WebClient — 메인 → 쇼핑
	 * ============================================================ */
	@PostMapping("/productManagement/mainToShop")
	@ResponseBody
	public ResponseEntity<MainToShopDTO> mainToShop(@RequestBody MainToShopDTO dto) {
		return productManagementService.mainToShop(dto);
	}

	/* ============================================================
	 *  WebClient — 쇼핑 → 메인 (주문 → 배송 자동 생성 + 재고비교)
	 * ============================================================ */
	@PostMapping("/productManagement/shopToMain")
	@ResponseBody
	public ResponseEntity<ShopToMainResponseDTO> shopToMain(@RequestBody ShopToMainResponseDTO shopRes) {
		// 배송 생성
		DeliveryCreateRequestDTO d = new DeliveryCreateRequestDTO();
		d.setUserName(shopRes.getUserName());
		d.setHp1(shopRes.getHp1());
		d.setHp2(shopRes.getHp2());
		d.setHp3(shopRes.getHp3());
		d.setAddress(shopRes.getAddress());
		d.setZipcode(shopRes.getZipcode());
		d.setProductName(shopRes.getProductName());
		d.setStockCount(shopRes.getOrderCount());
		d.setProductCode(shopRes.getProductCode());
		deliveryService.createDelivery(d);

		// 재고 비교 → 안전재고 미달 시 자동 입고 요청 트리거
		productManagementService.compareStockAndSafetyStock(shopRes.getProductCode());

		return ResponseEntity.status(HttpStatus.OK).body(shopRes);
	}

	// ---------- helper ----------
	private static String nullSafe(String s) { return s == null ? "" : s; }
	private static String currentDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
}
