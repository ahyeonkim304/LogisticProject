package com.logis.common.config;

import com.logis.auth.entity.Account;
import com.logis.auth.enums.AccountRole;
import com.logis.auth.enums.AccountStatus;
import com.logis.auth.repository.AccountRepository;
import com.logis.auth.service.AccountService;
import com.logis.wms.entity.Inventory;
import com.logis.wms.entity.Location;
import com.logis.wms.entity.Order;
import com.logis.wms.entity.OrderItem;
import com.logis.wms.entity.Product;
import com.logis.wms.enums.LocationType;
import com.logis.wms.enums.OrderStatus;
import com.logis.wms.repository.InventoryRepository;
import com.logis.wms.repository.LocationRepository;
import com.logis.wms.repository.OrderRepository;
import com.logis.wms.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 관리자 계정
        accountService.createInitialAdmin("admin", "admin1234");

        // 데이터가 이미 있으면 스킵
        if (accountRepository.count() > 1) {
            log.info("더미 데이터 이미 존재 - 초기화 생략");
            return;
        }

        // 클라이언트 계정 2개 생성
        Account client1 = createAccount("client1", "client1234", "삼성물산", AccountRole.CLIENT);
        Account client2 = createAccount("client2", "client1234", "LG전자", AccountRole.CLIENT);

        // 창고 구조: 창고 > 존 > 랙 > 빈
        Location wh1 = createLocation(null, "WH-001", "서울 물류창고", LocationType.WAREHOUSE, client1);
        Location zone1 = createLocation(wh1, "ZN-A", "A존", LocationType.ZONE, client1);
        Location rack1 = createLocation(zone1, "RK-A1", "A1 랙", LocationType.RACK, client1);
        Location bin1 = createLocation(rack1, "BN-A1-01", "A1-01 빈", LocationType.BIN, client1);
        Location bin2 = createLocation(rack1, "BN-A1-02", "A1-02 빈", LocationType.BIN, client1);

        Location wh2 = createLocation(null, "WH-001", "부산 물류창고", LocationType.WAREHOUSE, client2);
        Location zone2 = createLocation(wh2, "ZN-A", "A존", LocationType.ZONE, client2);
        Location rack2 = createLocation(zone2, "RK-A1", "A1 랙", LocationType.RACK, client2);
        Location bin3 = createLocation(rack2, "BN-A1-01", "A1-01 빈", LocationType.BIN, client2);

        // 상품 생성
        Product p1 = createProduct(client1, "SKU-001", "노트북 15인치", "전자제품", "EA", 10);
        Product p2 = createProduct(client1, "SKU-002", "무선 마우스", "전자제품", "EA", 20);
        Product p3 = createProduct(client1, "SKU-003", "USB 허브", "전자제품", "EA", 15);
        Product p4 = createProduct(client2, "SKU-001", "냉장고 600L", "가전제품", "EA", 5);
        Product p5 = createProduct(client2, "SKU-002", "세탁기 드럼", "가전제품", "EA", 3);

        // 재고 생성
        createInventory(p1, bin1, 50);
        createInventory(p2, bin1, 120);
        createInventory(p3, bin2, 80);
        createInventory(p4, bin3, 15);
        createInventory(p5, bin3, 8);

        // 주문 더미 데이터
        createOrder(client1, "ORD-20260801-001", "김철수", OrderStatus.ORDER_CREATED, p1, 2, p2, 3);
        createOrder(client1, "ORD-20260801-002", "이영희", OrderStatus.READY_TO_SHIP, p2, 1, p3, 5);
        createOrder(client1, "ORD-20260802-001", "박민준", OrderStatus.SHIPPED, p1, 1, p3, 2);
        createOrder(client1, "ORD-20260802-002", "최수진", OrderStatus.ORDER_HOLD, p2, 4, null, 0);
        createOrder(client1, "ORD-20260803-001", "정다은", OrderStatus.OUTBOUND_PENDING, p3, 3, null, 0);

        createOrder(client2, "ORD-20260801-001", "홍길동", OrderStatus.ORDER_CREATED, p4, 1, p5, 1);
        createOrder(client2, "ORD-20260802-001", "강지훈", OrderStatus.SHIPPED, p4, 2, null, 0);
        createOrder(client2, "ORD-20260803-001", "윤서연", OrderStatus.OUTBOUND_PENDING, p5, 1, null, 0);

        log.info("더미 데이터 초기화 완료");
    }

    private Account createAccount(String username, String password, String companyName, AccountRole role) {
        if (accountRepository.existsByUsername(username)) {
            return accountRepository.findByUsername(username).orElseThrow();
        }
        Account a = new Account();
        a.setUsername(username);
        a.setPassword(password);
        a.setCompanyName(companyName);
        a.setRole(role);
        a.setStatus(AccountStatus.APPROVED);
        return accountRepository.save(a);
    }

    private Location createLocation(Location parent, String code, String name, LocationType type, Account account) {
        Location loc = new Location();
        loc.setAccount(account);
        loc.setCode(code);
        loc.setName(name);
        loc.setType(type);
        loc.setParent(parent);
        loc.setActive(true);
        return locationRepository.save(loc);
    }

    private Product createProduct(Account account, String sku, String name, String category, String unit, int safetyStock) {
        Product p = new Product();
        p.setAccount(account);
        p.setSku(sku);
        p.setName(name);
        p.setCategory(category);
        p.setUnit(unit);
        p.setSafetyStock(safetyStock);
        p.setDeleted(false);
        return productRepository.save(p);
    }

    private void createOrder(Account account, String orderNo, String customerName,
                             OrderStatus status, Product p1, int qty1, Product p2, int qty2) {
        Order order = new Order();
        order.setAccount(account);
        order.setOrderNo(orderNo);
        order.setCustomerName(customerName);
        order.setStatus(status);
        order.setDeleted(false);
        orderRepository.save(order);

        if (p1 != null && qty1 > 0) {
            OrderItem item1 = new OrderItem();
            item1.setOrder(order);
            item1.setProduct(p1);
            item1.setQuantity(qty1);
            order.getItems().add(item1);
        }
        if (p2 != null && qty2 > 0) {
            OrderItem item2 = new OrderItem();
            item2.setOrder(order);
            item2.setProduct(p2);
            item2.setQuantity(qty2);
            order.getItems().add(item2);
        }
        orderRepository.save(order);
    }

    private void createInventory(Product product, Location location, int quantity) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setLocation(location);
        inv.setQuantity(quantity);
        inventoryRepository.save(inv);
    }
}
