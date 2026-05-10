# Main Fulfillment Service

3PL 풀필먼트 물류 시스템의 **재고 관리 / 입출고 관리** 기능을 담당하는 Spring Boot 서비스입니다.
프론트엔드는 JSP + JSTL + jQuery 기반으로 구성되어 있으며, 공통 CSS / 네비게이션 파일을 분리해 관리합니다.

---

## 1. 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 2.5.6, Spring MVC, Spring Data JPA |
| Java | 17 |
| DB | Oracle 11/12c (`ojdbc11`) |
| ORM | Hibernate (Oracle12cDialect) |
| View | JSP + JSTL (Jakarta Tags) |
| Front Asset | jQuery 3.x (CDN) + 자체 CSS (`common.css`, `layout.css`) |
| 외부 통신 | Spring WebFlux WebClient |
| 빌드 | Maven (war 패키징) |

---

## 2. 디렉터리 구조

```
main_fulfillment-main/
├─ pom.xml                         (Thymeleaf 제거 → JSP / JSTL 추가)
├─ src/main/java/com/ot/main/
│  ├─ MainFulfillmentApplication.java   (war 배포 대응)
│  ├─ config/
│  │   ├─ WebConfig.java                (정적 리소스 / 루트 리다이렉트)
│  │   └─ SwaggerConfiguration.java     (Springfox 호환 문제로 비활성화)
│  ├─ admin/                            (관리자 로그인 / 홈)
│  ├─ product/                          (상품 CRUD)
│  ├─ productmanagement/                (재고 관리)
│  ├─ in/                               (입고)
│  ├─ out/                              (출고)
│  └─ delivery/                         (배송)
├─ src/main/resources/
│  ├─ application.properties            (JSP ViewResolver 설정 추가)
│  ├─ static/css/
│  │   ├─ common.css                    (공통 스타일 — 버튼, 폼, 테이블)
│  │   ├─ layout.css                    (헤더 / 사이드바 / 로그인 레이아웃)
│  │   ├─ styles.css                    (기존 SB Admin 자산)
│  │   └─ admin_login.css               (기존 자산 그대로 보존)
│  ├─ static/js/                        (jQuery / 기존 JS 자산)
│  └─ templates_backup_thymeleaf/       (이전 Thymeleaf 템플릿 백업)
└─ src/main/webapp/WEB-INF/views/
   ├─ common/
   │   ├─ head.jsp                      (meta, CSS, jQuery 로드)
   │   ├─ header.jsp                    (상단 바)
   │   ├─ sidebar.jsp                   (좌측 네비게이션 — 단일 파일에 메뉴 전체 정의)
   │   └─ footer.jsp                    (하단)
   ├─ admin/        admin_login.jsp, admin_home.jsp
   ├─ account/      account_List.jsp
   ├─ product/      product_selectall / product_create / product_update / product_error
   ├─ in/           in_selectall / in_create / in_update / in_error
   ├─ out/          out_selectall / out_create / out_update / out_error
   ├─ delivery/     delivery_List
   └─ productManagement/   productManagement_List / productManagement_Detail / productManagement_create
```

> 기존 `src/main/resources/templates/*` 의 Thymeleaf 파일은
> `src/main/resources/templates_backup_thymeleaf/` 로 이동하여 보존되어 있습니다.

---

## 3. 백엔드 수정 사항

분석 결과 컴파일은 되지만 실제로 화면이 렌더링되지 않는 구조 문제가 있어 다음을 수정했습니다.

1. **`@RestController` → `@Controller` 변경**
   - 기존 컨트롤러가 `@RestController` (= `@Controller` + `@ResponseBody`) 임에도 `ModelAndView` 를 반환해서 JSP 가 렌더되지 않고 JSON 으로 직렬화되던 문제 수정.
   - JSON 응답이 필요한 메서드에는 `@ResponseBody` 만 개별 부착.
2. **JSP ViewResolver 설정 추가** (`spring.mvc.view.prefix=/WEB-INF/views/`, `suffix=.jsp`)
3. **Thymeleaf starter 제거** + **Tomcat Jasper / JSTL 추가**
4. **Springfox 2.9.2 의존성 제거**
   - Spring Boot 2.5+ 에서 Springfox 2.9.2 는 PathPatternMatchableHandlerMapping 충돌로 부팅 실패.
   - `SwaggerConfiguration` 은 빈 클래스 (deprecation 코멘트) 로 보존 — 필요 시 `springdoc-openapi-ui` 로 교체 권장.
5. **`MainFulfillmentApplication` 가 `SpringBootServletInitializer` 를 상속**
   - war 패키징 + 외부 톰캣 배포에도 그대로 동작.
6. **WebConfig 추가**
   - `/css/**`, `/js/**`, `/assets/**`, `/data/**` 경로를 `classpath:/static/` 로 매핑.
   - 루트 `/` 진입 시 로그인 화면으로 리다이렉트.
7. **로그인 처리 NPE 방어** — `loginResult` 가 null 일 때도 안전하게 처리.
8. **GET `createPage` 추가** — 사이드바에서 등록 화면을 바로 클릭해도 진입 가능 (POST 만 있던 부분 보완).

---

## 4. 프론트엔드 (JSP) 구성

### 4.1 공통 레이아웃

각 페이지는 다음 4 개 공통 파일을 include 합니다.

- `common/head.jsp` — meta, viewport, common.css/layout.css, jQuery
- `common/header.jsp` — 상단 헤더 (브랜드 + 사용자 영역)
- `common/sidebar.jsp` — 좌측 사이드바 (메뉴 트리)
- `common/footer.jsp` — 하단 footer

```jsp
<%@ include file="../common/head.jsp" %>
...
<c:set var="menu" value="product" />
<%@ include file="../common/sidebar.jsp" %>
<%@ include file="../common/header.jsp" %>
... 본문 ...
<%@ include file="../common/footer.jsp" %>
```

`<c:set var="menu" value="..." />` 로 현재 메뉴 키를 지정하면 사이드바에서 active 표시가 됩니다.
사용 가능한 키: `dashboard`, `product`, `stock`, `in`, `out`, `delivery`, `account`.

### 4.2 CSS 파일 분리

| 파일 | 역할 |
|------|------|
| `common.css` | 색상, 폼, 테이블, 버튼, 배지, 알림 등 공통 스타일 |
| `layout.css` | 헤더, 사이드바, 로그인 레이아웃, 대시보드 메트릭 카드 |

기존 SB Admin 의 `styles.css` 와 `admin_login.css` 는 보존되어 있어 필요시 함께 사용 가능합니다.

### 4.3 화면 흐름

```
/                                           → /api/v1/main-fulfillment/showLogin
/api/v1/main-fulfillment/showLogin          (로그인 화면)
/api/v1/main-fulfillment/login              (POST · 로그인 처리)
/api/v1/main-fulfillment/showHome           (홈 / 대시보드)

/api/v1/main-fulfillment/product/selectAll  (상품 목록)
/api/v1/main-fulfillment/product/createPage (상품 등록 화면)
/api/v1/main-fulfillment/product/create     (POST · 등록)
/api/v1/main-fulfillment/product/updatePage (POST · 수정 화면 진입)
/api/v1/main-fulfillment/product/update     (POST · 수정)
/api/v1/main-fulfillment/product/delete     (POST · 삭제)
/api/v1/main-fulfillment/product/search     (GET  · AJAX 검색 - JSON)

/api/v1/main-fulfillment/in/selectAll       (입고 목록)
/api/v1/main-fulfillment/in/createPage      (입고 등록 화면)
/api/v1/main-fulfillment/in/create          (POST · 등록)
/api/v1/main-fulfillment/in/updatePage      (POST · 수정 화면)
/api/v1/main-fulfillment/in/update          (POST · 수정)
/api/v1/main-fulfillment/in/delete          (POST · 삭제)

/api/v1/main-fulfillment/out/selectAll      (출고 목록)
... (in 과 동일한 패턴)

/api/v1/main-fulfillment/lookUpStock        (재고 목록)
/api/v1/main-fulfillment/selectStockDetail  (재고 상세)
/api/v1/main-fulfillment/createStock        (POST · 재고 생성)
/api/v1/main-fulfillment/modifyInStock      (PUT  · AJAX - 입고 처리)
/api/v1/main-fulfillment/modifyOutStock     (PUT  · AJAX - 출고 처리)

/api/v1/main-fulfillment/selectDeliverylist (배송 목록)
/api/v1/main-fulfillment/updateDelivery     (PUT  · AJAX - 배송 상태 변경)
```

---

## 5. 실행 방법

### 5.1 사전 준비

Oracle XE 가 `localhost:1521/xe` 로 떠 있어야 하며, `ot1 / tiger` 계정이 존재해야 합니다.
없다면 아래 SQL 로 계정을 만들 수 있습니다 (sys 계정).

```sql
CREATE USER ot1 IDENTIFIED BY tiger;
GRANT CONNECT, RESOURCE, CREATE SEQUENCE, UNLIMITED TABLESPACE TO ot1;
```

테이블은 `spring.jpa.hibernate.ddl-auto=update` 설정으로 첫 실행 시 자동 생성됩니다 (F_Admin, F_Product, F_In, F_Out, F_Delivery, F_Product_Management).

### 5.2 실행

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

- 기본 포트: **8000**
- 접속: <http://localhost:8000/>
  → 자동으로 `/api/v1/main-fulfillment/showLogin` 으로 이동합니다.
- 로그인 시 `F_Admin` 테이블에 등록된 ID 를 입력합니다.
  최초 실행 후 SQL 로 직접 INSERT 가 필요합니다.

```sql
INSERT INTO F_Admin (id, pw) VALUES ('admin', 'admin1234');
COMMIT;
```

### 5.3 빌드

```bash
./mvnw clean package
java -jar target/main_fulfillment.war
```

---

## 6. 알려진 제약 / TODO

- 로그인은 ID 만 검증하고 비밀번호 비교를 수행하지 않습니다 (원본 코드 동일). 실제 운영 시 BCrypt + Spring Security 적용을 권장합니다.
- `compareStockAndSafetyStock` 가 다른 컨트롤러를 직접 호출하는 구조 (DAO → Controller 의존성). 정리는 추후 리팩터링 권장.
- 외부 쇼핑몰 / 제조사 서버 (`localhost:9000`, `localhost:9002`) 가 떠 있어야 WebClient 호출이 정상 동작합니다.
- 계정 관리 (`/showAccount`) 화면은 백엔드 데이터 조회 API 가 별도 구현되지 않아 빈 목록으로 표시됩니다.
