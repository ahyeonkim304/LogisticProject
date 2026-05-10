package com.ot.main.product.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.product.data.dto.ProductCreateRequestDto;
import com.ot.main.product.data.dto.ProductSearchCodeOrNameResponseDto;
import com.ot.main.product.data.dto.ProductUpdateRequestDto;

/**
 * 상품(Product) 컨트롤러 계약.
 *
 *  표준 6기능 + 검색
 *   - 리스트 조회 / 단건 등록 / 단건 수정 / 단건 삭제
 *   - 일괄 등록 (CSV 업로드)
 *   - 양식 다운로드
 *   - 내역 다운로드 (선택된 행만)
 */
public interface ProductController {

	/** 1) 리스트 조회 */
	ModelAndView seletcAllProduct();

	/** 2) 단건 등록 */
	ModelAndView saveProduct(@ModelAttribute ProductCreateRequestDto productCreateRequestDto);

	/** (참고) 단건 등록 화면 이동 */
	ModelAndView saveProductPage();

	/** 3) 단건 수정 */
	ModelAndView updateProduct(@ModelAttribute ProductUpdateRequestDto productUpdateRequestDto) throws Exception;

	/** 4) 단건 삭제 */
	ModelAndView deleteProduct(@RequestParam String productCode) throws Exception;

	/** 5) 일괄 등록 (CSV 업로드) */
	ModelAndView bulkUploadProducts(@RequestParam("file") MultipartFile file);

	/** 6-1) 양식 다운로드 */
	void downloadTemplate(HttpServletResponse response) throws Exception;

	/** 6-2) 내역 다운로드 (선택된 상품코드 목록만) */
	void downloadSelected(@RequestParam("productCodes") List<String> productCodes,
	                      HttpServletResponse response) throws Exception;

	/** 검색 (AJAX, JSON) */
	ResponseEntity<List<ProductSearchCodeOrNameResponseDto>> searchProduct(
			@RequestParam String searchKeyword,
			@RequestParam Integer searchType);
}
