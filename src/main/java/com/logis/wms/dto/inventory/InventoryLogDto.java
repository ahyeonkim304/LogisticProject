package com.logis.wms.dto.inventory;

import com.logis.wms.entity.InventoryLog;
import com.logis.wms.enums.MovementType;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class InventoryLogDto {

    private final Long id;
    private final Long productId;
    private final String productSku;
    private final String productName;
    private final MovementType type;
    private final Integer quantity;
    private final String referenceType;
    private final Long referenceId;
    private final LocalDateTime createdAt;
    private final String locationCode;
    private final String accountName;

    private InventoryLogDto(InventoryLog log) {
        this.id = log.getId();
        this.productId = log.getProduct().getId();
        this.productSku = log.getProduct().getSku();
        this.productName = log.getProduct().getName();
        this.type = log.getType();
        this.quantity = log.getQuantity();
        this.referenceType = log.getReferenceType();
        this.referenceId = log.getReferenceId();
        this.createdAt = log.getCreatedAt();
        this.locationCode = log.getLocation() != null ? log.getLocation().getCode() : null;
        var acc = log.getProduct().getAccount();
        this.accountName = acc != null
                ? (acc.getCompanyName() != null && !acc.getCompanyName().isBlank()
                   ? acc.getCompanyName() : acc.getUsername())
                : null;
    }

    public static InventoryLogDto from(InventoryLog log) {
        return new InventoryLogDto(log);
    }

    public String getTypeLabel() {
        if (type == null) return "";
        switch (type) {
            case INBOUND:    return "입고";
            case OUTBOUND:   return "출고";
            case ADJUSTMENT: return "조정";
            default:         return type.name();
        }
    }

    public String getTypeBadgeClass() {
        if (type == null) return "badge-secondary";
        switch (type) {
            case INBOUND:    return "badge-inbound-done";
            case OUTBOUND:   return "badge-outbound-done";
            case ADJUSTMENT: return "badge-created";
            default:         return "badge-secondary";
        }
    }
}
