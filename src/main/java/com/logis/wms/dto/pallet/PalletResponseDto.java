package com.logis.wms.dto.pallet;

import com.logis.wms.dto.order.OrderResponseDto;
import com.logis.wms.entity.Pallet;
import com.logis.wms.enums.PalletStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class PalletResponseDto {

    private final Long id;
    private final String palletCode;
    private final String name;
    private final PalletStatus status;
    private final LocalDateTime createdAt;
    private List<OrderResponseDto> orders;
    private List<PickingItemDto> pickingList;

    private PalletResponseDto(Pallet p) {
        this.id = p.getId();
        this.palletCode = p.getPalletCode();
        this.name = p.getName();
        this.status = p.getStatus();
        this.createdAt = p.getCreatedAt();
    }

    public String getDisplayLabel() {
        return (name != null && !name.isBlank()) ? name + " (" + palletCode + ")" : palletCode;
    }

    public static PalletResponseDto from(Pallet p) {
        return new PalletResponseDto(p);
    }

    public static PalletResponseDto from(Pallet p, List<OrderResponseDto> orders, List<PickingItemDto> pickingList) {
        PalletResponseDto dto = new PalletResponseDto(p);
        dto.orders = orders;
        dto.pickingList = pickingList;
        return dto;
    }

    public String getStatusLabel() {
        if (status == null) return "-";
        switch (status) {
            case CREATED:  return "생성됨";
            case PACKED:   return "패킹 완료";
            case SHIPPED:  return "출하 완료";
            default:       return status.name();
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case CREATED:  return "badge-created";
            case PACKED:   return "badge-inbound-pending";
            case SHIPPED:  return "badge-outbound-done";
            default:       return "badge-secondary";
        }
    }
}
