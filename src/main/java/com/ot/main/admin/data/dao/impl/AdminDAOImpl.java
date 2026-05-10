package com.ot.main.admin.data.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ot.main.admin.data.dao.AdminDAO;
import com.ot.main.admin.data.dto.AdminResponseDTO;
import com.ot.main.admin.data.entity.Admin;
import com.ot.main.admin.data.repository.AdminRepository;

@Component
public class AdminDAOImpl implements AdminDAO {
	private final AdminRepository adminRepository;

	@Autowired
	public AdminDAOImpl(AdminRepository adminRepository) {
		System.out.println("==========AdminDAOImpl============"); // delete!!!!!
		this.adminRepository = adminRepository;
	}

	@Override
	// LOGIN
	public AdminResponseDTO login(String id) {
		AdminResponseDTO adminResponseDTO = new AdminResponseDTO();

		//로그인 정보 확인
		Boolean existLogin = adminRepository.existsById(id);

		if (existLogin == true) {
			Admin loginInformation = adminRepository.findById(id).get();
			//로그인 정보 셋팅
			adminResponseDTO.setId(loginInformation.getId());
			adminResponseDTO.setPw(loginInformation.getPw());

		} 
		//로그인 정보 반환
		return adminResponseDTO;
	}

}
