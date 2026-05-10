package com.ot.main.out.controller.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.out.controller.OutController;
import com.ot.main.out.data.dto.OutCreateRequestDto;
import com.ot.main.out.data.dto.OutSelectAllResponseDto;
import com.ot.main.out.data.dto.OutUpdateRequestDto;
import com.ot.main.out.service.OutService;

/**
 * 출고(Out) REST/MVC 컨트롤러.
 *
 *  Base URL : /api/v1/main-fulfillment/out
 *
 *  엔드포인트 요약
 *   GET  /selectAll          : 출고 리스트 조회 화면
 *   POST /create             : 단건 등록 처리
 *   GET/POST /createPage     : 단건 등록 화면 진입
 *   POST /update             : 단건 수정 처리
 *   POST /updatePage         : 단건 수정 화면 진입
 *   POST /delete             : 단건 삭제 처리
 *   POST /bulkUpload         : 일괄 등록 (CSV 업로드)
 *   GET  /downloadTemplate   : 일괄 등록용 CSV 양식 다운로드
 *   GET  /downloadSelected   : 선택된 출고 내역 다운로드 (CSV)
 */
@Controller
@RequestMapping("/api/v1/main-fulfillment/out")
public class OutControllerImpl implements OutController {

	private final OutService outService;

	@Autowired
	public OutControllerImpl(OutService outService) {
		this.outService = outService;
	}

	/* ============================================================
	 *  1) 리스트 조회
	 * ============================================================ */
	@Override
	@GetMapping("/selectAll")
	public ModelAndView selectAllOut() throws Exception {
		List<OutSelectAllResponseDto> outs = outService.selectAllOut();
		ModelAndView mav = new ModelAndView();
		mav.addObject("outs", outs);
		mav.setViewName("out/out_selectall");
		return mav;
	}

	/* ============================================================
	 *  2) 단건 등록
	 * ============================================================ */
	@Override
	@PostMapping("/create")
	public ModelAndView saveOut(@ModelAttribute OutCreateRequestDto dto, @RequestParam String productCode) {
		outService.saveOut(dto, productCode);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/out/selectAll");
	}

	/* (참고) 단건 등록 화면 진입 */
	@PostMapping("/createPage")
	public ModelAndView saveOutPage(@RequestParam String productCode) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("productCode", productCode);
		mav.setViewName("out/out_create");
		return mav;
	}
	@GetMapping("/createPage")
	public ModelAndView saveOutPageGet(@RequestParam(required = false, defaultValue = "") String productCode) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("productCode", productCode);
		mav.setViewName("out/out_create");
		return mav;
	}

	/* ============================================================
	 *  3) 단건 수정
	 * ============================================================ */
	@Override
	@PostMapping("/update")
	public ModelAndView updateOut(@ModelAttribute OutUpdateRequestDto dto) throws Exception {
		outService.updateOut(dto);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/out/selectAll");
	}

	/* (참고) 단건 수정 화면 진입 */
	@PostMapping("/updatePage")
	public ModelAndView updateOutPage(@ModelAttribute OutUpdateRequestDto dto) {
		ModelAndView mav = new ModelAndView("out/out_update");
		mav.addObject("out", dto);
		return mav;
	}

	/* ============================================================
	 *  4) 단건 삭제
	 * ============================================================ */
	@Override
	@PostMapping("/delete")
	public ModelAndView deleteOut(@RequestParam Long id) throws Exception {
		outService.deleteOut(id);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/out/selectAll");
	}

	/* ============================================================
	 *  5) 일괄 등록 (CSV 업로드)
	 *     CSV 헤더 : productCode, outStock, outStatus
	 * ============================================================ */
	@Override
	@PostMapping("/bulkUpload")
	public ModelAndView bulkUploadOuts(@RequestParam("file") MultipartFile file) {
		try {
			int n = outService.bulkInsertOuts(file);
			System.out.println("출고 일괄 등록 완료 : " + n + " 건");
		} catch (Exception e) {
			e.printStackTrace();
			ModelAndView mav = new ModelAndView("out/out_error");
			mav.addObject("errorMessage", "일괄 등록 중 오류: " + e.getMessage());
			return mav;
		}
		return new ModelAndView("redirect:/api/v1/main-fulfillment/out/selectAll");
	}

	/* ============================================================
	 *  6-1) 양식 다운로드
	 *     CSV 헤더 : productCode, outStock, outStatus
	 * ============================================================ */
	@Override
	@GetMapping("/downloadTemplate")
	public void downloadTemplate(HttpServletResponse response) throws Exception {
		String[] headers = { "productCode", "outStock", "outStatus" };
		List<String[]> sample = new ArrayList<>();
		sample.add(new String[] { "P0001", "50", "false" });

		CsvUtil.writeCsvResponse(response, "out_template.csv", headers, sample);
	}

	/* ============================================================
	 *  6-2) 내역 다운로드 — 선택된 출고 ID 목록만
	 * ============================================================ */
	@Override
	@GetMapping("/downloadSelected")
	public void downloadSelected(@RequestParam("ids") List<Long> ids,
	                              HttpServletResponse response) throws Exception {

		List<OutSelectAllResponseDto> list = outService.exportOutsByIds(ids);

		String[] headers = { "id", "productCode", "productName", "outStock", "outStatus", "outRequest_at", "outComplete_at" };
		List<String[]> rows = new ArrayList<>();
		for (OutSelectAllResponseDto out : list) {
			String pc = out.getProduct() != null ? out.getProduct().getProductCode() : "";
			String pn = out.getProduct() != null ? out.getProduct().getName() : "";
			rows.add(new String[] {
					String.valueOf(out.getId()),
					nullSafe(pc),
					nullSafe(pn),
					String.valueOf(out.getOutStock()),
					String.valueOf(out.isOutStatus()),
					out.getOutRequest_at()  == null ? "" : out.getOutRequest_at().toString(),
					out.getOutComplete_at() == null ? "" : out.getOutComplete_at().toString()
			});
		}

		CsvUtil.writeCsvResponse(response, "out_export_" + currentDateString() + ".csv", headers, rows);
	}

	// ---------- helper ----------
	private static String nullSafe(String s) { return s == null ? "" : s; }
	private static String currentDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
}
