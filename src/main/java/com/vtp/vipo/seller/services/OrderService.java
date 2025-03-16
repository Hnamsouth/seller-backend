package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.request.*;
import com.vtp.vipo.seller.common.dto.request.order.OrderFilterRequest;
import com.vtp.vipo.seller.common.dto.request.order.OrderRefuseCancelRequest;
import com.vtp.vipo.seller.common.dto.response.*;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import jakarta.validation.Valid;
import com.vtp.vipo.seller.common.dto.response.order.OrderRefuseCancelResponse;
import com.vtp.vipo.seller.common.dto.response.order.SellerOrderStatusResponse;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Empty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {

    List<OrderStatusResponse> getAllRootOrderStatus();

    Map<String,String>  getAllKeywordSearch();

    OrderResponse getOrderByCode(String code);

    PagingRs getAllByOrderStatus(SearchOrderByKeywordRequest request);

    PagingRs getWaitForPayOrders(SearchOrderByKeywordRequest request);

    PagingRs getAllRevenue(SearchOrderByKeywordRequest request);

    List<OrderPackageResponse> exportRevenue(SearchOrderByKeywordRequest request);

    BigDecimal sumOrdersByMerchant(String orderStatus);

    /**
     * Searches for orders based on various filters and returns a paginated list of results.
     *
     * This method retrieves a list of orders based on the provided search criteria, which includes
     * filters such as order code, buyer name, product name, shipment code, and a date range.
     * The results are paginated, with the specified page number and page size.
     *
     * @return A {@link PagingRs} object containing the paginated list order.
     */
    PagingRs searchOrder(OrderFilterRequest request);

    OrderDetailsResponse getOrderDetails(Long orderId);

    void adjustPriceByTotal(AdjustPriceByTotalRequest request);

    void adjustPriceBySku(AdjustPriceBySkuRequest request);

    List<PriceAdjustmentHistoryResponse> getPriceAdjustmentHistory(Long orderId);

    PriceAdjustmentTotalResponse getPriceAdjustmentTotalDetail(Long orderId, Long adjustmentHistoryId);

    PriceAdjustmentSkuResponse getPriceAdjustmentSkuDetail(Long orderId, Long adjustmentHistoryId);

    PrepareOrderResponse prepareOrder(@Valid PrepareOrderRequest request);

    PrintLabelResponse printLabel(@Valid PrintLabelRequest request);

    /**
     * Refuses an order based on the provided request.
     *
     * This method processes an order refusal request and returns a response indicating the status
     * of the refusal process. It expects a list of order refusal requests that contain necessary details
     * for the refusal action.
     *
     * @param request a list of {@link OrderRefuseCancelRequest} objects containing the details of the orders to be refused.
     * @return A {@link OrderRefuseCancelResponse} object containing the result of the refusal operation.
     * @throws IllegalArgumentException if the request list is empty or invalid.
     */
    OrderRefuseCancelResponse cancelOrder(List<OrderRefuseCancelRequest> request);

    /**
     * Rejects an order based on the provided request.
     *
     * This method processes an order rejection request and returns a response indicating the status
     * of the rejection process. It expects a list of order rejection requests that contain necessary details
     * for the rejection action.
     *
     * @param request a list of {@link OrderRefuseCancelRequest} objects containing the details of the orders to be rejected.
     * @return A {@link OrderRefuseCancelResponse} object containing the result of the rejection operation.
     * @throws IllegalArgumentException if the request list is empty or invalid.
     */
    OrderRefuseCancelResponse refuseOrder(List<OrderRefuseCancelRequest> request);

    /**
     * Retrieves the list of all order statuses for seller management.
     *
     * This method fetches a list of order statuses that are relevant to the seller's management
     * operations. The statuses represent the various stages or conditions of orders that a seller
     * needs to monitor. It provides a comprehensive overview of the current order statuses.
     *
     * @return A list of {@link OrderStatusResponse}
     */
    List<SellerOrderStatusResponse> getAllOrderSellerManagementStatus();

    List<SellerOrderStatusResponse> getAllOrderSellerManagementStatusV2();

    OrderRefuseCancelResponse cancelOrders(List<OrderRefuseCancelRequest> request);

    OrderRefuseCancelResponse rejectOrders(List<OrderRefuseCancelRequest> request);
}
