package com.ot.main.in.data.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InUpdateRequestDto {
	
	private Long id;
	
	// 입고 수량
	private Integer inStock;
	
	// 입고 상태 
	private boolean inStatus;
	
	// 입고 완료 시간
	private LocalDateTime inComplete_at;

}
