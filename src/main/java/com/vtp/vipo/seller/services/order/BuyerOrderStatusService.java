package com.vtp.vipo.seller.services.order;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Service interface for handling the buyer order status updates.
 * This service is responsible for updating the status of a buyer's order based on the event that is triggered.
 */
public interface BuyerOrderStatusService {

    /**
     * Updates the buyer's order status based on the given event.
     * The event is used to determine how the status of the order should change.
     *
     * @param orderPackageEntity the order package entity representing the buyer's order
     * @param event the event that triggers the change in order status
     *
     * @throws IllegalArgumentException if the provided order package or event is invalid
     */
    void updateBuyerOrderStatusByEvent(@NotNull OrderPackageEntity orderPackageEntity, @NotNull SellerOrderPackageEvent event);

    /**
     * Finds the root order status entity based on a given order status code.
     * If the provided status code has a corresponding root code, retrieves the entity using the root code.
     *
     * @param status the status code of the order
     * @return the root OrderStatusEntity, or null if no root exists or the status is invalid
     */
    OrderStatusDTO findRootOrderStatusByOrderStatusCode(String status);

    /**
     * Retrieves an {@link OrderStatusDTO} based on the given status code.
     *
     * @param statusCode the code representing the specific order status to be retrieved. Must not be null or empty.
     * @return an {@link OrderStatusDTO} that corresponds to the provided status code.
     * @throws IllegalArgumentException if the statusCode is blank.
     */
    OrderStatusDTO getOrderStatusDTOByCode(@NotBlank String statusCode);
}
