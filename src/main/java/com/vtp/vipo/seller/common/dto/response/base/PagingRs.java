package com.vtp.vipo.seller.common.dto.response.base;

import lombok.*;

import java.util.Collection;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagingRs {

    private long totalCount;

    private Collection<?> data;

    private int currentPage;
}

