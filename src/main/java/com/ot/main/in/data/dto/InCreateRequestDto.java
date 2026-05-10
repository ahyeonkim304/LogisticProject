package com.ot.main.in.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InCreateRequestDto {
	
	private Integer inStock;
	
	private boolean inStatus;
}
