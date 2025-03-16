package com.vtp.vipo.seller.common.dao.entity.projections;

/**
 * Projection interface for fetching specific fields of a Seller Package Product.
 *
 * <p>This interface is used in Spring Data JPA repositories to retrieve a subset of fields
 * from the {@link com.vtp.vipo.seller.common.dao.entity.PackageProductEntity} entity.
 * By using projections, you can optimize queries to fetch only the necessary data,
 * reducing memory consumption and improving performance.</p>
 *
 *
 * @see com.vtp.vipo.seller.common.dao.entity.PackageProductEntity
 */
public interface SellerPackageProductProjection {

    //package_product.packageId
    Long getPackageId();

    //package_product.id
    Long getId();

    //product_seller_sku.id
    Long getSkuId();

    //product_seller_sku.id
    Long getSkuStock();

    Long getLazbaoSkuId();

}

