package com.ot.main.in.data.dto;

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
public class InCreateResponseDto {
	
	private Long id;
	
	private Product Product;
	
	private Integer inStock;
	
	private LocalDateTime inRequest_at;
	
	private LocalDateTime inComplete_at;
	
	private boolean inStatus;
	
	
}
