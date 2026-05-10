package com.ot.main.out.data.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ot.main.product.data.entity.Product;

import lombok.Data;

@Entity
@Table(name="F_Out")
@SequenceGenerator(name = "out_seq", sequenceName = "out_seq", allocationSize = 1)
@Data
public class Out {
   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "out_seq")
   private Long id;
   
   
   @OneToOne
    @JoinColumn(name = "productCode")
   private Product Product;
   
   
   @Column(name="outStock")
   private Integer outStock;
   
   // 출고 요청 날짜 
   private LocalDateTime outRequest_at;
   
   // 출고 완료 날짜 
   private LocalDateTime outComplete_at;
   
   @Column(name="outStatus")
   // T : 출고 완료 , F : 출고 중 
   private boolean outStatus;
   
   
   @PrePersist
    protected void onCreate() {
      outRequest_at = LocalDateTime.now();
    }

}