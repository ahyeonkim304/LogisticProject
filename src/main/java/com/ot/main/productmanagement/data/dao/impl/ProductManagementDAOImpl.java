package com.ot.main.productmanagement.data.dao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.in.controller.impl.InControllerImpl;
import com.ot.main.in.data.dto.InCreateRequestDto;
import com.ot.main.out.controller.impl.OutControllerImpl;
import com.ot.main.out.data.dto.OutCreateRequestDto;
import com.ot.main.product.data.entity.Product;
import com.ot.main.product.data.repository.ProductRepository;
import com.ot.main.productmanagement.data.dao.ProductManagementDAO;
import com.ot.main.productmanagement.data.entity.ProductManagement;
import com.ot.main.productmanagement.data.repository.ProductManagementRepository;


@Component
public class ProductManagementDAOImpl implements ProductManagementDAO{

   private final ProductManagementRepository productManagementRepository;
   private final ProductRepository productRepository;
   private InControllerImpl inControllerImpl;
   private OutControllerImpl outControllerImpl;
   
   @Autowired
   public ProductManagementDAOImpl(ProductManagementRepository productManagementRepository, ProductRepository productRepository, InControllerImpl inControllerImpl, OutControllerImpl outControllerImpl) {
      this.productManagementRepository = productManagementRepository;
      this.productRepository = productRepository;
      this.inControllerImpl = inControllerImpl;
      this.outControllerImpl = outControllerImpl;
   }
   
    // 재고 생성
   @Override
   public ProductManagement createStock(ProductManagement productManagement) {
      Optional<Product> product= productRepository.findById(productManagement.getProductCode());
      
      productManagement.setLeadTime(product.get().getLeadTime());
      productManagement.setName(product.get().getName());
      productManagement.setProductCode(product.get().getProductCode());
      productManagement.setProductStock(product.get().getProductStock());
      productManagement.setSafetyStock(product.get().getSafetyStock());
   
       ProductManagement createStock = productManagementRepository.save(productManagement);
       System.out.println("createStock : " + createStock);
      return createStock;
   }

    // 입고_ 재고변경
   @Override
   public ProductManagement modifyInStock(String productCode, boolean inStatus, Integer inStock) {
      ProductManagement udpateStock = productManagementRepository.findByProductCode(productCode);
      
      if (udpateStock != null && inStatus == true) {
         udpateStock.setProductCode(productCode);
         
         //입고
         udpateStock.setProductStock(udpateStock.getProductStock() + inStock);
            
         productManagementRepository.save(udpateStock);
          System.out.println("checkStock : " + udpateStock);
      }
      return udpateStock;
   }
   
    // 출고_ 재고변경
   @Override
   public ProductManagement modifyOutStock(String productCode, boolean OutStatus, Integer OutStock) {
      ProductManagement udpateStock = productManagementRepository.findByProductCode(productCode);
      
      if (udpateStock != null && OutStatus == true) {
         udpateStock.setProductCode(productCode);
         
         udpateStock.setProductStock(udpateStock.getProductStock() - OutStock);
         
         productManagementRepository.save(udpateStock);
          System.out.println("checkStock : " + udpateStock);
          
      }
      return udpateStock;
   }

   //상세 보기
   @Override
   public ProductManagement selectOneStock(Long id) {
      ProductManagement stock = productManagementRepository.getById(id);
      return stock;
   }

   //목록 조회
   @Override
   public List<ProductManagement> selectStockList( ) {
      
      List<ProductManagement> stockList = productManagementRepository.findAll();      
      
      return stockList;
   }

   
   //재고 비교
   @Override
   public ProductManagement compareStockAndSafetyStock(String productCode) {
      System.out.println("productCode : " + productCode);
      ProductManagement compareManagement = productManagementRepository.findByProductCode(productCode);
      System.out.println("==============================compareManagement=======================================");
      System.out.println(compareManagement);
      System.out.println("==============================compareManagement=======================================");
      
      
      if (compareManagement != null) {
         Integer compareProductStock = compareManagement.getProductStock();
         Integer compareSafetyStock = compareManagement.getSafetyStock();
         
         if (compareProductStock != null && compareSafetyStock!= null &&  compareProductStock <= compareSafetyStock ) {
            System.out.println("compareProductStock : " + compareProductStock + ":::::::::: compareSafetyStock : " +  compareSafetyStock);
            System.out.println("입고요청");
         
            InCreateRequestDto inCreateRequestDto = new InCreateRequestDto();
            
            
            // 서버 통신으로부터 받은 값을 넣어주기 
            inCreateRequestDto.setInStatus(false);
            inCreateRequestDto.setInStock(10);
            
            
            inControllerImpl.saveIn(inCreateRequestDto, productCode);
            
         
         }else if (compareProductStock != null && compareSafetyStock!= null && compareProductStock > compareSafetyStock ) {
            System.out.println("출고 요청");
            
            OutCreateRequestDto outCreateRequestDto = new OutCreateRequestDto();
            
            // 서버 통신으로부터 받은 값을 넣어주기 
            outCreateRequestDto.setOutStatus(false);
            outCreateRequestDto.setOutStock(10);
            
            
            outControllerImpl.saveOut(outCreateRequestDto, productCode);
         }
   
      }
      return null;
   }

   /** 재고 단건 삭제 */
   @Override
   public String deleteStock(Long id) {
      Optional<ProductManagement> selected = productManagementRepository.findById(id);
      if (selected.isPresent()) {
         productManagementRepository.delete(selected.get());
         return "재고가 삭제되었습니다.";
      }
      return "해당 재고가 존재하지 않습니다.";
   }
}