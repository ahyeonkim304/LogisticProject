package com.ot.main.product.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.product.data.dao.ProductDAO;
import com.ot.main.product.data.dto.ProductCreateRequestDto;
import com.ot.main.product.data.dto.ProductCreateResponseDto;
import com.ot.main.product.data.dto.ProductSearchCodeOrNameResponseDto;
import com.ot.main.product.data.dto.ProductSelectAllResponseDto;
import com.ot.main.product.data.dto.ProductUpdateRequestDto;
import com.ot.main.product.data.dto.ProductUpdateResponseDto;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private ProductDAO productDAO;

	@Autowired
	public ProductServiceImpl(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	// ============================================================
	//  1) 리스트 조회
	// ============================================================
	@Override
	public List<ProductSelectAllResponseDto> seletcAllProduct() {
		List<Product> productList = productDAO.findAllProduct();

		return productList.stream()
				.map(p -> ProductSelectAllResponseDto.builder()
						.productCode(p.getProductCode())
						.safetyStock(p.getSafetyStock())
						.name(p.getName())
						.image(p.getImage())
						.productStock(p.getProductStock())
						.leadTime(p.getLeadTime())
						.create_at(p.getCreate_at())
						.updated_at(p.getUpdated_at())
						.build())
				.collect(Collectors.toList());
	}

	// ============================================================
	//  2) 단건 등록
	// ============================================================
	@Override
	public ProductCreateResponseDto saveProduct(ProductCreateRequestDto dto) {
		Product product = new Product();
		product.setProductCode(dto.getProductCode());
		product.setName(dto.getName());
		product.setImage(dto.getImage());
		product.setProductStock(dto.getProductStock());
		product.setSafetyStock(dto.getSafetyStock());
		product.setLeadTime(dto.getLeadTime());
		product.setCreate_at(LocalDateTime.now());

		Product saved = productDAO.insertProduct(product);

		ProductCreateResponseDto res = new ProductCreateResponseDto();
		res.setProductCode(saved.getProductCode());
		res.setName(saved.getName());
		res.setProductStock(saved.getProductStock());
		res.setImage(saved.getImage());
		res.setSafetyStock(saved.getSafetyStock());
		res.setLeadTime(saved.getLeadTime());
		res.setCreate_at(saved.getCreate_at());
		res.setUpdated_at(saved.getUpdated_at());
		return res;
	}

	// ============================================================
	//  3) 단건 수정
	// ============================================================
	@Override
	public ProductUpdateResponseDto updateProduct(ProductUpdateRequestDto dto) throws Exception {
		Product product = Product.builder()
				.productCode(dto.getProductCode())
				.name(dto.getName())
				.image(dto.getImage())
				.productStock(dto.getProductStock())
				.safetyStock(dto.getSafetyStock())
				.leadTime(dto.getLeadTime())
				.build();

		Product updated = productDAO.updateProduct(product);

		return ProductUpdateResponseDto.builder()
				.productCode(updated.getProductCode())
				.name(updated.getName())
				.productStock(updated.getProductStock())
				.image(updated.getImage())
				.safetyStock(updated.getSafetyStock())
				.leadTime(updated.getLeadTime())
				.updated_at(updated.getUpdated_at())
				.build();
	}

	// ============================================================
	//  4) 단건 삭제
	// ============================================================
	@Override
	public void deleteProduct(String productCode) throws Exception {
		productDAO.deleteProduct(productCode);
	}

	// ============================================================
	//  5) 일괄 등록 (CSV 업로드)
	//     CSV 헤더: productCode,name,productStock,safetyStock,leadTime,image
	//     반환값: 등록된 행 수
	// ============================================================
	@Override
	public int bulkInsertProducts(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
		}

		try (InputStream in = file.getInputStream()) {
			List<Map<String, String>> rows = CsvUtil.parseCsvAsMap(in);

			int count = 0;
			for (Map<String, String> row : rows) {
				if (isBlank(row.get("productCode"))) continue;

				Product product = new Product();
				product.setProductCode(row.get("productCode"));
				product.setName(row.get("name"));
				product.setImage(orDefault(row.get("image"), "/data/MON1.jpg"));
				product.setProductStock(parseInt(row.get("productStock"), 0));
				product.setSafetyStock(parseInt(row.get("safetyStock"), 0));
				product.setLeadTime(parseInt(row.get("leadTime"), 0));
				product.setCreate_at(LocalDateTime.now());

				productDAO.insertProduct(product);
				count++;
			}
			return count;
		}
	}

	// ============================================================
	//  6) 내역 다운로드 — 선택된 상품코드 목록만
	// ============================================================
	@Override
	public List<ProductSelectAllResponseDto> exportProductsByCodes(List<String> productCodes) {
		List<ProductSelectAllResponseDto> all = seletcAllProduct();
		if (productCodes == null || productCodes.isEmpty()) return new ArrayList<>();

		return all.stream()
				.filter(p -> productCodes.contains(p.getProductCode()))
				.collect(Collectors.toList());
	}

	// ============================================================
	//  검색
	// ============================================================
	@Override
	public List<ProductSearchCodeOrNameResponseDto> searchProductCodeOrName(String searchKeyword) {
		return toSearchDtoList(productDAO.findByProductCodeContainingOrNameContaining(searchKeyword));
	}

	@Override
	public List<ProductSearchCodeOrNameResponseDto> searchProductCode(String searchKeyword) {
		return toSearchDtoList(productDAO.findByProductCodeContaining(searchKeyword));
	}

	@Override
	public List<ProductSearchCodeOrNameResponseDto> searchName(String searchKeyword) {
		return toSearchDtoList(productDAO.findByNameContaining(searchKeyword));
	}

	// ============================================================
	//  내부 helper
	// ============================================================
	private List<ProductSearchCodeOrNameResponseDto> toSearchDtoList(List<Product> list) {
		return list.stream()
				.map(p -> ProductSearchCodeOrNameResponseDto.builder()
						.productCode(p.getProductCode())
						.safetyStock(p.getSafetyStock())
						.name(p.getName())
						.image(p.getImage())
						.productStock(p.getProductStock())
						.leadTime(p.getLeadTime())
						.create_at(p.getCreate_at())
						.build())
				.collect(Collectors.toList());
	}

	private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
	private static String orDefault(String v, String d) { return isBlank(v) ? d : v; }
	private static Integer parseInt(String v, int defaultValue) {
		try { return Integer.parseInt(v.trim()); } catch (Exception e) { return defaultValue; }
	}
}
