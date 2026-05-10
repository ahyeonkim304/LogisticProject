package com.ot.main.in.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.main.in.data.entity.In;

public interface InRepository extends JpaRepository<In, Long> {

}
