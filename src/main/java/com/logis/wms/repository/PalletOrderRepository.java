package com.logis.wms.repository;

import com.logis.wms.entity.PalletOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PalletOrderRepository extends JpaRepository<PalletOrder, Long> {

    List<PalletOrder> findByPalletId(Long palletId);
}
