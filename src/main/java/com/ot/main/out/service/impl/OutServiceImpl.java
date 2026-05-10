package com.ot.main.out.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ot.main.common.util.CsvUtil;
import com.ot.main.out.data.dao.OutDAO;
import com.ot.main.out.data.dto.OutCreateRequestDto;
import com.ot.main.out.data.dto.OutCreateResponseDto;
import com.ot.main.out.data.dto.OutSelectAllResponseDto;
import com.ot.main.out.data.dto.OutUpdateRequestDto;
import com.ot.main.out.data.dto.OutUpdateResponseDto;
import com.ot.main.out.data.entity.Out;
import com.ot.main.out.service.OutService;

@Service
public class OutServiceImpl implements OutService {

	private final OutDAO outDAO;

	@Autowired
	public OutServiceImpl(OutDAO outDAO) {
		this.outDAO = outDAO;
	}

	// ============================================================
	//  1) 리스트 조회
	// ============================================================
	@Override
	public List<OutSelectAllResponseDto> selectAllOut() {
		return outDAO.findAllout().stream()
				.map(out -> OutSelectAllResponseDto.builder()
						.id(out.getId())
						.Product(out.getProduct())
						.outStock(out.getOutStock())
						.outRequest_at(out.getOutRequest_at())
						.outComplete_at(out.getOutComplete_at())
						.outStatus(out.isOutStatus())
						.build())
				.collect(Collectors.toList());
	}

	// ============================================================
	//  2) 단건 등록
	// ============================================================
	@Override
	public OutCreateResponseDto saveOut(OutCreateRequestDto dto, String productCode) {
		Out out = new Out();
		out.setOutStatus(dto.isOutStatus());
		out.setOutStock(dto.getOutStock());
		out.setOutRequest_at(LocalDateTime.now());

		Out saved = outDAO.insertOut(out, productCode);

		OutCreateResponseDto res = new OutCreateResponseDto();
		res.setId(saved.getId());
		res.setOutStock(saved.getOutStock());
		res.setProduct(saved.getProduct());
		res.setOutRequest_at(saved.getOutRequest_at());
		res.setOutComplete_at(saved.getOutComplete_at());
		return res;
	}

	// ============================================================
	//  3) 단건 수정
	// ============================================================
	@Override
	public OutUpdateResponseDto updateOut(OutUpdateRequestDto dto) {
		Out out = new Out();
		out.setId(dto.getId());
		out.setOutStock(dto.getOutStock());
		out.setOutStatus(dto.isOutStatus());
		out.setOutComplete_at(LocalDateTime.now());

		Out updated = outDAO.updateOut(out);

		OutUpdateResponseDto res = new OutUpdateResponseDto();
		res.setId(updated.getId());
		res.setOutStock(updated.getOutStock());
		res.setOutStatus(updated.isOutStatus());
		res.setOutRequest_at(updated.getOutRequest_at());
		res.setOutComplete_at(updated.getOutComplete_at());
		return res;
	}

	// ============================================================
	//  4) 단건 삭제
	// ============================================================
	@Override
	public String deleteOut(Long id) {
		return outDAO.deleteOut(id);
	}

	// ============================================================
	//  5) 일괄 등록 (CSV 업로드)
	//     CSV 헤더 : productCode, outStock, outStatus
	// ============================================================
	@Override
	public int bulkInsertOuts(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("업로드된 파일이 비어있습니다.");
		}

		try (InputStream is = file.getInputStream()) {
			List<Map<String, String>> rows = CsvUtil.parseCsvAsMap(is);

			int count = 0;
			for (Map<String, String> row : rows) {
				String productCode = row.get("productCode");
				if (isBlank(productCode)) continue;

				Out out = new Out();
				out.setOutStock(parseInt(row.get("outStock"), 0));
				out.setOutStatus(parseBoolean(row.get("outStatus")));
				out.setOutRequest_at(LocalDateTime.now());

				outDAO.insertOut(out, productCode);
				count++;
			}
			return count;
		}
	}

	// ============================================================
	//  6) 내역 다운로드 — 선택된 ID 목록만
	// ============================================================
	@Override
	public List<OutSelectAllResponseDto> exportOutsByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) return new ArrayList<>();
		return selectAllOut().stream()
				.filter(o -> ids.contains(o.getId()))
				.collect(Collectors.toList());
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
