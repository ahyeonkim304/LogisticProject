package com.ot.main.in.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.in.data.dao.InDAO;
import com.ot.main.in.data.dto.InCreateRequestDto;
import com.ot.main.in.data.dto.InCreateResponseDto;
import com.ot.main.in.data.dto.InSelectAllResponseDto;
import com.ot.main.in.data.dto.InUpdateRequestDto;
import com.ot.main.in.data.dto.InUpdateResponseDto;
import com.ot.main.in.data.dto.MainToManufacturerDto;
import com.ot.main.in.data.entity.In;
import com.ot.main.in.service.InService;

@Service
public class InServiceImpl implements InService {

	private final InDAO inDAO;

	@Autowired
	public InServiceImpl(InDAO inDAO) {
		this.inDAO = inDAO;
	}

	// ============================================================
	//  1) 리스트 조회
	// ============================================================
	@Override
	public List<InSelectAllResponseDto> selectAllIn() throws Exception {
		List<In> inList = inDAO.findAllIn();

		return inList.stream()
				.map(in -> InSelectAllResponseDto.builder()
						.id(in.getId())
						.Product(in.getProduct())
						.inStock(in.getInStock())
						.inRequest_at(in.getInRequest_at())
						.inComplete_at(in.getInComplete_at())
						.inStatus(in.isInStatus())
						.build())
				.collect(Collectors.toList());
	}

	// ============================================================
	//  2) 단건 등록
	// ============================================================
	@Override
	public InCreateResponseDto saveIn(InCreateRequestDto dto, String productCode) {
		In in = new In();
		in.setInStatus(dto.isInStatus());
		in.setInStock(dto.getInStock());
		in.setInRequest_at(LocalDateTime.now());

		In saved = inDAO.insertIn(in, productCode);

		InCreateResponseDto res = new InCreateResponseDto();
		res.setId(saved.getId());
		res.setInStock(saved.getInStock());
		res.setProduct(saved.getProduct());
		res.setInRequest_at(saved.getInRequest_at());
		res.setInComplete_at(saved.getInComplete_at());
		return res;
	}

	// ============================================================
	//  3) 단건 수정
	// ============================================================
	@Override
	public InUpdateResponseDto updateIn(InUpdateRequestDto dto) throws Exception {
		In in = new In();
		in.setId(dto.getId());
		in.setInStock(dto.getInStock());
		in.setInStatus(dto.isInStatus());
		in.setInComplete_at(LocalDateTime.now());

		In updated = inDAO.updateIn(in);

		InUpdateResponseDto res = new InUpdateResponseDto();
		res.setId(updated.getId());
		res.setInStock(updated.getInStock());
		res.setProduct(updated.getProduct());
		res.setInStatus(updated.isInStatus());
		res.setInRequest_at(updated.getInRequest_at());
		res.setInComplete_at(updated.getInComplete_at());
		return res;
	}

	// ============================================================
	//  4) 단건 삭제
	// ============================================================
	@Override
	public String deleteIn(Long id) throws Exception {
		return inDAO.deleteIn(id);
	}

	// ============================================================
	//  5) 일괄 등록 (CSV 업로드)
	//     CSV 헤더 : productCode, inStock, inStatus
	// ============================================================
	@Override
	public int bulkInsertIns(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
		}

		try (InputStream is = file.getInputStream()) {
			List<Map<String, String>> rows = CsvUtil.parseCsvAsMap(is);

			int count = 0;
			for (Map<String, String> row : rows) {
				String productCode = row.get("productCode");
				if (isBlank(productCode)) continue;

				In in = new In();
				in.setInStock(parseInt(row.get("inStock"), 0));
				in.setInStatus(parseBoolean(row.get("inStatus")));
				in.setInRequest_at(LocalDateTime.now());

				inDAO.insertIn(in, productCode);
				count++;
			}
			return count;
		}
	}

	// ============================================================
	//  6) 내역 다운로드 — 선택된 ID 목록만
	// ============================================================
	@Override
	public List<InSelectAllResponseDto> exportInsByIds(List<Long> ids) throws Exception {
		if (ids == null || ids.isEmpty()) return new ArrayList<>();
		return selectAllIn().stream()
				.filter(in -> ids.contains(in.getId()))
				.collect(Collectors.toList());
	}

	// ============================================================
	//  제조사 통신 (메인 → 제조사)
	// ============================================================
	@Override
	public ResponseEntity<MainToManufacturerDto> mainToManufacturer(String out_productcode, String out_pname, Integer out_stock) {
		WebClient webClient = WebClient.builder()
				.baseUrl("http://localhost:9002")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

		MainToManufacturerDto dto = new MainToManufacturerDto();
		dto.setOut_productcode(out_productcode);
		dto.setOut_pname(out_pname);
		dto.setOut_stock(out_stock);

		return webClient.post()
				.uri("/manufacturer/mainToManufacturer")
				.bodyValue(dto)
				.retrieve()
				.toEntity(MainToManufacturerDto.class)
				.block();
	}

	// ---------- helper ----------
	private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
	private static Integer parseInt(String v, int defaultValue) {
		try { return Integer.parseInt(v.trim()); } catch (Exception e) { return defaultValue; }
	}
	private static boolean parseBoolean(String v) {
		if (v == null) return false;
		String s = v.trim().toLowerCase();
		return "true".equals(s) || "y".equals(s) || "1".equals(s) || "완료".equals(s);
	}
}
