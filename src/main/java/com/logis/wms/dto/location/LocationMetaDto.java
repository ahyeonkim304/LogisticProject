package com.logis.wms.dto.location;

import com.logis.auth.dto.AccountResponseDto;
import java.util.List;

public record LocationMetaDto(
        List<String> locationTypes,
        List<LocationResponseDto> parentCandidates,
        List<AccountResponseDto> accounts
) {}
