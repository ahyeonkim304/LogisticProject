package com.logis.wms.repository;

import com.logis.wms.entity.Location;
import com.logis.wms.enums.LocationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value =
        "SELECT z.name AS zoneName, COUNT(b.id) AS totalBins, " +
        "SUM(CASE WHEN i.quantity > 0 THEN 1 ELSE 0 END) AS occupiedBins " +
        "FROM location b " +
        "LEFT JOIN location rack ON b.parent_id = rack.id " +
        "LEFT JOIN location z ON rack.parent_id = z.id " +
        "LEFT JOIN inventory i ON i.location_id = b.id " +
        "WHERE b.type = 'BIN' AND z.type = 'ZONE' " +
        "GROUP BY z.id, z.name ORDER BY z.name",
        nativeQuery = true)
    List<Object[]> findZoneCapacityRaw();

    @Query(value =
        "SELECT z.name AS zoneName, COUNT(b.id) AS totalBins, " +
        "SUM(CASE WHEN i.quantity > 0 THEN 1 ELSE 0 END) AS occupiedBins " +
        "FROM location b " +
        "LEFT JOIN location rack ON b.parent_id = rack.id " +
        "LEFT JOIN location z ON rack.parent_id = z.id " +
        "LEFT JOIN inventory i ON i.location_id = b.id " +
        "WHERE b.type = 'BIN' AND z.type = 'ZONE' AND b.account_id = :accountId " +
        "GROUP BY z.id, z.name ORDER BY z.name",
        nativeQuery = true)
    List<Object[]> findZoneCapacityRawByAccount(@Param("accountId") Long accountId);
}
