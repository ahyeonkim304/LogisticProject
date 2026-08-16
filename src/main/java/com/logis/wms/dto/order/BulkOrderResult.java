package com.logis.wms.dto.order;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BulkOrderResult {

    private final int successCount;
    private final int failureCount;
    private final List<String> errors;
}
