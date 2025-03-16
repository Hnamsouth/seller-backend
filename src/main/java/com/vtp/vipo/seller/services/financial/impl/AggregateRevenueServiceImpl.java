package com.vtp.vipo.seller.services.financial.impl;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dao.repository.OrderPackageRepository;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.financial.*;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.financial.AggregateRevenueService;
import com.vtp.vipo.seller.services.financial.FinancialUtils;
import com.vtp.vipo.seller.services.financial.TotalVisitsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregateRevenueServiceImpl implements AggregateRevenueService {

    final OrderPackageRepository orderPackageRepository;

    final TotalVisitsService totalVisitsService;

    /**
     * Aggregates revenue data for a specific period.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Retrieves dashboard metrics for the given period.</li>
     *   <li>Calculates the total revenue from orders in the specified time range.
     *       If no revenue is found, it defaults to zero.</li>
     *   <li>Retrieves the top 5 best-selling products along with their SKU data.</li>
     *   <li>Calculates chart data based on the top products.</li>
     *   <li>Retrieves detailed financial data items for the period.</li>
     *   <li>Sets the top product report, chart data, and financial data into the response.</li>
     * </ol>
     *
     * @param request    the financial report request containing filter criteria.
     * @param merchantId the merchant's ID.
     * @param timeRange  the time range for the report.
     * @return a FinancialReportResponse containing aggregated revenue data, chart data, and financial data items.
     */
    @Override
    public FinancialReportResponse aggregateRevenueData(FinancialReportRequest request, Long merchantId, TimeRange timeRange) {
        FinancialReportResponse response = getDashboardMetrics(request, merchantId, timeRange);

        // Calculate total revenue for the period.
        BigDecimal totalRevenue = orderPackageRepository.getTotalRevenue(merchantId, timeRange.getCurrentStartSec(), timeRange.getCurrentEndSec());
        if (ObjectUtils.isEmpty(totalRevenue)) {
            totalRevenue = BigDecimal.ZERO;
        }
        log.info("[Financial exportReportRevenue] totalRevenue: {}", totalRevenue);

        // Retrieve the top 5 best-selling products.
        List<FinancialReportResponse.TopProduct> topProducts = getTopProducts(merchantId, totalRevenue, timeRange);
        log.info("[Financial exportReportRevenue] topProducts: {}", topProducts);

        // Calculate chart data for the top products.
        FinancialReportResponse.ChartData getChartData = getChartData(topProducts, totalRevenue);
        log.info("[Financial exportReportRevenue] getChartData: {}", getChartData);

        // Retrieve detailed financial data items for the period.
        List<FinancialReportResponse.FinancialDataItem> financialDataItems = getFinancialDataItems(request, merchantId, timeRange);
        log.info("[Financial exportReportRevenue] financialDataItems: {}", financialDataItems);

        // Set the computed values into the response.
        response.setTopProductReport(topProducts);
        response.setChartData(getChartData);
        response.setFinancialData(financialDataItems);
        return response;
    }

    /**
     * Retrieves dashboard metrics for a specified period.
     * <p>
     * This method calculates both the current and previous time ranges, retrieves raw dashboard data for each period,
     * and then computes key metrics including total orders, total buyers, revenue, and profit.
     * It also retrieves total visits data and calculates conversion, delivery, and return/cancel rates.
     *
     * @param request    the financial report request with filter type and value.
     * @param merchantId the merchant's ID.
     * @param timeRange  the time range for the report.
     * @return a FinancialReportResponse object containing the computed dashboard metrics.
     */
    public FinancialReportResponse getDashboardMetrics(FinancialReportRequest request, Long merchantId, TimeRange timeRange) {
        // Calculate current and previous time ranges.
        long currStart = timeRange.getCurrentStartSec();
        long currEnd = timeRange.getCurrentEndSec();
        long prevStart = timeRange.getPreviousStartSec();
        long prevEnd = timeRange.getPreviousEndSec();

        // Retrieve raw dashboard data for the current period.
        log.info("[Financial getDashboardMetrics] Get dashboard metrics with merchantId: {}, currStart: {}, currEnd: {}", merchantId, currStart, currEnd);
        FinancialDashboardRawData currentData = orderPackageRepository.getDashboardRawData(merchantId, currStart, currEnd);
        if (ObjectUtils.isEmpty(currentData)) {
            currentData = new FinancialDashboardRawDataImpl(); // Default to zero data if none found.
        }
        log.info("[Financial getDashboardMetrics] currentData: {}", currentData.show());

        // Retrieve raw dashboard data for the previous period.
        log.info("[Financial getDashboardMetrics] Get dashboard metrics with merchantId: {}, prevStart: {}, prevEnd: {}", merchantId, prevStart, prevEnd);
        FinancialDashboardRawData previousData = orderPackageRepository.getDashboardRawData(merchantId, prevStart, prevEnd);
        if (ObjectUtils.isEmpty(previousData)) {
            previousData = new FinancialDashboardRawDataImpl(); // Default to zero data if none found.
        }
        log.info("[Financial getDashboardMetrics] previousData: {}", previousData.show());

        // Retrieve total visits data for the current period and for all time.
        FinancialReportResponse.TotalVisitsData totalVisitsData = totalVisitsService.getTotalVisits(merchantId, currStart, currEnd);
        FinancialReportResponse.TotalVisitsData totalVisitsDataAllTime = totalVisitsService.getTotalVisitsAllTime(merchantId);

        // Build metric values for total orders, buyers, and revenue.
        MetricValue totalOrdersMetric = buildMetricValue(
                FinancialUtils.toBigDecimal(currentData.getTotalOrders()),
                FinancialUtils.toBigDecimal(previousData.getTotalOrders())
        );
        MetricValue totalBuyersMetric = buildMetricValue(
                FinancialUtils.toBigDecimal(currentData.getTotalBuyers()),
                FinancialUtils.toBigDecimal(previousData.getTotalBuyers())
        );
        MetricValue revenueMetric = buildMetricValue(
                FinancialUtils.safe(currentData.getRevenue()),
                FinancialUtils.safe(previousData.getRevenue())
        );

        // Calculate profit: profit = revenue - (platformFee + otherFee).
        BigDecimal currProfit = FinancialUtils.safe(currentData.getRevenue())
                .subtract(FinancialUtils.safe(currentData.getPlatformFee()))
                .subtract(FinancialUtils.safe(currentData.getOtherFee()));
        BigDecimal prevProfit = FinancialUtils.safe(previousData.getRevenue())
                .subtract(FinancialUtils.safe(previousData.getPlatformFee()))
                .subtract(FinancialUtils.safe(previousData.getOtherFee()));
        MetricValue profitMetric = buildMetricValue(currProfit, prevProfit);

        // Calculate conversion rate: (convertedOrders / totalVisits) * 100.
        BigDecimal conversionRate = calcConversionRate(
                currentData.getConvertedOrders(), totalVisitsData.getTotalVisits()
        );

        // Calculate delivery rate: (deliveredOrders / totalOrders) * 100.
        BigDecimal deliveryRate = calcRate(
                currentData.getDeliveredOrders(), currentData.getTotalOrders()
        );

        // Calculate return/cancel rate: (returnCancelOrders / totalOrders) * 100.
        BigDecimal returnCancelRate = calcRate(
                currentData.getReturnCancelOrders(), currentData.getTotalOrders()
        );

        // Build and return the FinancialReportResponse with all metrics.
        return FinancialReportResponse.builder()
                .period(PeriodType.valueOf(request.getFilterType().toUpperCase()))
                .periodStart(DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate())
                .periodEnd(DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate())
                .totalOrderPackages(totalOrdersMetric)
                .totalBuyers(totalBuyersMetric)
                .conversionRate(conversionRate)
                .deliveryRate(deliveryRate)
                .returnCancelRate(returnCancelRate)
                .revenue(revenueMetric)
                .totalPriceNegotiated(FinancialUtils.safe(currentData.getPriceNegotiated()))
                .totalProfit(profitMetric)
                .platformFee(FinancialUtils.safe(currentData.getPlatformFee()))
                .otherFee(FinancialUtils.safe(currentData.getOtherFee()))
                .totalVisitsData(totalVisitsDataAllTime)
                .build();
    }

    /**
     * Builds a MetricValue object containing the current value, previous value, their difference, and the growth rate.
     * <p>
     * The growth rate is calculated as:
     * <pre>
     *     growthRate = ((current - previous) / previous) * 100
     * </pre>
     * Special cases:
     * <ul>
     *   <li>If the previous value is zero:
     *       <ul>
     *         <li>growthRate is set to zero if current is also zero, or</li>
     *         <li>growthRate is set to null if current is not zero.</li>
     *       </ul>
     *   </li>
     *   <li>If current is zero and previous is non-zero, growthRate is set to -100%.</li>
     * </ul>
     *
     * @param current  the current period's value.
     * @param previous the previous period's value.
     * @return a MetricValue object with the computed values.
     */
    private MetricValue buildMetricValue(BigDecimal current, BigDecimal previous) {
        BigDecimal zero = BigDecimal.ZERO;
        current = ObjectUtils.isNotEmpty(current) ? current : zero;
        previous = ObjectUtils.isNotEmpty(previous) ? previous : zero;

        BigDecimal difference = current.subtract(previous);
        BigDecimal growthRate;

        if (previous.compareTo(zero) == 0) {
            // Handle the case where previous is zero.
            growthRate = current.compareTo(zero) == 0 ? zero : null;
        } else {
            // Normal calculation for growth rate.
            growthRate = difference
                    .divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // If current is zero while previous is not, set growthRate to -100%.
        if (current.compareTo(zero) == 0 && previous.compareTo(zero) != 0) {
            growthRate = BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_UP);
        }

        return MetricValue.builder()
                .currentValue(current)
                .previousValue(previous)
                .difference(difference)
                .growthRate(growthRate)
                .build();
    }

    /**
     * Calculates a percentage rate given a part and a total.
     * <p>
     * The rate is computed as:
     * <pre>
     *     rate = (part / total) * 100
     * </pre>
     * If the total is zero, the method returns 0.
     *
     * @param part  the numerator.
     * @param total the denominator.
     * @return the calculated rate as a BigDecimal with two decimal places.
     */
    private BigDecimal calcRate(long part, long total) {
        if (total == 0) return BigDecimal.ZERO;
        BigDecimal rate = BigDecimal.valueOf(part)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the conversion rate as the ratio of total orders to total visits multiplied by 100.
     * <p>
     * The conversion rate is computed as:
     * <pre>
     *     conversionRate = (totalOrders / totalVisits) * 100
     * </pre>
     * If totalVisits is zero, the method returns 0.
     *
     * @param totalOrders the total number of orders.
     * @param totalVisits the total number of visits.
     * @return the conversion rate as a BigDecimal with two decimal places.
     */
    private BigDecimal calcConversionRate(long totalOrders, long totalVisits) {
        if (totalVisits == 0) return BigDecimal.ZERO;
        BigDecimal rate = BigDecimal.valueOf(totalOrders)
                .divide(BigDecimal.valueOf(totalVisits), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retrieves the top products for the specified merchant and time range, aggregates SKU lines, and adjusts the financial metrics.
     *
     * @param merchantId   the merchant's ID.
     * @param totalRevenue the expected total revenue after negotiation (across all products).
     * @param timeRange    the time range within which to search for orders.
     * @return a list of TopProduct objects with aggregated, merged, and adjusted financial data.
     */
    public List<FinancialReportResponse.TopProduct> getTopProducts(Long merchantId, BigDecimal totalRevenue, TimeRange timeRange) {
        log.info("[Financial getTopProducts] Get top products with merchantId: {}", merchantId);
        long startSec = timeRange.getCurrentStartSec();
        long endSec = timeRange.getCurrentEndSec();

        // Retrieve total unique products count using a native query.
        Long totalProduct = orderPackageRepository.countUniqueProducts(merchantId, startSec, endSec);

        // Retrieve top product projections from the database.
        List<TopProductProjection> topProds = orderPackageRepository.findTopProducts(merchantId, startSec, endSec);
        if (topProds.isEmpty()) {
            return Collections.emptyList();
        }

        // Retrieve SKU line projections for the top products.
        List<Long> productIds = topProds.stream()
                .map(TopProductProjection::getProductId)
                .collect(Collectors.toList());
        List<TopProductSkuLineProjection> skuLines =
                orderPackageRepository.findAllSkuLinesByProductIds(merchantId, productIds, startSec, endSec);

        // If no SKU lines are found, map the top product projections directly to the response.
        if (skuLines.isEmpty()) {
            return mapEmptyResponse(topProds);
        }

        // Step 1: Build a map (packageQtyMap) to aggregate the total quantity per package.
        Map<Long, Integer> packageQtyMap = new HashMap<>();
        for (TopProductSkuLineProjection line : skuLines) {
            log.info("[Financial getTopProducts] line: {}", line.show());
            Long pkgId = line.getPackageId();
            int qty = (ObjectUtils.isEmpty(line.getQuantity())) ? 0 : line.getQuantity();
            packageQtyMap.put(pkgId, packageQtyMap.getOrDefault(pkgId, 0) + qty);
        }

        // Step 2: Group SKU lines by product ID.
        Map<Long, List<TopProductSkuLineProjection>> productSkuMap = skuLines.stream()
                .collect(Collectors.groupingBy(TopProductSkuLineProjection::getProductId));

        // Step 3: Build the list of TopProduct objects.
        List<FinancialReportResponse.TopProduct> result = new ArrayList<>();
        AtomicInteger productRank = new AtomicInteger(1);

        for (TopProductProjection prod : topProds) {
            Long productId = prod.getProductId();

            // Retrieve SKU lines corresponding to the current product.
            List<TopProductSkuLineProjection> linesOfProduct =
                    productSkuMap.getOrDefault(productId, Collections.emptyList());

            // Build a list of TopProductItems (initial SKU items) from the SKU line data.
            List<FinancialReportResponse.TopProductItem> skuItems =
                    linesOfProduct.stream()
                            .map(line -> buildTopProductItem(line, packageQtyMap))
                            .toList();

            // Merge SKU items having the same skuId.
            Map<Long, List<FinancialReportResponse.TopProductItem>> groupBySkuId =
                    skuItems.stream()
                            .collect(Collectors.groupingBy(FinancialReportResponse.TopProductItem::getSkuId));
            List<FinancialReportResponse.TopProductItem> mergedSkuItems = new ArrayList<>();

            for (Map.Entry<Long, List<FinancialReportResponse.TopProductItem>> entry : groupBySkuId.entrySet()) {
                Long skuId = entry.getKey();
                List<FinancialReportResponse.TopProductItem> sameSkuList = entry.getValue();

                int totalQty = 0;
                BigDecimal totalBefore = BigDecimal.ZERO;
                BigDecimal totalAfter = BigDecimal.ZERO;
                // Merge labels from the SKU items (either by concatenating or choosing the first, as needed).
                Set<String> labelParts = new LinkedHashSet<>();

                for (FinancialReportResponse.TopProductItem item : sameSkuList) {
                    totalQty += item.getQuantitySold();
                    totalBefore = totalBefore.add(item.getRevenueBeforeNegotiation());
                    totalAfter = totalAfter.add(item.getRevenueAfterNegotiation());
                    if (item.getLabel() != null) {
                        labelParts.add(item.getLabel());
                    }
                }
                String mergedLabel = String.join(" | ", labelParts);

                // Create the merged SKU item.
                FinancialReportResponse.TopProductItem mergedItem =
                        FinancialReportResponse.TopProductItem.builder()
                                .skuId(skuId)
                                .label(mergedLabel)
                                .quantitySold(totalQty)
                                .revenueBeforeNegotiation(totalBefore)
                                .revenueAfterNegotiation(totalAfter)
                                .ranking(0) // Ranking will be set later.
                                .build();

                mergedSkuItems.add(mergedItem);
            }

            // Sort the merged SKU items by quantity sold (descending) and then by revenueAfterNegotiation (descending).
            mergedSkuItems.sort(
                    Comparator
                            .comparing(FinancialReportResponse.TopProductItem::getQuantitySold, Comparator.reverseOrder())
                            .thenComparing(FinancialReportResponse.TopProductItem::getRevenueAfterNegotiation, Comparator.reverseOrder())
            );

            // Set the ranking for each SKU item.
            AtomicInteger skuRank = new AtomicInteger(1);
            mergedSkuItems.forEach(i -> i.setRanking(skuRank.getAndIncrement()));

            // Calculate the revenue percentage at product-level.
            BigDecimal productRevenueAfter = (ObjectUtils.isEmpty(prod.getRevenueAfterNegotiation()))
                    ? BigDecimal.ZERO
                    : prod.getRevenueAfterNegotiation();
            BigDecimal revenuePercent = BigDecimal.ZERO;
            if (ObjectUtils.isNotEmpty(totalRevenue) && totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                revenuePercent = productRevenueAfter
                        .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Build the TopProduct object.
            FinancialReportResponse.TopProduct topProduct =
                    FinancialReportResponse.TopProduct.builder()
                            .productId(productId)
                            .productName(prod.getProductName())
                            .productCode(prod.getProductCode())
                            .quantitySold(prod.getQuantitySold())
                            .revenueBeforeNegotiation(FinancialUtils.roundToTwoDecimalPlaces(prod.getRevenueBeforeNegotiation()))
                            .revenueAfterNegotiation(FinancialUtils.roundToTwoDecimalPlaces(productRevenueAfter))
                            .revenuePercentage(revenuePercent)
                            .ranking(productRank.getAndIncrement())
                            .skus(mergedSkuItems)  // assign the merged SKU list.
                            .build();

            result.add(topProduct);
        }

        // Step 4: Adjust and round the values at the product and SKU levels so that:
        //   - For totalProduct <= 5:
        //       * The sum of revenueAfterNegotiation of products equals totalRevenue.
        //       * The sum of revenuePercentage of products equals 100.
        //   - For each product, the sum of revenueAfterNegotiation of its SKUs equals the product's revenueAfterNegotiation.
        prepareTopProducts(result, totalRevenue, totalProduct);

        return result;
    }

    /**
     * Adjusts and rounds the revenueAfterNegotiation and revenuePercentage values for top products and their SKUs.
     *
     * @param topProducts  List of top products to adjust. Each product contains revenueAfterNegotiation, revenuePercentage, and a list of SKUs.
     * @param totalRevenue The expected total revenueAfterNegotiation for all products combined.
     * @param totalProduct The total number of unique products.
     */
    private void prepareTopProducts(List<FinancialReportResponse.TopProduct> topProducts, BigDecimal totalRevenue, Long totalProduct) {
        // Product-level adjustments (only if totalProduct <= 5)
        if (totalProduct <= 5) {
            // Calculate the sum of revenueAfterNegotiation for all products.
            BigDecimal sumProductsRevenueAfter = BigDecimal.ZERO;
            for (FinancialReportResponse.TopProduct product : topProducts) {
                sumProductsRevenueAfter = sumProductsRevenueAfter.add(product.getRevenueAfterNegotiation());
            }
            // Determine the difference between the expected totalRevenue and the current sum.
            BigDecimal diffRevenue = totalRevenue.subtract(sumProductsRevenueAfter);
            // If there is a difference, adjust the last product's revenueAfterNegotiation.
            if (diffRevenue.compareTo(BigDecimal.ZERO) != 0 && !topProducts.isEmpty()) {
                FinancialReportResponse.TopProduct lastProduct = topProducts.get(topProducts.size() - 1);
                lastProduct.setRevenueAfterNegotiation(lastProduct.getRevenueAfterNegotiation().add(diffRevenue));
            }

            // Adjust revenuePercentage at the product level.
            // Calculate the sum of revenuePercentage for all products.
            BigDecimal sumPercentage = BigDecimal.ZERO;
            for (FinancialReportResponse.TopProduct product : topProducts) {
                sumPercentage = sumPercentage.add(product.getRevenuePercentage());
            }
            // The target is 100%.
            BigDecimal targetPercentage = new BigDecimal("100");
            BigDecimal diffPercentage = targetPercentage.subtract(sumPercentage);
            // If there is a difference, adjust the last product's revenuePercentage.
            if (diffPercentage.compareTo(BigDecimal.ZERO) != 0 && !topProducts.isEmpty()) {
                FinancialReportResponse.TopProduct lastProduct = topProducts.get(topProducts.size() - 1);
                lastProduct.setRevenuePercentage(lastProduct.getRevenuePercentage().add(diffPercentage));
            }
        }

        // If totalProduct > 5, product-level values (revenueAfterNegotiation and revenuePercentage) remain unchanged.
        // SKU-level adjustments for each product ---
        for (FinancialReportResponse.TopProduct product : topProducts) {
            List<FinancialReportResponse.TopProductItem> skus = product.getSkus();
            if (skus != null && !skus.isEmpty()) {
                // Calculate the sum of revenueAfterNegotiation for all SKUs of this product.
                BigDecimal sumSkusRevenueAfter = BigDecimal.ZERO;
                for (FinancialReportResponse.TopProductItem sku : skus) {
                    sumSkusRevenueAfter = sumSkusRevenueAfter.add(sku.getRevenueAfterNegotiation());
                }
                // Determine the difference between the product's revenueAfterNegotiation and the sum of its SKUs.
                BigDecimal diffSku = product.getRevenueAfterNegotiation().subtract(sumSkusRevenueAfter);
                // If there is a difference, adjust the last SKU's revenueAfterNegotiation to match the product's total.
                if (diffSku.compareTo(BigDecimal.ZERO) != 0) {
                    FinancialReportResponse.TopProductItem lastSku = skus.get(skus.size() - 1);
                    lastSku.setRevenueAfterNegotiation(lastSku.getRevenueAfterNegotiation().add(diffSku));
                }
            }
        }
    }

    /**
     * Constructs a TopProductItem from a single SKU line projection by applying one of three discount cases.
     * <p>
     * The discount cases are as follows:
     * <ol>
     *   <li><b>Case 1:</b> If an order-level discount is applied (opNeg > 0) and no product-level discount (ppNeg == 0),
     *       then the discount per unit is calculated by dividing the order-level discount (opNeg) by the total
     *       quantity in the package (from packageQtyMap). The discounted unit price is then used to compute the
     *       revenueAfterNegotiation.</li>
     *   <li><b>Case 2:</b> If no order-level discount (opNeg == 0) is applied but a product-level discount exists (ppNeg > 0),
     *       the revenueAfterNegotiation is calculated as the quantity multiplied by the product-level discount (ppNeg).</li>
     *   <li><b>Case 3:</b> If no discount is applied, the revenueAfterNegotiation is simply equal to the revenueBeforeNegotiation
     *       (i.e. price * quantity).</li>
     * </ol>
     * The revenueBeforeNegotiation is always calculated as (price * quantity). Finally, the resulting revenue values are rounded
     * to two decimal places.
     *
     * @param line          the SKU line projection containing packageId, productId, skuId, price, quantity, and discount details.
     * @param packageQtyMap a map containing the total quantity for each packageId.
     * @return a TopProductItem with computed revenueBeforeNegotiation, revenueAfterNegotiation, and an initial ranking of 0.
     */
    private FinancialReportResponse.TopProductItem buildTopProductItem(TopProductSkuLineProjection line, Map<Long, Integer> packageQtyMap) {
        // Retrieve quantity and price; default to 0 if null.
        int qty = (ObjectUtils.isEmpty(line.getQuantity())) ? 0 : line.getQuantity();
        BigDecimal price = (ObjectUtils.isEmpty(line.getPrice())) ? BigDecimal.ZERO : line.getPrice();

        // Retrieve discounts: order-level discount (opNeg) and product-level discount (ppNeg).
        BigDecimal opNeg = (ObjectUtils.isEmpty(line.getOpNegotiatedAmount()))
                ? BigDecimal.ZERO : line.getOpNegotiatedAmount();
        BigDecimal ppNeg = (ObjectUtils.isEmpty(line.getPpNegotiatedAmount()))
                ? BigDecimal.ZERO : line.getPpNegotiatedAmount();

        // Calculate revenueBeforeNegotiation as price * quantity.
        BigDecimal revenueBefore = price.multiply(BigDecimal.valueOf(qty));

        // Initialize revenueAfterNegotiation.
        BigDecimal revenueAfter = BigDecimal.ZERO;

        // CASE 1: Order-level discount applied and no product-level discount.
        if (opNeg.compareTo(BigDecimal.ZERO) > 0 && ppNeg.compareTo(BigDecimal.ZERO) == 0) {
            // Retrieve total quantity for the package.
            Integer sumQty = packageQtyMap.getOrDefault(line.getPackageId(), 0);
            if (sumQty > 0) {
                // Calculate discount per unit without rounding at this step.
                // Using MathContext.DECIMAL128 for high precision division.
                BigDecimal diff = opNeg.divide(BigDecimal.valueOf(sumQty), MathContext.DECIMAL128);

                // Calculate the discounted price.
                BigDecimal priceAfter = price.subtract(diff);
                if (priceAfter.compareTo(BigDecimal.ZERO) < 0) {
                    priceAfter = BigDecimal.ZERO;
                }

                // Compute revenueAfterNegotiation using the discounted price.
                // Round the final result to 2 decimal places.
                revenueAfter = priceAfter.multiply(BigDecimal.valueOf(qty))
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                // Fallback: if package quantity is zero, use revenueBeforeNegotiation.
                revenueAfter = revenueBefore;
            }

            // CASE 2: No order-level discount but a product-level discount exists.
        } else if (opNeg.compareTo(BigDecimal.ZERO) == 0 && ppNeg.compareTo(BigDecimal.ZERO) > 0) {
            revenueAfter = BigDecimal.valueOf(qty).multiply(ppNeg);

            // CASE 3: No discount applied.
        } else {
            revenueAfter = revenueBefore;
        }

        // Build and return the TopProductItem DTO, rounding revenue values to two decimal places.
        return FinancialReportResponse.TopProductItem.builder()
                .skuId(line.getSkuId())
                .label(line.getLabel())
                .quantitySold(qty)
                .revenueBeforeNegotiation(FinancialUtils.roundToTwoDecimalPlaces(revenueBefore))
                .revenueAfterNegotiation(FinancialUtils.roundToTwoDecimalPlaces(revenueAfter))
                .ranking(0)
                .build();
    }

    /**
     * Maps a list of TopProductProjection objects to a list of TopProduct DTOs when there are no SKU lines available.
     * <p>
     * Each TopProduct is built using its product-level data. The revenuePercentage is set to zero and the SKU list is empty.
     *
     * @param topProds a list of TopProductProjection objects.
     * @return a list of TopProduct DTOs.
     */
    private List<FinancialReportResponse.TopProduct> mapEmptyResponse(List<TopProductProjection> topProds) {
        AtomicInteger rank = new AtomicInteger(1);
        return topProds.stream().map(prod -> FinancialReportResponse.TopProduct.builder()
                .productId(prod.getProductId())
                .productName(prod.getProductName())
                .productCode(prod.getProductCode())
                .quantitySold(prod.getQuantitySold())
                .revenueBeforeNegotiation(prod.getRevenueBeforeNegotiation())
                .revenueAfterNegotiation(prod.getRevenueAfterNegotiation())
                .revenuePercentage(BigDecimal.ZERO)
                .ranking(rank.getAndIncrement())
                .skus(Collections.emptyList())
                .build()).collect(Collectors.toList());
    }

    /**
     * Calculates chart data for top products.
     * <p>
     * This method creates chart data items for each top product using its name and revenue percentage.
     * It computes the total revenue and percentage from the top products. If the total revenue of the top products is less
     * than the expected totalRevenue, an additional chart data item is added for the remaining percentage labeled "Sản phẩm khác".
     *
     * @param topProducts  a list of TopProduct DTOs.
     * @param totalRevenue the expected total revenue.
     * @return a ChartData object containing the totalRevenue and a list of ChartDataItem objects.
     */
    public FinancialReportResponse.ChartData getChartData(List<FinancialReportResponse.TopProduct> topProducts, BigDecimal totalRevenue) {
        log.info("[Financial getChartData] Get chart data with topProducts: {}, totalRevenue: {}", JsonMapperUtils.writeValueAsString(topProducts), totalRevenue);
        if (topProducts.isEmpty()) {
            log.info("[Financial getChartData] Top products is empty");
            return FinancialReportResponse.ChartData.builder()
                    .totalRevenue(totalRevenue)
                    .data(Collections.emptyList())
                    .build();
        }

        // Create chart data items with ranking and display order.
        AtomicInteger ranking = new AtomicInteger(1);
        AtomicInteger displayOrder = new AtomicInteger(1);
        List<FinancialReportResponse.ChartDataItem> chartDataItems = new ArrayList<>(topProducts.stream()
                .map(product -> FinancialReportResponse.ChartDataItem.builder()
                        .label(product.getProductName())
                        .percentage(product.getRevenuePercentage())
                        .ranking(ranking.getAndIncrement())
                        .displayOrder(displayOrder.getAndIncrement())
                        .build())
                .toList());

        // Calculate total revenue of top products.
        BigDecimal totalRevenueTopProducts = topProducts.stream()
                .map(FinancialReportResponse.TopProduct::getRevenueAfterNegotiation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("[Financial getChartData] totalRevenueTopProducts: {}", totalRevenueTopProducts);

        // Calculate total revenue percentage of top products.
        BigDecimal totalPercentageTopProducts = topProducts.stream()
                .map(FinancialReportResponse.TopProduct::getRevenuePercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("[Financial getChartData] totalPercentageTopProducts: {}", totalPercentageTopProducts);

        // If the total revenue of top products is less than the expected totalRevenue,
        // add a chart data item for the remaining percentage ("Sản phẩm khác").
        if (totalRevenueTopProducts.compareTo(totalRevenue) < 0) {
            BigDecimal remainingPercentage = new BigDecimal(100)
                    .subtract(totalPercentageTopProducts)
                    .setScale(2, RoundingMode.HALF_UP);

            chartDataItems.add(FinancialReportResponse.ChartDataItem.builder()
                    .label("Sản phẩm khác")
                    .percentage(remainingPercentage)
                    .ranking(-1)
                    .displayOrder(displayOrder.getAndIncrement())
                    .build());
        }

        return FinancialReportResponse.ChartData.builder()
                .totalRevenue(totalRevenue)
                .data(chartDataItems)
                .build();
    }

    /**
     * Calculates financial data items for a given period.
     * <p>
     * This method determines the filter type (day, week, month, quarter, or year) from the request and delegates the data retrieval
     * to the corresponding method. The returned financial data items include revenue, cost, and profit for the specified period.
     *
     * @param request    the financial report request containing filter type and filter value.
     * @param merchantId the ID of the merchant.
     * @param timeRange  the time range for which to calculate the financial data.
     * @return a list of FinancialDataItem objects representing financial metrics for the specified period.
     * @throws VipoBusinessException if the filter type is invalid.
     */
    public List<FinancialReportResponse.FinancialDataItem> getFinancialDataItems(FinancialReportRequest request, Long merchantId, TimeRange timeRange) {
        String filterType = request.getFilterType().toLowerCase();
        String filterValue = request.getFilterValue();
        long startSec = timeRange.getCurrentStartSec();
        long endSec = timeRange.getCurrentEndSec();

        return switch (filterType) {
            case "day" -> getDataForSingleDay(filterValue, merchantId, startSec, endSec);
            case "week" -> getDataForWeek(filterValue, merchantId, startSec, endSec);
            case "month" -> getDataForMonth(filterValue, merchantId, startSec, endSec);
            case "quarter" -> getDataForQuarter(filterValue, merchantId, startSec, endSec);
            case "year" -> getDataForYear(filterValue, merchantId, startSec, endSec);
            default ->
                    throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, "Lọc theo không hợp lệ");
        };
    }

    /**
     * Retrieves data for a single day.
     * Uses effectiveToday = LocalDate.now() - 1 day.
     *
     * @param filterValue The day string (e.g., "2025-01-20").
     * @param merchantId  The merchant identifier.
     * @param startSec    The start epoch seconds.
     * @param endSec      The end epoch seconds.
     * @return A list containing the financial data item for the day.
     */
    private List<FinancialReportResponse.FinancialDataItem> getDataForSingleDay(
            String filterValue, Long merchantId, Long startSec, Long endSec) {

        AtomicInteger displayOrder = new AtomicInteger(1);
        // Parse the day from the filter value
        LocalDate day = LocalDate.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // Calculate effectiveToday (current date minus 1 day)
        LocalDate effectiveToday = LocalDate.now().minusDays(1);

        // If the requested day is after effectiveToday, return an empty list.
        if (day.isAfter(effectiveToday)) {
            return Collections.emptyList();
        }

        // Retrieve data from the database grouped by day.
        List<FinancialDataItemProjection> projections =
                orderPackageRepository.getRevenueCostGroupByDay(merchantId, startSec, endSec);

        // If no data is found, return a default data item with zero values.
        int displayOrderValue = displayOrder.getAndIncrement();
        if (projections.isEmpty()) {
            return List.of(FinancialReportResponse.FinancialDataItem.builder()
                    .periodName(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .totalRevenue(BigDecimal.ZERO)
                    .totalCost(BigDecimal.ZERO)
                    .totalProfit(BigDecimal.ZERO)
                    .displayOrder(displayOrderValue)
                    .build());
        } else {
            FinancialDataItemProjection p = projections.get(0);
            return List.of(mapProjectionToDataItem(
                    p.getPeriodName(),
                    p.getTotalRevenue(),
                    p.getTotalCost(),
                    p.getTotalProfit(),
                    displayOrderValue,
                    "dd-MM-yyyy"
            ));
        }
    }

    /**
     * Retrieves data for a week specified in the format "yyyy-Wxx".
     * Displays each day of the week, trimming any days beyond effectiveToday.
     * Uses effectiveToday = LocalDate.now() - 1 day.
     *
     * @param filterValue The week string (e.g., "2025-W03").
     * @param merchantId  The merchant identifier.
     * @param startSec    The start epoch seconds.
     * @param endSec      The end epoch seconds.
     * @return A list of financial data items for each day in the week.
     */
    private List<FinancialReportResponse.FinancialDataItem> getDataForWeek(
            String filterValue, Long merchantId, Long startSec, Long endSec) {

        AtomicInteger displayOrder = new AtomicInteger(1);

        // Split the filter value to obtain year and week number.
        String[] parts = filterValue.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        // Compute the Monday of the given week (ISO standard)
        LocalDate startOfWeek = LocalDate.of(year, 1, 1)
                .with(WeekFields.ISO.weekOfYear(), week)
                .with(WeekFields.ISO.dayOfWeek(), 1);
        // Calculate the Sunday (or last day) as Monday + 6 days.
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Calculate effectiveToday (current date minus 1 day)
        LocalDate effectiveToday = LocalDate.now().minusDays(1);

        // Trim the endOfWeek if it exceeds effectiveToday.
        if (endOfWeek.isAfter(effectiveToday)) {
            endOfWeek = effectiveToday;
        }
        // If the week starts after effectiveToday, return empty.
        if (startOfWeek.isAfter(effectiveToday)) {
            return Collections.emptyList();
        }

        // Retrieve data grouped by day.
        List<FinancialDataItemProjection> projections =
                orderPackageRepository.getRevenueCostGroupByDay(merchantId, startSec, endSec);

        // Map each projection to its corresponding date.
        Map<LocalDate, FinancialDataItemProjection> map = new HashMap<>();
        for (FinancialDataItemProjection p : projections) {
            LocalDate d = LocalDate.parse(p.getPeriodName()); // periodName is in "YYYY-MM-DD" format.
            map.put(d, p);
        }

        // Loop from startOfWeek to endOfWeek and build the result list.
        List<FinancialReportResponse.FinancialDataItem> result = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate d = startOfWeek;
        while (!d.isAfter(endOfWeek)) {
            FinancialDataItemProjection p = map.get(d);
            int displayOrderValue = displayOrder.getAndIncrement();
            if (ObjectUtils.isEmpty(p)) {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(d.format(dayFormatter))
                        .totalRevenue(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .totalProfit(BigDecimal.ZERO)
                        .displayOrder(displayOrderValue)
                        .build());
            } else {
                result.add(mapProjectionToDataItem(
                        p.getPeriodName(),
                        p.getTotalRevenue(),
                        p.getTotalCost(),
                        p.getTotalProfit(),
                        displayOrderValue,
                        "dd-MM-yyyy"
                ));
            }
            d = d.plusDays(1);
        }
        return result;
    }

    /**
     * Retrieves data for a month specified in the format "yyyy-MM".
     * Displays each day of the month, trimming any dates beyond effectiveToday.
     * Uses effectiveToday = LocalDate.now() - 1 day.
     *
     * @param filterValue The month string (e.g., "2025-02").
     * @param merchantId  The merchant identifier.
     * @param startSec    The start epoch seconds.
     * @param endSec      The end epoch seconds.
     * @return A list of financial data items for each day in the month.
     */
    private List<FinancialReportResponse.FinancialDataItem> getDataForMonth(
            String filterValue, Long merchantId, Long startSec, Long endSec) {

        AtomicInteger displayOrder = new AtomicInteger(1);

        YearMonth ym = YearMonth.parse(filterValue, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        // Calculate effectiveToday (current date minus 1 day)
        LocalDate effectiveToday = LocalDate.now().minusDays(1);

        // Trim the end of the month if it exceeds effectiveToday.
        if (endOfMonth.isAfter(effectiveToday)) {
            endOfMonth = effectiveToday;
        }
        // If the start of the month is after effectiveToday, return empty.
        if (startOfMonth.isAfter(effectiveToday)) {
            return Collections.emptyList();
        }

        // Retrieve data grouped by day.
        List<FinancialDataItemProjection> projections =
                orderPackageRepository.getRevenueCostGroupByDay(merchantId, startSec, endSec);

        // Map the projections by date.
        Map<LocalDate, FinancialDataItemProjection> map = new HashMap<>();
        for (FinancialDataItemProjection p : projections) {
            LocalDate d = LocalDate.parse(p.getPeriodName());
            map.put(d, p);
        }

        // Loop through each day of the month.
        List<FinancialReportResponse.FinancialDataItem> result = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate d = startOfMonth;
        while (!d.isAfter(endOfMonth)) {
            FinancialDataItemProjection p = map.get(d);
            int displayOrderValue = displayOrder.getAndIncrement();
            if (ObjectUtils.isEmpty(p)) {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(d.format(dayFormatter))
                        .totalRevenue(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .totalProfit(BigDecimal.ZERO)
                        .displayOrder(displayOrderValue)
                        .build());
            } else {
                result.add(mapProjectionToDataItem(
                        p.getPeriodName(),
                        p.getTotalRevenue(),
                        p.getTotalCost(),
                        p.getTotalProfit(),
                        displayOrderValue,
                        "dd-MM-yyyy"
                ));
            }
            d = d.plusDays(1);
        }
        return result;
    }

    /**
     * Retrieves data for a quarter specified in the format "yyyy-Qx".
     * Displays data for the 3 months in the quarter, trimming any months beyond effectiveToday.
     * Uses effectiveToday = LocalDate.now() - 1 day.
     *
     * @param filterValue The quarter string (e.g., "2025-Q1").
     * @param merchantId  The merchant identifier.
     * @param startSec    The start epoch seconds.
     * @param endSec      The end epoch seconds.
     * @return A list of financial data items for each month in the quarter.
     */
    private List<FinancialReportResponse.FinancialDataItem> getDataForQuarter(
            String filterValue, Long merchantId, Long startSec, Long endSec) {

        AtomicInteger displayOrder = new AtomicInteger(1);

        String[] arr = filterValue.split("-Q");
        int year = Integer.parseInt(arr[0]);
        int q = Integer.parseInt(arr[1]);

        // Calculate the starting month of the quarter.
        int startMonth = (q - 1) * 3 + 1;
        YearMonth startYM = YearMonth.of(year, startMonth);
        YearMonth endYM = startYM.plusMonths(2); // 3 months in the quarter

        // Compute start and end dates of the quarter.
        LocalDate startDate = startYM.atDay(1);
        LocalDate endDate = endYM.atEndOfMonth();

        // Calculate effectiveToday (current date minus 1 day)
        LocalDate effectiveToday = LocalDate.now().minusDays(1);

        // Trim the quarter's end date if it exceeds effectiveToday.
        if (endDate.isAfter(effectiveToday)) {
            endDate = effectiveToday;
        }
        // If the quarter starts after effectiveToday, return empty.
        if (startDate.isAfter(effectiveToday)) {
            return Collections.emptyList();
        }

        // Retrieve data grouped by month.
        List<FinancialDataItemProjection> projections =
                orderPackageRepository.getRevenueCostGroupByMonth(merchantId, startSec, endSec);

        // Map each projection by its period name (format "yyyy-MM").
        Map<String, FinancialDataItemProjection> map = new HashMap<>();
        for (FinancialDataItemProjection p : projections) {
            map.put(p.getPeriodName(), p);
        }

        // Iterate through the 3 months of the quarter.
        List<FinancialReportResponse.FinancialDataItem> result = new ArrayList<>();
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

        YearMonth current = startYM;
        for (int i = 0; i < 3; i++) {
            // If the current month is after effectiveToday, stop iteration.
            if (current.atEndOfMonth().isAfter(effectiveToday)) {
                if (current.atDay(1).isAfter(effectiveToday)) break;
            }
            String key = current.format(parseFormatter); // e.g., "YYYY-MM"
            FinancialDataItemProjection p = map.get(key);
            int displayOrderValue = displayOrder.getAndIncrement();
            if (ObjectUtils.isEmpty(p)) {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(current.format(displayFormatter))
                        .totalRevenue(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .totalProfit(BigDecimal.ZERO)
                        .displayOrder(displayOrderValue)
                        .build());
            } else {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(current.format(displayFormatter))
                        .totalRevenue(p.getTotalRevenue())
                        .totalCost(p.getTotalCost())
                        .totalProfit(p.getTotalProfit())
                        .displayOrder(displayOrderValue)
                        .build());
            }
            current = current.plusMonths(1);
        }
        return result;
    }

    /**
     * Retrieves data for a year specified in the format "yyyy".
     * Displays data for 12 months, trimming any months beyond effectiveToday.
     * Uses effectiveToday = LocalDate.now() - 1 day.
     *
     * @param filterValue The year string (e.g., "2025").
     * @param merchantId  The merchant identifier.
     * @param startSec    The start epoch seconds.
     * @param endSec      The end epoch seconds.
     * @return A list of financial data items for each month in the year.
     */
    private List<FinancialReportResponse.FinancialDataItem> getDataForYear(
            String filterValue, Long merchantId, Long startSec, Long endSec) {

        AtomicInteger displayOrder = new AtomicInteger(1);

        int year = Integer.parseInt(filterValue);
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Calculate effectiveToday (current date minus 1 day)
        LocalDate effectiveToday = LocalDate.now().minusDays(1);

        // Trim the end date if it exceeds effectiveToday.
        if (endDate.isAfter(effectiveToday)) {
            endDate = effectiveToday;
        }
        // If the year's start date is after effectiveToday, return empty.
        if (startDate.isAfter(effectiveToday)) {
            return Collections.emptyList();
        }

        // Retrieve data grouped by month.
        List<FinancialDataItemProjection> projections =
                orderPackageRepository.getRevenueCostGroupByMonth(merchantId, startSec, endSec);

        // Map each projection by its period name (format "yyyy-MM").
        Map<String, FinancialDataItemProjection> map = new HashMap<>();
        for (FinancialDataItemProjection p : projections) {
            map.put(p.getPeriodName(), p);
        }

        // Iterate through the 12 months of the year.
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

        List<FinancialReportResponse.FinancialDataItem> result = new ArrayList<>();
        YearMonth current = YearMonth.of(year, 1);
        for (int i = 0; i < 12; i++) {
            // Trim if the current month exceeds effectiveToday.
            if (current.atEndOfMonth().isAfter(effectiveToday)) {
                if (current.atDay(1).isAfter(effectiveToday)) break;
            }
            String key = current.format(parseFormatter); // e.g., "YYYY-MM"
            FinancialDataItemProjection p = map.get(key);
            int displayOrderValue = displayOrder.getAndIncrement();
            if (ObjectUtils.isEmpty(p)) {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(current.format(displayFormatter))
                        .totalRevenue(BigDecimal.ZERO)
                        .totalCost(BigDecimal.ZERO)
                        .totalProfit(BigDecimal.ZERO)
                        .displayOrder(displayOrderValue)
                        .build());
            } else {
                result.add(FinancialReportResponse.FinancialDataItem.builder()
                        .periodName(current.format(displayFormatter))
                        .totalRevenue(p.getTotalRevenue())
                        .totalCost(p.getTotalCost())
                        .totalProfit(p.getTotalProfit())
                        .displayOrder(displayOrderValue)
                        .build());
            }
            current = current.plusMonths(1);
        }
        return result;
    }

    /**
     * Helper method to map a FinancialDataItemProjection to a FinancialReportResponse.FinancialDataItem.
     *
     * @param periodNameRaw The raw period name (e.g., "YYYY-MM-DD").
     * @param revenue       The revenue value.
     * @param cost          The cost value.
     * @param profit        The profit value.
     * @param displayOrder  The display order for this item.
     * @param formatPattern The pattern for formatting the date (e.g., "dd-MM-yyyy").
     * @return The mapped FinancialDataItem.
     */
    private FinancialReportResponse.FinancialDataItem mapProjectionToDataItem(
            String periodNameRaw,
            BigDecimal revenue,
            BigDecimal cost,
            BigDecimal profit,
            int displayOrder,
            String formatPattern
    ) {
        String displayName;
        if (formatPattern.equals("dd-MM-yyyy")) {
            LocalDate d = LocalDate.parse(periodNameRaw);
            displayName = d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } else {
            displayName = periodNameRaw;
        }
        return FinancialReportResponse.FinancialDataItem.builder()
                .periodName(displayName)
                .totalRevenue(FinancialUtils.roundToTwoDecimalPlaces(revenue))
                .totalCost(FinancialUtils.roundToTwoDecimalPlaces(cost))
                .totalProfit(FinancialUtils.roundToTwoDecimalPlaces(profit))
                .displayOrder(displayOrder)
                .build();
    }
}
