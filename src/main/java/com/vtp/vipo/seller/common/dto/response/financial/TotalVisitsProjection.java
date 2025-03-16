package com.vtp.vipo.seller.common.dto.response.financial;

import java.time.LocalDateTime;

public interface TotalVisitsProjection {
    Long getTotalVisits();

    LocalDateTime getUpdatedAt();
}
