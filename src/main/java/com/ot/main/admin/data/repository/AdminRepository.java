package com.ot.main.admin.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.main.admin.data.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {
	Optional<Admin> findById(String id);
}
