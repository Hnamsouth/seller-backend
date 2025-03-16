package com.vtp.vipo.seller.services.impl;

import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.CountryEntity;
import com.vtp.vipo.seller.common.dao.entity.DistrictEntity;
import com.vtp.vipo.seller.common.dao.entity.ProvinceEntity;
import com.vtp.vipo.seller.common.dao.entity.WardEntity;
import com.vtp.vipo.seller.common.dao.repository.CountryRepository;
import com.vtp.vipo.seller.common.dao.repository.DistrictRepository;
import com.vtp.vipo.seller.common.dao.repository.ProvinceRepository;
import com.vtp.vipo.seller.common.dao.repository.WardRepository;
import com.vtp.vipo.seller.common.dto.response.CountryResponse;
import com.vtp.vipo.seller.common.dto.response.DistrictResponse;
import com.vtp.vipo.seller.common.dto.response.ProvinceResponse;
import com.vtp.vipo.seller.common.dto.response.WardResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.common.utils.StringProcessingUtils;
import com.vtp.vipo.seller.services.LocationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service("locationService")
@RequiredArgsConstructor
public class LocationServiceImpl extends BaseService<ProvinceEntity, Long, ProvinceRepository> implements LocationService {

    private final ModelMapper mapper;

    private final DistrictRepository disRepository;

    private final WardRepository wardRepository;

    private final CountryRepository countryRepository;

    private final SellerMapper sellerMapper;

    @Override
    public List<ProvinceResponse> getAllProvince() {
        List<ProvinceEntity> provinces = repo.findAll();
        return provinces.stream().map(sellerMapper::toProvinceResponse).toList();
    }

    @Override
    public List<DistrictResponse> getAllDisByProvinceCode(Long code) {
        List<DistrictEntity> districts = disRepository.findAllByProvinceId(code);
        if (CollectionUtils.isEmpty(districts)) {
            throw new VipoBusinessException(BaseExceptionConstant.NOT_FOUND_ENTITY,BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION);
        }
        return districts.stream().map(sellerMapper::toDistrictResponse).toList();
    }

    @Override
    public List<WardResponse> getAllWardByDisCode(Long code) {
        List<WardEntity> wards = wardRepository.findAllByDistrictId(code);
        if (CollectionUtils.isEmpty(wards)) {
           throw new VipoBusinessException(BaseExceptionConstant.NOT_FOUND_ENTITY,BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION);
        }
        return wards.stream().map(sellerMapper::toWardResponse).toList();
    }

    @Override
    public List<CountryResponse> getAllCountry() {
        List<CountryEntity> countries = countryRepository.findAll();
        if(CollectionUtils.isEmpty(countries)){
            throw new VipoBusinessException(BaseExceptionConstant.ENTITY_NAME_EMPTY_OR_NULL_DESCRIPTION);
        }
        return countries.stream().map(country -> mapper.map(country,CountryResponse.class)).collect(Collectors.toList());
    }

    /**
     * Retrieves a ward by its ID. This method queries the `wardRepository` to find
     * the ward associated with the given ID and maps it to a `WardResponse` object
     * using the `sellerMapper`. If the ward with the specified ID is not found,
     * it throws a `VipoNotFoundException`.
     *
     * @param id the ID of the ward to be retrieved.
     * @return the mapped `WardResponse` object containing the ward data.
     * @throws VipoNotFoundException if no ward is found with the specified ID.
     */
    @Override
    public WardResponse getWardById(long id) {
        return wardRepository.findById(id)
                // Map the retrieved ward to a WardResponse using the sellerMapper
                .map(sellerMapper::toWardResponse)
                // Reformat the ward response to ensure the address fields are in the correct format
                .map(this::reformatWardResponse)
                // Throw an exception if the ward with the given ID is not found
                .orElseThrow(() -> new VipoNotFoundException("Not found ward data!"));
    }

    /**
     * Reformats the ward response to ensure that address fields (ward, district, province) are in correct text format.
     *
     * This method checks if the ward, district, or province is missing, and throws an exception if any of these
     * fields are not found. It also reformats the names of the ward, district, and province into the correct format.
     *
     * @param wardResponse the `WardResponse` object to reformat
     * @return the reformatted `WardResponse` with corrected address fields
     * @throws VipoNotFoundException if ward, district, or province is missing
     */
    private WardResponse reformatWardResponse(@NotNull WardResponse wardResponse) {
        // Check if the ward, district, or province data is missing and throw an exception if so
        if (
                ObjectUtils.isEmpty(wardResponse)
                        || ObjectUtils.isEmpty(wardResponse.getDistrict())
                        || ObjectUtils.isEmpty(wardResponse.getDistrict().getProvince())
        )
            // Throw an exception if any part of the address information is missing
            throw new VipoNotFoundException("Not found ward data!");

        // Reformat the ward name to ensure it is in the correct case (uppercase)
        String wardName = returnAddressWithCorrectForm(wardResponse.getName());
        if (StringUtils.isNotBlank(wardName))
            wardResponse.setName(wardName);

        // Reformat the district name to ensure it is in the correct case (uppercase)
        String districtName = returnAddressWithCorrectForm(wardResponse.getDistrict().getName());
        if (StringUtils.isNotBlank(districtName))
            wardResponse.getDistrict().setName(districtName);

        // Reformat the province name to ensure it is in the correct case (uppercase)
        String provinceName = returnAddressWithCorrectForm(wardResponse.getDistrict().getProvince().getName());
        if (StringUtils.isNotBlank(provinceName))
            wardResponse.getDistrict().getProvince().setName(provinceName);

        // Return the wardResponse with the reformatted address fields
        return wardResponse;
    }

    /**
     * Returns the address in the correct format by capitalizing each word and ensuring proper case.
     * The input string is first converted to lowercase, then leading and trailing whitespace is removed.
     * Finally, each word in the string is capitalized.
     *
     * @param addressStr The address string to be processed.
     * @return The address string with each word capitalized, or null if the input is null.
     */
    private String returnAddressWithCorrectForm(String addressStr) {
        // If the input address string is null, return null
        if (addressStr == null)
            return null;

        // Process the address string: convert to lowercase, strip whitespace, and capitalize each word
        return StringProcessingUtils.capitalizeEachWord(addressStr.toLowerCase().strip());
    }

}
