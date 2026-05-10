package com.ot.main.out.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ot.main.out.data.dto.OutCreateRequestDto;
import com.ot.main.out.data.dto.OutCreateResponseDto;
import com.ot.main.out.data.dto.OutSelectAllResponseDto;
import com.ot.main.out.data.dto.OutUpdateRequestDto;
import com.ot.main.out.data.dto.OutUpdateResponseDto;

/**
 * 출고(Out) 도메인 서비스 계약.
 *
 * 표준 6기능
 *  1) 리스트 조회       : selectAllOut
 *  2) 단건 등록         : saveOut
 *  3) 단건 수정         : updateOut
 *  4) 단건 삭제         : deleteOut
 *  5) 일괄 등록 (CSV)   : bulkInsertOuts
 *  6) 내역 다운로드     : exportOutsByIds
 */
public interface OutService  {

	/** 1) 리스트 조회 */
	List<OutSelectAllResponseDto> selectAllOut();

	/** 2) 단건 등록 */
	OutCreateResponseDto saveOut(OutCreateRequestDto outCreateRequestDto, String productCode);

	/** 3) 단건 수정 */
	OutUpdateResponseDto updateOut(OutUpdateRequestDto outUpdateRequestDto);

	/** 4) 단건 삭제 */
	String deleteOut(Long id);

	/** 5) 일괄 등록 (CSV 업로드) */
	int bulkInsertOuts(MultipartFile file) throws Exception;

	/** 6) 내역 다운로드 — 선택된 출고 ID 목록만 */
	List<OutSelectAllResponseDto> exportOutsByIds(List<Long> ids);
}
