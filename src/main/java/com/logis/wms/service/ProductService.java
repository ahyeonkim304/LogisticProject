package com.logis.wms.service;

import com.logis.auth.entity.Account;
import com.logis.auth.repository.AccountRepository;
import com.logis.wms.dto.product.ProductRequestDto;
import com.logis.wms.dto.product.ProductResponseDto;
import com.logis.wms.entity.Product;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final AccountRepository accountRepository;

    // 키워드·계정 필터로 상품 목록을 페이징 조회하고 현재 재고 수량을 포함한다
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(String keyword, Long accountId, Pageable pageable) {
        Page<Product> page;
        if (accountId != null) {
            page = (keyword != null && !keyword.isBlank())
                    ? productRepository.searchByKeywordAndAccount(keyword.trim(), accountId, pageable)
                    : productRepository.findByAccount_IdAndDeletedFalse(accountId, pageable);
        } else {
            page = (keyword != null && !keyword.isBlank())
                    ? productRepository.searchByKeyword(keyword.trim(), pageable)
                    : productRepository.findByDeletedFalse(pageable);
        }
        return page.map(p -> {
            Long qty = inventoryRepository.sumQuantityByProductId(p.getId());
            return ProductResponseDto.of(p, qty != null ? qty.intValue() : 0);
        });
    }

    // 페이징 없이 전체 상품 목록을 반환한다
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts(Long accountId) {
        List<Product> products = (accountId != null)
                ? productRepository.findByAccount_IdAndDeletedFalse(accountId)
                : productRepository.findByDeletedFalse();
        return products.stream().map(ProductResponseDto::of).collect(Collectors.toList());
    }

    // 새 상품을 등록한다 (SKU 중복 불가)
    public ProductResponseDto createProduct(ProductRequestDto dto, Long sessionAccountId) {
        Long accountId = resolveAccountId(sessionAccountId, dto.getAccountId());

        boolean duplicateSku = (accountId != null)
                ? productRepository.findBySkuAndAccount_IdAndDeletedFalse(dto.getSku(), accountId).isPresent()
                : productRepository.findBySkuAndDeletedFalse(dto.getSku()).isPresent();
        if (duplicateSku)
            throw new IllegalArgumentException("이미 등록된 SKU입니다: " + dto.getSku());

        Account account = (accountId != null)
                ? accountRepository.findById(accountId)
                        .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."))
                : null;

        Product product = new Product();
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setUnit(dto.getUnit());
        product.setDescription(dto.getDescription());
        product.setSafetyStock(dto.getSafetyStock());
        product.setAccount(account);
        product.setDeleted(false);
        return ProductResponseDto.of(productRepository.save(product));
    }

    // 상품명과 안전 재고량을 수정한다
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto, Long accountId) {
        Product product = findActiveProduct(id);
        verifyOwnership(product, accountId);
        product.setName(dto.getName());
        product.setCategory(dto.getCategory());
        product.setUnit(dto.getUnit());
        product.setDescription(dto.getDescription());
        product.setSafetyStock(dto.getSafetyStock());
        return ProductResponseDto.of(productRepository.save(product));
    }

    // 상품을 소프트 삭제한다
    public void deleteProduct(Long id, Long accountId) {
        Product product = findActiveProduct(id);
        verifyOwnership(product, accountId);
        product.setDeleted(true);
        productRepository.save(product);
    }

    // 삭제되지 않은 상품을 ID로 조회하고 없으면 예외를 던진다
    Product findActiveProduct(Long id) {
        return productRepository.findById(id)
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    // 세션 accountId를 우선 사용하고, 없으면 DTO의 accountId를 사용한다 (둘 다 없으면 null 허용)
    private Long resolveAccountId(Long sessionAccountId, Long dtoAccountId) {
        if (sessionAccountId != null) return sessionAccountId;
        if (dtoAccountId != null) return dtoAccountId;
        return null;
    }

    // 일반 사용자가 다른 계정의 상품에 접근하면 예외를 던진다
    private void verifyOwnership(Product product, Long accountId) {
        if (accountId != null
                && (product.getAccount() == null
                    || !accountId.equals(product.getAccount().getId()))) {
            throw new IllegalStateException("접근 권한이 없습니다.");
        }
    }
}
