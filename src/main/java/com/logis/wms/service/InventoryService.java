package com.logis.wms.service;

import com.logis.wms.dto.inventory.InventoryLogDto;
import com.logis.wms.dto.inventory.LocationInventoryDto;
import com.logis.wms.dto.inventory.StockAddRequestDto;
import com.logis.wms.dto.inventory.StockAdjustRequestDto;
import com.logis.wms.dto.product.ProductResponseDto;
import com.logis.wms.entity.Inventory;
import com.logis.wms.entity.InventoryLog;
import com.logis.wms.entity.Location;
import com.logis.wms.entity.Product;
import com.logis.wms.enums.MovementType;
import com.logis.wms.repository.InventoryLogRepository;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.LocationRepository;
import com.logis.wms.repository.ProductRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;

    // 지정 로케이션에 재고를 추가하고 INBOUND 타입 로그를 기록한다
    public void addStock(Long productId, Long locationId, int quantity, String memo) {
        if (quantity <= 0) throw new IllegalArgumentException("입고 수량은 1 이상이어야 합니다.");
        Product product = findActiveProduct(productId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("로케이션을 찾을 수 없습니다: " + locationId));
        validateAccountMatch(product, location);

        Inventory inventory = inventoryRepository
                .findByProduct_IdAndLocation_Id(productId, locationId)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setLocation(location);
                    inv.setQuantity(0);
                    return inv;
                });
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);

        InventoryLog log = new InventoryLog();
        log.setProduct(product);
        log.setType(MovementType.INBOUND);
        log.setQuantity(quantity);
        log.setLocation(location);
        log.setReferenceType("MANUAL");
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogRepository.save(log);
    }

    // 재고를 증감 조정하고 ADJUSTMENT 타입 로그를 기록한다 (음수 결과 시 예외)
    public void adjustStock(Long productId, Long locationId, int quantity, String memo) {
        Product product = findActiveProduct(productId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("로케이션을 찾을 수 없습니다: " + locationId));
        validateAccountMatch(product, location);

        Inventory inventory = inventoryRepository
                .findByProduct_IdAndLocation_Id(productId, locationId)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProduct(product);
                    inv.setLocation(location);
                    inv.setQuantity(0);
                    return inv;
                });

        int newQty = inventory.getQuantity() + quantity;
        if (newQty < 0)
            throw new IllegalStateException(String.format(
                    "조정 후 재고가 음수가 됩니다. (현재: %d, 조정: %d, 로케이션: %s)",
                    inventory.getQuantity(), quantity, location.getCode()));
        inventory.setQuantity(newQty);
        inventoryRepository.save(inventory);

        InventoryLog log = new InventoryLog();
        log.setProduct(product);
        log.setType(MovementType.ADJUSTMENT);
        log.setQuantity(quantity);
        log.setLocation(location);
        log.setReferenceType("MANUAL");
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogRepository.save(log);
    }

    // 상품별 현재 재고 수량을 포함한 목록을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getInventoryList(String keyword, Long accountId, Pageable pageable) {
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

    // 특정 상품의 로케이션별 재고 현황을 반환한다 (수량 0 제외)
    @Transactional(readOnly = true)
    public List<LocationInventoryDto> getInventoryByLocation(Long productId) {
        return inventoryRepository.findByProduct_IdOrderByIdAsc(productId).stream()
                .filter(i -> i.getQuantity() > 0)
                .map(i -> new LocationInventoryDto(
                        i.getLocation().getId(),
                        i.getLocation().getCode(),
                        i.getLocation().getName(),
                        i.getLocation().getType(),
                        i.getQuantity()))
                .collect(Collectors.toList());
    }

    // 최근 30건의 입출고 로그를 최신순으로 반환한다
    @Transactional(readOnly = true)
    public List<InventoryLogDto> getRecentLogs() {
        return inventoryLogRepository.findTop30ByOrderByCreatedAtDesc().stream()
                .map(InventoryLogDto::from).collect(Collectors.toList());
    }

    // 키워드·날짜 범위로 입출고 이력을 페이징 조회한다
    @Transactional(readOnly = true)
    public Page<InventoryLogDto> getHistoryList(String keyword, Long accountId,
                                                LocalDate startDate, LocalDate endDate,
                                                Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank())
                ? "%" + keyword.trim().toUpperCase() + "%" : null;
        LocalDateTime st = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime et = (endDate   != null) ? endDate.atTime(23, 59, 59) : null;
        return inventoryLogRepository.searchHistory(kw, accountId, st, et, pageable)
                .map(InventoryLogDto::from);
    }

    // SKU로 상품을 조회 후 재고를 추가한다; 로케이션 미지정 시 기존 재고 로케이션을 사용한다
    public void addStockBySku(StockAddRequestDto dto) {
        Product product = productRepository.findBySkuAndDeletedFalse(dto.getProductSku())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + dto.getProductSku()));

        if (dto.getLocationId() != null) {
            addStock(product.getId(), dto.getLocationId(), dto.getQuantity(), dto.getMemo());
            return;
        }

        List<Inventory> inventories = inventoryRepository.findByProduct_IdOrderByIdAsc(product.getId());
        Long locationId = inventories.isEmpty() ? null : inventories.get(0).getLocation().getId();
        if (locationId == null) {
            throw new IllegalArgumentException("로케이션을 지정하거나 기존 재고가 있어야 합니다.");
        }
        addStock(product.getId(), locationId, dto.getQuantity(), dto.getMemo());
    }

    // SKU로 재고를 조정한다; 증가는 첫 번째 로케이션에, 차감은 FIFO 순으로 적용한다
    public void adjustStockBySku(StockAdjustRequestDto dto) {
        Product product = productRepository.findBySkuAndDeletedFalse(dto.getProductSku())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + dto.getProductSku()));

        List<Inventory> inventories = inventoryRepository.findByProduct_IdOrderByIdAsc(product.getId());
        if (inventories.isEmpty()) {
            throw new IllegalStateException("재고 기록이 없습니다: " + dto.getProductSku());
        }

        int remaining = dto.getQuantity();
        if (remaining > 0) {
            Long locationId = inventories.get(0).getLocation().getId();
            adjustStock(product.getId(), locationId, remaining, dto.getReason());
        } else {
            for (Inventory inv : inventories) {
                if (remaining >= 0) break;
                int deduct = Math.max(remaining, -inv.getQuantity());
                adjustStock(product.getId(), inv.getLocation().getId(), deduct, dto.getReason());
                remaining -= deduct;
            }
            if (remaining < 0) {
                throw new IllegalStateException("차감 수량이 총 재고를 초과합니다.");
            }
        }
    }

    // 여러 상품을 한 번에 입고 처리한다
    public void bulkInbound(List<Long> productIds, List<Long> locationIds,
                            List<Integer> quantities, List<String> memos) {
        if (productIds == null || productIds.isEmpty())
            throw new IllegalArgumentException("입고할 상품이 없습니다.");
        for (int i = 0; i < productIds.size(); i++) {
            String memo = (memos != null && i < memos.size()) ? memos.get(i) : null;
            addStock(productIds.get(i), locationIds.get(i), quantities.get(i), memo);
        }
    }

    // 삭제되지 않은 상품을 ID로 조회하고 없으면 예외를 던진다
    private Product findActiveProduct(Long id) {
        return productRepository.findById(id)
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    // 상품과 로케이션의 고객사가 다르면 예외를 던진다
    private void validateAccountMatch(Product product, Location location) {
        if (product.getAccount() != null && location.getAccount() != null
                && !product.getAccount().getId().equals(location.getAccount().getId())) {
            throw new IllegalStateException("상품과 로케이션의 고객사가 일치하지 않습니다.");
        }
    }
}
