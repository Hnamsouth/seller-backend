package com.vtp.vipo.seller.business.calculator;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.OrderEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.PackageProductEntity;
import com.vtp.vipo.seller.common.dao.entity.ProductEntity;
import com.vtp.vipo.seller.common.dto.response.OrderDetailsResponse;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for calculating various financial aspects of a seller's order package.
 * <p>
 * This class provides methods to compute the total price of an {@link OrderPackageEntity} by aggregating
 * product amounts, international and domestic shipping fees, and negotiated amounts. It ensures that
 * all calculations adhere to the business rules defined for seller orders.
 * </p>
 *
 * <p>
 * <strong>Formula for Total Price:</strong>
 * <pre>
 * Total Price = Product Amount + Total International Shipping Fee + Total Domestic Shipping Fee - Negotiated Amount
 * </pre>
 * </p>
 *
 * <p>
 * <strong>Description of Components:</strong>
 * <ul>
 *     <li><strong>Product Amount:</strong> The cumulative cost of all products within the order package.</li>
 *     <li><strong>Total International Shipping Fee:</strong> The aggregate international shipping costs associated with the order.</li>
 *     <li><strong>Total Domestic Shipping Fee:</strong> The aggregate domestic shipping costs associated with the order.</li>
 *     <li><strong>Negotiated Amount:</strong> Any discounts or negotiated adjustments applied to the order package.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Note:</strong> This calculator is exclusively applicable to orders from Vipo sellers and does not support Lazbao orders.
 * </p>
 *
 * <p>
 * <strong>Design Considerations:</strong>
 * <ul>
 *     <li>The class is final to prevent inheritance, ensuring its utility nature.</li>
 *     <li>All methods are static, allowing invocation without instantiating the class.</li>
 *     <li>Uses Apache Commons Lang's {@code ObjectUtils} for null and empty checks to enhance reliability.</li>
 * </ul>
 * </p>
 *
 * @see OrderPackageEntity
 * @see PackageProductEntity
 */
@Slf4j
public final class SellerOrderPackageCalculator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always thrown to indicate that instantiation is not supported.
     */
    private SellerOrderPackageCalculator() {
        throw new UnsupportedOperationException(Constants.UTITLITY_CLASS_ERROR);
    }

    /**
     * Calculates the total price of an entire order by summing the total prices of all its associated order packages.
     * <p>
     * The total price of an {@link OrderEntity} is computed using the following formula:
     * <pre>
     * Total Order Price = Σ (Total Price of Each Order Package)
     * </pre>
     *
     * This method performs the following steps:
     * <ol>
     *     <li>Retrieves all {@link OrderPackageEntity} instances associated with the given {@link OrderEntity}.</li>
     *     <li>Validates that the order has at least one associated order package.</li>
     *     <li>Calculates the total price for each order package using {@link SellerOrderPackageCalculator#calculateTotalPriceOrderPackage(OrderPackageEntity)}.</li>
     *     <li>Aggregates the total prices of all order packages to compute the overall order total.</li>
     * </ol>
     * </p>
     *
     * <p>
     * <strong>Formula:</strong>
     * <pre>
     * Total Order Price = Σ (Total Price of Each Order Package)
     * </pre>
     * </p>
     *
     * <p>
     * <strong>Example:</strong>
     * If an order has three packages with total prices of $100.00, $200.00, and $150.00 respectively,
     * the total order price would be $450.00.
     * </p>
     *
     * @param orderEntity the {@link OrderEntity} for which the total price is to be calculated
     * @return the total price of the order as a {@link BigDecimal}
     * @throws VipoInvalidDataRequestException if the order has no associated order packages
     * @throws IllegalArgumentException if {@code orderEntity} is {@code null}
     */
    public static BigDecimal calculateTotalPriceOrder(@NotNull OrderEntity orderEntity) {
        // Retrieve all associated order packages for the given order
        Set<OrderPackageEntity> orderPackageEntities = orderEntity.getOrderPackageEntities();

        // Validate that there is at least one order package
        if (ObjectUtils.isEmpty(orderPackageEntities)) {
            throw new VipoInvalidDataRequestException(
                    "Missing order package for order with id " + orderEntity.getId()
            );
        }

        // Calculate the sum of total prices for all order packages
        return orderPackageEntities.stream()
                .map(SellerOrderPackageCalculator::calculateTotalPriceOrderPackage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total price of an order package.
     * <p>
     * The total price is computed using the following formula:
     * <pre>
     * Total Price = Product Amount
     *             + Total International Shipping Fee
     *             + Total Domestic Shipping Fee
     *             - Negotiated Amount
     * </pre>
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} for which the total price is to be calculated
     * @return the total price as a {@link BigDecimal}
     * @throws VipoInvalidDataRequestException if the order package has no associated products
     */
    public static BigDecimal calculateTotalPriceOrderPackage(@NotNull OrderPackageEntity orderPackageEntity) {

        // Calculate the total product amount
        BigDecimal productAmount = calculateProductAmount(orderPackageEntity);

        // Calculate the total international shipping fee
        BigDecimal totalInternationalShippingFee = calculateTotalInternationalShippingFee(orderPackageEntity);

        // Calculate the total domestic shipping fee
        BigDecimal totalDomesticShippingFee = calculateTotalDomesticShippingFee(orderPackageEntity);

        // Compute the total price using the formula
        return productAmount
                .add(totalInternationalShippingFee)
                .add(totalDomesticShippingFee);
    }

    /**
     * Computes the total amount for all products within the order package.
     * <p>
     * This method aggregates the cost of each product by multiplying the unit price by the quantity
     * ordered. It ensures that each product's price and quantity are valid before inclusion in the total.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} containing the products
     * @return the total product amount as a {@link BigDecimal}
     * @throws VipoInvalidDataRequestException if the order package has no associated products
     */
    public static BigDecimal calculateProductAmount(@NotNull OrderPackageEntity orderPackageEntity) {
        Set<PackageProductEntity> packageProductEntities = orderPackageEntity.getSku();

        // Validate that the order package contains products
        if (ObjectUtils.isEmpty(packageProductEntities)) {
            throw new VipoInvalidDataRequestException(
                    "Missing package product for order package with id " + orderPackageEntity.getId()
            );
        }

        // Sum the total amount for all products
        return packageProductEntities.stream()
                .map(SellerOrderPackageCalculator::calculatePackageProductAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(getNegotiatedAmount(orderPackageEntity));
    }

    /**
     * Retrieves the unit price for a given package product.
     * <p>
     * The unit price is determined based on whether a negotiated amount is present and valid.
     * If a negotiated amount exists and is less than or equal to zero, it is used as the unit price.
     * Otherwise, the standard SKU price is returned. If neither is available, {@code BigDecimal.ZERO} is returned.
     * </p>
     *
     * @param packageProductEntity the {@link PackageProductEntity} whose unit price is to be retrieved
     * @return the unit price as a {@link BigDecimal}
     */
    public static BigDecimal getUnitPrice(@NotNull PackageProductEntity packageProductEntity) {
        // Use negotiated amount if available and valid
        if (ObjectUtils.isNotEmpty(packageProductEntity.getNegotiatedAmount()) &&
                packageProductEntity.getNegotiatedAmount().compareTo(BigDecimal.ZERO) > 0) {
            return packageProductEntity.getNegotiatedAmount();
        }

        // Otherwise, use the SKU price if available
        return ObjectUtils.isNotEmpty(packageProductEntity.getSkuPrice()) ?
                packageProductEntity.getSkuPrice() : BigDecimal.ZERO;
    }

    /**
     * Retrieves the total international shipping fee for the order package.
     * <p>
     * This method sums up all international shipping costs associated with the order package.
     * If no such fees are present, it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} for which the international shipping fee is to be retrieved
     * @return the total international shipping fee as a {@link BigDecimal}
     */
    public static BigDecimal calculateTotalInternationalShippingFee(@NotNull OrderPackageEntity orderPackageEntity) {
        // TODO: Update when the complete fee document is available
        return ObjectUtils.isNotEmpty(orderPackageEntity.getTotalInternationalShippingFee()) ?
                orderPackageEntity.getTotalInternationalShippingFee() : BigDecimal.ZERO;
    }

    /**
     * Retrieves the total domestic shipping fee for the order package.
     * <p>
     * This method sums up all domestic shipping costs associated with the order package.
     * If no such fees are present, it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} for which the domestic shipping fee is to be retrieved
     * @return the total domestic shipping fee as a {@link BigDecimal}
     */
    public static BigDecimal calculateTotalDomesticShippingFee(@NotNull OrderPackageEntity orderPackageEntity) {
        // TODO: Update when the complete fee document is available
        return ObjectUtils.isNotEmpty(orderPackageEntity.getTotalDomesticShippingFee()) ?
                orderPackageEntity.getTotalDomesticShippingFee() : BigDecimal.ZERO;
    }

    /**
     * Retrieves the negotiated amount for the order package.
     * <p>
     * The negotiated amount represents any discounts or adjustments agreed upon for the entire order package.
     * If no negotiated amount is present, it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} for which the negotiated amount is to be retrieved
     * @return the negotiated amount as a {@link BigDecimal}
     */
    public static BigDecimal getNegotiatedAmount(@NotNull OrderPackageEntity orderPackageEntity) {
        // TODO: Ensure correctness; currently returns totalDomesticShippingFee which may be an error
        return ObjectUtils.isNotEmpty(orderPackageEntity.getNegotiatedAmount()) ?
                orderPackageEntity.getNegotiatedAmount() : BigDecimal.ZERO;
    }

    /**
     * Calculates the total price of a package product.
     * <p>
     * This method computes the total amount for a package product by multiplying its unit price
     * by the quantity. If the quantity is null or zero, it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param packageProductEntity the {@link PackageProductEntity} containing product details
     *                              such as quantity and unit price
     * @return the total price as a {@link BigDecimal}, or {@code BigDecimal.ZERO} if the quantity is null or zero
     */
    public static BigDecimal calculatePackageProductAmount(@NotNull PackageProductEntity packageProductEntity) {
        Long quantity = packageProductEntity.getQuantity();

        // Skip products with no quantity or zero quantity
        if (ObjectUtils.isEmpty(quantity) || quantity == 0) {
            return BigDecimal.ZERO;
        }

        // Get the unit price for the product
        BigDecimal unitPrice = getUnitPrice(packageProductEntity);

        // Multiply unit price by quantity to get total price for this product
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Retrieves the original unit price of a package product.
     * <p>
     * This method returns the original unit price of a product (SKU price) from the package product entity.
     * If the SKU price is not available, it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param packageProductEntity the {@link PackageProductEntity} containing the SKU price
     * @return the original unit price as a {@link BigDecimal}, or {@code BigDecimal.ZERO} if the SKU price is null or empty
     */
    public static BigDecimal getOriginalUnitPrice(@NotNull PackageProductEntity packageProductEntity) {
        return ObjectUtils.isNotEmpty(packageProductEntity.getSkuPrice()) ?
                packageProductEntity.getSkuPrice() : BigDecimal.ZERO;
    }

    /**
     * Calculates the original total price of a package product.
     * <p>
     * This method computes the total amount for a package product by multiplying its original unit price
     * (retrieved via {@link #getOriginalUnitPrice}) by the quantity. If the quantity is null or zero,
     * it defaults to {@code BigDecimal.ZERO}.
     * </p>
     *
     * @param packageProductEntity the {@link PackageProductEntity} containing product details
     *                              such as quantity and SKU price
     * @return the original total price as a {@link BigDecimal}, or {@code BigDecimal.ZERO} if the quantity is null or zero
     */
    public static BigDecimal calculateOriginalPackageProductAmount(@NotNull PackageProductEntity packageProductEntity) {
        Long quantity = packageProductEntity.getQuantity();

        // Skip products with no quantity or zero quantity
        if (ObjectUtils.isEmpty(quantity) || quantity == 0) {
            return BigDecimal.ZERO;
        }

        // Get the unit price for the product
        BigDecimal unitPrice = getOriginalUnitPrice(packageProductEntity);

        // Multiply unit price by quantity to get total price for this product
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calculates the total original price of all products in an order package.
     * <p>
     * This method computes the total original amount for all package products within the given order package entity.
     * It uses {@link #calculateOriginalPackageProductAmount} to calculate the amount for each package product.
     * If the order package does not contain any products, a {@link VipoInvalidDataRequestException} is thrown.
     * </p>
     *
     * @param orderPackageEntity the {@link OrderPackageEntity} containing the package products
     * @return the total original price for all package products as a {@link BigDecimal}
     * @throws VipoInvalidDataRequestException if no package products are found in the order package
     */
    public static BigDecimal calculateOriginalProductAmount(@NotNull OrderPackageEntity orderPackageEntity) {
        Set<PackageProductEntity> packageProductEntities = orderPackageEntity.getSku();

        // Validate that the order package contains products
        if (ObjectUtils.isEmpty(packageProductEntities)) {
            throw new VipoInvalidDataRequestException(
                    "Missing package product for order package with id " + orderPackageEntity.getId()
            );
        }

        // Sum the total amount for all products
        return packageProductEntities.stream()
                .map(SellerOrderPackageCalculator::calculateOriginalPackageProductAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total amount of platform discount for all products in an order package.
     * <p>
     *     This method computes the total amount of platform discount for all package products within the given order package entity.
     *     It uses {@link #calculateProductPlatformDiscount} to calculate the amount for each package product.
        * </p>
     *
     * */
    public static BigDecimal calculateProductPlatformDiscount(List<PackageProductEntity> packageProducts) {

        if (CollectionUtils.isEmpty(packageProducts)) {
            return BigDecimal.ZERO;
        }
        return packageProducts.stream().map(SellerOrderPackageCalculator::calculateProductPlatformDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal calculateProductPlatformDiscount(PackageProductEntity packageProduct) {
        if(ObjectUtils.isNotEmpty(packageProduct)){
//            if (packageProduct.getSellerPlatformDiscountRate() != null ) {
//                return packageProduct.getSkuPrice()
//                        .multiply(packageProduct.getSellerPlatformDiscountRate())
//                        .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
//                        .multiply(new BigDecimal(packageProduct.getQuantity()));
//            }

            if(packageProduct.getSellerPlatformDiscountAmount() != null){
                return packageProduct.getSellerPlatformDiscountAmount();
            }
        }

        return BigDecimal.ZERO;
    }

}
