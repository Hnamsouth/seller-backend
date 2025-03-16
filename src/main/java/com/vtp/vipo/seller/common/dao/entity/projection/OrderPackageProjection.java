package com.vtp.vipo.seller.common.dao.entity.projection;

/**
 * Projection interface for {@link com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity}.
 * <p>
 * This interface defines a subset of properties from the {@code OrderPackageEntity}
 * that are relevant for specific query operations. Using projections can optimize
 * performance by fetching only the necessary fields from the database.
 * </p>
 *
 *
 * @see com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity
 * @author anhdev
 * @version 1.0
 * @since 2024-04-27
 */
public interface OrderPackageProjection {

    /**
     * Retrieves the unique identifier of the order package.
     *
     * @return the {@code Long} value representing the order package ID.
     */
    Long getId();

    /**
     * Retrieves the seller's order status.
     * <p>
     * Depending on your use case, this could either be a {@code String} or an {@code enum}.
     * Using an {@code enum} type can provide type safety and restrict the values to predefined constants.
     * </p>
     *
     * @return the {@code String} representing the seller's order status.
     */
    String getSellerOrderStatus();

}
