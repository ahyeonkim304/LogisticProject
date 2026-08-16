package com.logis.wms.service;

import com.logis.auth.dto.AccountResponseDto;
import com.logis.auth.entity.Account;
import com.logis.auth.enums.AccountStatus;
import com.logis.auth.repository.AccountRepository;
import com.logis.wms.dto.location.LocationMetaDto;
import com.logis.wms.dto.location.LocationRequestDto;
import com.logis.wms.dto.location.LocationResponseDto;
import com.logis.wms.entity.Location;
import com.logis.wms.enums.LocationType;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.LocationRepository;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final AccountRepository accountRepository;
    private final InventoryRepository inventoryRepository;

    // 로케이션 목록을 타입·코드 순으로 정렬하여 반환한다
    @Transactional(readOnly = true)
    public List<LocationResponseDto> getLocations(Long accountId) {
        List<Location> list = (accountId != null)
                ? locationRepository.findByAccount_IdOrderByTypeAscCodeAsc(accountId)
                : locationRepository.findAllByOrderByTypeAscCodeAsc();
        return list.stream().map(LocationResponseDto::from).collect(Collectors.toList());
    }

    // 로케이션 폼용 메타데이터(타입 목록·BIN 제외 상위 후보·승인된 고객사)를 조합하여 반환한다
    @Transactional(readOnly = true)
    public LocationMetaDto getLocationMeta(Long accountId) {
        List<LocationResponseDto> allLocations = getLocations(accountId);

        List<LocationResponseDto> parentCandidates = allLocations.stream()
                .filter(l -> !LocationType.BIN.name().equals(l.getType()))
                .collect(Collectors.toList());

        List<String> locationTypes = Arrays.stream(LocationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        List<AccountResponseDto> accounts = accountRepository
                .findByStatusAndDeletedFalse(AccountStatus.APPROVED).stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());

        return new LocationMetaDto(locationTypes, parentCandidates, accounts);
    }

    // BIN을 제외한 상위 로케이션 후보 목록을 반환한다
    @Transactional(readOnly = true)
    public List<Location> getParentCandidates(Long accountId) {
        List<Location> all = (accountId != null)
                ? locationRepository.findByAccount_IdOrderByTypeAscCodeAsc(accountId)
                : locationRepository.findAllByOrderByTypeAscCodeAsc();
        return all.stream()
                .filter(l -> l.getType() != LocationType.BIN)
                .collect(Collectors.toList());
    }

    // 새 로케이션을 생성한다 (코드 중복 불가, 상위 로케이션 소유권 검증 포함)
    public void createLocation(LocationRequestDto dto, Long sessionAccountId) {
        Long effectiveAccountId = resolveAccountId(sessionAccountId, dto.getAccountId());

        String code = dto.getCode().trim().toUpperCase();
        boolean duplicate = (effectiveAccountId != null)
                ? locationRepository.existsByAccount_IdAndCode(effectiveAccountId, code)
                : locationRepository.existsByCode(code);
        if (duplicate)
            throw new IllegalArgumentException("이미 사용 중인 코드입니다: " + dto.getCode());

        Account account = (effectiveAccountId != null)
                ? accountRepository.findById(effectiveAccountId)
                        .orElseThrow(() -> new IllegalArgumentException("고객사를 찾을 수 없습니다."))
                : null;

        Location loc = new Location();
        loc.setAccount(account);
        loc.setCode(code);
        loc.setName(dto.getName());
        loc.setType(dto.getType());
        loc.setActive(true);

        if (dto.getParentId() != null) {
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 로케이션을 찾을 수 없습니다."));
            verifyOwnership(parent, effectiveAccountId);
            loc.setParent(parent);
        }

        locationRepository.save(loc);
    }

    // 로케이션 코드·이름·타입·상위를 수정한다
    public void updateLocation(Long id, LocationRequestDto dto, Long sessionAccountId) {
        Location loc = findAndVerify(id, sessionAccountId);
        Long effectiveAccountId = loc.getAccount() != null ? loc.getAccount().getId() : null;

        String newCode = dto.getCode().trim().toUpperCase();
        if (!loc.getCode().equals(newCode)
                && locationRepository.existsByAccount_IdAndCodeAndIdNot(effectiveAccountId, newCode, id))
            throw new IllegalArgumentException("이미 사용 중인 코드입니다: " + dto.getCode());

        loc.setCode(newCode);
        loc.setName(dto.getName());
        loc.setType(dto.getType());

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) throw new IllegalArgumentException("자기 자신을 상위로 지정할 수 없습니다.");
            Location parent = locationRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("상위 로케이션을 찾을 수 없습니다."));
            verifyOwnership(parent, effectiveAccountId);
            loc.setParent(parent);
        } else {
            loc.setParent(null);
        }
    }

    // 로케이션 활성 상태를 토글한다 (재고 있는 로케이션은 비활성화 불가)
    public void toggleActive(Long id, Long accountId) {
        Location loc = findAndVerify(id, accountId);
        if (Boolean.TRUE.equals(loc.getActive())
                && inventoryRepository.existsByLocation_IdAndQuantityGreaterThan(id, 0))
            throw new IllegalStateException("재고가 존재하는 로케이션은 비활성화할 수 없습니다.");
        loc.setActive(!Boolean.TRUE.equals(loc.getActive()));
    }

    // 로케이션을 삭제한다 (재고 있으면 삭제 불가)
    public void deleteLocation(Long id, Long accountId) {
        Location loc = findAndVerify(id, accountId);
        if (inventoryRepository.existsByLocation_IdAndQuantityGreaterThan(id, 0))
            throw new IllegalStateException("재고가 존재하는 로케이션은 삭제할 수 없습니다.");
        locationRepository.delete(loc);
    }

    // 세션 accountId를 우선 사용하고, 없으면 DTO의 accountId를 사용한다 (둘 다 없으면 null 허용)
    private Long resolveAccountId(Long sessionAccountId, Long dtoAccountId) {
        if (sessionAccountId != null) return sessionAccountId;
        if (dtoAccountId != null) return dtoAccountId;
        return null;
    }

    // 로케이션을 조회하고 접근 권한을 검증한다
    private Location findAndVerify(Long id, Long accountId) {
        Location loc = locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("로케이션을 찾을 수 없습니다."));
        verifyOwnership(loc, accountId);
        return loc;
    }

    // 일반 사용자가 다른 계정의 로케이션에 접근하면 예외를 던진다
    private void verifyOwnership(Location loc, Long accountId) {
        if (accountId != null && (loc.getAccount() == null
                || !loc.getAccount().getId().equals(accountId)))
            throw new IllegalStateException("접근 권한이 없습니다.");
    }
}
