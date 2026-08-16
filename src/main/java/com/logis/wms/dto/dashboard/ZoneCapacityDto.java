package com.logis.wms.dto.dashboard;

import lombok.Getter;

@Getter
public class ZoneCapacityDto {
    private final String zoneName;
    private final long totalBins;
    private final long occupiedBins;
    private final int capacityPct;

    public ZoneCapacityDto(String zoneName, long totalBins, long occupiedBins) {
        this.zoneName = zoneName;
        this.totalBins = totalBins;
        this.occupiedBins = occupiedBins;
        this.capacityPct = totalBins == 0 ? 0 : (int) Math.round(occupiedBins * 100.0 / totalBins);
    }
}
