package com.vtp.vipo.seller.common.dto.response.order;

/**
 * Interface representing the projection for seller order status.
 * This projection contains information about the order status group and the total number of orders
 * associated with that status group.
 */
public interface SellerOrderStatusProjection {

    /**
     * @return The group number of the order sellerOderStatus by parentId
     */
    Integer getStatusGroup();

    /**
     * @return The total number of orders that belong to the given status group
     */
    Long getToltalOrder();
}

