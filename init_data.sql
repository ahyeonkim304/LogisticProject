-- ===========================================================
--  Main Fulfillment 초기 데이터 (개발/데모용)
--  ot1 / tiger 계정에서 실행
-- ===========================================================

-- 1) 관리자 계정
INSERT INTO F_Admin (id, pw) VALUES ('admin', 'admin1234');

-- 2) 샘플 상품 (필요 시)
INSERT INTO F_Product (productcode, name, productstock, safetystock, leadtime, image, create_at, updated_at)
VALUES ('P0001', '샘플 상품 A', 100, 30, 3, '/data/MON1.jpg', SYSTIMESTAMP, SYSTIMESTAMP);

INSERT INTO F_Product (productcode, name, productstock, safetystock, leadtime, image, create_at, updated_at)
VALUES ('P0002', '샘플 상품 B',  20, 30, 5, '/data/MON1.jpg', SYSTIMESTAMP, SYSTIMESTAMP);

-- 3) 재고 관리 (선택) - 화면에서 [재고 생성] 으로도 등록 가능
INSERT INTO F_Product_Management (id, productcode, name, productstock, safetystock, leadtime)
VALUES (product_management_seq.NEXTVAL, 'P0001', '샘플 상품 A', 100, 30, 3);

INSERT INTO F_Product_Management (id, productcode, name, productstock, safetystock, leadtime)
VALUES (product_management_seq.NEXTVAL, 'P0002', '샘플 상품 B',  20, 30, 5);

COMMIT;

-- ===========================================================
-- 확인 쿼리
-- ===========================================================
-- SELECT * FROM F_Admin;
-- SELECT * FROM F_Product;
-- SELECT * FROM F_Product_Management;
