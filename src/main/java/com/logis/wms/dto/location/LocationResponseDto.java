package com.logis.wms.dto.location;

import com.logis.wms.entity.Location;
import com.logis.wms.enums.LocationType;
import lombok.Getter;

@Getter
public class LocationResponseDto {

    private final Long id;
    private final String code;
    private final String name;
    private final LocationType type;
    private final String typeName;
    private final Long parentId;
    private final String parentCode;
    private final boolean active;

    private LocationResponseDto(Location loc) {
        this.id = loc.getId();
        this.code = loc.getCode();
        this.name = loc.getName();
        this.type = loc.getType();
        this.typeName = typeLabel(loc.getType());
        this.parentId = loc.getParent() != null ? loc.getParent().getId() : null;
        this.parentCode = loc.getParent() != null ? loc.getParent().getCode() : null;
        this.active = Boolean.TRUE.equals(loc.getActive());
    }

    public static LocationResponseDto from(Location loc) {
        return new LocationResponseDto(loc);
    }

    private static String typeLabel(LocationType type) {
        if (type == null) return "";
        return switch (type) {
            case WAREHOUSE -> "창고";
            case ZONE      -> "존";
            case RACK      -> "랙";
            case BIN       -> "빈";
        };
    }
}
