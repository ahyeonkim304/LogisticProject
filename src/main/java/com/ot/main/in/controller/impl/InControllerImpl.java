package com.ot.main.in.controller.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.in.controller.InController;
import com.ot.main.in.data.dto.InCreateRequestDto;
import com.ot.main.in.data.dto.InSelectAllResponseDto;
import com.ot.main.in.data.dto.InUpdateRequestDto;
import com.ot.main.in.data.dto.MainToManufacturerDto;
import com.ot.main.in.data.dto.ManufacturerToMainDto;
import com.ot.main.in.service.InService;

/**
 * 입고(In) REST/MVC 컨트롤러.
 *
 *  Base URL : /api/v1/main-fulfillment/in
 *
 *  엔드포인트 요약
 *   GET  /selectAll          : 입고 리스트 조회 화면
 *   POST /create             : 단건 등록 처리
 *   GET/POST /createPage     : 단건 등록 화면 진입
 *   POST /update             : 단건 수정 처리
 *   POST /updatePage         : 단건 수정 화면 진입
 *   POST /delete             : 단건 삭제 처리
 *   POST /bulkUpload         : 일괄 등록 (CSV 업로드)
 *   GET  /downloadTemplate   : 일괄 등록용 CSV 양식 다운로드
 *   GET  /downloadSelected   : 선택된 입고 내역 다운로드 (CSV)
 *   POST /mainToManufacturer : 메인 → 제조사 발주 통신
 *   POST /manufacturerToMain : 제조사 → 메인 응답 수신
 */
@Controller
@RequestMapping("/api/v1/main-fulfillment/in")
public class InControllerImpl implements InController {

	private final InService inService;

	@Autowired
	public InControllerImpl(InService inService) {
		this.inService = inService;
	}

	/* ============================================================
	 *  1) 리스트 조회
	 * ============================================================ */
	@Override
	@GetMapping("/selectAll")
	public ModelAndView selectAllIn() throws Exception {
		List<InSelectAllResponseDto> ins = inService.selectAllIn();
		ModelAndView mav = new ModelAndView();
		mav.addObject("ins", ins);
		mav.setViewName("in/in_selectall");
		return mav;
	}

	/* ============================================================
	 *  2) 단건 등록
	 * ============================================================ */
	@Override
	@PostMapping("/create")
	public ModelAndView saveIn(@ModelAttribute InCreateRequestDto dto, @RequestParam String productCode) {
		inService.saveIn(dto, productCode);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/in/selectAll");
	}

	/* (참고) 단건 등록 화면 진입 */
	@PostMapping("/createPage")
	public ModelAndView saveInPage(@RequestParam String productCode) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("productCode", productCode);
		mav.setViewName("in/in_create");
		return mav;
	}
	@GetMapping("/createPage")
	public ModelAndView saveInPageGet(@RequestParam(required = false, defaultValue = "") String productCode) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("productCode", productCode);
		mav.setViewName("in/in_create");
		return mav;
	}

	/* ============================================================
	 *  3) 단건 수정
	 * ============================================================ */
	@Override
	@PostMapping("/update")
	public ModelAndView updateIn(@ModelAttribute InUpdateRequestDto dto) throws Exception {
		inService.updateIn(dto);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/in/selectAll");
	}

	/* (참고) 단건 수정 화면 진입 */
	@PostMapping("/updatePage")
	public ModelAndView updateInPage(@ModelAttribute InUpdateRequestDto dto) {
		ModelAndView mav = new ModelAndView("in/in_update");
		mav.addObject("in", dto);
		return mav;
	}

	/* ============================================================
	 *  4) 단건 삭제
	 * ============================================================ */
	@Override
	@PostMapping("/delete")
	public ModelAndView deleteIn(@RequestParam Long id) throws Exception {
		inService.deleteIn(id);
		return new ModelAndView("redirect:/api/v1/main-fulfillment/in/selectAll");
	}

	/* ============================================================
	 *  5) 일괄 등록 (CSV 업로드)
	 *     CSV 헤더 : productCode, inStock, inStatus
	 * ============================================================ */
	@Override
	@PostMapping("/bulkUpload")
	public ModelAndView bulkUploadIns(@RequestParam("file") MultipartFile file) {
		try {
			int n = inService.bulkInsertIns(file);
			System.out.println("입고 일괄 등록 완료 : " + n + " 건");
		} catch (Exception e) {
			e.printStackTrace();
			ModelAndView mav = new ModelAndView("in/in_error");
			mav.addObject("errorMessage", "일괄 등록 중 오류: " + e.getMessage());
			return mav;
		}
		return new ModelAndView("redirect:/api/v1/main-fulfillment/in/selectAll");
	}

	/* ============================================================
	 *  6-1) 양식 다운로드
	 *     CSV 헤더 : productCode, inStock, inStatus
	 * ============================================================ */
	@Override
	@GetMapping("/downloadTemplate")
	public void downloadTemplate(HttpServletResponse response) throws Exception {
		String[] headers = { "productCode", "inStock", "inStatus" };
		List<String[]> sample = new ArrayList<>();
		sample.add(new String[] { "P0001", "100", "false" });

		CsvUtil.writeCsvResponse(response, "in_template.csv", headers, sample);
	}

	/* ============================================================
	 *  6-2) 내역 다운로드 — 선택된 입고 ID 목록만
	 *     GET /downloadSelected?ids=1,2,3
	 * ============================================================ */
	@Override
	@GetMapping("/downloadSelected")
	public void downloadSelected(@RequestParam("ids") List<Long> ids,
	                              HttpServletResponse response) throws Exception {

		List<InSelectAllResponseDto> list = inService.exportInsByIds(ids);

		String[] headers = { "id", "productCode", "productName", "inStock", "inStatus", "inRequest_at", "inComplete_at" };
		List<String[]> rows = new ArrayList<>();
		for (InSelectAllResponseDto in : list) {
			String pc = (in.getProduct() != null) ? in.getProduct().getProductCode() : "";
			String pn = (in.getProduct() != null) ? in.getProduct().getName() : "";
			rows.add(new String[] {
					String.valueOf(in.getId()),
					nullSafe(pc),
					nullSafe(pn),
					String.valueOf(in.getInStock()),
					String.valueOf(in.isInStatus()),
					in.getInRequest_at()  == null ? "" : in.getInRequest_at().toString(),
					in.getInComplete_at() == null ? "" : in.getInComplete_at().toString()
			});
		}

		CsvUtil.writeCsvResponse(response, "in_export_" + currentDateString() + ".csv", headers, rows);
	}

	/* ============================================================
	 *  WebClient 통신 — 메인 → 제조사 발주
	 * ============================================================ */
	@PostMapping("/mainToManufacturer")
	@ResponseBody
	public ResponseEntity<MainToManufacturerDto> mainToManufacturer(
			@RequestParam String out_productcode,
			@RequestParam String out_pname,
			@RequestParam Integer out_stock) {
		return inService.mainToManufacturer(out_productcode, out_pname, out_stock);
	}

	/* ============================================================
	 *  WebClient 통신 — 제조사 → 메인 응답 수신
	 * ============================================================ */
	@PostMapping("/manufacturerToMain")
	@ResponseBody
	public ResponseEntity<ManufacturerToMainDto> ManufacturerToMain(@RequestBody ManufacturerToMainDto dto) {
		System.out.println(dto.getOut_pname() + " / " + dto.getOut_stock());
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}

	// ---------- helper ----------
	private static String nullSafe(String s) { return s == null ? "" : s; }
	private static String currentDateString() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	}
}
