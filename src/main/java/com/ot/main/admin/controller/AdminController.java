package com.ot.main.admin.controller;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public interface AdminController {

	//showLogin, 로그인 페이지 이동
	ModelAndView showLogin();
	
	//Login, 로그인 백로직
	ModelAndView login(@RequestParam String id);
	
	//Home, 대시보드 페이지 이동
	ModelAndView showHome();
	
	//showAccount, 계정관리 페이지 이동
	ModelAndView showAccount();
	
	//계정 리스트 조회
	//ModelAndView selectAll();
	
	//계정 등록
	
	//계정 수정
	
	//계정 상세조회
	
	//계정 삭제
	
	//로그아웃
	
	//계정 조회
}
