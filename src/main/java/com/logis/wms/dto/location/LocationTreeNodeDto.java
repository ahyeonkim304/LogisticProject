package com.logis.wms.dto.location;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationTreeNodeDto {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String typeName;
    private Long parentId;
    private boolean active;
    private int skuCount;
    private int totalQty;
    private int lowStockCount;
    private List<LocationTreeNodeDto> children = new ArrayList<>();
}
