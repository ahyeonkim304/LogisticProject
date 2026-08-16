package com.logis.wms.service;

import com.logis.wms.dto.location.LocationInventoryDetailDto;
import com.logis.wms.dto.location.LocationInventoryDetailDto.SkuRowDto;
import com.logis.wms.dto.location.LocationTreeNodeDto;
import com.logis.wms.entity.Inventory;
import com.logis.wms.entity.Location;
import com.logis.wms.enums.LocationType;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.LocationRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationInventoryService {

    private final LocationRepository  locationRepository;
    private final InventoryRepository inventoryRepository;

    // 로케이션 계층 트리를 구성하고 각 노드에 SKU 수·총 재고·부족 재고 수를 집계한다
    public List<LocationTreeNodeDto> getLocationTree(Long accountId) {
        List<Location> all = loadLocations(accountId);
        Map<Long, LocationTreeNodeDto> nodeMap = new LinkedHashMap<>();

        for (Location loc : all) {
            if (loc.getType() == null) continue; // type 미설정 로케이션 건너뜀
            LocationTreeNodeDto node = new LocationTreeNodeDto();
            node.setId(loc.getId());
            node.setCode(loc.getCode());
            node.setName(loc.getName());
            node.setType(loc.getType().name());
            node.setTypeName(typeLabel(loc.getType()));
            node.setParentId(loc.getParent() != null ? loc.getParent().getId() : null);
            node.setActive(Boolean.TRUE.equals(loc.getActive()));
            nodeMap.put(loc.getId(), node);
        }

        List<Long> allIds = new ArrayList<>(nodeMap.keySet());
        if (!allIds.isEmpty()) {
            List<Inventory> inventories = inventoryRepository.findByLocationIdsWithProducts(allIds);
            Map<Long, List<Inventory>> byLoc = inventories.stream()
                    .collect(Collectors.groupingBy(i -> i.getLocation().getId()));
            for (Map.Entry<Long, List<Inventory>> e : byLoc.entrySet()) {
                LocationTreeNodeDto node = nodeMap.get(e.getKey());
                if (node == null) continue;
                node.setSkuCount(e.getValue().size());
                node.setTotalQty(e.getValue().stream().mapToInt(Inventory::getQuantity).sum());
                node.setLowStockCount((int) e.getValue().stream().filter(this::isLowStock).count());
            }
        }

        List<LocationTreeNodeDto> roots = new ArrayList<>();
        for (LocationTreeNodeDto node : nodeMap.values()) {
            if (node.getParentId() == null) {
                roots.add(node);
            } else {
                LocationTreeNodeDto parent = nodeMap.get(node.getParentId());
                if (parent != null) parent.getChildren().add(node);
                else roots.add(node);
            }
        }

        roots.forEach(this::aggregateCounts);
        return roots;
    }

    // 지정 로케이션과 하위 로케이션 전체의 재고 상세를 SKU 기준으로 집계하여 반환한다
    public LocationInventoryDetailDto getLocationDetail(Long locationId, Long accountId) {
        Location loc = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("로케이션을 찾을 수 없습니다."));
        if (accountId != null && (loc.getAccount() == null
                || !loc.getAccount().getId().equals(accountId)))
            throw new IllegalStateException("접근 권한이 없습니다.");

        List<Location> all = loadLocations(accountId);
        Map<Long, Location> locMap = all.stream().collect(Collectors.toMap(Location::getId, l -> l));
        Map<Long, List<Long>> childrenMap = buildChildrenMap(locMap);

        List<Long> targetIds = collectDescendantIds(locationId, childrenMap);
        targetIds.add(locationId);

        List<Inventory> inventories = inventoryRepository.findByLocationIdsWithProducts(targetIds);
        List<SkuRowDto> rows = inventories.stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getSku()))
                .map(inv -> toSkuRow(inv, locMap))
                .collect(Collectors.toList());

        return LocationInventoryDetailDto.builder()
                .locationId(loc.getId())
                .code(loc.getCode())
                .name(loc.getName())
                .type(loc.getType() != null ? loc.getType().name() : null)
                .typeName(loc.getType() != null ? typeLabel(loc.getType()) : null)
                .breadcrumb(buildBreadcrumb(loc, locMap))
                .skuCount(rows.size())
                .totalQty(rows.stream().mapToInt(SkuRowDto::getQty).sum())
                .lowStockCount((int) rows.stream().filter(r -> "ql".equals(r.getStatus())).count())
                .items(rows)
                .build();
    }

    // 전체 재고를 SKU 행 목록으로 반환한다
    public List<SkuRowDto> getAllSkuList(Long accountId) {
        List<Inventory> inventories = (accountId != null)
                ? inventoryRepository.findAllWithDetailsByAccount(accountId)
                : inventoryRepository.findAllWithDetails();
        List<Location> all = loadLocations(accountId);
        Map<Long, Location> locMap = all.stream().collect(Collectors.toMap(Location::getId, l -> l));
        return inventories.stream().map(inv -> toSkuRow(inv, locMap)).collect(Collectors.toList());
    }

    // SKU 또는 상품명 키워드로 재고를 검색하여 SKU 행 목록으로 반환한다
    public List<SkuRowDto> searchBySku(String keyword, Long accountId) {
        List<Inventory> inventories = (accountId != null)
                ? inventoryRepository.searchByKeywordAndAccount(keyword, accountId)
                : inventoryRepository.searchByKeyword(keyword);
        List<Location> all = loadLocations(accountId);
        Map<Long, Location> locMap = all.stream().collect(Collectors.toMap(Location::getId, l -> l));
        return inventories.stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getSku()))
                .map(inv -> toSkuRow(inv, locMap))
                .collect(Collectors.toList());
    }

    // 계정 범위에 맞는 로케이션 목록을 타입·코드 순으로 불러온다
    private List<Location> loadLocations(Long accountId) {
        return (accountId != null)
                ? locationRepository.findByAccount_IdOrderByTypeAscCodeAsc(accountId)
                : locationRepository.findAllByOrderByTypeAscCodeAsc();
    }

    // 로케이션 맵에서 부모→자식 ID 관계를 담은 맵을 생성한다
    private Map<Long, List<Long>> buildChildrenMap(Map<Long, Location> locMap) {
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (Location loc : locMap.values()) {
            if (loc.getParent() != null)
                childrenMap.computeIfAbsent(loc.getParent().getId(), k -> new ArrayList<>()).add(loc.getId());
        }
        return childrenMap;
    }

    // BFS로 rootId의 모든 하위 로케이션 ID를 수집한다
    private List<Long> collectDescendantIds(Long rootId, Map<Long, List<Long>> childrenMap) {
        List<Long> result = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>(childrenMap.getOrDefault(rootId, List.of()));
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            result.add(id);
            queue.addAll(childrenMap.getOrDefault(id, List.of()));
        }
        return result;
    }

    // 자식 노드의 재고 집계 값을 부모 노드에 재귀적으로 합산한다
    private void aggregateCounts(LocationTreeNodeDto node) {
        node.getChildren().forEach(this::aggregateCounts);
        int sku = node.getSkuCount(), qty = node.getTotalQty(), low = node.getLowStockCount();
        for (LocationTreeNodeDto c : node.getChildren()) {
            sku += c.getSkuCount();
            qty += c.getTotalQty();
            low += c.getLowStockCount();
        }
        node.setSkuCount(sku);
        node.setTotalQty(qty);
        node.setLowStockCount(low);
    }

    // Inventory 엔티티를 SKU 행 DTO로 변환하고 로케이션 경로를 포함한다
    private SkuRowDto toSkuRow(Inventory inv, Map<Long, Location> locMap) {
        List<String> path = new ArrayList<>(), pathTypes = new ArrayList<>();
        buildLocationPath(inv.getLocation(), locMap, path, pathTypes);
        return SkuRowDto.builder()
                .sku(inv.getProduct().getSku())
                .productName(inv.getProduct().getName())
                .qty(inv.getQuantity())
                .status(stockStatus(inv))
                .locationCode(inv.getLocation().getCode())
                .locationPath(path)
                .locationPathTypes(pathTypes)
                .build();
    }

    // 루트부터 현재 로케이션까지의 이름 경로(breadcrumb)를 생성한다
    private List<String> buildBreadcrumb(Location loc, Map<Long, Location> locMap) {
        Deque<String> deque = new ArrayDeque<>();
        Location cur = loc;
        while (cur != null) {
            deque.addFirst(cur.getName() != null ? cur.getName() : cur.getCode());
            cur = cur.getParent() != null ? locMap.get(cur.getParent().getId()) : null;
        }
        return new ArrayList<>(deque);
    }

    // WAREHOUSE를 제외한 로케이션 계층 경로와 타입 목록을 구성한다
    private void buildLocationPath(Location loc, Map<Long, Location> locMap,
                                   List<String> path, List<String> pathTypes) {
        Deque<String> nameDeque = new ArrayDeque<>(), typeDeque = new ArrayDeque<>();
        Location cur = loc;
        while (cur != null) {
            if (cur.getType() != null && cur.getType() != LocationType.WAREHOUSE) {
                nameDeque.addFirst(cur.getName() != null ? cur.getName() : cur.getCode());
                typeDeque.addFirst(cur.getType().name());
            }
            cur = cur.getParent() != null ? locMap.get(cur.getParent().getId()) : null;
        }
        path.addAll(nameDeque);
        pathTypes.addAll(typeDeque);
    }

    // 안전 재고 기준으로 재고 상태를 qh(충분)·qm(보통)·ql(부족)로 분류한다
    private String stockStatus(Inventory inv) {
        int qty = inv.getQuantity();
        Integer safety = inv.getProduct().getSafetyStock();
        if (safety == null || safety <= 0)
            return qty >= 100 ? "qh" : qty >= 30 ? "qm" : "ql";
        return qty >= safety * 2 ? "qh" : qty >= safety ? "qm" : "ql";
    }

    // 재고 상태가 부족(ql)인지 여부를 반환한다
    private boolean isLowStock(Inventory inv) { return "ql".equals(stockStatus(inv)); }

    // 로케이션 타입 enum을 한국어 레이블로 변환한다
    private String typeLabel(LocationType type) {
        return switch (type) {
            case WAREHOUSE -> "창고";
            case ZONE      -> "존";
            case RACK      -> "랙";
            case BIN       -> "빈";
        };
    }
}
