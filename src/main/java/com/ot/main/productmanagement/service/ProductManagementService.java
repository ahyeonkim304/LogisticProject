package com.ot.main.productmanagement.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.ot.main.productmanagement.data.dto.MainToShopDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementCompareResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementCreateResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectListResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectOneResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementUpdateResponseDTO;

/**
 * 재고(ProductManagement) 도메인 서비스 계약.
 *
 * 표준 6기능
 *  1) 리스트 조회        : selectStockList
 *  2) 단건 등록 (생성)   : createStock
 *  3) 단건 수정 (입/출고): modifyInStock / modifyOutStock
 *  4) 단건 삭제          : deleteStock
 *  5) 일괄 등록 (CSV)    : bulkInsertStocks
 *  6) 내역 다운로드      : exportStocksByIds
 *
 * 기타
 *  - 상세 조회 / 안전재고 비교 / 쇼핑 통신
 */
public interface ProductManagementService {

	/** 1) 리스트 조회 */
	List<ProductManagementSelectListResponseDTO> selectStockList();

	/** 단건 상세 조회 */
	ProductManagementSelectOneResponseDTO selectStockDetail(Long id);

	/** 2) 단건 등록 (재고 생성 — productCode 만 받아서 상품 정보로 자동 채움) */
	ProductManagementCreateResponseDTO createStock(String productCode);

	/** 3-1) 입고 처리 (재고 +) */
	ProductManagementUpdateResponseDTO modifyInStock(String productCode, boolean inStatus, Integer inStock);

	/** 3-2) 출고 처리 (재고 −) */
	ProductManagementUpdateResponseDTO modifyOutStock(String productCode, boolean outStatus, Integer outStock);

	/** 4) 단건 삭제 */
	String deleteStock(Long id);

	/** 5) 일괄 등록 (CSV 업로드) */
	int bulkInsertStocks(MultipartFile file) throws Exception;

	/** 6) 내역 다운로드 — 선택된 재고 ID 목록만 */
	List<ProductManagementSelectListResponseDTO> exportStocksByIds(List<Long> ids);

	/** 안전재고 vs 보유재고 비교 (자동 입/출고 트리거) */
	ProductManagementCompareResponseDTO compareStockAndSafetyStock(String productCode);

	/** 메인 → 쇼핑 통신 */
	ResponseEntity<MainToShopDTO> mainToShop(MainToShopDTO mainToShopDTO);
}
