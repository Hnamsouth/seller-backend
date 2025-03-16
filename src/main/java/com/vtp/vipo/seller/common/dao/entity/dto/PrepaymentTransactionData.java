package com.vtp.vipo.seller.common.dao.entity.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
public class PrepaymentTransactionData {

    Long packageId;

    String prepaymentTransactionCode;

    Timestamp createPaymentTime;

}
