package com.vtp.vipo.seller.common.dto.response.cbb;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Author: hieuhm12
 * Date: 9/17/2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboboxRes {
    Object key;
    Object value;
    List<ComboboxRes> subs;
}
