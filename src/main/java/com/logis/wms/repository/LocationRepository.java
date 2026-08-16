package com.logis.wms.repository;

import com.logis.wms.entity.Location;
import com.logis.wms.enums.LocationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByActiveTrueOrderByTypeAscCodeAsc();
    List<Location> findByTypeAndActiveTrueOrderByCodeAsc(LocationType type);
    List<Location> findAllByOrderByTypeAscCodeAsc();

    List<Location> findByAccount_IdOrderByTypeAscCodeAsc(Long accountId);
    List<Location> findByAccount_IdAndTypeAndActiveTrueOrderByCodeAsc(Long accountId, LocationType type);

    Optional<Location> findByAccount_IdAndCode(Long accountId, String code);

    boolean existsByCode(String code);
    boolean existsByAccount_IdAndCode(Long accountId, String code);
    boolean existsByAccount_IdAndCodeAndIdNot(Long accountId, String code, Long id);
}
