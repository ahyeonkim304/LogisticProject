package com.logis.wms.dto.order;

import com.logis.wms.entity.OrderItem;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderItemDto {

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    private String productSku;
    private String productName;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    public static OrderItemDto from(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.productId = item.getProduct().getId();
        dto.productSku = item.getProduct().getSku();
        dto.productName = item.getProduct().getName();
        dto.quantity = item.getQuantity();
        return dto;
    }
}
