package com.logis.wms.controller;

import com.logis.auth.dto.SessionUser;
import com.logis.wms.dto.product.ProductRequestDto;
import com.logis.wms.dto.product.ProductResponseDto;
import com.logis.wms.service.ProductService;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);  
        return ResponseEntity.ok(productService.getProducts(keyword, accountId, pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveAccountId(user);
        return ResponseEntity.ok(productService.getAllProducts(accountId));
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDto dto,
                                           HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        // 쓰기 작업: 클라이언트는 자신의 ID, 어드민은 DTO에서 전달된 accountId 사용
        Long sessionAccountId = resolveWriteAccountId(user);
        try {
            return ResponseEntity.ok(productService.createProduct(dto, sessionAccountId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @RequestBody ProductRequestDto dto,
                                           HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveWriteAccountId(user);
        try {
            return ResponseEntity.ok(productService.updateProduct(id, dto, accountId));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id, HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        Long accountId = resolveWriteAccountId(user);
        try {
            productService.deleteProduct(id, accountId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 읽기: 어드민은 null(전체), 클라이언트는 자신의 accountId
    private Long resolveAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }

    // 쓰기: 클라이언트는 자신의 accountId, 어드민은 null(DTO의 accountId를 서비스에서 사용)
    private Long resolveWriteAccountId(SessionUser user) {
        return (user != null && !user.isAdmin()) ? user.getId() : null;
    }
}
