package com.vtp.vipo.seller.services.order;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dto.response.order.OrderStatusDTO;
import jakarta.validation.constraints.NotNull;

/**
 * Service interface for managing and updating the status of seller orders.
 * <p>
 * The {@link SellerOrderStatusService} defines operations related to the state management
 * of {@link OrderPackageEntity} instances.
 * </p>
 *
 * <p>
 * Implementations of this interface are responsible for handling the logic required to
 * process events that affect an order's status, such as approvals, cancellations, shipments,
 * and deliveries. By abstracting these operations into a service layer, the application
 * maintains a clean separation of concerns, facilitating easier testing and maintenance.
 * </p>
 *
 * <p>
 * This service interacts with the state machine configured for seller orders to ensure that
 * state transitions are valid and that the order's current state is accurately persisted
 * after each event.
 * </p>
 *
 * @see OrderPackageEntity
 * @see SellerOrderPackageEvent
 */
public interface SellerOrderStatusService {

    /**
     * Updates the status of a seller's order based on the provided event.
     * <p>
     * This method processes a {@link SellerOrderPackageEvent} to transition the given
     * {@link OrderPackageEntity} to a new {@link com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus}.
     * The transition is managed by a state machine that ensures the event is valid for the
     * order's current state. If the event leads to a valid transition, the order's status
     * is updated and persisted accordingly.
     * </p>
     *
     * <p>
     * For example, sending an {@code APPROVED} event to an order in the {@code WAITING_FOR_SELLER_CONFIRMATION}
     * state would transition the order to the {@code WAITING_FOR_ORDER_PREPARATION} state.
     * </p>
     *
     * <p>
     * If the event is invalid for the current state (i.e., the state machine does not accept the event),
     * a {@link com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException} is thrown to indicate
     * the failure, preventing the order from entering an inconsistent state.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} whose status is to be updated
     *                           <p>
     *                           Must not be {@code null}. Represents the order whose status is being managed.
     *                           </p>
     * @param event              the {@link SellerOrderPackageEvent} that triggers the state transition
     *                           <p>
     *                           Must not be {@code null}. Represents the event causing the status change,
     *                           such as {@code APPROVED}, {@code CANCEL}, {@code SHIP}, etc.
     *                           </p>
     * @throws com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException if the event is invalid for the current state, or if an error occurs during the
     *                                                                           state restoration or persistence process
     * @throws IllegalArgumentException                                          if either {@code orderPackageEntity} or {@code event} is {@code null}
     */
    void updateSellerOrderStatusByEvent(@NotNull OrderPackageEntity orderPackageEntity, @NotNull SellerOrderPackageEvent event);

}
