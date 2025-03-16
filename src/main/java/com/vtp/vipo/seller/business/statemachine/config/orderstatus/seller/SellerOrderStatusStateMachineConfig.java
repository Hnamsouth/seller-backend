package com.vtp.vipo.seller.business.statemachine.config.orderstatus.seller;

import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Configuration class for the Seller Order State Machine.
 * <p>
 * This class defines the states and transitions for managing the lifecycle of a seller's order
 * using Spring State Machine. It specifies the initial state, all possible states, and the events
 * that trigger transitions between these states.
 * </p>
 *
 * <p>
 * The state machine is responsible for enforcing valid state transitions based on predefined events.
 * This ensures that the order status progresses through its lifecycle in a controlled and predictable manner.
 * </p>
 *
 * @see SellerOrderStatus
 * @see SellerOrderPackageEvent
 * @see org.springframework.statemachine.StateMachine
 */
@Configuration
@EnableStateMachineFactory(name = "sellerOrderStatusStateMachineFactory")
public class SellerOrderStatusStateMachineConfig extends StateMachineConfigurerAdapter<SellerOrderStatus, SellerOrderPackageEvent> {

    /**
     * Configures the states of the state machine.
     * <p>
     * Defines the initial state and includes all possible states that an order can be in.
     * </p>
     *
     * @param states the {@link StateMachineStateConfigurer} to configure the states
     * @throws Exception if an error occurs during state configuration
     */
    @Override
    public void configure(StateMachineStateConfigurer<SellerOrderStatus, SellerOrderPackageEvent> states) throws Exception {
        states
                .withStates()
                // Define the initial state of the state machine
                .initial(SellerOrderStatus.WAITING_FOR_PAYMENT)

                // Include all possible states in the state machine
                .states(EnumSet.allOf(SellerOrderStatus.class));
    }

    /**
     * Configures the transitions between states based on events.
     * <p>
     * Defines how the state machine should transition from one state to another when specific events occur.
     * Each transition is triggered by an event that moves the order from a source state to a target state.
     * </p>
     *
     * @param transitions the {@link StateMachineTransitionConfigurer} to configure the transitions
     * @throws Exception if an error occurs during transition configuration
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<SellerOrderStatus, SellerOrderPackageEvent> transitions) throws Exception {
        transitions
                // Transition for Event: UC08: Duyệt (Approve)
                .withExternal()
                .source(SellerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION)
                .target(SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION)
                .event(SellerOrderPackageEvent.APPROVED)
                .and()

                // Transition for Event: UC05: Hủy (Cancel)
                .withExternal()
                .source(SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION)
                .target(SellerOrderStatus.ORDER_CANCELLED_BY_SELLER)
                .event(SellerOrderPackageEvent.CANCEL)
                .and()

        // Additional transitions can be defined here
        ;
    }
}