package com.ot.main.admin.controller.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import com.ot.main.admin.controller.AdminController;
import com.ot.main.admin.data.dto.AdminResponseDTO;
import com.ot.main.admin.service.AdminService;
import com.ot.main.delivery.data.dto.DeliveryListResponseDTO;
import com.ot.main.delivery.service.DeliveryService;
import com.ot.main.in.data.dto.InSelectAllResponseDto;

@Controller
@RequestMapping(value = "/api/v1/main-fulfillment")
public class AdminControllerImpl implements AdminController {
	
	private final AdminService adminService;
	private final DeliveryService deliveryService;
	
	@Autowired
	public AdminControllerImpl(AdminService adminService, DeliveryService deliveryService) {
		this.adminService = adminService;
		this.deliveryService = deliveryService;
	}
	
	@GetMapping("/showLogin")
	@Override
	public
	ModelAndView showLogin() {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("admin/admin_login");
		return mav;
	}

	@PostMapping("/login")
	@Override
	// login
	public ModelAndView login(@RequestParam String id) {

		ModelAndView mav = new ModelAndView();
		AdminResponseDTO loginResult = adminService.login(id);

		if (loginResult != null && loginResult.getId() != null) {
			// login success
			System.out.println("loginResult : " + loginResult);
			mav.setViewName("redirect:/api/v1/main-fulfillment/showHome");
			mav.addObject("loginResult", loginResult);
			return mav;
		} else {
			// login fail
			System.out.println("loginResult : " + loginResult);
			mav.addObject("loginError", "아이디 또는 비밀번호가 일치하지 않습니다.");
			mav.setViewName("admin/admin_login");
			return mav;
		}

	}

	@GetMapping("/showHome")
	@Override
	public ModelAndView showHome() {
		ModelAndView mav = new ModelAndView();

		List<DeliveryListResponseDTO> delivery = deliveryService.selectDeliverlist();
		mav.addObject("deliveryList",delivery);

		mav.setViewName("admin/admin_home");

		return mav;
	}

	@GetMapping("/showAccount")
	@Override
	public ModelAndView showAccount() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("account/account_List");

		return mv;
	}
	
	

	
}
