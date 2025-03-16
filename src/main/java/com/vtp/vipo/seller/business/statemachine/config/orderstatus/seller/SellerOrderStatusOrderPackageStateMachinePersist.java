package com.vtp.vipo.seller.business.statemachine.config.orderstatus.seller;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link StateMachinePersist} for persisting and retrieving the state of a seller's order.
 * <p>
 * This class handles the persistence logic for the state machine associated with {@link OrderPackageEntity}.
 * It updates the order's status based on state machine transitions and retrieves the current state when needed.
 * </p>
 * <p>
 * Note: The actual saving of the order entity is deferred to the service layer to maintain separation of concerns.
 * </p>
 *
 * @see StateMachinePersist
 * @see OrderPackageEntity
 * @see SellerOrderStatus
 * @see SellerOrderPackageEvent
 */
@RequiredArgsConstructor
@Component
public class SellerOrderStatusOrderPackageStateMachinePersist implements StateMachinePersist<SellerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> {

    // Uncomment and inject the repository if you decide to handle persistence within this class
    // private final OrderPackageRepository orderPackageRepository;

    /**
     * Persists the current state of the state machine to the given {@link OrderPackageEntity}.
     * <p>
     * This method updates the {@code sellerOrderStatus} field of the order entity with the new state
     * obtained from the state machine context. The actual saving of the entity to the database is handled
     * elsewhere (e.g., in the service layer) to maintain transaction boundaries and separation of concerns.
     * </p>
     *
     * @param context the current state machine context containing the new state
     * @param order the {@link OrderPackageEntity} to be updated with the new state
     * @throws Exception if an error occurs during the persistence process
     */
    @Override
    public void write(StateMachineContext<SellerOrderStatus, SellerOrderPackageEvent> context, OrderPackageEntity order) throws Exception {
        // Retrieve the new state from the state machine context
        SellerOrderStatus newState = context.getState();

        // Update the order entity's status with the new state
        order.setSellerOrderStatus(newState);

        // Note: The actual persistence (saving) of the order entity is handled in the service layer.
        // Uncomment the following line if you choose to handle persistence here.
        // orderPackageRepository.save(order);
    }

    /**
     * Reads and constructs the state machine context from the given {@link OrderPackageEntity}.
     * <p>
     * This method retrieves the current state of the order from the entity and constructs a new
     * {@link StateMachineContext} with it. Additional context information (such as event history)
     * can be added if necessary.
     * </p>
     *
     * @param order the {@link OrderPackageEntity} from which to read the current state
     * @return a {@link StateMachineContext} representing the current state of the state machine
     * @throws Exception if an error occurs during the retrieval process
     */
    @Override
    public StateMachineContext<SellerOrderStatus, SellerOrderPackageEvent> read(OrderPackageEntity order) throws Exception {
        // Retrieve the current state from the order entity
        SellerOrderStatus currentState = order.getSellerOrderStatus();

        // Construct and return a new state machine context with the current state
        // Additional context information (like event history) can be included as needed
        return new DefaultStateMachineContext<>(currentState, null, null, null);
    }

}