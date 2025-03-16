package com.vtp.vipo.seller.business.statemachine.config.orderstatus.buyer;

import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import com.vtp.vipo.seller.common.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

/**
 * This class implements the persistence mechanism for the buyer order status state machine.
 * It saves and restores the state of the order packages during state transitions.
 *
 * It manages the state machine context using the {@link OrderPackageEntity} as the persistence object.
 */
@RequiredArgsConstructor
@Component
public class BuyerOrderStatusOrderPackageStateMachinePersist
        implements StateMachinePersist<BuyerOrderStatus, SellerOrderPackageEvent, OrderPackageEntity> {

    // Uncomment and inject the repository if you decide to handle persistence within this class
    // private final OrderPackageRepository orderPackageRepository;

    /**
     * Writes the current state of the order package into the persistence storage.
     *
     * @param context the state machine context containing the current state and events
     * @param order   the order package entity to be updated
     * @throws Exception if writing to persistence fails
     */
    @Override
    public void write(StateMachineContext<BuyerOrderStatus, SellerOrderPackageEvent> context, OrderPackageEntity order) throws Exception {
        // Retrieve the new state from the state machine context
        BuyerOrderStatus newState = context.getState();

        // Update the order entity's status with the new state
        order.setOrderStatus(newState.getOrderStatusCode());

        // Update the time field based on the new state if necessary
        updateTimeField(order, newState);

        // Note: The actual persistence (saving) of the order entity is expected to be handled
        // in the service layer or repository. Uncomment the following line if needed.
        // orderPackageRepository.save(order);
    }

    /**
     * Reads the current state of the order package from the persistence storage.
     *
     * @param order the order package entity containing the current order status
     * @return a new state machine context based on the current order status
     * @throws Exception if reading from persistence fails or if the order status is missing
     */
    @Override
    public StateMachineContext<BuyerOrderStatus, SellerOrderPackageEvent> read(OrderPackageEntity order) throws Exception {
        // Validate that the order status is not blank
        if (StringUtils.isBlank(order.getOrderStatus())) {
            throw new VipoInvalidDataRequestException("Missing status");
        }

        // Retrieve the current state from the order entity
        BuyerOrderStatus currentState = BuyerOrderStatus.fromOrderStatusCode(order.getOrderStatus());

        // Return a new state machine context with the current state
        return new DefaultStateMachineContext<>(currentState, null, null, null);
    }

    /**
     * Updates the appropriate time field in the order package entity based on the current state.
     *
     * @param orderPackage the order package entity to be updated
     * @param newState     the new buyer order status
     */
    private void updateTimeField(OrderPackageEntity orderPackage, BuyerOrderStatus newState) {
        switch (newState) {
            case SUPPLIER_IS_PREPARING_ORDER:
                // Set the confirm time when the supplier starts preparing the order
                orderPackage.setConfirmTime(DateUtils.getCurrentTimeInSeconds());
                break;
            default:
                // No action needed for other states
        }
    }
}