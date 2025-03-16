package com.vtp.vipo.seller.common.enumseller;

import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderAction {

    /**
     * View order details.
     * <p>This action allows users to view the details of the order.</p>
     */
    VIEW_DETAILS,

    /**
     * Adjust the price of the order.
     * <p>This action allows users to adjust the price of the order.</p>
     */
    ADJUST_PRICE,

    /**
     * Cancel the order.
     * <p>This action allows users to cancel the order.</p>
     */
    CANCEL,

    /**
     * Approve the order.
     * <p>This action allows users to approve the order for further processing.</p>
     */
    APPROVE,

    /**
     * Reject the order.
     * <p>This action allows users to reject the order if necessary.</p>
     */
    REJECT,

    /**
     * Prepare the order for shipment.
     * <p>This action allows users to prepare the order for shipment.</p>
     */
    PREPARE,

    PRINT,

    HELP_CENTER;

    public static List<OrderAction> getActionsFromStatus(SellerOrderStatus status){
        List<OrderAction> actions = new LinkedList<>();
        //todo: add action for cancel
        switch (status) {
            case WAITING_FOR_PAYMENT:
                actions.add(VIEW_DETAILS);
                actions.add(ADJUST_PRICE);
                break;
            case WAITING_FOR_SELLER_CONFIRMATION:
                actions.add(VIEW_DETAILS);
                actions.add(APPROVE);
                actions.add(REJECT);
                break;
            case WAITING_FOR_ORDER_PREPARATION:
                actions.add(VIEW_DETAILS);
                actions.add(PREPARE);
                actions.add(CANCEL);
                break;
            case ORDER_PREPARED, ORDER_SHIPMENT_CONNECTION_SUCCESS:
                actions.add(VIEW_DETAILS);
                actions.add(PRINT);
                actions.add(CANCEL);
                break;
            case ORDER_IN_TRANSIT, ORDER_DELIVERED_TO_SHIPPING, ORDER_COMPLETED:
                actions.add(VIEW_DETAILS);
                break;
            default:
                break;
        }
        /**
         * ORDER_IN_TRANSIT, ORDER_DELIVERED_TO_SHIPPING, ORDER_COMPLETED, SELLER_REJECTED_ORDER, ORDER_CANCELLED_BY_SELLER
         * only have VIEW_DETAILS
         * */
        return actions;
    }

}

