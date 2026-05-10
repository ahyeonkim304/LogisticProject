package com.ot.main.in.data.entity;

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
@Table(name="F_In")
@SequenceGenerator(name = "in_seq", sequenceName = "in_seq", allocationSize = 1)
@Data
public class In {

   @Id
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "in_seq")
   private Long id;
   
   @OneToOne
    @JoinColumn(name = "productCode")
   private Product Product;
   
   // 입고 개수 default
   @Column(name="inStock")
   private Integer inStock;
   
   // 입고 요청 날짜 
   private LocalDateTime inRequest_at;
   
   // 입고 완료 날짜 
   private LocalDateTime inComplete_at;
   
   @Column(name="inStatus")
   // T : 입고완료 , F : 입고 중 
   private boolean inStatus;
   
   
   @PrePersist

    protected void onCreate() {
      inRequest_at = LocalDateTime.now();
    }

   
}