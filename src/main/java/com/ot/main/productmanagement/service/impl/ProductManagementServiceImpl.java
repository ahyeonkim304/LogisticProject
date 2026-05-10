package com.ot.main.productmanagement.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.productmanagement.data.dao.ProductManagementDAO;
import com.ot.main.productmanagement.data.dto.MainToShopDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementCompareResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementCreateResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectListResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementSelectOneResponseDTO;
import com.ot.main.productmanagement.data.dto.ProductManagementUpdateResponseDTO;
import com.ot.main.productmanagement.data.entity.ProductManagement;
import com.ot.main.productmanagement.service.ProductManagementService;

@Service
public class ProductManagementServiceImpl implements ProductManagementService {

	private final ProductManagementDAO productManagementDAO;

	@Autowired
	public ProductManagementServiceImpl(ProductManagementDAO productManagementDAO) {
		this.productManagementDAO = productManagementDAO;
	}

	// ============================================================
	//  1) 리스트 조회
	// ============================================================
	@Override
	public List<ProductManagementSelectListResponseDTO> selectStockList() {
		return productManagementDAO.selectStockList().stream()
				.map(s -> new ProductManagementSelectListResponseDTO(
						s.getId(), s.getProductCode(), s.getLeadTime(),
						s.getName(), s.getProductStock(), s.getSafetyStock()))
				.collect(Collectors.toList());
	}

	/** 단건 상세 조회 */
	@Override
	public ProductManagementSelectOneResponseDTO selectStockDetail(Long id) {
		ProductManagement s = productManagementDAO.selectOneStock(id);

		ProductManagementSelectOneResponseDTO res = new ProductManagementSelectOneResponseDTO();
		res.setId(s.getId());
		res.setLeadTime(s.getLeadTime());
		res.setName(s.getName());
		res.setProductCode(s.getProductCode());
		res.setProductStock(s.getProductStock());
		res.setSafetyStock(s.getSafetyStock());
		return res;
	}

	// ============================================================
	//  2) 단건 등록 (재고 생성)
	// ============================================================
	@Override
	public ProductManagementCreateResponseDTO createStock(String productCode) {
		ProductManagement pm = new ProductManagement();
		pm.setProductCode(productCode);

		ProductManagement created = productManagementDAO.createStock(pm);

		ProductManagementCreateResponseDTO res = new ProductManagementCreateResponseDTO();
		res.setId(created.getId());
		res.setLeadTime(created.getLeadTime());
		res.setName(created.getName());
		res.setProductCode(created.getProductCode());
		res.setProductStock(created.getProductStock());
		res.setSafetyStock(created.getSafetyStock());
		return res;
	}

	// ============================================================
	//  3-1) 입고 처리 (재고 +)
	// ============================================================
	@Override
	public ProductManagementUpdateResponseDTO modifyInStock(String productCode, boolean inStatus, Integer inStock) {
		ProductManagement updated = productManagementDAO.modifyInStock(productCode, inStatus, inStock);
		return toUpdateDto(updated);
	}

	// ============================================================
	//  3-2) 출고 처리 (재고 −)
	// ============================================================
	@Override
	public ProductManagementUpdateResponseDTO modifyOutStock(String productCode, boolean outStatus, Integer outStock) {
		ProductManagement updated = productManagementDAO.modifyOutStock(productCode, outStatus, outStock);
		return toUpdateDto(updated);
	}

	// ============================================================
	//  4) 단건 삭제
	// ============================================================
	@Override
	public String deleteStock(Long id) {
		return productManagementDAO.deleteStock(id);
	}

	// ============================================================
	//  5) 일괄 등록 (CSV 업로드)
	//     CSV 헤더 : productCode
	//     (재고 생성은 기존 상품 정보로부터 자동 채워지므로 productCode 만 필요)
	// ============================================================
	@Override
	public int bulkInsertStocks(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
		}

		try (InputStream is = file.getInputStream()) {
			List<Map<String, String>> rows = CsvUtil.parseCsvAsMap(is);

			int count = 0;
			for (Map<String, String> row : rows) {
				String productCode = row.get("productCode");
				if (productCode == null || productCode.trim().isEmpty()) continue;

				ProductManagement pm = new ProductManagement();
				pm.setProductCode(productCode);
				productManagementDAO.createStock(pm);
				count++;
			}
			return count;
		}
	}

	// ============================================================
	//  6) 내역 다운로드 — 선택된 재고 ID 목록만
	// ============================================================
	@Override
	public List<ProductManagementSelectListResponseDTO> exportStocksByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return new ArrayList<>();
		return selectStockList().stream()
				.filter(s -> ids.contains(s.getId()))
				.collect(Collectors.toList());
	}

	// ============================================================
	//  안전재고 vs 보유재고 비교
	// ============================================================
	@Override
	public ProductManagementCompareResponseDTO compareStockAndSafetyStock(String productCode) {
		productManagementDAO.compareStockAndSafetyStock(productCode);
		return null;
	}

	// ============================================================
	//  쇼핑 통신
	// ============================================================
	@Override
	public ResponseEntity<MainToShopDTO> mainToShop(MainToShopDTO mainToShopDTO) {
		WebClient webClient = WebClient.builder()
				.baseUrl("http://localhost:9000")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

		return webClient.post()
				.uri("/api/v1/shop-fulfillment/mainToShop")
				.bodyValue(mainToShopDTO)
				.retrieve()
				.toEntity(MainToShopDTO.class)
				.block();
	}

	// ---------- helper ----------
	private ProductManagementUpdateResponseDTO toUpdateDto(ProductManagement s) {
		ProductManagementUpdateResponseDTO res = new ProductManagementUpdateResponseDTO();
		res.setId(s.getId());
		res.setLeadTime(s.getLeadTime());
		res.setName(s.getName());
		res.setProductCode(s.getProductCode());
		res.setProductStock(s.getProductStock());
		res.setSafetyStock(s.getSafetyStock());
		return res;
	}
}
