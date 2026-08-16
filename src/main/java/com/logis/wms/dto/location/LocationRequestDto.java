package com.logis.wms.dto.location;

import com.logis.wms.enums.LocationType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequestDto {

    @NotBlank(message = "코드는 필수입니다.")
    private String code;

    private String name;

    @NotNull(message = "유형은 필수입니다.")
    private LocationType type;

    private Long parentId;

    private Long accountId;
}
