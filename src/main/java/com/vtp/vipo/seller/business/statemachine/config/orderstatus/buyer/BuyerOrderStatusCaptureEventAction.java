package com.vtp.vipo.seller.business.statemachine.config.orderstatus.buyer;

import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Action class that captures the event related to a buyer's order status.
 * This action stores the last event (SellerOrderPackageEvent) from the state machine's message headers
 * into the extended state of the state machine.
 *
 * It implements the Action interface of Spring StateMachine, specifically for handling state transitions
 * between {@link BuyerOrderStatus} and {@link SellerOrderPackageEvent}.
 */
@Component
public class BuyerOrderStatusCaptureEventAction implements Action<BuyerOrderStatus, SellerOrderPackageEvent> {

    /**
     * Executes the action for capturing the event.
     * The method extracts the event from the message headers and stores it in the extended state.
     *
     * @param context the context of the state machine, containing information about the state and event
     */
    @Override
    public void execute(StateContext<BuyerOrderStatus, SellerOrderPackageEvent> context) {
        // Retrieve the message from the state machine context
        Message<SellerOrderPackageEvent> message = context.getMessage();

        // Check if the message and the header key "EVENT_KEY" are present
        if (message != null && message.getHeaders().containsKey("EVENT_KEY")) {
            // Extract the event from the headers of the message
            SellerOrderPackageEvent event = (SellerOrderPackageEvent) message.getHeaders().get("EVENT_KEY");

            // Store the event in the extended state under the "lastEvent" variable
            context.getExtendedState().getVariables().put("lastEvent", event);
        }
    }
}