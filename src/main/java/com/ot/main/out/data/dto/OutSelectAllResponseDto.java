package com.ot.main.out.data.dto;

import java.time.LocalDateTime;

import com.ot.main.product.data.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutSelectAllResponseDto {
	
	private Long id;
	
	private Product Product;
	
	private Integer outStock;
	
	private LocalDateTime outRequest_at;
	
	private LocalDateTime outComplete_at;
	
	private boolean outStatus;
}
