package com.vtp.vipo.seller.services.impl;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.WarehouseAddressEntity;
import com.vtp.vipo.seller.common.dao.entity.dto.WarehouseAddressDTO;
import com.vtp.vipo.seller.common.dao.entity.projection.WarehouseAddressProjection;
import com.vtp.vipo.seller.common.dao.repository.WarehouseAddressRepository;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.CreateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.UpdateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.response.DistrictResponse;
import com.vtp.vipo.seller.common.dto.response.ProvinceResponse;
import com.vtp.vipo.seller.common.dto.response.WardResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.common.utils.StringProcessingUtils;
import com.vtp.vipo.seller.services.LocationService;
import com.vtp.vipo.seller.services.WarehouseAddressService;
import com.vtp.vipo.seller.services.impl.base.BaseServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class WarehouseAddressServiceImpl extends BaseServiceImpl implements WarehouseAddressService {

    final WarehouseAddressRepository warehouseAddressRepository;

    final LocationService locationService;

    final SellerMapper sellerMapper;

    static final Long MERCHANT_WAREHOUSE_ADDRESSES_LIMIT = 30L;


    /**
     * Creates a new warehouse address.
     *
     * @param createWarehouseAddressRequest The request object containing the warehouse address details.
     * @return WarehouseAddressDTO - Data Transfer Object representing the created warehouse address.
     * @throws VipoFailedToExecuteException if the warehouse address could not be saved.
     */
    @Transactional
    @Override
    public WarehouseAddressDTO createWarehouseAddress(
            @NotNull @Valid CreateWarehouseAddressRequest createWarehouseAddressRequest
    ) {
        // get the merchant Id (user id)
        Long merchantId = getCurrentUser().getId();

        // Retrieve the count of warehouse addresses for the specified merchant
        Long warehouseAddressCount = warehouseAddressRepository.countByMerchantIdAndDeletedFalse(merchantId);

        // Get the configured limit for the number of warehouse addresses for a merchant
        Long warehouseAddressLimit = getMerchantWarehouseAddressesLimit();

        // If the current number of warehouse addresses exceeds or equals the limit, throw an exception
        if (warehouseAddressCount >= warehouseAddressLimit) {
            // Construct a custom error message indicating the limit has been reached
            throw new VipoInvalidDataRequestException(String.format("Đã đạt giới hạn %s địa chỉ. Vui lòng xóa bớt địa chỉ để thêm mới", warehouseAddressLimit));
        }

        /* Initialize the WarehouseAddressEntity from the incoming request */
        WarehouseAddressEntity warehouseAddressEntity
                = sellerMapper.toWarehouseAddressEntity(createWarehouseAddressRequest);

        /* Set the current user's merchant ID */
        warehouseAddressEntity.setMerchantId(merchantId);

        /* Retrieve the full address by using the wardId from the request */
        WardResponse wardResponse = locationService.getWardById(warehouseAddressEntity.getWardId());
        warehouseAddressEntity.setFullAddress(createFullAddressForWarehouseEntity(warehouseAddressEntity, wardResponse));

        /* Handle the case if the address is marked as default */
        if (
                ObjectUtils.isNotEmpty(warehouseAddressEntity.getIsDefault())
                        && Boolean.TRUE.equals(warehouseAddressEntity.getIsDefault())
        ) {
            // Set other addresses for this merchant to non-default if this one is default
            getAllDefaultWarehouseEntityAndPutToNonDefault(warehouseAddressEntity.getMerchantId(), warehouseAddressEntity.getId());
        }

        // Save the entity, throwing an exception if it failed
        if (ObjectUtils.isEmpty(warehouseAddressRepository.save(warehouseAddressEntity)))
            throw new VipoFailedToExecuteException("Failed to create warehouse address!");

        // Return the created address as a DTO
        WarehouseAddressDTO warehouseAddressDTO = sellerMapper.toWarehouseAddressDTO(warehouseAddressEntity);
        warehouseAddressDTO.setWardResponse(wardResponse);
        return warehouseAddressDTO;
    }

    /**
     * Retrieves the warehouse address by its ID for the current merchant (user).
     *
     * This method fetches the warehouse address details for the given ID and the current merchant
     * (identified by the current user's ID). It also fetches the ward information associated with
     * the warehouse address and includes it in the response.
     *
     * @param id the ID of the warehouse address to retrieve
     * @return the `WarehouseAddressDTO` containing the warehouse address details and associated ward information
     * @throws VipoNotFoundException if the warehouse address or the merchant is not found, or if the ward is missing
     */
    @Override
    public WarehouseAddressDTO getWarehouseAddressById(@NotNull Long id) {
        // get the merchant Id (user id)
        Long merchantId = getCurrentUser().getId();
        if (ObjectUtils.isEmpty(merchantId))
            throw new VipoNotFoundException();
        WarehouseAddressEntity warehouseAddressEntity
                = warehouseAddressRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.WAREHOUSE_ADDRESS_NOT_EXIST));
        WarehouseAddressDTO warehouseAddressDTO = sellerMapper.toWarehouseAddressDTO(warehouseAddressEntity);
        WardResponse wardResponse = locationService.getWardById(warehouseAddressEntity.getWardId());
        warehouseAddressDTO.setWardResponse(wardResponse);
        return warehouseAddressDTO;
    }

    /**
     * Updates an existing warehouse address with the provided data.
     *
     * This method fetches the existing warehouse address based on the provided ID and the current user's (merchant's) ID.
     * It then updates the entity with the new data from `UpdateWarehouseAddressRequest` and performs additional operations
     * such as updating the full address, handling default address flags, and saving the updated entity.
     *
     * @param id the ID of the warehouse address to be updated.
     * @param updateWarehouseAddressRequest the data to update the warehouse address with.
     * @return the updated warehouse address as a `WarehouseAddressDTO`.
     * @throws VipoNotFoundException if the warehouse address or merchant is not found.
     * @throws VipoFailedToExecuteException if the update operation fails.
     */
    @Transactional
    @Override
    public WarehouseAddressDTO updateWarehouseAddress(Long id, UpdateWarehouseAddressRequest updateWarehouseAddressRequest) {
        // Get the current user's (merchant's) ID
        Long merchantId = getCurrentUser().getId();

        // If the merchant ID is not found, throw a VipoNotFoundException
        if (ObjectUtils.isEmpty(merchantId)) {
            throw new VipoNotFoundException("Merchant not found!");
        }

        // Fetch the warehouse address entity based on the provided ID and merchant ID
        WarehouseAddressEntity warehouseAddressEntity
                = warehouseAddressRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.WAREHOUSE_ADDRESS_NOT_EXIST));

        //check if the update request change the address from default to non default, then throw exception
        if (
                (
                        ObjectUtils.isNotEmpty(warehouseAddressEntity.getIsDefault())
                                && Boolean.TRUE.equals(warehouseAddressEntity.getIsDefault())
                ) && (
                        ObjectUtils.isNotEmpty(updateWarehouseAddressRequest.getIsDefault())
                                && Boolean.FALSE.equals(updateWarehouseAddressRequest.getIsDefault())
                )
        )
            throw new VipoInvalidDataRequestException("Địa chỉ gửi hàng đầu tiên được cài đặt làm mặc định. Vui lòng thêm địa chỉ khác để thay đổi cài đặt này!");

        // Update the warehouse address entity with the new data from the request
        sellerMapper.updateWarehouseAddressEntity(updateWarehouseAddressRequest, warehouseAddressEntity);

        // Retrieve the full address by using the wardId from the warehouse entity
        WardResponse wardResponse = locationService.getWardById(warehouseAddressEntity.getWardId());
        warehouseAddressEntity.setFullAddress(createFullAddressForWarehouseEntity(warehouseAddressEntity, wardResponse));

        // Handle the case if the address is marked as the default address
        if (
                ObjectUtils.isNotEmpty(warehouseAddressEntity.getIsDefault())
                        && Boolean.TRUE.equals(warehouseAddressEntity.getIsDefault())
        ) {
            // Set other addresses for this merchant to non-default if this one is set as default
            getAllDefaultWarehouseEntityAndPutToNonDefault(warehouseAddressEntity.getMerchantId(), warehouseAddressEntity.getId());
        }

        // Save the updated warehouse address entity to the database
        if (ObjectUtils.isEmpty(warehouseAddressRepository.save(warehouseAddressEntity))) {
            throw new VipoFailedToExecuteException("Failed to update warehouse address!");
        }

        // Convert the updated entity to a DTO and set the ward response
        WarehouseAddressDTO warehouseAddressDTO = sellerMapper.toWarehouseAddressDTO(warehouseAddressEntity);
        warehouseAddressDTO.setWardResponse(wardResponse);

        // Return the updated warehouse address as a DTO
        return warehouseAddressDTO;
    }

    /**
     * Deletes a warehouse address by its ID if the current user is the owner of the address.
     *
     * This method retrieves the current user's (merchant's) ID, checks if the warehouse address exists for that merchant,
     * and verifies that the address is not set as the default. If the address is the default, an exception is thrown.
     * If all conditions are met, the warehouse address is deleted and the method returns `true`.
     *
     * @param id the ID of the warehouse address to be deleted.
     * @return `true` if the warehouse address was successfully deleted.
     * @throws VipoNotFoundException if the warehouse address or merchant is not found.
     * @throws VipoInvalidDataRequestException if the address is marked as the default address.
     */
    @Override
    public boolean deleteWarehouseAddress(Long id) {
        // Get the current user's (merchant's) ID
        Long merchantId = getCurrentUser().getId();

        // If the merchant ID is not found, throw a VipoNotFoundException
        if (ObjectUtils.isEmpty(merchantId)) {
            throw new VipoNotFoundException("Merchant not found!");
        }

        // Fetch the warehouse address entity based on the provided ID and merchant ID
        WarehouseAddressEntity warehouseAddressEntity
                = warehouseAddressRepository.findByIdAndMerchantIdAndDeletedFalse(id, merchantId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.WAREHOUSE_ADDRESS_NOT_EXIST));

        // Check if the warehouse address is marked as the default
        if (
                ObjectUtils.isNotEmpty(warehouseAddressEntity.getIsDefault())
                        && Boolean.TRUE.equals(warehouseAddressEntity.getIsDefault())
        ) {
            // Throw an exception if the address is the default address
            throw new VipoInvalidDataRequestException("Please select another address as default before deleting this one.");
        }

        // Delete the warehouse address entity from the repository
        warehouseAddressEntity.setIsDeleted(true);
        warehouseAddressRepository.save(warehouseAddressEntity);

        // Return true to indicate successful deletion
        return true;
    }
    /**
     * Retrieves a paginated list of warehouse addresses for a specific merchant and ward,
     * filtered by phone number and name.
     *
     * @param wardId The ID of the ward to filter by.
     * @param phoneNumber The phone number to filter by.
     * @param name The name to filter by.
     * @param pageable The Pageable object for pagination information.
     * @return A {@link PagingRs} object containing the paginated list of warehouse addresses.
     * @throws VipoNotFoundException If the merchant (user) is not found.
     */
    @Override
    public PagingRs getWarehouseAddresses(
            Long wardId, Long districtId, Long provinceId, String phoneNumber, String name, Pageable pageable
    ) {
        // Get the current user's (merchant's) ID
        Long merchantId = getCurrentUser().getId();

        // If the merchant ID is not found, throw a VipoNotFoundException
        if (ObjectUtils.isEmpty(merchantId)) {
            throw new VipoNotFoundException("Merchant not found!");
        }

        // Retrieve the paginated warehouse addresses for the given filters
//        String phone = null ;
//        if(!DataUtils.isNullOrEmpty(phoneNumber)){
//            phone = phoneNumber.matches("^84\\d+$") ? "+" + phoneNumber : phoneNumber;
//        }
        if(StringUtils.isNotBlank(phoneNumber)){
            phoneNumber = phoneNumber.strip();
            if (StringUtils.isBlank(phoneNumber))
                phoneNumber = null;
//            phone = StringProcessingUtils.cleanVietnamesePhoneNumber(phoneNumber);
        }


        Page<WarehouseAddressProjection> warehouseAddressProjections
                = warehouseAddressRepository.findByMerchantIdAndWardIdAndPhoneNumberAndName(
                merchantId, wardId, districtId, provinceId, phoneNumber , name, pageable
        );

        // Prepare the response object with pagination information
        var response = new PagingRs();
        response.setCurrentPage(pageable.getPageNumber());
        response.setTotalCount(warehouseAddressProjections.getTotalElements());

        // Map the warehouse address projections to WarehouseAddressDTOs with additional data
        response.setData(
                warehouseAddressProjections.getContent().stream()
                        .map(projection -> {
                                    // Map each projection to WarehouseAddressDTO
                                    WarehouseAddressDTO warehouseAddress = sellerMapper.toWarehouseAddressDTO(projection);

                                    // Set the Ward, District, and Province information in the DTO
                                    warehouseAddress.setWardResponse(
                                            WardResponse.builder()
                                                    .id(projection.getWardId())
                                                    .name(projection.getWardName())
                                                    .district(
                                                            DistrictResponse.builder()
                                                                    .id(projection.getDistrictId())
                                                                    .name(projection.getDistrictName())
                                                                    .province(
                                                                            ProvinceResponse.builder()
                                                                                    .id(projection.getProvinceId())
                                                                                    .name(projection.getProvinceName())
                                                                                    .code(projection.getProvinceCode())
                                                                                    .build()
                                                                    )
                                                                    .build()
                                                    )
                                                    .build()
                                    );

                                    // Return the populated WarehouseAddressDTO
                                    return warehouseAddress;
                                }
                        ).toList()
        );

        // Return the paginated response
        return response;
    }

    @Override
    public PagingRs getMerchantWarehouseAddress(Pageable pageable) {
        // Get the current user's (merchant's) ID
        Long merchantId = getCurrentUser().getId();

        // If the merchant ID is not found, throw a VipoNotFoundException
        if (ObjectUtils.isEmpty(merchantId)) {
            throw new VipoNotFoundException("Merchant not found!");
        }

        Page<WarehouseAddressProjection> warehouseAddressProjections
                = warehouseAddressRepository.findAllByMerchantId(
                merchantId, pageable
        );

        // Prepare the response object with pagination information
        var response = new PagingRs();
        response.setCurrentPage(pageable.getPageNumber());
        response.setTotalCount(warehouseAddressProjections.getTotalElements());

        // Map the warehouse address projections to WarehouseAddressDTOs with additional data
        response.setData(
                warehouseAddressProjections.getContent().stream()
                        .map(projection -> {
                                    // Map each projection to WarehouseAddressDTO
                                    WarehouseAddressDTO warehouseAddress = sellerMapper.toWarehouseAddressDTO(projection);

                                    // Set the Ward, District, and Province information in the DTO
                                    warehouseAddress.setWardResponse(
                                            WardResponse.builder()
                                                    .id(projection.getWardId())
                                                    .name(StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(projection.getWardName()))
                                                    .district(
                                                            DistrictResponse.builder()
                                                                    .id(projection.getDistrictId())
                                                                    .name(StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(projection.getDistrictName()))
                                                                    .province(
                                                                            ProvinceResponse.builder()
                                                                                    .id(projection.getProvinceId())
                                                                                    .name(StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(projection.getProvinceName()))
                                                                                    .code(projection.getProvinceCode())
                                                                                    .build()
                                                                    )
                                                                    .build()
                                                    )
                                                    .build()
                                    );

                                    // Return the populated WarehouseAddressDTO
                                    return warehouseAddress;
                                }
                        ).toList()
        );

        // Return the paginated response
        return response;
    }

    /**
     * Retrieves the maximum number of warehouse addresses allowed for a merchant.
     *
     * @return the configured limit for the number of warehouse addresses
     */
    private Long getMerchantWarehouseAddressesLimit() {
        return MERCHANT_WAREHOUSE_ADDRESSES_LIMIT;
    }

    /**
     * Creates a full address for a warehouse entity by combining multiple address components.
     * The method takes into account detailed address, street, ward, district, and province. It checks
     * for null or empty values and ensures that the necessary components are present.
     *
     * @param warehouseAddressEntity The warehouse address entity containing detailed address and street information.
     * @param wardResponse The ward response containing information about the ward, district, and province.
     * @return A string representing the full address in the format:
     *         [detailed address], [street address], [ward], [district], [province].
     * @throws VipoNotFoundException If any part of the address (ward, district, or province) is not found.
     */
    private String createFullAddressForWarehouseEntity(
            @NotNull WarehouseAddressEntity warehouseAddressEntity,
            @NotNull WardResponse wardResponse
    ) {
        // Initialize a StringBuilder to construct the full address
        var fullAddressStringBuilder = new StringBuilder();

        // Append detailed address if provided
        if (StringUtils.isNotBlank(warehouseAddressEntity.getDetailedAddress()))
            fullAddressStringBuilder.append(warehouseAddressEntity.getDetailedAddress().strip())
                    .append(", ");

        // Append street address if provided
        if (StringUtils.isNotBlank(warehouseAddressEntity.getStreet()))
            fullAddressStringBuilder.append(warehouseAddressEntity.getStreet().strip())
                    .append(", ");

        // Append ward, district, or other parts of the full address
        if (
                ObjectUtils.isEmpty(wardResponse)
                        || ObjectUtils.isEmpty(wardResponse.getDistrict())
                        || ObjectUtils.isEmpty(wardResponse.getDistrict().getProvince())
        )
            // Throw an exception if ward, district, or province is missing
            throw new VipoNotFoundException("Not found ward data!");

        // Finalize the full address by adding ward, district, and province (as needed)
        String wardName = wardResponse.getName();
        if (StringUtils.isNotBlank(wardName))
            fullAddressStringBuilder.append(wardName).append(", ");

        String districtName = wardResponse.getDistrict().getName();
        if (StringUtils.isNotBlank(districtName))
            fullAddressStringBuilder.append(districtName).append(", ");

        String provinceName = wardResponse.getDistrict().getProvince().getName();
        if (StringUtils.isNotBlank(provinceName))
            fullAddressStringBuilder.append(provinceName);

        // Return the full address as a string
        return fullAddressStringBuilder.toString();
    }

    /**
     * Retrieves all warehouse address entities marked as default for the given merchant,
     * and updates their "isDefault" status to false. The updated entities are then saved back
     * to the repository. If no default warehouse addresses are found, the method does nothing.
     *
     * @param merchantId The ID of the merchant whose default warehouse addresses are to be retrieved and updated.
     * @throws VipoFailedToExecuteException If there is an error while saving the updated warehouse addresses to the repository.
     */
    private void getAllDefaultWarehouseEntityAndPutToNonDefault(long merchantId, Long addressDefaultId) {
        // Retrieve all warehouse address entities marked as default for the specified merchant
        List<WarehouseAddressEntity> defaultWarehouseAddressEntity
                = warehouseAddressRepository.findByMerchantIdAndIsDefaultAndDeletedFalse(merchantId, true);

        // If no default warehouse addresses are found, exit the method
        if (ObjectUtils.isEmpty(defaultWarehouseAddressEntity))
            return;

        // Set the "isDefault" flag to false for each default warehouse address entity
        defaultWarehouseAddressEntity.forEach(warehouseEntityItem ->{
            if(addressDefaultId == null || !Objects.equals(warehouseEntityItem.getId(),
                    addressDefaultId)){
                warehouseEntityItem.setIsDefault(false);
            }
        });

        // Save all updated warehouse address entities. If save operation fails, throw an exception
        if (ObjectUtils.isEmpty(warehouseAddressRepository.saveAll(defaultWarehouseAddressEntity)))
            throw new VipoFailedToExecuteException("Failed to create warehouse address!");

        // Flush changes to the repository to ensure persistence
        warehouseAddressRepository.flush();
    }

}