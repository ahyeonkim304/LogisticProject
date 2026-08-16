package com.logis.wms.repository;

import com.logis.wms.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByDeletedFalse(Pageable pageable);
    List<Product> findByDeletedFalse();
    Optional<Product> findBySkuAndDeletedFalse(String sku);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND (p.sku LIKE %:kw% OR p.name LIKE %:kw%)")
    Page<Product> searchByKeyword(@Param("kw") String keyword, Pageable pageable);

    Page<Product> findByAccount_IdAndDeletedFalse(Long accountId, Pageable pageable);
    List<Product> findByAccount_IdAndDeletedFalse(Long accountId);
    Optional<Product> findBySkuAndAccount_IdAndDeletedFalse(String sku, Long accountId);

    @Query("SELECT p FROM Product p WHERE p.account.id = :accountId AND p.deleted = false " +
           "AND (p.sku LIKE %:kw% OR p.name LIKE %:kw%)")
    Page<Product> searchByKeywordAndAccount(@Param("kw") String keyword,
                                            @Param("accountId") Long accountId,
                                            Pageable pageable);
}
