package com.vtp.vipo.seller.common.dao.entity.projection;

import java.time.LocalDateTime;

public interface WarehouseAddressProjection {

    Long getId();

    String getFullAddress();

    String getName();

    String getPhoneNumber();

    Long getWardId();

    String getWardName();

    Long getDistrictId();

    String getDistrictName();

    Long getProvinceId();

    String getProvinceName();

    String getProvinceCode();

    String getStreet();

    String getDetailedAddress();

    Boolean getIsDefault();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

}
