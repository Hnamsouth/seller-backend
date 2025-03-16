package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetProductResponse {

    private Long id;

    private String key;

    private String image;

    private String[] images;

    private String supplier;

    private int countryId;

    private String[] trailerVideo;

    private String description;

    private String name;

    private Long price;

    private int status;

    private int buyCount;

    private String createTime;

    private String updateTime;

    private Long hold;

    private int activated;

    private List<ChildProductResponse> children;

    public GetProductResponse(Long id, String image, String name, Long price, int status, int buyCount, Long createTime, Long updateTime, Long hold, int activated) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.status = status;
        this.buyCount = buyCount;
        this.hold = hold;
        this.activated = activated;
        this.createTime = DateTimeFormatter.ofPattern(DateUtils.HHmmSSddMMyyyy).format(LocalDateTime.ofInstant(Instant.ofEpochSecond(createTime), ZoneId.systemDefault()));
        this.updateTime = DateTimeFormatter.ofPattern(DateUtils.HHmmSSddMMyyyy).format(LocalDateTime.ofInstant(Instant.ofEpochSecond(updateTime), ZoneId.systemDefault()));
    }
}
