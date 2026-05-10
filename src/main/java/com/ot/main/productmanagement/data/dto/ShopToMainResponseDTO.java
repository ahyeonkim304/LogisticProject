package com.ot.main.productmanagement.data.dto;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopToMainResponseDTO {
   private String userName;
   
   private String hp1;
   
   private String hp2;
   
   private String hp3;
   
   private String zipcode;
   
   private String address;
   
   private String productCode;

   private String productName;

   private int orderCount;
   
}