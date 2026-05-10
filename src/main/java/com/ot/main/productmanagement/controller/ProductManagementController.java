package com.ot.main.productmanagement.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.productmanagement.data.dto.ProductManagementUpdateResponseDTO;

/**
 * 재고(ProductManagement) 컨트롤러 계약.
 *
 * 표준 6기능
 *   - 리스트 조회 / 단건 등록(재고 생성) / 단건 수정(입출고) / 단건 삭제
 *   - 일괄 등록 (CSV) / 양식 다운로드 / 내역 다운로드 (선택 행)
 */
public interface ProductManagementController {

	/** 1) 리스트 조회 */
	public ModelAndView selectStockList();

	/** 단건 상세 */
	public ModelAndView selectStockDetail(@RequestParam Long id);

	/** 2) 단건 등록 (재고 생성) */
	public ModelAndView createStock(@RequestParam String productCode);

	/** 3-1) 입고 처리 */
	public ResponseEntity<ProductManagementUpdateResponseDTO> modifyInStock(
			@RequestParam String productCode, boolean inStatus, Integer inStock);

	/** 3-2) 출고 처리 */
	public ResponseEntity<ProductManagementUpdateResponseDTO> modifyOutStock(
			@RequestParam String productCode, boolean outStatus, Integer outStock);

	/** 4) 단건 삭제 */
	public ModelAndView deleteStock(@RequestParam Long id);

	/** 5) 일괄 등록 (CSV 업로드) */
	public ModelAndView bulkUploadStocks(@RequestParam("file") MultipartFile file);

	/** 6-1) 양식 다운로드 */
	public void downloadTemplate(HttpServletResponse response) throws Exception;

	/** 6-2) 내역 다운로드 (선택된 재고 ID 목록만) */
	public void downloadSelected(@RequestParam("ids") List<Long> ids,
	                             HttpServletResponse response) throws Exception;

	/** 등록 화면 진입 */
	public ModelAndView goToCreateStock();

	/** 안전재고 vs 보유재고 비교 (자동 입/출고 트리거) */
	public void compareStockAndSafetyStock(@RequestParam String productCode);
}
