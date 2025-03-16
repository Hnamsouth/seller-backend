package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.response.CountryResponse;
import com.vtp.vipo.seller.common.dto.response.DistrictResponse;
import com.vtp.vipo.seller.common.dto.response.ProvinceResponse;
import com.vtp.vipo.seller.common.dto.response.WardResponse;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;

import java.util.List;

public interface LocationService {

    List<ProvinceResponse> getAllProvince();

    List<DistrictResponse> getAllDisByProvinceCode(Long code);

    List<WardResponse> getAllWardByDisCode(Long code);

    List<CountryResponse> getAllCountry();

    /**
     * Retrieves a ward by its ID. This method queries the `wardRepository` to find
     * the ward associated with the given ID and maps it to a `WardResponse` object
     * using the `sellerMapper`. If the ward with the specified ID is not found,
     * it throws a `VipoNotFoundException`.
     *
     * @param id the ID of the ward to be retrieved.
     */
    WardResponse getWardById(long id);
}
