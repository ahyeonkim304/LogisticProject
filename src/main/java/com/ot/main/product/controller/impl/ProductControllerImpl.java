package com.ot.main.product.controller.impl;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.product.controller.ProductController;
import com.ot.main.product.data.dto.ProductCreateRequestDto;
import com.ot.main.product.data.dto.ProductCreateResponseDto;
import com.ot.main.product.data.dto.ProductSearchCodeOrNameResponseDto;
import com.ot.main.product.data.dto.ProductSelectAllResponseDto;
import com.ot.main.product.data.dto.ProductUpdateRequestDto;
import com.ot.main.product.data.dto.ProductUpdateResponseDto;
import com.ot.main.product.service.ProductService;

/**
 * 상품(Product) REST/MVC 컨트롤러.
 *
 *  Base URL : /api/v1/main-fulfillment/product
 *
 *  엔드포인트 요약
 *   GET  /selectAll          : 리스트 조회 화면
 *   POST /create             : 단건 등록 처리
 *   GET  /createPage         : 단건 등록 화면 진입
 *   POST /update             : 단건 수정 처리
 *   POST /updatePage         : 단건 수정 화면 진입
 *   POST /delete             : 단건 삭제 처리
 *   POST /bulkUpload         : 일괄 등록 (CSV 업로드)
 *   GET  /downloadTemplate   : 일괄 등록용 CSV 양식 다운로드
 *   GET  /downloadSelected   : 선택된 상품 내역 다운로드 (CSV)
 *   GET  /search             : 검색 (AJAX, JSON)
 */
@Controller
@RequestMapping("/api/v1/main-fulfillment/product")
public class ProductControllerImpl implements ProductController {

	private final ProductService productService;

	@Autowired
	public ProductControllerImpl(ProductService productService) {
		this.productService = productService;
	}

	/* ============================================================
	 *  1) 리스트 조회
	 *     GET /selectAll
	 *     상품 전체 목록을 조회해서 product_selectall.jsp 로 렌더
	 * ============================================================ */
	@Override
	@GetMapping("/selectAll")
	public ModelAndView seletcAllProduct() {
		List<ProductSelectAllResponseDto> products = productService.seletcAllProduct();

		ModelAndView mav = new ModelAndView();
		mav.addObject("products", products);
		mav.setViewName("product/product_selectall");
		return mav;
	}

	/* ============================================================
	 *  2) 단건 등록
	 *     POST /create
	 *     폼 데이터를 받아 신규 상품 1건 등록 후 목록으로 redirect
	 * ============================================================ */
	@Override
	@PostMapping("/create")
	public ModelAndView saveProduct(@ModelAttribute ProductCreateRequestDto dto) {
		ProductCreateResponseDto saved = productService.saveProduct(dto);
		System.out.println("saved : " + saved);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/product/selectAll");
		//return new ModelAndView("product/selectAll");
	}

	/* ============================================================
	 *  (참고) 단건 등록 화면 진입
	 *     GET / POST /createPage
	 *     별도 페이지 등록 화면 (drawer 외 fallback 용)
	 * ============================================================ */
	@Override
	@PostMapping("/createPage")
	public ModelAndView saveProductPage() {
		return new ModelAndView("product/product_create");
	}

	@GetMapping("/createPage")
	public ModelAndView saveProductPageGet() {
		return new ModelAndView("product/product_create");
	}

	/* ============================================================
	 *  3) 단건 수정
	 *     POST /update
	 *     상품 1건 수정. 실패 시 product_error 로 이동, 성공 시 목록 redirect
	 * ============================================================ */
	@Override
	@PostMapping("/update")
	public ModelAndView updateProduct(@ModelAttribute ProductUpdateRequestDto dto) throws Exception {
		ProductUpdateResponseDto updated = productService.updateProduct(dto);

		if (updated.getProductCode() == null || updated.getProductCode().isEmpty()) {
			return new ModelAndView("product/product_error");
		}
		return new ModelAndView("redirect:/api/v1/main-fulfillment/product/selectAll");
	}

	/* (참고) 단건 수정 화면 진입 - POST /updatePage */
	@PostMapping("/updatePage")
	public ModelAndView updateProductPage(@ModelAttribute ProductUpdateRequestDto dto) {
		ModelAndView mav = new ModelAndView("product/product_update");
		mav.addObject("product", dto);
		return mav;
	}

	/* ============================================================
	 *  4) 단건 삭제
	 *     POST /delete
	 *     productCode 로 상품 1건 삭제 후 목록 redirect
	 * ============================================================ */
	@Override
	@PostMapping("/delete")
	public ModelAndView deleteProduct(@RequestParam String productCode) throws Exception {
		productService.deleteProduct(productCode);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/product/selectAll");
	}

	/* ============================================================
	 *  5) 일괄 등록 (CSV 업로드)
	 *     POST /bulkUpload  (multipart/form-data, name="file")
	 *     CSV 1행 = 상품 1건. 헤더는 양식 다운로드 컬럼과 동일해야 함
	 *     성공 시 목록으로 redirect (등록건수 alert 는 프론트에서 처리)
	 * ============================================================ */
	@Override
	@PostMapping("/bulkUpload")
	public ModelAndView bulkUploadProducts(@RequestParam("file") MultipartFile file) {
		try {
			int inserted = productService.bulkInsertProducts(file);
			System.out.println("일괄 등록 완료 : " + inserted + " 건");
		} catch (Exception e) {
			e.printStackTrace();
			ModelAndView mav = new ModelAndView("product/product_error");
			mav.addObject("errorMessage", "일괄 등록 중 오류: " + e.getMessage());
			return mav;
		}
		return new ModelAndView("redirect:/api/v1/main-fulfillment/product/selectAll");
	}

	/* ============================================================
	 *  6-1) 양식 다운로드
	 *     GET /downloadTemplate
	 *     일괄 등록용 빈 CSV 양식을 내려보냄. 헤더만 들어있음
	 * ============================================================ */
	@Override
	@GetMapping("/downloadTemplate")
	public void downloadTemplate(HttpServletResponse response) throws Exception {
		String[] headers = { "productCode", "name", "productStock", "safetyStock", "leadTime", "image" };
		// 예시 행 1줄 같이 제공 (사용자 편의)
		List<String[]> sample = new ArrayList<>();
		sample.add(new String[] { "P0001", "샘플 상품", "100", "30", "3", "/data/MON1.jpg" });

		CsvUtil.writeCsvResponse(response, "product_template.csv", headers, sample);
	}

	/* ============================================================
	 *  6-2) 내역 다운로드 (선택된 행만)
	 *     GET /downloadSelected?productCodes=P0001,P0002,...
	 *     체크박스로 선택된 상품 코드 목록을 받아 CSV 로 내려보냄
	 * ============================================================ */
	@Override
	@GetMapping("/downloadSelected")
	public void downloadSelected(@RequestParam("productCodes") List<String> productCodes,
	                              HttpServletResponse response) throws Exception {

		List<ProductSelectAllResponseDto> list = productService.exportProductsByCodes(productCodes);

		String[] headers = { "productCode", "name", "productStock", "safetyStock", "leadTime", "image", "create_at", "updated_at" };
		List<String[]> rows = new ArrayList<>();
		for (ProductSelectAllResponseDto p : list) {
			rows.add(new String[] {
					nullSafe(p.getProductCode()),
					nullSafe(p.getName()),
					String.valueOf(p.getProductStock()),
					String.valueOf(p.getSafetyStock()),
					String.valueOf(p.getLeadTime()),
					nullSafe(p.getImage()),
					p.getCreate_at() == null ? "" : p.getCreate_at().toString(),
					p.getUpdated_at() == null ? "" : p.getUpdated_at().toString()
			});
		}

		String fileName = "product_export_" + currentDateString() + ".csv";
		CsvUtil.writeCsvResponse(response, fileName, headers, rows);
	}

	/* ============================================================
	 *  검색 (AJAX, JSON)
	 *     GET /search?searchKeyword=...&searchType=0|1|2
	 * ============================================================ */
	@Override
	@GetMapping("/search")
	@ResponseBody
	public ResponseEntity<List<ProductSearchCodeOrNameResponseDto>> searchProduct(
			@RequestParam String searchKeyword,
			@RequestParam Integer searchType) {

		List<ProductSearchCodeOrNameResponseDto> result;

		// 0: 코드+이름 / 1: 코드 / 2: 이름
		if (searchType.equals(0))      result = productService.searchProductCodeOrName(searchKeyword);
		else if (searchType.equals(1)) result = productService.searchProductCode(searchKeyword);
		else if (searchType.equals(2)) result = productService.searchName(searchKeyword);
		else                            result = new ArrayList<>();

		if (result == null || result.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		return ResponseEntity.ok(result);
	}

	// ---------- helper ----------
	private static String nullSafe(String s) { return s == null ? "" : s; }
	private static String currentDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
}
