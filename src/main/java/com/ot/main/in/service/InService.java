package com.ot.main.in.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.ot.main.in.data.dto.InCreateRequestDto;
import com.ot.main.in.data.dto.InCreateResponseDto;
import com.ot.main.in.data.dto.InSelectAllResponseDto;
import com.ot.main.in.data.dto.InUpdateRequestDto;
import com.ot.main.in.data.dto.InUpdateResponseDto;
import com.ot.main.in.data.dto.MainToManufacturerDto;

/**
 * 입고(In) 도메인 서비스 계약.
 *
 * 표준 6기능
 *  1) 리스트 조회       : selectAllIn
 *  2) 단건 등록         : saveIn
 *  3) 단건 수정         : updateIn
 *  4) 단건 삭제         : deleteIn
 *  5) 일괄 등록 (CSV)   : bulkInsertIns
 *  6) 내역 다운로드     : exportInsByIds (양식은 컨트롤러에서 정적 헤더 처리)
 *
 * 추가
 *  - 제조사 통신 (WebClient) : mainToManufacturer
 */
public interface InService {

	/** 1) 리스트 조회 */
	List<InSelectAllResponseDto> selectAllIn() throws Exception;

	/** 2) 단건 등록 */
	InCreateResponseDto saveIn(InCreateRequestDto inCreateRequestDto, String productCode);

	/** 3) 단건 수정 */
	InUpdateResponseDto updateIn(InUpdateRequestDto inUpdateRequestDto) throws Exception;

	/** 4) 단건 삭제 */
	String deleteIn(Long id) throws Exception;

	/** 5) 일괄 등록 (CSV 업로드) */
	int bulkInsertIns(MultipartFile file) throws Exception;

	/** 6) 내역 다운로드 — 선택된 입고 ID 목록만 */
	List<InSelectAllResponseDto> exportInsByIds(List<Long> ids) throws Exception;

	/** 제조사 통신 (메인 → 제조사) */
	ResponseEntity<MainToManufacturerDto> mainToManufacturer(String out_productcode, String out_pname, Integer out_stock);
}
