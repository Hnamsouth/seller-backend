package com.vtp.vipo.seller.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: hieuhm12
 * Date: 9/30/2024
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String phone;
    private String password;
    private String refreshToken;
    private Integer countryId;
    private String sellerOpenId;
    private String email;
    private String name;
}
