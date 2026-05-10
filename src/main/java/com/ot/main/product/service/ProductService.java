package com.ot.main.product.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ot.main.product.data.dto.ProductCreateRequestDto;
import com.ot.main.product.data.dto.ProductCreateResponseDto;
import com.ot.main.product.data.dto.ProductSearchCodeOrNameResponseDto;
import com.ot.main.product.data.dto.ProductSelectAllResponseDto;
import com.ot.main.product.data.dto.ProductUpdateRequestDto;
import com.ot.main.product.data.dto.ProductUpdateResponseDto;
import com.ot.main.product.service.impl.ProductServiceImpl;

/**
 * 상품(Product) 도메인 서비스 계약.
 *
 *  표준 6기능
 *   1) 리스트 조회       : seletcAllProduct
 *   2) 단건 등록         : saveProduct
 *   3) 단건 수정         : updateProduct
 *   4) 단건 삭제         : deleteProduct
 *   5) 일괄 등록 (CSV)   : bulkInsertProducts
 *   6) 양식 다운로드     : 컨트롤러에서 직접 처리 (정적 헤더)
 *      / 내역 다운로드   : exportProductsByCodes
 *
 *  추가
 *   - 검색 (코드/이름) : searchProductCodeOrName / searchProductCode / searchName
 */
public interface ProductService  {
	
	// ---------- 1) 리스트 조회 ----------
	List<ProductSelectAllResponseDto> seletcAllProduct();
	
	// ---------- 2) 단건 등록 ----------
	ProductCreateResponseDto saveProduct(ProductCreateRequestDto productCreateRequestDto);

	// ---------- 3) 단건 수정 ----------
	ProductUpdateResponseDto updateProduct(ProductUpdateRequestDto productUpdateRequestDto) throws Exception;

	// ---------- 4) 단건 삭제 ----------
	void deleteProduct(String productCode) throws Exception;

	// ---------- 5) 일괄 등록 (CSV 업로드) ----------
	int bulkInsertProducts(MultipartFile file) throws Exception;

	// ---------- 6-2) 내역 다운로드 (선택된 상품코드 목록만) ----------
	List<ProductSelectAllResponseDto> exportProductsByCodes(List<String> productCodes);

	// ---------- 검색 ----------
	List<ProductSearchCodeOrNameResponseDto> searchProductCodeOrName(String searchKeyword);
	List<ProductSearchCodeOrNameResponseDto> searchProductCode(String searchKeyword);
	List<ProductSearchCodeOrNameResponseDto> searchName(String searchKeyword);
}
