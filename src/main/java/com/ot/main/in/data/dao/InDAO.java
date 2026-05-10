package com.ot.main.in.data.dao;

import java.util.List;

import com.ot.main.in.data.entity.In;

public interface InDAO  {
	In insertIn(In in, String productCode);
	List<In> findAllIn() throws Exception;
	In updateIn(In in) throws Exception ;
	String deleteIn(Long id) throws Exception;
}
