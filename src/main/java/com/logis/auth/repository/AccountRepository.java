package com.logis.auth.repository;

import com.logis.auth.entity.Account;
import com.logis.auth.enums.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);

    Page<Account> findByDeletedFalse(Pageable pageable);

    List<Account> findByStatusAndDeletedFalse(AccountStatus status);

    boolean existsByUsername(String username);

    @Query("SELECT a FROM Account a WHERE a.deleted = false " +
           "AND (LOWER(a.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR (a.companyName IS NOT NULL AND LOWER(a.companyName) LIKE LOWER(CONCAT('%', :kw, '%'))))")
    Page<Account> searchAllByKeyword(@Param("kw") String kw, Pageable pageable);

    @Query("SELECT a FROM Account a WHERE a.deleted = false AND a.status = :status " +
           "AND (LOWER(a.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR (a.companyName IS NOT NULL AND LOWER(a.companyName) LIKE LOWER(CONCAT('%', :kw, '%'))))")
    List<Account> searchByStatus(@Param("status") AccountStatus status, @Param("kw") String kw);

    @Query("SELECT a FROM Account a WHERE a.deleted = false " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:kw IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "OR :kw IS NULL OR (a.companyName IS NOT NULL AND LOWER(a.companyName) LIKE LOWER(CONCAT('%', :kw, '%'))))")
    Page<Account> searchAccounts(
            @Param("status") AccountStatus status,
            @Param("kw") String kw,
            Pageable pageable);
}
