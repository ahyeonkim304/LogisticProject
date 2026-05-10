package com.ot.main.in.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.in.data.dto.InCreateRequestDto;
import com.ot.main.in.data.dto.InUpdateRequestDto;

/**
 * 입고(In) 컨트롤러 계약.
 *
 * 표준 6기능
 *   - 리스트 조회 / 단건 등록 / 단건 수정 / 단건 삭제
 *   - 일괄 등록 (CSV 업로드)
 *   - 양식 다운로드 / 내역 다운로드 (선택 행)
 */
public interface InController {

	/** 1) 리스트 조회 */
	ModelAndView selectAllIn() throws Exception;

	/** 2) 단건 등록 */
	ModelAndView saveIn(@ModelAttribute InCreateRequestDto inCreateRequestDto, @RequestParam String productCode);

	/** 3) 단건 수정 */
	ModelAndView updateIn(@ModelAttribute InUpdateRequestDto inUpdateRequestDto) throws Exception;

	/** 4) 단건 삭제 */
	ModelAndView deleteIn(@RequestParam Long id) throws Exception;

	/** 5) 일괄 등록 (CSV 업로드) */
	ModelAndView bulkUploadIns(@RequestParam("file") MultipartFile file);

	/** 6-1) 양식 다운로드 */
	void downloadTemplate(HttpServletResponse response) throws Exception;

	/** 6-2) 내역 다운로드 (선택된 입고 ID 목록만) */
	void downloadSelected(@RequestParam("ids") List<Long> ids,
	                      HttpServletResponse response) throws Exception;
}
