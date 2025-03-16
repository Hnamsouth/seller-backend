package com.vtp.vipo.seller.common.dto;

import com.vtp.vipo.seller.common.enumseller.Copies;
import com.vtp.vipo.seller.common.enumseller.PaperSize;
import lombok.*;
import lombok.experimental.FieldDefaults;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrintLabelData extends ActivityDetailsData {
    Long orderId;

    Boolean confirmPrintedStatus;

    Copies copies;

    PaperSize paperSize;
}
