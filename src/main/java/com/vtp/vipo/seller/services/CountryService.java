package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dao.entity.CountryEntity;
import com.vtp.vipo.seller.common.dto.response.CountryResponse;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface CountryService {

    List<ComboboxRes> getCbb();

    /**
     * Finds a country entity by its ID.
     *
     * @param countryId the ID of the country, must not be null
     * @return the corresponding CountryEntity if found, or null if no country matches the given ID
     */
    CountryEntity findById(@NotNull Long countryId);

    List<CountryResponse> getAllCountryResponse();
}
