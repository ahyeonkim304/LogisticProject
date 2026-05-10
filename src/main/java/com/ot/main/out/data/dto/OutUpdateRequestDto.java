package com.ot.main.out.data.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutUpdateRequestDto {
	
	private Long id;
	
	private Integer outStock;

	private boolean outStatus;
	
}
