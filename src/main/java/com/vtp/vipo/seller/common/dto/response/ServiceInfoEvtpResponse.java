package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceInfoEvtpResponse {
    @JsonProperty("MA_DV_CHINH")
    String maDvChinh;

    @JsonProperty("TEN_DICHVU")
    String tenDichVu;

    @JsonProperty("GIA_CUOC")
    int giaCuoc;

    @JsonProperty("THOI_GIAN")
    String thoiGian;

    @JsonProperty("EXCHANGE_WEIGHT")
    int exchangeWeight;

    @JsonProperty("EXTRA_SERVICE")
    List<ExtraService> extraService;

    @ToString
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExtraService {

        @JsonProperty("SERVICE_CODE")
        String serviceCode;

        @JsonProperty("SERVICE_NAME")
        String serviceName;

        @JsonProperty("DESCRIPTION")
        String description;
    }
}
