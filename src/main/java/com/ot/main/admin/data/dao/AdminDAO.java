package com.ot.main.admin.data.dao;

import com.ot.main.admin.data.dto.AdminResponseDTO;

public interface AdminDAO {
	//LOGIN
	AdminResponseDTO login(String id);
	
}
