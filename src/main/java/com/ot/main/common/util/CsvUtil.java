package com.ot.main.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

/**
 * CSV 읽기/쓰기 유틸리티 (의존성 없이 순수 Java).
 *
 *  - UTF-8 BOM 을 붙여서 엑셀에서 한글 깨짐 없이 열림
 *  - 따옴표/콤마/줄바꿈 이스케이프 처리
 *  - 헤더 기반 Map 파서 제공 (일괄 등록용)
 *  - HttpServletResponse 에 직접 다운로드 응답 작성
 */
public class CsvUtil {

	private static final char DELIM = ',';
	private static final String NEW_LINE = "\r\n";
	private static final byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

	private CsvUtil() {}

	/* ============================================================
	 *  CSV 다운로드
	 * ============================================================ */

	/**
	 * CSV 파일을 HTTP 응답으로 내려보낸다.
	 *
	 * @param response  HttpServletResponse
	 * @param fileName  다운로드 파일명 (확장자 .csv 포함)
	 * @param headers   첫 행 (컬럼명 배열)
	 * @param rows      데이터 행. 각 행은 헤더와 같은 길이의 배열
	 */
	public static void writeCsvResponse(HttpServletResponse response, String fileName,
	                                    String[] headers, List<String[]> rows) throws IOException {

		response.setContentType("text/csv; charset=UTF-8");
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + encodeFileName(fileName) + "\"");

		// UTF-8 BOM (엑셀이 한글 인식하려면 필요)
		response.getOutputStream().write(UTF8_BOM);

		StringBuilder sb = new StringBuilder();
		sb.append(toLine(headers));

		if (rows != null) {
			for (String[] row : rows) {
				sb.append(toLine(row));
			}
		}

		response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
		response.getOutputStream().flush();
	}

	/* ============================================================
	 *  CSV 파싱 (일괄 등록 업로드용)
	 * ============================================================ */

	/**
	 * 업로드된 CSV InputStream 을 헤더 기반 Map 리스트로 변환한다.
	 *
	 *  ex) 첫 행이 [productCode, name, productStock] 이면
	 *      각 데이터 행은 { "productCode" : "P0001", "name" : "샘플", "productStock" : "100" }
	 *
	 * @return 헤더-값 매핑 리스트
	 */
	public static List<Map<String, String>> parseCsvAsMap(InputStream in) throws IOException {
		List<Map<String, String>> result = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String headerLine = br.readLine();
			if (headerLine == null) return result;

			// BOM 제거
			if (headerLine.startsWith("﻿")) {
				headerLine = headerLine.substring(1);
			}
			String[] headers = parseLine(headerLine);

			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) continue;

				String[] cols = parseLine(line);
				Map<String, String> row = new LinkedHashMap<>();
				for (int i = 0; i < headers.length; i++) {
					row.put(headers[i].trim(), i < cols.length ? cols[i] : "");
				}
				result.add(row);
			}
		}
		return result;
	}

	/* ============================================================
	 *  내부 helper
	 * ============================================================ */

	/** 한 행을 CSV 한 줄로 변환 (필요 시 따옴표 escape) */
	private static String toLine(String[] cells) {
		if (cells == null) return NEW_LINE;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cells.length; i++) {
			if (i > 0) sb.append(DELIM);
			sb.append(escape(cells[i]));
		}
		sb.append(NEW_LINE);
		return sb.toString();
	}

	/** 콤마/따옴표/줄바꿈이 들어있는 셀은 따옴표로 감싸고 내부 따옴표는 두 번 이스케이프 */
	private static String escape(String value) {
		if (value == null) return "";
		boolean needQuote = value.indexOf(DELIM) >= 0
		                 || value.indexOf('"') >= 0
		                 || value.indexOf('\n') >= 0
		                 || value.indexOf('\r') >= 0;
		if (!needQuote) return value;
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}

	/** CSV 한 줄을 셀 배열로 파싱 (따옴표 안의 콤마는 무시) */
	private static String[] parseLine(String line) {
		List<String> cells = new ArrayList<>();
		StringBuilder cell = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (inQuotes) {
				if (c == '"') {
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						cell.append('"'); // escaped quote
						i++;
					} else {
						inQuotes = false;
					}
				} else {
					cell.append(c);
				}
			} else {
				if (c == DELIM) {
					cells.add(cell.toString());
					cell.setLength(0);
				} else if (c == '"') {
					inQuotes = true;
				} else {
					cell.append(c);
				}
			}
		}
		cells.add(cell.toString());
		return cells.toArray(new String[0]);
	}

	/** 한글 파일명 깨짐 방지 (RFC 5987 / URLEncoder 형식) */
	private static String encodeFileName(String fileName) {
		try {
			return new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
		} catch (Exception e) {
			return fileName;
		}
	}
}
