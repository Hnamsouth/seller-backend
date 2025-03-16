package com.vtp.vipo.seller.services.order;

import com.vtp.vipo.seller.common.dto.request.order.ApproveOrderPackagesRequest;
import com.vtp.vipo.seller.common.dto.response.order.ApproveOrderPackagesResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Service interface for managing order packages.
 *
 * <p>This interface defines the contract for operations related to order packages,
 * including approving multiple order packages based on provided requests.</p>
 *
 * <p>Implementations of this interface should handle the business logic
 * associated with order package approvals, ensuring proper validation,
 * state transitions, and persistence as required.</p>
 */
public interface OrderPackageService {

    /**
     * Approves multiple order packages based on the provided request.
     *
     * <p>This method processes a request to approve one or more order packages.
     * It performs necessary validations, updates the status of the order packages,
     * and returns a response indicating the outcome of the operation.</p>
     *
     * <p><strong>Security Considerations:</strong></p>
     * <p>Ensure that only authorized users can invoke this method to prevent unauthorized
     * modifications to order packages.</p>
     */
    ApproveOrderPackagesResponse approveOrderPackages(
            @NotNull @Valid ApproveOrderPackagesRequest approveOrderPackagesRequest
    );

}
