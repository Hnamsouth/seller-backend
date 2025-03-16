package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dao.entity.dto.WarehouseAddressDTO;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.CreateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.UpdateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing warehouse addresses.
 * <p>
 * This interface defines the business operations for creating and managing warehouse addresses.
 * Implementations of this service handle the logic related to warehouse addresses, including
 * validation, transformation, and persistence operations.
 * </p>
 */
public interface WarehouseAddressService {

    /**
     * Creates a new warehouse address based on the provided request data.
     * <p>
     * The method performs the necessary business logic, such as validation and saving the address.
     * A successful address creation results in returning a `WarehouseAddressDTO` with the created address details.
     * </p>
     *
     * @param createWarehouseAddressRequest The request object containing the data for the new warehouse address.
     * @return A `WarehouseAddressDTO` containing the details of the newly created warehouse address.
     * @throws IllegalArgumentException If the provided request data is invalid.
     */
    WarehouseAddressDTO createWarehouseAddress(@NotNull @Valid CreateWarehouseAddressRequest createWarehouseAddressRequest);

    /**
     * Retrieves the warehouse address by its ID for the current merchant (user).
     *
     * This method fetches the warehouse address details for the given ID and the current merchant
     * (identified by the current user's ID). It also fetches the ward information associated with
     * the warehouse address and includes it in the response.
     *
     * @param id the ID of the warehouse address to retrieve
     * @return the `WarehouseAddressDTO` containing the warehouse address details and associated ward information
     */
    WarehouseAddressDTO getWarehouseAddressById(@NotNull Long id);

    WarehouseAddressDTO updateWarehouseAddress(Long id,@NotNull @Valid UpdateWarehouseAddressRequest updateWarehouseAddressRequest);

    boolean deleteWarehouseAddress(Long id);

    /**
     * Retrieves a paginated list of warehouse addresses for a specific merchant and ward,
     * filtered by phone number and name.
     *
     * @param wardId The ID of the ward to filter by.
     * @param phoneNumber The phone number to filter by.
     * @param name The name to filter by.
     * @param pageable The Pageable object for pagination information.
     * @return A {@link PagingRs} object containing the paginated list of warehouse addresses.
     */
    PagingRs getWarehouseAddresses(Long wardId, Long districtId, Long provinceId, String phoneNumber, String name, Pageable pageable);

    PagingRs getMerchantWarehouseAddress(Pageable pageable);
}