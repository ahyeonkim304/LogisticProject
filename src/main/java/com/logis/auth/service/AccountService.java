package com.logis.auth.service;

import com.logis.auth.dto.AccountCreateDto;
import com.logis.auth.dto.AccountResponseDto;
import com.logis.auth.dto.AccountUpdateDto;
import com.logis.auth.dto.RegisterDto;
import com.logis.auth.dto.SessionUser;
import com.logis.auth.entity.Account;
import com.logis.auth.repository.AccountRepository;
import com.logis.auth.enums.AccountRole;
import com.logis.auth.enums.AccountStatus;

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
public class AccountService {

    private final AccountRepository accountRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // 인증
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 로그인: APPROVED 상태만 허용
     */
    @Transactional(readOnly = true)
    public SessionUser login(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (Boolean.TRUE.equals(account.getDeleted())) {
            throw new IllegalStateException("삭제된 계정입니다.");
        }

        if (!account.getPassword().equals(password)) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (account.getStatus() == AccountStatus.PENDING) {
            throw new IllegalStateException("관리자 승인 대기 중입니다. 승인 후 로그인 가능합니다.");
        }

        if (account.getStatus() == AccountStatus.REJECTED) {
            throw new IllegalStateException("승인이 거부된 계정입니다. 관리자에게 문의하세요.");
        }

        return new SessionUser(account.getId(), account.getUsername(), account.getRole(), account.getCompanyName());
    }

    /**
     * 회원가입: 기본 상태 PENDING
     */
    public void register(RegisterDto dto) {
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(dto.getPassword());
        account.setCompanyName(dto.getCompanyName());
        account.setRole(AccountRole.CLIENT);
        account.setStatus(AccountStatus.PENDING);
        accountRepository.save(account);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 승인 처리
    // ─────────────────────────────────────────────────────────────────────────

    // 계정을 승인 처리한다
    public void approve(Long id) {
        findActiveAccount(id).setStatus(AccountStatus.APPROVED);
    }

    // 계정을 거부 처리한다
    public void reject(Long id) {
        findActiveAccount(id).setStatus(AccountStatus.REJECTED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 조회
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 단건 조회
     */
    @Transactional(readOnly = true)
    public AccountResponseDto getAccount(Long id) {
        return AccountResponseDto.from(findActiveAccount(id));
    }

    /**
     * 전체 목록 + 키워드 검색 (아이디 OR 회사명)
     */
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getAllAccounts(String keyword, Pageable pageable) {
        Page<Account> page = (keyword != null && !keyword.isBlank())
                ? accountRepository.searchAllByKeyword("%" + keyword.trim().toLowerCase() + "%", pageable)
                : accountRepository.findByDeletedFalse(pageable);
        return page.map(AccountResponseDto::from);
    }

    /**
     * 승인 대기 목록 (키워드 없음 — 다른 Controller 호환용)
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getPendingAccounts() {
        return getPendingAccounts(null);
    }

    /**
     * 승인 대기 목록 + 키워드 검색
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getPendingAccounts(String keyword) {
        List<Account> accounts = (keyword != null && !keyword.isBlank())
                ? accountRepository.searchByStatus(AccountStatus.PENDING, "%" + keyword.trim().toLowerCase() + "%")
                : accountRepository.findByStatusAndDeletedFalse(AccountStatus.PENDING);
        return accounts.stream().map(AccountResponseDto::from).collect(Collectors.toList());
    }

    /**
     * 승인 완료 목록 (키워드 없음 — 다른 Controller 호환용)
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getApprovedAccounts() {
        return getApprovedAccounts(null);
    }

    /**
     * 승인 완료 목록 + 키워드 검색
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getApprovedAccounts(String keyword) {
        List<Account> accounts = (keyword != null && !keyword.isBlank())
                ? accountRepository.searchByStatus(AccountStatus.APPROVED, "%" + keyword.trim().toLowerCase() + "%")
                : accountRepository.findByStatusAndDeletedFalse(AccountStatus.APPROVED);
        return accounts.stream().map(AccountResponseDto::from).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 생성 / 삭제 (ADMIN 전용)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 관리자가 계정 직접 생성
     */
    public void createAccount(AccountCreateDto dto) {
        String username = dto.getUsername() == null ? "" : dto.getUsername().trim();
        if (username.isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + username);
        }
        String password = dto.getPassword() == null ? "" : dto.getPassword().trim();
        if (password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);

        String companyName = dto.getCompanyName();
        if (companyName != null && !companyName.isBlank()) {
            account.setCompanyName(companyName.trim());
        }

        account.setRole(AccountRole.valueOf(
                dto.getRole() != null && !dto.getRole().isBlank() ? dto.getRole() : "CLIENT"));
        account.setStatus(AccountStatus.valueOf(
                dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus() : "APPROVED"));

        accountRepository.save(account);
    }

    /**
     * 계정 소프트 삭제 (ADMIN 전용)
     */
    public void deleteAccount(Long id) {
        Account account = findActiveAccount(id);
        account.setDeleted(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 수정
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 계정 정보 수정 (ADMIN 전용)
     */
    public void updateAccount(Long id, AccountUpdateDto dto) {
        Account account = findActiveAccount(id);

        // 회사명
        if (dto.getCompanyName() != null) {
            String name = dto.getCompanyName().trim();
            account.setCompanyName(name.isEmpty() ? null : name);
        }
        // 권한
        if (dto.getRole() != null && !dto.getRole().isBlank()) {
            account.setRole(AccountRole.valueOf(dto.getRole()));
        }
        // 상태
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            account.setStatus(AccountStatus.valueOf(dto.getStatus()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 초기 관리자 생성 (앱 시작 시 호출 — 차후 삭제 필요)
    // ─────────────────────────────────────────────────────────────────────────

    // 상태·키워드 필터로 계정 목록을 페이징 조회한다 (AdminAccountController 전용)
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> getAccounts(String keyword, String status, Pageable pageable) {
        AccountStatus accountStatus = (status != null && !status.isBlank())
                ? AccountStatus.valueOf(status)
                : null;
        String pattern = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.trim().toLowerCase() + "%" : null;
        Page<Account> page = accountRepository.searchAccounts(accountStatus, pattern, pageable);
        return page.map(AccountResponseDto::from);
    }

    // 계정 상태를 문자열로 받아 enum으로 변환 후 저장한다
    public void changeStatus(Long id, String status) {
        AccountStatus newStatus = AccountStatus.valueOf(status);
        findActiveAccount(id).setStatus(newStatus);
    }

    // 계정 역할을 문자열로 받아 enum으로 변환 후 저장한다
    public void changeRole(Long id, String role) {
        AccountRole newRole = AccountRole.valueOf(role);
        findActiveAccount(id).setRole(newRole);
    }

    // 앱 최초 실행 시 관리자 계정이 없으면 생성한다
    public void createInitialAdmin(String username, String password) {
        if (!accountRepository.existsByUsername(username)) {
            Account admin = new Account();
            admin.setUsername(username);
            admin.setPassword(password);
            admin.setRole(AccountRole.ADMIN);
            admin.setStatus(AccountStatus.APPROVED);
            accountRepository.save(admin);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    // 삭제되지 않은 계정을 ID로 조회하고 없으면 예외를 던진다
    private Account findActiveAccount(Long id) {
        return accountRepository.findById(id)
                .filter(a -> Boolean.FALSE.equals(a.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다: " + id));
    }
}
