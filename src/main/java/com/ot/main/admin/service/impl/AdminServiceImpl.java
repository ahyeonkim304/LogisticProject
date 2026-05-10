package com.ot.main.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ot.main.admin.data.dao.AdminDAO;
import com.ot.main.admin.data.dto.AdminResponseDTO;
import com.ot.main.admin.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {


	private final AdminDAO adminDAO;
	
	@Autowired
	public AdminServiceImpl(AdminDAO adminDAO) {
		System.out.println("==========AdminServiceImpl============"); // delete!!!!!
		this.adminDAO = adminDAO;
	}
	
	@Override
	//Login
	public AdminResponseDTO login(String id) {
		return adminDAO.login(id);
	}

}
