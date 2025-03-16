package com.vtp.vipo.seller.services.order.impl;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.services.order.BuyerOrderStatusService;
import com.vtp.vipo.seller.services.order.SellerOrderStatusService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link SellerOrderStatusService} that manages the state transitions of seller orders
 * using Spring State Machine.
 * <p>
 * This service handles updating the status of {@link OrderPackageEntity} based on incoming {@link SellerOrderPackageEvent}s.
 * It leverages a state machine factory to create and manage state machines for each order and persists the
 * state changes using a state machine persister.
 * </p>
 *
 * <p>
 * The service ensures that state transitions are valid and maintains the integrity of the order's current state.
 * In case of any failures during the state restoration or persistence, a {@link VipoFailedToExecuteException}
 * is thrown to indicate the failure.
 * </p>
 *
 * @see SellerOrderStatusService
 * @see OrderPackageEntity
 * @see SellerOrderPackageEvent
 * @see SellerOrderStatus
 */
@RequiredArgsConstructor
@Service
public class SellerOrderStatusServiceImpl implements SellerOrderStatusService {

    /**
     * Factory for creating {@link StateMachine} instances configured for seller order statuses and events.
     * Each state machine instance is uniquely identified by the order ID to ensure isolated state management per order.
     */
    private final StateMachineFactory<SellerOrderStatus, SellerOrderPackageEvent> sellerOrderStatusStateMachineFactory;

    /**
     * Persister for saving and restoring the state of {@link StateMachine} instances associated with {@link OrderPackageEntity}.
     * It handles the persistence logic to ensure that the state machine's current state is accurately reflected in the order entity.
     */
    private final StateMachinePersister<SellerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> sellerOrderStatusPersister;

    /**
     * Updates the seller order status based on the provided event.
     * <p>
     * This method performs the following steps:
     * <ol>
     *     <li>Retrieves a state machine instance associated with the given order's ID.</li>
     *     <li>Restores the current state of the state machine from the order entity.</li>
     *     <li>Starts the state machine.</li>
     *     <li>Sends the provided event to the state machine to trigger a state transition.</li>
     *     <li>Persists the updated state of the state machine back to the order entity.</li>
     * </ol>
     * </p>
     *
     * <p>
     * If any step fails (e.g., restoring or persisting the state), a {@link VipoFailedToExecuteException}
     * is thrown with the relevant error message.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} whose status needs to be updated
     * @param event the {@link SellerOrderPackageEvent} that triggers the state transition
     * @throws VipoFailedToExecuteException if an error occurs during state restoration or persistence
     */
    @Override
    public void updateSellerOrderStatusByEvent(@NotNull OrderPackageEntity orderPackageEntity, @NotNull SellerOrderPackageEvent event) {
        // Obtain a state machine instance uniquely identified by the order's ID
        StateMachine<SellerOrderStatus, SellerOrderPackageEvent> stateMachine =
                sellerOrderStatusStateMachineFactory.getStateMachine(String.valueOf(orderPackageEntity.getId()));

        try {
            // Restore the state machine's state from the order entity
            sellerOrderStatusPersister.restore(stateMachine, orderPackageEntity);
        } catch (Exception ex) {
            // Wrap and rethrow any exceptions during state restoration
            throw new VipoFailedToExecuteException("Failed to restore state machine: " + ex.getMessage());
        }

        // Start the state machine to begin processing events
        stateMachine.start();

        // Send the event to the state machine and capture whether it was accepted
        boolean accepted = stateMachine.sendEvent(event);

        // If the event was not accepted (i.e., invalid for the current state), throw an exception
        if (!accepted) {
            throw new VipoFailedToExecuteException("Invalid event '" + event + "' for order ID " + orderPackageEntity.getId());
        }

        try {
            // Persist the updated state of the state machine back to the order entity
            sellerOrderStatusPersister.persist(stateMachine, orderPackageEntity);
        } catch (Exception ex) {
            // Wrap and rethrow any exceptions during state persistence
            throw new VipoFailedToExecuteException("Failed to persist state machine: " + ex.getMessage());
        }

        stateMachine.stop();
    }
}
