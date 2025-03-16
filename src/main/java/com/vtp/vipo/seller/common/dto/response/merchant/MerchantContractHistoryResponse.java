package com.vtp.vipo.seller.common.dto.response.merchant;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MerchantContractHistoryResponse {

    @Builder.Default
    List<MerchantContractHistoryInfo> histories = new ArrayList<>();

    @Builder.Default
    Long totalNum = 0L;

}
