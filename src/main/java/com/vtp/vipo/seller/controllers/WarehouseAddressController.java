package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.dto.WarehouseAddressDTO;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.CreateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.request.address.warehouse.UpdateWarehouseAddressRequest;
import com.vtp.vipo.seller.common.dto.response.VTPVipoResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.base.ResponseData;
import com.vtp.vipo.seller.common.utils.PagingUtils;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.WarehouseAddressService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing warehouse addresses.
 * <p>
 * This controller exposes REST APIs for operations such as creating a new warehouse address,
 * searching warehouse addresses (commented out), and updating or deleting warehouse addresses (commented out).
 * </p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/warehouse/address")
public class WarehouseAddressController {

    private final WarehouseAddressService service;

    /**
     * Endpoint to search for warehouse addresses based on optional filters such as ward ID,
     * phone number, and name. It supports pagination and sorting.
     *
     * @param wardId      The ID of the ward to filter by (optional).
     * @param districtId  The ID of the district to filter by (optional).
     * @param provinceId  The ID of the ward to filter by (optional).
     * @param phoneNumber The phone number to filter by (optional).
     * @param name        The name to filter by (optional).
     * @param pageNum     The page number for pagination (default is 0).
     * @param pageSize    The page size for pagination (default is 20).
     * @param sort        The sorting criteria in the format "field,direction" (default is "isDefault,desc,updatedAt,desc").
     * @return A {@link ResponseEntity} containing a {@link ResponseData} object with paginated results of warehouse addresses.
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseData<PagingRs>> searchWarehouseAddresses(
            @RequestParam(required = false, name = "ward_id") Long wardId,
            @RequestParam(required = false, name = "district_id") Long districtId,
            @RequestParam(required = false, name = "province_id") Long provinceId,
            @RequestParam(required = false, name = "phone_number") String phoneNumber,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0", name = "page_num") Integer pageNum,
            @RequestParam(defaultValue = "20", name = "page_size") Integer pageSize,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable
                = PagingUtils.createPageable(pageNum, pageSize, sort, Constants.DEFAULT_SORT_FOR_WAREHOUSE_ADDRESS_LIST);
        return ResponseUtils.success(service.getWarehouseAddresses(wardId, districtId, provinceId, phoneNumber, name, pageable));
    }

    /**
     * Creates a new warehouse address.
     *
     * @param createWarehouseAddressRequest the request body containing the warehouse address details
     * @return ResponseEntity with the created WarehouseAddressDTO
     */
    @PostMapping
    public ResponseEntity<ResponseData<WarehouseAddressDTO>> createWarehouseAddress(
            @RequestBody CreateWarehouseAddressRequest createWarehouseAddressRequest
    ) {
        return ResponseUtils.success(service.createWarehouseAddress(createWarehouseAddressRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<WarehouseAddressDTO>> getWarehouseAddressById(@PathVariable Long id) {
        return ResponseUtils.success(service.getWarehouseAddressById(id));
    }

    /**
     * Updates an existing warehouse address based on the provided ID.
     *
     * @param id the ID of the warehouse address to be updated.
     * @param updateWarehouseAddressRequest the request body containing the new data for the warehouse address.
     * @return a ResponseEntity containing the updated warehouse address in a DTO wrapped in a `ResponseData` object.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<WarehouseAddressDTO>> updateWarehouseAddress(
            @PathVariable Long id,  // Path variable containing the ID of the warehouse address to update
            @RequestBody UpdateWarehouseAddressRequest updateWarehouseAddressRequest // Request body containing the new warehouse address data
    ) {
        return ResponseUtils.success(service.updateWarehouseAddress(id, updateWarehouseAddressRequest));
    }

    /**
     * Deletes a warehouse address by its ID.
     *
     * This method handles the deletion of a warehouse address identified by the provided `id`.
     * If the deletion is successful, it returns a response with a `true` value indicating success.
     *
     * @param id the ID of the warehouse address to be deleted.
     * @return a `ResponseEntity` containing a `ResponseData` with a boolean value (`true` for success)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Boolean>> deleteWarehouseAddress(@PathVariable Long id) {
        return ResponseUtils.success(service.deleteWarehouseAddress(id));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleConstraintViolationException(
            final HttpServletRequest request, final ConstraintViolationException e) {
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        Set<String> missingFields = new HashSet<>();
        Set<String> invalidFieldMsgs = new HashSet<>();

        // Bạn có thể duyệt qua các vi phạm ràng buộc (constraint violations) và lấy thông tin chi tiết
        StringBuilder errorMessage = new StringBuilder();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            if (violation.getMessage().contains("không được để trống.")) {
                missingFields.add(violation.getMessage().replace("không được để trống.", ""));
            } else {
                if (!violation.getMessage().equals("Vui lòng không để trống")) {
                    invalidFieldMsgs.add(violation.getMessage());
                }
            }
        }

        if (!missingFields.isEmpty()) {
            errorMessage.append(String.join(", ", missingFields)).append("không được để trống");
        }

        if (!invalidFieldMsgs.isEmpty()) {
            errorMessage.append(missingFields.isEmpty() ? "" : ", ")
                    .append(String.join(", ", invalidFieldMsgs));
        }

        response.setStatus("02");
        response.setMessage(errorMessage.toString().strip());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @GetMapping("/get-by-merchant")
    public ResponseEntity<?> getMerchantWarehouseAddress(
            @RequestParam(defaultValue = "0", name = "page_num") Integer pageNum,
            @RequestParam(defaultValue = "20", name = "page_size") Integer pageSize,
            @RequestParam(required = false) String sort
    ) {
        Pageable pageable
                = PagingUtils.createPageable(pageNum, pageSize, sort, Constants.DEFAULT_SORT_FOR_WAREHOUSE_ADDRESS_LIST);
        return ResponseUtils.success(service.getMerchantWarehouseAddress(pageable));
    }

}

