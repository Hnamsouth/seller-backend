package com.vtp.vipo.seller.business.statemachine.config.orderstatus.buyer;

import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Configuration class for the Buyer Order Status State Machine.
 * Defines the states and transitions for managing buyer order statuses.
 */
@RequiredArgsConstructor
@Configuration
@EnableStateMachineFactory(name = "buyerOrderStatusStateMachineFactory")
public class BuyerOrderStatusStateMachineConfig
        extends StateMachineConfigurerAdapter<BuyerOrderStatus, SellerOrderPackageEvent> {

    /**
     * Action to be executed when a state transition occurs.
     * Captures the event associated with the state change.
     */
    private final BuyerOrderStatusCaptureEventAction buyerOrderStatusCaptureEventAction;

    /**
     * Configures the states for the buyer order status state machine.
     * Defines the initial state and includes all possible states.
     *
     * @param states the state machine state configurator
     * @throws Exception if an error occurs during configuration
     */
    @Override
    public void configure(StateMachineStateConfigurer<BuyerOrderStatus, SellerOrderPackageEvent> states) throws Exception {
        states
                .withStates()
                // Define the initial state of the state machine
                .initial(BuyerOrderStatus.WAIT_FOR_PAY_ORDER)

                // Include all possible states in the state machine
                .states(EnumSet.allOf(BuyerOrderStatus.class));
    }

    /**
     * Configures the transitions for the buyer order status state machine.
     * Defines how the state machine moves from one state to another based on events.
     *
     * @param transitions the state machine transition configurator
     * @throws Exception if an error occurs during configuration
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<BuyerOrderStatus, SellerOrderPackageEvent> transitions) throws Exception {
        transitions
                // External transition for Event: UC08: Duyệt (Approve)
                .withExternal()
                // Source state before the transition
                .source(BuyerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION)
                // Target state after the transition
                .target(BuyerOrderStatus.SUPPLIER_IS_PREPARING_ORDER)
                // Action to execute during the transition
                .action(buyerOrderStatusCaptureEventAction)
                // Event that triggers this transition
                .event(SellerOrderPackageEvent.APPROVED)
        ;
    }

}