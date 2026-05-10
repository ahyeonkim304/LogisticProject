package com.ot.main.product.data.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name="F_Product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

   @Id
   @Column(name="productcode")
   private String productCode;
   
   @Column (nullable= false , name="safetystock")
   private Integer safetyStock;
   
   private String name;
   
   @Column (nullable= false , name="productstock")
   private Integer productStock;
   
   @Column(name="leadtime")
   private Integer leadTime;
   
   @Column(nullable = false)
   private String image;
   
   @Column(nullable = false)
   private LocalDateTime create_at;
   
   private LocalDateTime updated_at;

   @PrePersist

    protected void onCreate() {
        create_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

}
