package com.ot.main.out.data.dao;

import java.util.List;

import com.ot.main.out.data.entity.Out;

public interface OutDAO {

	Out insertOut(Out out, String productCode);

	List<Out> findAllout();

	Out updateOut(Out out);

	String deleteOut(Long id);

}
