package com.ot.main.in.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManufacturerToMainDto {
	String out_productcode;
	String out_pname;
	Integer out_stock;
}
