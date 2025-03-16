package com.vtp.vipo.seller.common.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchOrderByKeywordRequest extends LazbaoPagingRequest{

    private String searchQuery = "";

    private String searchBy;

    private String orderStatus;

    private Long startDate;

    private Long endDate;

}
