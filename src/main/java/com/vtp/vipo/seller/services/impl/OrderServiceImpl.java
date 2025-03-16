package com.vtp.vipo.seller.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.business.calculator.SellerOrderPackageCalculator;
import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.OrderConstant;
import com.vtp.vipo.seller.common.constants.RootStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.OrderActivityHistoryEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderStatusEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.BuyerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderProjection;
import com.vtp.vipo.seller.common.dto.*;
import com.vtp.vipo.seller.common.dao.repository.OrderPackageRepository;
import com.vtp.vipo.seller.common.dao.repository.OrderRepository;
import com.vtp.vipo.seller.common.dao.repository.OrderStatusRepository;
import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.ActivityDetailsData;
import com.vtp.vipo.seller.common.dto.PackageProductDto;
import com.vtp.vipo.seller.common.dto.PriceAdjustmentSkuData;
import com.vtp.vipo.seller.common.dto.PriceAdjustmentTotalData;
import com.vtp.vipo.seller.common.dto.request.AdjustPriceBySkuRequest;
import com.vtp.vipo.seller.common.dto.request.AdjustPriceByTotalRequest;
import com.vtp.vipo.seller.common.dto.request.PrepareOrderRequest;
import com.vtp.vipo.seller.common.dto.request.SearchOrderByKeywordRequest;
import com.vtp.vipo.seller.common.dto.request.*;
import com.vtp.vipo.seller.common.dto.request.order.OrderFilterRequest;
import com.vtp.vipo.seller.common.dto.request.order.OrderRefuseCancelRequest;
import com.vtp.vipo.seller.common.dto.request.order.OrderRefuseCancelRequest;
import com.vtp.vipo.seller.common.dto.request.AdjustPriceBySkuRequest;
import com.vtp.vipo.seller.common.dto.request.AdjustPriceByTotalRequest;
import com.vtp.vipo.seller.common.dto.request.SearchOrderByKeywordRequest;
import com.vtp.vipo.seller.common.dto.response.*;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.order.OrderRefuseCancelInfo;
import com.vtp.vipo.seller.common.dto.response.order.OrderRefuseCancelResponse;
import com.vtp.vipo.seller.common.dto.response.order.SellerOrderStatusProjection;
import com.vtp.vipo.seller.common.dto.response.order.*;
import com.vtp.vipo.seller.common.dto.response.order.search.OrderListResponse;
import com.vtp.vipo.seller.common.enumseller.*;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.utils.*;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.mapper.OrderMapper;
import com.vtp.vipo.seller.services.evtp.EvtpOrderService;
import com.vtp.vipo.seller.services.OrderService;
import com.vtp.vipo.seller.services.order.OrderTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service("orderService")
@RequiredArgsConstructor
public class OrderServiceImpl extends BaseService<OrderEntity, Long, OrderRepository> implements OrderService {

    @Value("${evtp.domain.print-label-order}")
    private String printLabelOrderDomainUrl;

    private final OrderStatusRepository orderStatusRepository;

    private final OrderPackageRepository orderPackageRepository;

    private final OrderMapper orderMapper;

    private final OrderActivityHistoryEntityRepository orderActivityHistoryEntityRepository;

    private final PackageProductEntityRepository packageProductEntityRepository;

    private final ProductSellerSkuRepository productSellerSkuRepository;

    private final OrderExtraServiceFeeEntityRepository orderExtraServiceFeeEntityRepository;

    private final OrderTrackingEntityRepository orderTrackingEntityRepository;

    private final OrderShipmentEntityRepository orderShipmentEntityRepository;

    private final OrderPackageSplitEntityRepository orderPackageSplitEntityRepository;

    private final PlatformFeeDetailRepository platformFeeDetailRepository;

    private final EvtpOrderService evtpOrderService;

    private final CarrierEntityRepository carrierEntityRepository;

    private final WarehouseAddressEntityRepository warehouseAddressEntityRepository;

    private final ProductRepository productEntityRepository;

    private final PackageLogEntityRepository packageLogEntityRepository;

    private final WardRepository wardRepository;

    private final DistrictRepository districtRepository;

    private final MakeShipmentLogEntityRepository makeShipmentLogEntityRepository;

    private final OrderTrackingService orderTrackingService;

    private final MerchantRepository merchantRepository;

    @Override
    public List<OrderStatusResponse> getAllRootOrderStatus() {
        VipoUserDetails user = getCurrentUser();
        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findALlRootStatus();
        List<OrderStatusResponse> res = orderStatusEntities.stream().map(item -> {

            List<OrderStatusEntity> chillOrderStatus = orderStatusRepository.findAllByRootCode(item.getStatusCode());
            List<String> chillStatusCodes = chillOrderStatus.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());
            long totalOrderByStatus = 0;
            if (item.getStatusCode().equals(RootStatusEnum.WAIT_FOR_PAY.getCode())) {
                totalOrderByStatus = repo.countByOrderStatus(chillStatusCodes, user.getId());
            } else {
                totalOrderByStatus = orderPackageRepository.countByOrderStatusInAndMerchantId(chillStatusCodes, user.getId());
            }
            return new OrderStatusResponse(item.getStatusCode(), item.getName(), totalOrderByStatus);
        }).collect(Collectors.toList());
        res.add(new OrderStatusResponse("0", "Tất cả", res.stream().mapToLong(OrderStatusResponse::getTotalOrder).sum()));
        res.sort(Comparator.comparing(OrderStatusResponse::getCode));
        return res;
    }

    @Override
    public Map<String, String> getAllKeywordSearch() {
        return Constants.LIST_KEYWORD_SEARCH;
    }

    @Override
    public OrderResponse getOrderByCode(String code) {
        VipoUserDetails user = getCurrentUser();
        OrderResponse response = repo.findOrdersByOrderCodeAndMerchant(code, user.getId());
        if (!ObjectUtils.isEmpty(response)) {
            response.setChildren(repo.findOrderPackageByCodeAndMerchantId(user.getId(), response.getOrderId()));
        }
        return response;
    }


    @Override
    public PagingRs getAllByOrderStatus(SearchOrderByKeywordRequest request) {
        VipoUserDetails user = getCurrentUser();
        PagingRs pagingRs = new PagingRs();
        Page<OrderPackageResponse> myOrderResponsePage = null;

        if (RootStatusEnum.WAIT_FOR_PAY.getCode().equals(request.getOrderStatus())) {
            throw new VipoNotFoundException(BaseExceptionConstant.KEY_SEARCH_NOTFOUND);
        }

        OrderStatusEntity parentStatus = orderStatusRepository.findByStatusCodeAndRootCode(request.getOrderStatus(), null);
        if (parentStatus == null) {
            throw new VipoNotFoundException();
        }

        List<OrderStatusEntity> chillOrderStatus = statusCodes(List.of(parentStatus.getStatusCode()));
        if (!chillOrderStatus.isEmpty()) {
            List<String> chillStatusCodes = chillOrderStatus.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());
            myOrderResponsePage = getPageOfOrderPackages(chillStatusCodes, user.getId(), request);
            myOrderResponsePage.getContent().forEach(item -> {
                item.setKey(item.getOrderCode());
                if (RootStatusEnum.CANCELLED.getCode().equals(parentStatus.getStatusCode()) && item.getPaymentTime() != null) {
                    item.setReturnAmount(item.getPrepayment());
                } else {
                    item.setReturnAmount(BigDecimal.ZERO);
                }
            });

            int total = myOrderResponsePage.getTotalPages();
            pagingRs.setCurrentPage(Math.min(request.getPageNo(), total));
            pagingRs.setTotalCount(total);
            pagingRs.setData(myOrderResponsePage.getContent());
        }

        return pagingRs;
    }

    @Override
    public PagingRs getWaitForPayOrders(SearchOrderByKeywordRequest request) {
        VipoUserDetails user = getCurrentUser();
        Pageable pageRequest = PageRequest.of(request.getPageNo() - 1, request.getPageSize());

        List<OrderStatusEntity> chillOrderStatus = orderStatusRepository.findAllByRootCode(RootStatusEnum.WAIT_FOR_PAY.getCode());
        if (CollectionUtils.isEmpty(chillOrderStatus)) {
            return new PagingRs(); // or throw exception as needed
        }

        List<String> waitToPayStatusCodes = chillOrderStatus.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());
        Page<OrderResponse> waitingPayOrderResponsePage = repo.getOrderByFilters(
                waitToPayStatusCodes,
                DateUtils.convertMilTimeToSecond(request.getStartDate()),
                DateUtils.convertMilTimeToSecond(request.getEndDate()),
                user.getId(),
                request.getSearchQuery(),
                pageRequest);

        List<OrderResponse> waitingPayOrderResponses = waitingPayOrderResponsePage.getContent().stream()
                .map(order -> {
                    order.setKey(order.getOrderCode());
                    List<OrderPackageResponse> orderPackagesOfOrder = repo.getListOrderPackage(
                            waitToPayStatusCodes,
                            order.getOrderId(),
                            user.getId());
                    if (!CollectionUtils.isEmpty(orderPackagesOfOrder)) {
                        orderPackagesOfOrder.forEach(item -> item.setSpec(ConvertSpecProd.buildSpecList(item.getSkuInfo())));
                        order.setChildren(orderPackagesOfOrder);
                        BigDecimal prepayment = BigDecimal.ZERO;
                        int count = 1;
                        for (OrderPackageResponse orderPackage : orderPackagesOfOrder) {
                            orderPackage.setKey(order.getKey() + "-" + count++);
                            prepayment = prepayment.add(orderPackage.getPrepayment());
                        }
                        order.setPrepayment(prepayment);
                    }
                    return order;
                })
                .collect(Collectors.toList());

        PagingRs page = new PagingRs();
        int total = waitingPayOrderResponsePage.getTotalPages();
        page.setCurrentPage(Math.min(request.getPageNo(), total));
        page.setTotalCount(total);
        page.setData(waitingPayOrderResponses);
        return page;
    }

    @Override
    public PagingRs getAllRevenue(SearchOrderByKeywordRequest request) {
        VipoUserDetails user = getCurrentUser();
        PagingRs pagingRs = new PagingRs();
        Page<OrderPackageResponse> myOrderResponsePage = null;
        if (!ObjectUtils.isEmpty(request.getOrderStatus()) && request.getOrderStatus().equals("0")) {
            if (RootStatusEnum.WAIT_FOR_PAY.getCode().equals(request.getOrderStatus())) {
                throw new VipoNotFoundException(BaseExceptionConstant.KEY_SEARCH_NOTFOUND);
            }

            List<OrderStatusEntity> chillOrderStatus = statusCodes(rootStatusWithUnearnedRevenue());
            List<OrderStatusEntity> listStatusCancelled = statusCodes(List.of(RootStatusEnum.CANCELLED.getCode()));
            if (CollectionUtils.isEmpty(listStatusCancelled)) {
                throw new VipoNotFoundException();
            }
            List<String> listCodeCancelled = listStatusCancelled.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());

            if (!chillOrderStatus.isEmpty()) {
                List<String> chillStatusCodes = chillOrderStatus.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());
                myOrderResponsePage = getPageOfOrderPackages(chillStatusCodes, user.getId(), request);
                myOrderResponsePage.getContent().forEach(item -> {
                    item.setKey(item.getOrderCode());
                    if (listCodeCancelled.contains(item.getOrderStatus()) && item.getPaymentTime() != null) {
                        item.setReturnAmount(item.getPrepayment());
                    } else {
                        item.setReturnAmount(BigDecimal.ZERO);
                    }
                });

                int total = myOrderResponsePage.getTotalPages();
                pagingRs.setCurrentPage(Math.min(request.getPageNo(), total));
                pagingRs.setTotalCount(total);
                pagingRs.setData(myOrderResponsePage.getContent());
            }
        }

        return pagingRs;
    }

    @Override
    public List<OrderPackageResponse> exportRevenue(SearchOrderByKeywordRequest request) {
        VipoUserDetails user = getCurrentUser();
        List<OrderStatusEntity> childOrderStatus = statusCodes(rootStatusWithUnearnedRevenue());
        List<OrderStatusEntity> cancelledStatus = statusCodes(List.of(RootStatusEnum.CANCELLED.getCode()));

        if (cancelledStatus.isEmpty()) {
            throw new VipoNotFoundException();
        }

        List<String> cancelledStatusCodes = cancelledStatus.stream()
                .map(OrderStatusEntity::getStatusCode)
                .collect(Collectors.toList());

        List<String> childStatusCodes = childOrderStatus.stream()
                .map(OrderStatusEntity::getStatusCode)
                .collect(Collectors.toList());

        Page<OrderPackageResponse> orderPackagesPage = getPageOfOrderPackages(childStatusCodes, user.getId(), request);
        orderPackagesPage.getContent().forEach(item -> {
            item.setKey(item.getOrderCode());
            if (cancelledStatusCodes.contains(item.getOrderStatus()) && item.getPaymentTime() != null) {
                item.setReturnAmount(item.getPrepayment());
            } else {
                item.setReturnAmount(BigDecimal.ZERO);
            }
        });

        return orderPackagesPage.getContent();
    }

    @Override
    public BigDecimal sumOrdersByMerchant(String status) {
        VipoUserDetails user = getCurrentUser();
        List<String> chillStatusCodes = null;

        if ("0".equals(status) || "1".equals(status)) {
            List<OrderStatusEntity> chillOrderStatus = statusCodes(rootStatusWithUnearnedRevenue());
            if (CollectionUtils.isEmpty(chillOrderStatus)) {
                throw new VipoNotFoundException();
            }
            chillStatusCodes = chillOrderStatus.stream().map(OrderStatusEntity::getStatusCode).collect(Collectors.toList());
        }

        if ("0".equals(status)) {
            return repo.sumOrderByMerchant(chillStatusCodes, user.getId());
        }

        return BigDecimal.ZERO;
    }


    @Override
    public PagingRs searchOrder(OrderFilterRequest request) {

        VipoUserDetails merchantInfo = getCurrentUser();

        /* Phase 6: Allow foreigner merchant to see the order packages */
//        if(!isVietnamMerchant(merchantInfo))
//            throw new VipoBusinessException(OrderConstant.VIETNAM_MERCHANT_ONLY);

        Pageable pageRequest = PageRequest.of(request.getPageNum(), request.getPageSize());
        PagingRs res = new PagingRs();
        List<OrderListResponse> orderListRes = new ArrayList<>();

        Page<OrderProjection> orderList = null;
        if (!DataUtils.isNullOrEmpty(request.getStatus())) {
            List<SellerOrderStatus> sellerOrderStatus = new ArrayList<>();
            sellerOrderStatus.add(request.getStatus());
            if (request.getStatus().equals(SellerOrderStatus.ORDER_PREPARED)) {
                sellerOrderStatus.add(SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS);
            }

            orderList =  orderPackageRepository.findAllByMerchantV2(OrderFilterTab.getValueFromEnum(request.getTabCode()), request.getOrderCode(), request.getBuyerName(), request.getProductName(), request.getShipmentCode(), request.getStartDate(), request.getEndDate(),
                    merchantInfo.getId(), sellerOrderStatus.stream().map(Enum::toString).toList(), pageRequest);
        }else{
            orderList = orderPackageRepository.findAllByMerchant(OrderFilterTab.getValueFromEnum(request.getTabCode()), request.getOrderCode(), request.getBuyerName(), request.getProductName(), request.getShipmentCode(), request.getStartDate(), request.getEndDate(),
                    merchantInfo.getId(), pageRequest);
        }

        orderList.getContent().forEach(od -> {

            // Convert each OrderProjection object into an OrderListRes object
            OrderListResponse order = orderMapper.orderProjectionToRes(od);

            // Convert the list of package products from JSON into a List<PackageProductDto>
            List<PackageProductDto> packageProductDtos = JsonMapperUtils.convertJsonToObject(od.getPackageProductList(), new TypeReference<>() {
            });

            // If the list of products is not empty, process each product
            if (!CollectionUtils.isEmpty(packageProductDtos)) {
                // Set the list of products for the OrderListRes object
                order.setPackageProducts(packageProductDtos.stream().map(orderMapper::productDtoToPackageProduct).toList());
                order.setQuantityProduct(packageProductDtos.size());
                order.setProductAmount(BigDecimal.valueOf(order.getPackageProducts().stream().mapToDouble(o -> o.getSkuPrice().doubleValue()).sum()));
            }

            // Add the OrderListRes object to the result list
            orderListRes.add(order);
        });

        res.setTotalCount(orderList.getTotalElements());
        res.setData(orderListRes);
        res.setCurrentPage(request.getPageNum());
        return res;
    }

    @Override
    public OrderDetailsResponse getOrderDetails(Long orderId) {
        // Retrieve the order package entity based on the given order ID, throwing an exception if not found
        OrderPackageEntity orderPackage = orderPackageRepository.findById(orderId)
                .orElseThrow(() -> new VipoBusinessException(BaseExceptionConstant.NOT_FOUND_ENTITY_DESCRIPTION));

// Retrieve the list of package products associated with the order ID
        List<PackageProductEntity> packageProductEntities = packageProductEntityRepository.findByOrderPackage_Id(orderId);

// Retrieve the list of extra service fees for the given order ID
        List<OrderExtraServiceFeeEntity> orderExtraServiceFeeEntities = orderExtraServiceFeeEntityRepository.findByOrderId(orderId);

// Fetch the price adjustment history for the given order ID
        List<PriceAdjustmentHistoryResponse> priceAdjustmentHistoryResponses = getPriceAdjustmentHistory(orderId);

// Retrieve order tracking details for the given order ID
        List<OrderTrackingEntity> orderTrackingEntities = SellerOrderStatus.WAITING_FOR_PAYMENT.equals(orderPackage.getSellerOrderStatus()) ?  new ArrayList<>(): orderTrackingEntityRepository.findByPackageId(orderId);

// Retrieve the shipment details for the order package, if available
        Optional<OrderShipmentEntity> orderShipmentEntityOptional = orderShipmentEntityRepository.findByPackageId(orderId);

// Retrieve the platform fee details for the given order ID
        List<PlatformFeeDetailEntity> platformFeeDetailEntities = platformFeeDetailRepository.findByPackageId(orderId);

// Initialize variables for calculating total product price, service fees, platform fees, etc.
        BigDecimal totalPriceService = orderExtraServiceFeeEntities.stream().map(OrderExtraServiceFeeEntity::getComponentFee).reduce(BigDecimal.ZERO, BigDecimal::add);
        // Calcuate platform fee and platform fee details
        BigDecimal platformFee = BigDecimal.ZERO;
        List<OrderDetailsResponse.PlatformDetail> platformFeeDetails = new ArrayList<>();

        // get product platform discount
        OrderDetailsResponse.PlatformDetail productPlatformDiscount = getProductPlatformDiscount(packageProductEntities);
        // add product platform discount to platform fee details and platform fee
        if(ObjectUtils.isNotEmpty(productPlatformDiscount)){
            platformFeeDetails.add(productPlatformDiscount);
            platformFee = platformFee.add(productPlatformDiscount.getAmount());
        }

        if (!CollectionUtils.isEmpty(platformFeeDetailEntities)) {
            platformFee = platformFee.add(platformFeeDetailEntities.stream().map(PlatformFeeDetailEntity::getFeeValue).reduce(BigDecimal.ZERO, BigDecimal::add));
            platformFeeDetails.addAll(platformFeeDetailEntities.stream().map(p -> OrderDetailsResponse.PlatformDetail.builder()
                    .amount(p.getFeeValue())
                    .desc(p.getFeeDescription())
                    .name(p.getFeeName())
                    .build()).toList());
        }

        // Calculate the total price adjustment amount
        BigDecimal totalAdjustmentPrice = NumUtils.minusBigDecimals(orderPackage.getOriginPrice(), orderPackage.getPrice());
        BigDecimal shipmentFee = DataUtils.getBigDecimal(orderPackage.getTotalDomesticShippingFee()).equals(BigDecimal.ZERO) ? orderPackage.getTotalShippingFee() : orderPackage.getTotalDomesticShippingFee();
        boolean isPaid = !ObjectUtils.isEmpty(orderPackage.getPaymentTime()) && orderPackage.getPaymentTime() > 0;
// Initialize the list of package products to be included in the response
        List<OrderDetailsResponse.PackageProduct> packageProducts = new ArrayList<>();

// If price adjustment history is not empty, process the price adjustment data
        if (!CollectionUtils.isEmpty(priceAdjustmentHistoryResponses)) {
            // If the first entry is of type PriceAdjustmentSkuData, process the individual SKU price adjustments
            if (priceAdjustmentHistoryResponses.get(0).getDetailsData() instanceof PriceAdjustmentSkuData) {
                PriceAdjustmentSkuData data = (PriceAdjustmentSkuData) priceAdjustmentHistoryResponses.get(0).getDetailsData();
                packageProducts.addAll(getPackageProductList(packageProductEntities, data.getSkus()));
            }
        }
        // check if the order is in the status of shipment connection success, then set the seller order status to prepared
        SellerOrderStatus sellerOrderStatus = SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.equals(orderPackage.getSellerOrderStatus()) ? SellerOrderStatus.ORDER_PREPARED : orderPackage.getSellerOrderStatus();

        List<LogisticsTrackInfoVO> logisticsTrackInfoVOS = orderTrackingService.getLogisticTrackInfo(orderPackage);
        /* front end does not interact with statusDesc so I did this way */
        logisticsTrackInfoVOS.stream().filter(logisticsTrackInfoVO -> StringUtils.isNotBlank(logisticsTrackInfoVO.getStatusDesc()))
                .forEach(
                        logisticsTrackInfoVO -> logisticsTrackInfoVO.setContext(logisticsTrackInfoVO.getStatusDesc() + " - " +  logisticsTrackInfoVO.getContext())
        );
        logisticsTrackInfoVOS = SellerOrderStatus.WAITING_FOR_PAYMENT.equals(orderPackage.getSellerOrderStatus()) ?  new ArrayList<>(): logisticsTrackInfoVOS;

        // Create the response object for order details
        OrderDetailsResponse response = OrderDetailsResponse.builder()
                .orderId(orderId) // Set the order ID
                .orderStatus(orderPackage.getOrderStatus()) // Set the order status
                .sellerOrderStatus(sellerOrderStatus) // Set the seller's order status
                .sellerOrderStatusDesc(sellerOrderStatus.getDescription()) // Set the seller's order status
                .orderCode(orderPackage.getOrderCode()) // Set the order code
                .createdAt(orderPackage.getCreateTime()) // Set the order creation timestamp
                .notes(orderPackage.getNoteSeller()) // Set any seller-provided notes
                .totalProductPrice(orderPackage.getOriginPrice()) // Set the total product price
                .totalShippingFee(shipmentFee) // Set the total shipping fee
                .refCode(orderPackage.getRefCode())
                // Calculate the total price the buyer has to pay (product price + shipping fee - price adjustments)
                .totalPrice(
                        NumUtils.minusBigDecimals(
                                NumUtils.sumBigDecimals(orderPackage.getOriginPrice(), shipmentFee, totalPriceService),
                                totalAdjustmentPrice
                        )
                )
                .totalPriceAdjustment(totalAdjustmentPrice) // Set the total price adjustment
                .actions(OrderAction.getActionsFromStatus(orderPackage.getSellerOrderStatus())) // Set the list of available actions based on the seller's order status
                // Set customer information (name, phone, full address)
                .customerInfo(OrderDetailsResponse.CustomerInfo.builder()
                        .name(orderPackage.getReceiverName())
                        .phone(DataUtils.hideStringValue(orderPackage.getReceiverPhone(), 0, 2)) // Mask phone number
                        .fullAddress(orderPackage.getReceiverAddressDetail()) // Set the full shipping address
                        .build())
                // Set the list of package products, if available
                .packageProducts(!CollectionUtils.isEmpty(packageProducts) ? packageProducts : getPackageProductList(packageProductEntities, null))
                .totalProduct(packageProductEntities.stream().collect(Collectors.groupingBy(PackageProductEntity::getProductId)).size())
                // Set the list of extra service fees for domestic services
                .extraServiceDomesticInfo(orderExtraServiceFeeEntities.stream()
                        .filter(e -> e.getOrderExtraService().getServiceType().equals(OrderExtraServiceType.domestic))
                        .map(esf -> OrderDetailsResponse.ExtraServiceInfo.builder()
                                .extraServiceFeeId(esf.getId())
                                .extraServiceId(esf.getOrderExtraService().getId())
                                .name(esf.getOrderExtraService().getServiceName())
                                .fee(esf.getComponentFee())
                                .description(esf.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                // Set the list of extra service fees for international services
                .extraServiceInternationalInfo(orderExtraServiceFeeEntities.stream()
                        .filter(e -> e.getOrderExtraService().getServiceType().equals(OrderExtraServiceType.international))
                        .map(esf -> OrderDetailsResponse.ExtraServiceInfo.builder()
                                .extraServiceFeeId(esf.getId())
                                .extraServiceId(esf.getOrderExtraService().getId())
                                .name(esf.getOrderExtraService().getServiceName())
                                .fee(esf.getComponentFee())
                                .description(esf.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                // Set the payment information (prepayment, paid amount, payment time, etc.)
                .paymentInfo(OrderDetailsResponse.PaymentInfo.builder()
                        .prepayment(orderPackage.getPrePayment()) // Prepayment amount
                        .paidAmount(isPaid ? orderPackage.getPrePayment() : BigDecimal.ZERO) // Paid amount
                        .paymentTime(DateUtils.convertMilTimeToSecond(orderPackage.getPaymentTime())) // Payment timestamp
                        .paymentMethod(orderPackage.getOrder().getPaymentMethod()) // Payment method used
                        .paymentMessage(PaymentMethod.getMessage(orderPackage.getOrder().getPaymentMethod()))
                        .isPaid(isPaid) // Whether the order is fully paid
                        .build())
                // Set revenue details (product price, platform fees, negotiated amount, and estimated revenue)
                .revenue(OrderDetailsResponse.Revenue.builder()
                        .totalProductPrice(orderPackage.getOriginPrice()) // Total product price
                        .platformFeeMap(platformFeeDetails) // Platform fee details
                        .negotiatedAmount(totalAdjustmentPrice) // Negotiated amount
                        // Calculate the estimated revenue by subtracting platform fees, service fees, and price adjustments
                        .estimatedRevenue(NumUtils.minusBigDecimals(orderPackage.getPrice(), platformFee))
                        .build())
                // Set the price adjustment history details
                .priceAdjustmentHistory(priceAdjustmentHistoryResponses.stream()
                        .map(pah -> OrderDetailsResponse.PriceAdjustmentHistory.builder()
                                .id(String.valueOf(pah.getId()))
                                .orderId(pah.getOrderId())
                                .type(pah.getType())
                                .createdBy(pah.getCreatedBy())
                                .createdAt(Optional.ofNullable(pah.getCreatedAt()).map(DateUtils::getTimeInSeconds).orElse(0L))
                                .build()).sorted(Comparator.comparing(OrderDetailsResponse.PriceAdjustmentHistory::getCreatedAt).reversed())
                        .collect(Collectors.toList()))
                // Set the logistics tracking details
//                .logisticsTrackInfo(orderTrackingEntities.stream()
//                        .map(oti -> OrderDetailsResponse.LogisticsTrackInfo.builder()
//                                .context(oti.getContent())
//                                .status(oti.getOrderStatus())
//                                .statusDesc(null) // Placeholder for status description (needs review)
//                                .time(oti.getTime())
//                                .build()).sorted(Comparator.comparing(OrderDetailsResponse.LogisticsTrackInfo::getTime).reversed())
//                        .collect(Collectors.toList()))
                .logisticsTrackInfo(logisticsTrackInfoVOS)
                // Set the shipment details (carrier, shipment code, and warehouse address)
                .shipmentInfo(orderShipmentEntityOptional.map(os -> OrderDetailsResponse.ShipmentInfo.builder()
                        .carrierName(os.getCarrier().getName()) // Carrier name
                        .shipmentCode(os.getShipmentCode()) // Shipment code
                        .shipmentMessage(os.getCreateOrderMessage()) // Shipment name
                        .pickupAddress(OrderDetailsResponse.WarehouseAddressInfo.builder()
                                .id(os.getWarehouseAddress().getId()) // Warehouse address ID
                                .fullAddress(os.getWarehouseAddress().getFullAddress()) // Full address
                                .name(os.getWarehouseAddress().getName()) // Warehouse name
                                .phone(os.getWarehouseAddress().getPhoneNumber()) // Phone number
                                .build())
                        .packageSplits(orderPackageSplitEntityRepository.findByPackageId(orderId).stream()
                                .map(split -> OrderDetailsResponse.PackageSplit.builder()
                                        .logisticCode(split.getLogisticsCode())
                                        .logisticNo(split.getLogisticsNo())
                                        .logisticStatus(split.getStatus())
                                        .description(split.getStatusDesc())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()).orElse(null))
                .build();

// Return the constructed order details response
        return response;
    }


    private OrderDetailsResponse.PlatformDetail getProductPlatformDiscount(List<PackageProductEntity> packageProducts) {
        // Calculate the total platform discount for the given product IDs
        BigDecimal discount = SellerOrderPackageCalculator.calculateProductPlatformDiscount(packageProducts);

        if(discount.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }

        return OrderDetailsResponse.PlatformDetail.builder()
                .name(OrderConstant.PRODUCT_PLATFORM_DISCOUNT)
                .amount(discount)
                .build();
    }

    @Transactional
    @Override
    public void adjustPriceByTotal(AdjustPriceByTotalRequest request) {
        log.info("[adjustPriceByTotal] Adjusting price by total for order ID: {}", request.getOrderId());
        VipoUserDetails user = getCurrentUser();

        // Find the OrderPackage by orderId
        OrderPackageEntity orderPackage = orderPackageRepository.findByIdAndMerchantId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND));

        // Check if the seller order status is WAITING_FOR_PAYMENT
        if (!orderPackage.getSellerOrderStatus().equals(SellerOrderStatus.WAITING_FOR_PAYMENT)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, OrderConstant.STATUS_ORDER_CHANGED);
        }

        // Check if the adjusted amount is less than or equal to the total price
        if (request.getAdjustedAmount().compareTo(orderPackage.getOriginPrice()) > 0) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.ADJUSTED_AMOUNT_GREATER_THAN_TOTAL_PRICE);
        }

        // Clone the current state of the order package for logging purposes
        OrderPackageEntity orderPackageBeforeAdjustment = deepCloneOrderPackage(orderPackage);

        // Reset any previous adjustments for this order
        resetPreviousAdjustment(orderPackage, PriceAdjustmentType.TOTAL);

        // Calculate the order package prices before the new adjustment
        BigDecimal priceBeforeAdjustment = DataUtils.getBigDecimal(orderPackage.getOriginPrice());
        BigDecimal totalPriceBeforeAdjustment = SellerOrderPackageCalculator.calculateTotalPriceOrderPackage(orderPackage);
        log.info("[adjustPriceByTotal] priceBeforeAdjustment: {}, totalPriceBeforeAdjustment: {}, originalPrice: {}, prepayment: {}",
                priceBeforeAdjustment, totalPriceBeforeAdjustment, orderPackage.getOriginPrice(), orderPackage.getPrePayment());

        // Update the order package with the new adjusted amount and refCode
        orderPackage.setRefCode(request.getRefCode());
        orderPackage.setIsChangePrice(true);
        orderPackage.setNegotiatedAmount(request.getAdjustedAmount());

        // Calculate total price order package after adjustment
        BigDecimal priceAfterAdjustment = priceBeforeAdjustment.subtract(request.getAdjustedAmount());
        BigDecimal totalPriceAfterAdjustment = SellerOrderPackageCalculator.calculateTotalPriceOrderPackage(orderPackage);
        BigDecimal newPrepayment = priceAfterAdjustment
                .multiply(new BigDecimal(Constants.PERCENTAGE_PREPAYMENT));
        log.info("[adjustPriceByTotal] priceAfterAdjustment: {}, totalPriceAfterAdjustment: {}, originalPrice: {}, newPrepayment: {}",
                priceAfterAdjustment, totalPriceAfterAdjustment, orderPackage.getOriginPrice(), newPrepayment);
        orderPackage.setPrice(priceAfterAdjustment);
        orderPackage.setTotalPrice(totalPriceAfterAdjustment);
        orderPackage.setPrePayment(newPrepayment);

        // Save the updated state of the order package
        OrderPackageEntity orderPackageAfterAdjustment = orderPackageRepository.save(orderPackage);

        // Save order after update order package
        updateOrderAfterAdjustmentPrice(orderPackageAfterAdjustment.getOrder());

        // Save the adjustment history to the order_activity_history table
        saveAdjustPriceByTotalOrderActivityHistory(
                request,
                orderPackageBeforeAdjustment,
                orderPackageAfterAdjustment,
                priceBeforeAdjustment,
                priceAfterAdjustment
        );
    }

    private void saveAdjustPriceByTotalOrderActivityHistory(AdjustPriceByTotalRequest request,
                                                            OrderPackageEntity orderPackageBeforeAdjustment,
                                                            OrderPackageEntity orderPackageAfterAdjustment,
                                                            BigDecimal priceBeforeAdjustment,
                                                            BigDecimal priceAfterAdjustment) {
        // Common fields
        String key = UUID.randomUUID().toString();
        Long orderId = request.getOrderId();
        PriceAdjustmentTotalData details = PriceAdjustmentTotalData.builder()
                .type(PriceAdjustmentType.TOTAL)
                .adjustedAmount(request.getAdjustedAmount())
                .priceBeforeAdjustment(priceBeforeAdjustment)
                .priceAfterAdjustment(priceAfterAdjustment)
                .refCode(request.getRefCode())
                .build();

        // Common states
        String beforeWaitForPayState = JsonMapperUtils.writeValueAsString(
                updateStatusOrderPackage(orderPackageBeforeAdjustment, BuyerOrderStatus.WAIT_FOR_PAY_ORDER));
        String afterWaitForAdjustState = JsonMapperUtils.writeValueAsString(
                updateStatusOrderPackage(orderPackageAfterAdjustment, BuyerOrderStatus.WAIT_FOR_ADJUST_PRICE));
        String afterWaitForPayState = JsonMapperUtils.writeValueAsString(
                updateStatusOrderPackage(orderPackageAfterAdjustment, BuyerOrderStatus.WAIT_FOR_PAY_ORDER));

        // First history
        PriceAdjustmentMetadata firstMetadata = PriceAdjustmentMetadata.builder()
                .key(key)
                .beforeStatus(BuyerOrderStatus.WAIT_FOR_PAY_ORDER.getOrderStatusCode())
                .afterStatus(BuyerOrderStatus.WAIT_FOR_ADJUST_PRICE.getOrderStatusCode())
                .visible(false)
                .build();

        OrderActivityHistoryEntity firstHistory = OrderActivityHistoryEntity.builder()
                .orderId(orderId)
                .type(ActivityType.PRICE_ADJUSTMENT)
                .details(details)
                .beforeState(beforeWaitForPayState)
                .afterState(afterWaitForAdjustState)
                .metadata(firstMetadata)
                .build();

        // Second history
        PriceAdjustmentMetadata secondMetadata = PriceAdjustmentMetadata.builder()
                .key(key)
                .beforeStatus(BuyerOrderStatus.WAIT_FOR_ADJUST_PRICE.getOrderStatusCode())
                .afterStatus(BuyerOrderStatus.WAIT_FOR_PAY_ORDER.getOrderStatusCode())
                .visible(true)
                .build();

        OrderActivityHistoryEntity secondHistory = OrderActivityHistoryEntity.builder()
                .orderId(orderId)
                .type(ActivityType.PRICE_ADJUSTMENT)
                .details(details)
                .beforeState(afterWaitForAdjustState)
                .afterState(afterWaitForPayState)
                .metadata(secondMetadata)
                .build();

        // Save histories
        orderActivityHistoryEntityRepository.saveAll(List.of(firstHistory, secondHistory));
        log.info("[adjustPriceByTotal] Price adjusted successfully for order ID: {}", orderId);
    }

    private boolean isSameAmount(BigDecimal adjustedAmount, BigDecimal negotiatedAmount) {
        if (ObjectUtils.isEmpty(adjustedAmount) || ObjectUtils.isEmpty(negotiatedAmount)) {
            return false;
        }
        return adjustedAmount.compareTo(negotiatedAmount) == 0;
    }

    private OrderPackageEntity updateStatusOrderPackage(OrderPackageEntity orderPackage, BuyerOrderStatus status) {
        OrderPackageEntity cloneOrderPackage = deepCloneOrderPackage(orderPackage);
        cloneOrderPackage.setOrderStatus(status.getOrderStatusCode());
        return cloneOrderPackage;
    }

    @Transactional
    @Override
    public void adjustPriceBySku(AdjustPriceBySkuRequest request) {
        log.info("Adjusting price by SKU for order ID: {}", request.getOrderId());
        VipoUserDetails user = getCurrentUser();

        // Fetch the orderPackage based on orderId
        OrderPackageEntity orderPackage = orderPackageRepository.findByIdAndMerchantId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND));

        // Verify that the seller order status is WAITING_FOR_PAYMENT
        if (!SellerOrderStatus.WAITING_FOR_PAYMENT.equals(orderPackage.getSellerOrderStatus())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    OrderConstant.STATUS_ORDER_CHANGED);
        }

        // Retrieve packageProducts from orderPackage (rather than querying the DB)
        List<PackageProductEntity> packageProducts = new ArrayList<>(orderPackage.getSku());
        if (packageProducts.isEmpty()) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.NO_SKU_FOUND);
        }

        // Check whether each SKU in the request exists in orderPackage
        List<String> skuIdsFromRequest = request.getSkus().stream()
                .map(AdjustPriceBySkuRequest.SkuItem::getSkuId)
                .toList();

        // Ensure that every requested SKU ID is present in packageProducts
        for (String skuId : skuIdsFromRequest) {
            boolean skuExists = packageProducts.stream()
                    .anyMatch(product -> skuId.equals(product.getLazbaoSkuId()));
            if (!skuExists) {
                throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                        Constants.SKU_ID_NOT_FOUND_IN_ORDER_PACKAGE.replace("{skuId}", skuId));
            }
        }

        // Validate condition: "All adjustedAmount < skuPrice"
        boolean isValidPrice = request.getSkus().stream().allMatch(skuItem -> {
            // Locate the matching PackageProductEntity by SKU
            PackageProductEntity product = packageProducts.stream()
                    .filter(p -> skuItem.getSkuId().equals(p.getLazbaoSkuId()))
                    .findFirst()
                    .orElseThrow(() -> new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                            Constants.SKU_ID_NOT_FOUND_IN_ORDER_PACKAGE.replace("{skuId}", skuItem.getSkuId())));

            // Derive the effective price: use negotiatedAmount if it exists, otherwise use skuPrice
            BigDecimal skuPrice = (ObjectUtils.isEmpty(product.getNegotiatedAmount())
                    || product.getNegotiatedAmount().compareTo(BigDecimal.ZERO) == 0)
                    ? product.getSkuPrice()
                    : product.getNegotiatedAmount();

            // Check whether adjustedAmount < skuPrice
            return skuItem.getAdjustedAmount().compareTo(skuPrice) <= 0;
        });

        if (!isValidPrice) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.ADJUSTED_AMOUNT_GREATER_THAN_SKU_PRICE);
        }

        // Clone current packageProducts state (before adjustment) for logging
        List<PackageProductEntity> packageProductsBeforeAdjustment = deepClonePackageProduct(packageProducts);

        // Reset any previous price adjustment of type SKU for this order (in-memory)
        resetPreviousAdjustment(orderPackage, PriceAdjustmentType.SKU);

        // Calculate the order package prices before the new adjustment
        BigDecimal priceBeforeAdjustment = DataUtils.getBigDecimal(orderPackage.getOriginPrice());
        BigDecimal totalPriceBeforeAdjustment = SellerOrderPackageCalculator.calculateTotalPriceOrderPackage(orderPackage);
        log.info("[adjustPriceBySku] priceBeforeAdjustment: {}, totalPriceBeforeAdjustment: {}, originalPrice: {}, prepayment: {}",
                priceBeforeAdjustment, totalPriceBeforeAdjustment, orderPackage.getOriginPrice(), orderPackage.getPrePayment());

        // Update additional fields in the orderPackage
        orderPackage.setRefCode(request.getRefCode());
        orderPackage.setIsChangePrice(true);

        // Update negotiatedAmount and totalPrice in each product (in-memory)
        Map<String, AdjustPriceBySkuRequest.SkuItem> skuItemMap = request.getSkus().stream()
                .collect(Collectors.toMap(AdjustPriceBySkuRequest.SkuItem::getSkuId, item -> item));

        for (PackageProductEntity product : packageProducts) {
            AdjustPriceBySkuRequest.SkuItem skuItem = skuItemMap.get(product.getLazbaoSkuId());
            if (!ObjectUtils.isEmpty(skuItem)) {
                product.setNegotiatedAmount(skuItem.getAdjustedAmount());
            } else {
                product.setNegotiatedAmount(BigDecimal.ZERO);
            }
            BigDecimal productTotalPrice = SellerOrderPackageCalculator.calculatePackageProductAmount(product);
            product.setTotalPrice(productTotalPrice);
        }

        // Re-calculate the entire orderPackage prices after adjustment
        BigDecimal priceAfterAdjustment = SellerOrderPackageCalculator.calculateProductAmount(orderPackage);
        BigDecimal totalPriceAfterAdjustment = SellerOrderPackageCalculator.calculateTotalPriceOrderPackage(orderPackage);
        BigDecimal newPrepayment = priceAfterAdjustment.multiply(new BigDecimal(Constants.PERCENTAGE_PREPAYMENT));
        log.info("[adjustPriceBySku] priceAfterAdjustment: {}, totalPriceAfterAdjustment: {}, originalPrice: {}, newPrepayment: {}",
                priceAfterAdjustment, totalPriceAfterAdjustment, orderPackage.getOriginPrice(), newPrepayment);
        orderPackage.setPrice(priceAfterAdjustment);
        orderPackage.setTotalPrice(totalPriceAfterAdjustment);
        orderPackage.setPrePayment(newPrepayment);

        // Save the orderPackage, which will also cascade-save any updated packageProducts
        OrderPackageEntity orderPackageAfterAdjustment = orderPackageRepository.save(orderPackage);

        // Save order after update order package
        updateOrderAfterAdjustmentPrice(orderPackageAfterAdjustment.getOrder());

        // Clone the packageProducts after adjustment (for logging)
        List<PackageProductEntity> packageProductsAfterAdjustment = deepClonePackageProduct(packageProducts);

        // Save the adjustment history to the order_activity_history table
        saveAdjustPriceBySkuOrderActivityHistory(
                request,
                skuIdsFromRequest,
                packageProductsBeforeAdjustment,
                packageProductsAfterAdjustment
        );
    }

    private void saveAdjustPriceBySkuOrderActivityHistory(AdjustPriceBySkuRequest request,
                                                          List<String> skuIdsFromRequest,
                                                          List<PackageProductEntity> packageProductsBeforeAdjustment,
                                                          List<PackageProductEntity> packageProductsAfterAdjustment) {
        // Common fields
        String key = UUID.randomUUID().toString();
        Long orderId = request.getOrderId();

        // Build SKU details
        List<PriceAdjustmentSkuData.SkuDetails> skuDetails = packageProductsAfterAdjustment.stream()
                .filter(p -> skuIdsFromRequest.contains(p.getLazbaoSkuId()))
                .map(product -> PriceAdjustmentSkuData.SkuDetails.builder()
                        .skuId(product.getLazbaoSkuId())
                        .priceBeforeAdjustment(product.getSkuPrice())
                        .priceAfterAdjustment(product.getNegotiatedAmount())
                        .quantity(product.getQuantity())
                        .build())
                .toList();

        // Build details
        PriceAdjustmentSkuData details = PriceAdjustmentSkuData.builder()
                .type(PriceAdjustmentType.SKU)
                .refCode(request.getRefCode())
                .skus(skuDetails)
                .build();

        // Serialize states once
        String beforeState = JsonMapperUtils.writeValueAsString(packageProductsBeforeAdjustment);
        String afterState = JsonMapperUtils.writeValueAsString(packageProductsAfterAdjustment);

        // First history
        PriceAdjustmentMetadata firstMetadata = PriceAdjustmentMetadata.builder()
                .key(key)
                .beforeStatus(BuyerOrderStatus.WAIT_FOR_PAY_ORDER.getOrderStatusCode())
                .afterStatus(BuyerOrderStatus.WAIT_FOR_ADJUST_PRICE.getOrderStatusCode())
                .visible(false)
                .build();

        OrderActivityHistoryEntity firstHistory = OrderActivityHistoryEntity.builder()
                .orderId(orderId)
                .type(ActivityType.PRICE_ADJUSTMENT)
                .details(details)
                .beforeState(beforeState)
                .afterState(afterState)
                .metadata(firstMetadata)
                .build();

        // Second history
        PriceAdjustmentMetadata secondMetadata = PriceAdjustmentMetadata.builder()
                .key(key)
                .beforeStatus(BuyerOrderStatus.WAIT_FOR_ADJUST_PRICE.getOrderStatusCode())
                .afterStatus(BuyerOrderStatus.WAIT_FOR_PAY_ORDER.getOrderStatusCode())
                .visible(true)
                .build();

        OrderActivityHistoryEntity secondHistory = OrderActivityHistoryEntity.builder()
                .orderId(orderId)
                .type(ActivityType.PRICE_ADJUSTMENT)
                .details(details)
                .beforeState(beforeState)
                .afterState(afterState)
                .metadata(secondMetadata)
                .build();

        // Save histories
        orderActivityHistoryEntityRepository.saveAll(List.of(firstHistory, secondHistory));
        log.info("[adjustPriceBySku] Price adjustment by SKU completed for order ID: {}", orderId);
    }

    public void updateOrderAfterAdjustmentPrice(OrderEntity order) {
        BigDecimal totalPriceAfterAdjustment = SellerOrderPackageCalculator.calculateTotalPriceOrder(order);
        order.setTotalPaymentAmount(totalPriceAfterAdjustment);
        repo.save(order);
        log.info("Update order after adjustment price successfully");
    }

    public void resetPreviousAdjustment(OrderPackageEntity orderPackage, PriceAdjustmentType currentType) {
        Long orderId = orderPackage.getId();
        log.info("Resetting previous adjustment for order ID: {}", orderId);

        // Get history of price adjustment
        List<OrderActivityHistoryEntity> activityHistories = orderActivityHistoryEntityRepository.findByOrderIdAndType(orderId, ActivityType.PRICE_ADJUSTMENT);
        if (activityHistories.isEmpty()) {
            log.info("No previous adjustment found for order ID: {}", orderId);
            return;
        }

        // Get last adjustment
        OrderActivityHistoryEntity lastAdjustment = activityHistories.stream()
                .max(Comparator.comparing(OrderActivityHistoryEntity::getCreatedAt))
                .orElseThrow(VipoBusinessException::new);

        Object lastDetails = lastAdjustment.getDetails();

        if (lastDetails instanceof PriceAdjustmentTotalData response
                && response.getType().equals(PriceAdjustmentType.TOTAL) && currentType.equals(PriceAdjustmentType.SKU)) {
            log.info("Resetting previous TOTAL adjustment for order ID: {}", orderId);
            resetTotalAdjustment(response, orderPackage);
        } else if (lastDetails instanceof PriceAdjustmentSkuData response
                && response.getType().equals(PriceAdjustmentType.SKU) && currentType.equals(PriceAdjustmentType.TOTAL)) {
            log.info("Resetting previous SKU adjustment for order ID: {}", orderId);
            resetSkuAdjustment(response, orderPackage);
        } else {
            log.info("No adjustment reset required for order ID: {}", orderId);
            // TODO: Reset order, orderPackage, packageProduct, etc. to original state
        }
    }

    private void resetTotalAdjustment(PriceAdjustmentTotalData lastAdjustment, OrderPackageEntity orderPackage) {
        Long orderId = orderPackage.getId();
        log.info("Resetting TOTAL adjustment: adjustedAmount={}, orderId={}", lastAdjustment.getAdjustedAmount(), orderId);
        orderPackage.setNegotiatedAmount(BigDecimal.ZERO);
        log.info("TOTAL adjustment reset successfully for order ID: {}", orderId);
    }

    private void resetSkuAdjustment(PriceAdjustmentSkuData lastAdjustment, OrderPackageEntity orderPackage) {
        Long orderId = orderPackage.getId();
        List<String> skuIds = lastAdjustment.getSkus().stream()
                .map(PriceAdjustmentSkuData.SkuDetails::getSkuId)
                .toList();

        log.info("Resetting SKU adjustment: skuIds={}, orderId={}", skuIds, orderId);
        Set<PackageProductEntity> skus = orderPackage.getSku();
        if (CollectionUtils.isEmpty(skus)) {
            log.info("No SKU found for order ID: {}", orderId);
            return;
        }
        for (PackageProductEntity sku : skus) {
            if (skuIds.contains(sku.getLazbaoSkuId())) {
                sku.setNegotiatedAmount(BigDecimal.ZERO);
                sku.setTotalPrice(sku.getSkuPrice().multiply(BigDecimal.valueOf(sku.getQuantity())));
            }
        }
        log.info("SKU adjustment reset successfully for order ID: {}", orderId);
    }

    private OrderPackageEntity cloneOrderPackage(OrderPackageEntity original) {
        OrderPackageEntity cloned = new OrderPackageEntity();
        BeanUtils.copyProperties(original, cloned);
        return cloned;
    }

    private OrderPackageEntity deepCloneOrderPackage(OrderPackageEntity original) {
        String json = JsonMapperUtils.writeValueAsString(original);
        OrderPackageEntity cloned = JsonMapperUtils.convertJsonToObject(json, OrderPackageEntity.class);
        return cloned;
    }

    private List<PackageProductEntity> clonePackageProduct(List<PackageProductEntity> original) {
        List<PackageProductEntity> cloned = new ArrayList<>();
        for (PackageProductEntity item : original) {
            PackageProductEntity clone = new PackageProductEntity();
            BeanUtils.copyProperties(item, clone);
            cloned.add(clone);
        }
        return cloned;
    }

    private List<PackageProductEntity> deepClonePackageProduct(List<PackageProductEntity> original) {
        String json = JsonMapperUtils.writeValueAsString(original);
        TypeReference<List<PackageProductEntity>> typeRef = new TypeReference<>() {
        };
        List<PackageProductEntity> cloned = JsonMapperUtils.convertJsonToObject(json, typeRef);
        return cloned;
    }

    @Override
    public List<PriceAdjustmentHistoryResponse> getPriceAdjustmentHistory(Long orderId) {
        // Validate and retrieve order
        validateOrderExistence(orderId);

        // Get all price adjustment history for the order
        List<OrderActivityHistoryEntity> orderActivityHistoryEntities = orderActivityHistoryEntityRepository
                .findVisibleMetadataRecords(orderId, ActivityType.PRICE_ADJUSTMENT);

        return orderActivityHistoryEntities.stream()
                .map(this::mapToPriceAdjustmentHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PriceAdjustmentTotalResponse getPriceAdjustmentTotalDetail(Long orderId, Long adjustmentHistoryId) {
        // Validate input and retrieve required data
        OrderActivityHistoryEntity historyEntity = validateAndRetrieveHistory(orderId, adjustmentHistoryId, PriceAdjustmentTotalData.class);

        PriceAdjustmentTotalData response = (PriceAdjustmentTotalData) historyEntity.getDetails();
        return PriceAdjustmentTotalResponse.builder()
                .id(historyEntity.getId())
                .orderId(historyEntity.getOrderId())
                .type(response.getType())
                .createdBy(historyEntity.getCreatedBy())
                .createdAt(historyEntity.getCreatedAt())
                .adjustedAmount(response.getAdjustedAmount())
                .priceBeforeAdjustment(response.getPriceBeforeAdjustment())
                .priceAfterAdjustment(response.getPriceAfterAdjustment())
                .refCode(response.getRefCode())
                .build();
    }

    @Override
    public PriceAdjustmentSkuResponse getPriceAdjustmentSkuDetail(Long orderId, Long adjustmentHistoryId) {
        // Validate input and retrieve required data
        OrderActivityHistoryEntity historyEntity = validateAndRetrieveHistory(orderId, adjustmentHistoryId, PriceAdjustmentSkuData.class);

        PriceAdjustmentSkuData response = (PriceAdjustmentSkuData) historyEntity.getDetails();
        return PriceAdjustmentSkuResponse.builder()
                .id(historyEntity.getId())
                .orderId(historyEntity.getOrderId())
                .type(response.getType())
                .createdBy(historyEntity.getCreatedBy())
                .createdAt(historyEntity.getCreatedAt())
                .refCode(response.getRefCode())
                .skus(response.getSkus().stream().map(e -> PriceAdjustmentSkuResponse.SkuDetails.builder()
                                .skuId(e.getSkuId())
                                .priceBeforeAdjustment(e.getPriceBeforeAdjustment())
                                .priceAfterAdjustment(e.getPriceAfterAdjustment())
                                .quantity(e.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private void validateOrderExistence(Long orderId) {
        VipoUserDetails user = getCurrentUser();
        orderPackageRepository.findByIdAndMerchantId(orderId, user.getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND));
    }

    private OrderActivityHistoryEntity validateAndRetrieveHistory(Long orderId, Long adjustmentHistoryId, Class<?> expectedType) {
        log.info("orderId: {}, adjustmentHistoryId: {}", orderId, adjustmentHistoryId);
        VipoUserDetails user = getCurrentUser();
        if (ObjectUtils.isEmpty(orderId) || ObjectUtils.isEmpty(adjustmentHistoryId)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION);
        }

        OrderPackageEntity orderPackageEntity = orderPackageRepository.findByIdAndMerchantId(orderId, user.getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND));

        OrderActivityHistoryEntity historyEntity = orderActivityHistoryEntityRepository.findVisibleMetadataRecord(adjustmentHistoryId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.HISTORY_ADJUSTMENT_NOT_FOUND));

        if (!historyEntity.getOrderId().equals(orderPackageEntity.getId())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.HISTORY_ADJUSTMENT_NOT_MATCH_ORDER);
        }

        log.info("Order activity details: {}", historyEntity.getDetails());
        if (!expectedType.isInstance(historyEntity.getDetails())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION);
        }

        return historyEntity;
    }

    private PriceAdjustmentHistoryResponse mapToPriceAdjustmentHistoryResponse(OrderActivityHistoryEntity history) {
        log.info("History details: {}", history);
        PriceAdjustmentType adjustmentType = null;
        String refCode = null;

        ActivityDetailsData details = history.getDetails();
        if (details instanceof PriceAdjustmentTotalData totalData) {
            adjustmentType = totalData.getType();
            refCode = totalData.getRefCode();
            log.info("Total data: {}, adjustmentType: {}, refCode: {}", totalData, adjustmentType, refCode);
        } else if (details instanceof PriceAdjustmentSkuData skuData) {
            adjustmentType = skuData.getType();
            refCode = skuData.getRefCode();
            log.info("SKU data: {}, adjustmentType: {}, refCode: {}", skuData, adjustmentType, refCode);
        } else {
            log.warn("Unexpected type in details: {}", details != null ? details.getClass() : "null");
        }

        return PriceAdjustmentHistoryResponse.builder()
                .id(history.getId())
                .orderId(history.getOrderId())
                .type(adjustmentType)
                .createdBy(history.getCreatedBy())
                .createdAt(history.getCreatedAt())
                .refCode(refCode)
                .detailsData(history.getDetails())
                .build();
    }

    @Transactional
    @Override
    public PrepareOrderResponse prepareOrder(PrepareOrderRequest request) {
        validatePrepareOrderRequest(request);

        VipoUserDetails user = getCurrentUser();

        // Retrieve the list of order packages by IDs
        List<OrderPackageEntity> orderPackages = orderPackageRepository.findByMerchantIdAndIdIn(user.getId(), request.getOrderIds());
        if (ObjectUtils.isEmpty(orderPackages) || orderPackages.size() != request.getOrderIds().size()) {
            throw new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND);
        }

        boolean allWaiting = orderPackages.stream()
                .allMatch(op -> SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION.equals(op.getSellerOrderStatus()));
        if (!allWaiting) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    OrderConstant.STATUS_ORDER_CHANGED);
        }

        CarrierEntity carrier = carrierEntityRepository.findByCarrierCode(request.getCarrierCode())
                .orElseThrow(() -> new VipoNotFoundException(Constants.CARRIER_NOT_FOUND));

        WarehouseAddressEntity warehouseAddress = warehouseAddressEntityRepository
                .findByIdAndMerchantIdAndDeletedFalse(Long.valueOf(request.getWarehouseAddressId()), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.WAREHOUSE_NOT_FOUND));

        List<PrepareOrderResponse.PrepareOrderData> prepareOrderDataList = orderPackages.stream()
                .map(orderPackage -> processOrderPackage(orderPackage, carrier, warehouseAddress, request))
                .collect(Collectors.toList());

        PrepareOrderResponse response = new PrepareOrderResponse();
        response.setData(prepareOrderDataList);

        savePrepareOrderActivityHistory(prepareOrderDataList, request);
        return response;
    }

    private void validatePrepareOrderRequest(PrepareOrderRequest request) {
        if (ObjectUtils.isEmpty(request.getOrderIds())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.ORDER_IDS_NOT_EMPTY);
        }

        if (ObjectUtils.isEmpty(request.getCarrierCode())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.CARRIER_CODE_NOT_EMPTY);
        }

        if (ObjectUtils.isEmpty(request.getWarehouseAddressId())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.WAREHOUSE_ADDRESS_ID_NOT_EMPTY);
        }

        // If warehouse address ID is not a number
        if (!StringUtils.isNumeric(request.getWarehouseAddressId())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.WAREHOUSE_ADDRESS_ID_INVALID);
        }
    }

    private ServiceInfoEvtpRequest buildServiceInfoEvtpRequest(OrderPackageEntity orderPackage, WarehouseAddressEntity warehouseAddress) {
        // Get warehouse address information
        WardEntity ward = wardRepository.findById(warehouseAddress.getWardId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.WARD_NOT_FOUND));

        DistrictEntity district = districtRepository.findById(ward.getDistrict().getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.DISTRICT_NOT_FOUND));

        Long productWeight = orderPackage.getSku().stream()
                .mapToLong(p -> ObjectUtils.isEmpty(p.getWeight()) ? 0L : p.getWeight() * p.getQuantity())
                .sum();

        ServiceInfoEvtpRequest requestEvtp = ServiceInfoEvtpRequest.builder()
                .senderProvince(district.getProvince().getId())
                .senderDistrict(district.getId())
                .receiverProvince(orderPackage.getReceiverProvinceId())
                .receiverDistrict(orderPackage.getReceiverDistrictId())
                .productType(Constants.PRODUCT_TYPE_VIPO)
                .productWeight(productWeight)
                .productPrice(orderPackage.getPrice().longValue())
                .moneyCollection(orderPackage.getPrice().subtract(orderPackage.getPrePayment()).longValue())
                .type(1L)
                .productLength(0L)
                .productWidth(0L)
                .productHeight(0L)
                .build();
        log.info("[buildServiceInfoEvtpRequest] requestEvtp: {}", requestEvtp);

        return requestEvtp;
    }

    private CreateFullOrderEvtpRequest buildCreateOrderEvtpRequest(OrderPackageEntity orderPackage,
                                                                   WarehouseAddressEntity warehouseAddress,
                                                                   PrepareOrderRequest request,
                                                                   List<String> orderExtraServices) {
        // Calculate the information for the order package
        String productNameOriginal = orderPackage.getSku().stream()
                .filter(name -> !ObjectUtils.isEmpty(name))
                .map(p -> String.format("[%s]", p.getName()))
                .distinct()
                .collect(Collectors.joining(", "));

        String productName = ObjectUtils.isEmpty(productNameOriginal)
                ? productNameOriginal : productNameOriginal.substring(0, Math.min(productNameOriginal.length(), Constants.MAX_LENGTH_OF_PRODUCT_NAME));

        String productDescription = request.getNote().substring(0, Math.min(request.getNote().length(), Constants.MAX_LENGTH_OF_PRODUCT_DESCRIPTION));

        Long totalQuantity = orderPackage.getSku().stream()
                .mapToLong(PackageProductEntity::getQuantity)
                .sum();
        Long productWeight = orderPackage.getSku().stream()
                .mapToLong(p -> ObjectUtils.isEmpty(p.getWeight()) ? 0L : p.getWeight() * p.getQuantity())
                .sum();

        // TODO: Fix the orderExtraServicesStr if the orderExtraServices is empty
        String orderExtraServicesStr = ObjectUtils.isEmpty(orderExtraServices)
                ? String.join(", ", Constants.EXTRA_SERVICE_VIPO) : String.join(", ", orderExtraServices);

        // Get warehouse address information
        WardEntity ward = wardRepository.findById(warehouseAddress.getWardId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.WARD_NOT_FOUND));

        DistrictEntity district = districtRepository.findById(ward.getDistrict().getId())
                .orElseThrow(() -> new VipoNotFoundException(Constants.DISTRICT_NOT_FOUND));

        // Build the ListItem list (assuming orderPackage has an item list or details to be mapped)
        List<CreateFullOrderEvtpRequest.ListItem> listItems = orderPackage.getSku().stream()
                .map(p -> CreateFullOrderEvtpRequest.ListItem.builder()
                        .productName(p.getName())
                        .productPrice(p.getSkuPrice().longValue() * p.getQuantity())
                        .productWeight(ObjectUtils.isEmpty(p.getWeight()) ? 0L : p.getWeight())
                        .productQuantity(p.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Build CreateFullOrderEvtpRequest using the builder pattern
        CreateFullOrderEvtpRequest requestEvtp = CreateFullOrderEvtpRequest.builder()
                .orderNumber(orderPackage.getOrderCode().toUpperCase()) // Mã đơn VIPO
                .groupAddressId(0L)
                .senderFullname(warehouseAddress.getName()) // Theo dia chi kho
                .senderAddress(warehouseAddress.getFullAddress()) // Theo dia chi kho
                .senderPhone(warehouseAddress.getPhoneNumber()) // Theo dia chi kho
                .senderWards(ward.getId()) // Theo dia chi kho
                .senderDistrict(district.getId()) // Theo dia chi kho
                .senderProvince(district.getProvince().getId()) // Theo dia chi kho
                .receiverFullname(orderPackage.getReceiverName()) //
                .receiverAddress(orderPackage.getReceiverAddressDetail())
                .receiverPhone(orderPackage.getReceiverPhone())
                .receiverWards(orderPackage.getReceiverWardId())
                .receiverDistrict(orderPackage.getReceiverDistrictId())
                .receiverProvince(orderPackage.getReceiverProvinceId())
                .productName(productName)
                .productDescription(productDescription) // Confirm BA
                .productQuantity(totalQuantity)
                .productPrice(orderPackage.getPrice().longValue())
                .productWeight(productWeight)
                .productLength(0L)
                .productWidth(0L)
                .productHeight(0L)
                .productType(Constants.PRODUCT_TYPE_VIPO)
                .orderPayment(2L)
                .orderService(Constants.SERVICE_VIPO)
                .orderServiceAdd(orderExtraServicesStr)
                .orderNote(orderPackage.getNoteSeller())
                .moneyCollection(orderPackage.getPrice().subtract(orderPackage.getPrePayment()).longValue())
                .checkUnique(false)
                .extraMoney(0L) // TODO: Confirm BA
                .listItem(listItems)
                .build();
        log.info("[buildCreateOrderEvtpRequest] requestEvtp: {}", requestEvtp);

        return requestEvtp;
    }

    private String createOrderNumber(String orderCode) {
        if (ObjectUtils.isEmpty(orderCode)) {
            return null;
        }
        if (orderCode.startsWith(Constants.PREFIX_ORDER_CODE_VIPO)) {
            return orderCode.substring(Constants.PREFIX_ORDER_CODE_VIPO.length()).toUpperCase();
        }
        return orderCode.toUpperCase();
    }

    private PrepareOrderResponse.PrepareOrderData processOrderPackage(OrderPackageEntity orderPackage, CarrierEntity carrier, WarehouseAddressEntity warehouseAddress, PrepareOrderRequest request) {
        log.info("[prepareOrder] Processing order ID: {}", orderPackage.getId());

        List<String> orderExtraServices = getExtraServices(orderPackage, warehouseAddress);
        log.info("[prepareOrder] orderExtraServices: {}", orderExtraServices);

        // Process create order in EVTP
        CreateFullOrderEvtpRequest createOrderEvtpRequest = buildCreateOrderEvtpRequest(orderPackage, warehouseAddress, request, orderExtraServices);
        try {
            CreateOrderEvtpResponse evtpResponse = evtpOrderService.createFullOrder(createOrderEvtpRequest);
            if (evtpResponse.getStatus() == 200 && !evtpResponse.isError()) {
                String orderNumber = evtpResponse.getData().getOrderNumber();
                if (!ObjectUtils.isEmpty(orderNumber)) {
                    log.info("[prepareOrder] Order number {} created successfully for order ID: {}", orderNumber, orderPackage.getId());
                    // Update orderPackage
                    updateOrderPackage(orderPackage, SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS,
                            BuyerOrderStatus.SUPPLIER_IS_PREPARING_ORDER_SUCCESS, evtpResponse.getData());

                    // Create or update orderShipment
                    Optional<OrderShipmentEntity> orderShipmentEntityOptional = orderShipmentEntityRepository
                            .findByPackageIdAndCarrier_Id(orderPackage.getId(), carrier.getId());
                    createOrUpdateOrderShipmentEntity(orderShipmentEntityOptional, orderPackage, carrier, warehouseAddress,
                            orderNumber, request.getNote(), ShippingConnectionStatus.SUCCESS, Constants.MESSAGE_CREATE_ORDER_SUCCESS,
                            evtpResponse.getData());
                    saveOrderShippmentConnectionLogs(orderPackage, ShippingConnectionStatus.SUCCESS, createOrderEvtpRequest, evtpResponse);

                    return buildPrepareOrderData(orderPackage, orderNumber, ShippingConnectionStatus.SUCCESS, Constants.MESSAGE_CREATE_ORDER_SUCCESS);
                } else {
                    log.info("[prepareOrder] Order number is empty for order ID: {}", orderPackage.getId());
                    // Update orderPackage
                    updateOrderPackage(orderPackage, SellerOrderStatus.ORDER_PREPARED,
                            BuyerOrderStatus.SUPPLIER_IS_PREPARING_ORDER_PENDING, evtpResponse.getData());

                    // Create or update orderShipment
                    Optional<OrderShipmentEntity> orderShipmentEntityOptional = orderShipmentEntityRepository
                            .findByPackageIdAndCarrier_Id(orderPackage.getId(), carrier.getId());
                    createOrUpdateOrderShipmentEntity(orderShipmentEntityOptional, orderPackage, carrier, warehouseAddress,
                            null, request.getNote(), ShippingConnectionStatus.PENDING, Constants.MESSAGE_CREATE_ORDER_PENDING,
                            evtpResponse.getData());
                    saveOrderShippmentConnectionLogs(orderPackage, ShippingConnectionStatus.PENDING, createOrderEvtpRequest, evtpResponse);

                    return buildPrepareOrderData(orderPackage, null, ShippingConnectionStatus.PENDING, Constants.MESSAGE_CREATE_ORDER_PENDING);
                }
            } else {
                log.info("[prepareOrder] Failed to create order in EVTP for order ID: {}", orderPackage.getId());
                // Create orderShipment
                String message = EvtpErrorMessage.getMessage(evtpResponse.getMessage());
                Optional<OrderShipmentEntity> orderShipmentEntityOptional = orderShipmentEntityRepository
                        .findByPackageIdAndCarrier_Id(orderPackage.getId(), carrier.getId());
                createOrUpdateOrderShipmentEntity(orderShipmentEntityOptional, orderPackage, carrier, warehouseAddress,
                        null, request.getNote(), ShippingConnectionStatus.FAIL, message, evtpResponse.getData());
                saveOrderShippmentConnectionLogs(orderPackage, ShippingConnectionStatus.FAIL, createOrderEvtpRequest, evtpResponse);

                return buildPrepareOrderData(orderPackage, null, ShippingConnectionStatus.FAIL, message);
            }
        } catch (Exception e) {
            log.error("[prepareOrder Exception] Failed to create order in EVTP for order ID: {}", orderPackage.getId(), e);
            Optional<OrderShipmentEntity> orderShipmentEntityOptional = orderShipmentEntityRepository
                    .findByPackageIdAndCarrier_Id(orderPackage.getId(), carrier.getId());
            createOrUpdateOrderShipmentEntity(orderShipmentEntityOptional, orderPackage, carrier, warehouseAddress,
                    null, request.getNote(), ShippingConnectionStatus.FAIL, Constants.MESSAGE_CREATE_ORDER_FAIL, null);
            saveOrderShippmentConnectionLogs(orderPackage, ShippingConnectionStatus.FAIL, createOrderEvtpRequest, null);

            return buildPrepareOrderData(orderPackage, null, ShippingConnectionStatus.FAIL, Constants.MESSAGE_CREATE_ORDER_FAIL);
        }
    }

    private List<String> getExtraServices(OrderPackageEntity orderPackage, WarehouseAddressEntity warehouseAddress) {
        // Process fetch service info from EVTP
        List<String> orderExtraServices = new ArrayList<>();
        List<ServiceInfoEvtpResponse> serviceInfoEvtpResponseList = new ArrayList<>();
        ServiceInfoEvtpRequest serviceInfoEvtpRequest = buildServiceInfoEvtpRequest(orderPackage, warehouseAddress);
        try {
            serviceInfoEvtpResponseList = evtpOrderService.getServiceInfo(serviceInfoEvtpRequest);
            if (ObjectUtils.isEmpty(serviceInfoEvtpResponseList)) {
                log.info("[getExtraServices] Failed to get service info from EVTP for order ID: {}", orderPackage.getId());
                return orderExtraServices;
            }

            Optional<ServiceInfoEvtpResponse> serviceVipoOptional = serviceInfoEvtpResponseList.stream()
                    .filter(service -> service.getMaDvChinh().equalsIgnoreCase(Constants.SERVICE_VIPO))
                    .findFirst();
            if (serviceVipoOptional.isEmpty()) {
                log.info("[getExtraServices] Service VIPO not found for order ID: {}", orderPackage.getId());
                return orderExtraServices;
            }

            // TODO: Hiện tại chỉ lấy dịch vụ cộng thêm là đồng kiểm (GDK) trong dịch vụ chính (VVPO) nếu có
            ServiceInfoEvtpResponse serviceVipo = serviceVipoOptional.get();
            orderExtraServices = serviceVipo.getExtraService()
                    .stream()
                    .map(ServiceInfoEvtpResponse.ExtraService::getServiceCode)
                    .filter(Constants.EXTRA_SERVICE_VIPO::contains)
                    .toList();
        } catch (Exception e) {
            log.error("[getExtraServices Exception] Failed to get service info from EVTP for order ID: {}", orderPackage.getId(), e);
        } finally {
            saveOrderOrderShipmentLogs(orderPackage.getId(), serviceInfoEvtpRequest, serviceInfoEvtpResponseList);
            log.info("[getExtraServices] Saved service info logs for order ID: {}", orderPackage.getId());
        }
        return orderExtraServices;
    }

    private void createOrUpdateOrderShipmentEntity(Optional<OrderShipmentEntity> optionalEntity,
                                                   OrderPackageEntity orderPackage,
                                                   CarrierEntity carrier,
                                                   WarehouseAddressEntity warehouseAddress,
                                                   String orderNumber,
                                                   String note,
                                                   ShippingConnectionStatus status,
                                                   String message,
                                                   CreateOrderEvtpResponse.OrderData createEvtpOrderData) {
        if (optionalEntity.isPresent()) {
            OrderShipmentEntity orderShipment = optionalEntity.get();
            orderShipment.setShipmentCode(orderNumber);
            orderShipment.setWarehouseAddress(warehouseAddress);
            orderShipment.setShipmentDate(LocalDateTime.now());
            orderShipment.setPickupAddress(warehouseAddress.getFullAddress());
            orderShipment.setCreateOrderStatus(status);
            orderShipment.setCreateOrderMessage(message);
            orderShipment.setNote(note);
            if (
                    ObjectUtils.isNotEmpty(createEvtpOrderData)
                    && ObjectUtils.isNotEmpty(createEvtpOrderData.getKpiHt())
            )
                orderShipment.setExpectedDeliveryTime(
                        DateUtils.getCurrentLocalDateTime().minusHours(createEvtpOrderData.getKpiHt().longValue())
                );
            orderShipmentEntityRepository.save(orderShipment);
            log.info("[prepareOrder] OrderShipment ID {} updated with status {}, shipmentCode {}", orderShipment.getId(), status, orderNumber);
        } else {
            OrderShipmentEntity orderShipment = OrderShipmentEntity.builder()
                    .packageId(orderPackage.getId())
                    .carrier(carrier)
                    .warehouseAddress(warehouseAddress)
                    .shipmentCode(orderNumber)
                    .shipmentDate(LocalDateTime.now())
                    .pickupAddress(warehouseAddress.getFullAddress())
                    .createOrderStatus(status)
                    .status(null)
                    .createOrderMessage(message)
                    .isPrinted(false)
                    .note(note)
                    .expectedDeliveryTime(
                            ObjectUtils.isNotEmpty(createEvtpOrderData)
                                    && ObjectUtils.isNotEmpty(createEvtpOrderData.getKpiHt()) ?
                                    DateUtils.getCurrentLocalDateTime().plusHours(createEvtpOrderData.getKpiHt().longValue())
                                    : null
                    )
                    .build();
            orderShipmentEntityRepository.save(orderShipment);
            log.info("[prepareOrder] OrderShipment ID {} created with status {}, shipmentCode {}", orderShipment.getId(), status, orderNumber);
        }
    }

    private void saveOrderShippmentConnectionLogs(OrderPackageEntity orderPackage, ShippingConnectionStatus status,
                                                  CreateFullOrderEvtpRequest evtpRequest, CreateOrderEvtpResponse evtpResponse) {
        log.info("[prepareOrder] Saving logs for order ID: {}", orderPackage.getId());
        // Save package logs
        PackageLogEntity packageLog = new PackageLogEntity();
        packageLog.setPackageId(orderPackage.getId());
        packageLog.setLog(status.equals(ShippingConnectionStatus.FAIL)
                ? Constants.MESSAGE_LOG_CONNECT_ORDER_SHIPMENT_FAIL : Constants.MESSAGE_LOG_CONNECT_ORDER_SHIPMENT_SUCCESS);
        packageLog.setData(JsonMapperUtils.writeValueAsString(orderPackage));
        packageLog.setCustomerId(orderPackage.getCustomerId());
        packageLog.setMerchantId(orderPackage.getMerchantId());
        packageLog.setCreateTime(System.currentTimeMillis() / 1000);
        packageLogEntityRepository.save(packageLog);
        log.info("[prepareOrder] Package log saved successfully");

        // Save shippment logs
        saveOrderOrderShipmentLogs(orderPackage.getId(), evtpRequest, evtpResponse);
        log.info("[prepareOrder saveOrderShippmentConnectionLogs] Saved service info logs for order ID: {}", orderPackage.getId());
    }

    public <T, U> void saveOrderOrderShipmentLogs(Long orderId, T request, U response) {
        MakeShipmentLogEntity makeShipmentLog = new MakeShipmentLogEntity();
        makeShipmentLog.setPackageId(orderId);
        makeShipmentLog.setSent(JsonMapperUtils.writeValueAsString(request));
        makeShipmentLog.setContent(JsonMapperUtils.writeValueAsString(response));
        makeShipmentLogEntityRepository.save(makeShipmentLog);
        log.info("[prepareOrder] Make shipment log saved successfully");
    }

    private void updateOrderPackage(OrderPackageEntity orderPackage, SellerOrderStatus sellerStatus,
                                    BuyerOrderStatus buyerStatus, CreateOrderEvtpResponse.OrderData orderData) {
        orderPackage.setSellerOrderStatus(sellerStatus);
        orderPackage.setOrderStatus(buyerStatus.getOrderStatusCode());
        if (!ObjectUtils.isEmpty(orderData.getOrderNumber())) {
            orderPackage.setShipmentId(orderData.getOrderNumber());
        }
        orderPackage.setTotalShippingFee(BigDecimal.valueOf(orderData.getMoneyTotal()));
        orderPackage.setShippingFee(BigDecimal.valueOf(orderData.getMoneyTotalFee()));
        orderPackage.setCodFee(BigDecimal.valueOf(orderData.getMoneyCollection()));
        orderPackage.setMoneyFee(BigDecimal.valueOf(orderData.getMoneyFee()));
        orderPackage.setOtherFee(BigDecimal.valueOf(orderData.getMoneyOtherFee()));
        orderPackage.setVasFee(BigDecimal.valueOf(orderData.getMoneyVas()));
        orderPackage.setVatFee(BigDecimal.valueOf(orderData.getMoneyVat()));
        orderPackage.setMakeShipmentTime(DateUtils.getCurrentTimeInSeconds());
        orderPackage.setDomesticExtraServices(Constants.SERVICE_VIPO);
        orderPackageRepository.save(orderPackage);
    }

    private PrepareOrderResponse.PrepareOrderData buildPrepareOrderData(OrderPackageEntity orderPackage, String shipmentCode, ShippingConnectionStatus status, String message) {
        return PrepareOrderResponse.PrepareOrderData.builder()
                .orderId(orderPackage.getId())
                .orderCode(orderPackage.getOrderCode())
                .shipmentCode(shipmentCode)
                .status(status)
                .message(message)
                .orderStatus(orderPackage.getOrderStatus())
                .build();
    }

    private void savePrepareOrderActivityHistory(List<PrepareOrderResponse.PrepareOrderData> prepareOrderDataList, PrepareOrderRequest request) {
        log.info("[prepareOrder] Saving prepare order activity history");
        // Save order activity history prepare order
        List<OrderActivityHistoryEntity> prepareOrderHistories = request.getOrderIds().stream()
                .map(id -> OrderActivityHistoryEntity.builder()
                        .orderId(id)
                        .type(ActivityType.ORDER_PREPARE)
                        .details(PrepareOrderBeforeShipmentData.builder()
                                .orderId(id)
                                .carrierCode(request.getCarrierCode())
                                .warehouseAddressId(Long.valueOf(request.getWarehouseAddressId()))
                                .note(request.getNote())
                                .build())
                        .beforeState(null)
                        .afterState(null)
                        .build())
                .toList();
        orderActivityHistoryEntityRepository.saveAll(prepareOrderHistories);
        log.info("[prepareOrder] Prepare order activity history saved successfully");

        // Save order activity history connect to shipment
        List<OrderActivityHistoryEntity> connectShipmentHistories = prepareOrderDataList.stream()
                .map(data -> OrderActivityHistoryEntity.builder()
                        .orderId(data.getOrderId())
                        .type(ActivityType.ORDER_CONNECT_SHIPMENT)
                        .details(data)
                        .beforeState(null)
                        .afterState(null)
                        .build())
                .collect(Collectors.toList());
        orderActivityHistoryEntityRepository.saveAll(connectShipmentHistories);
        log.info("[prepareOrder] Prepare order activity history connect to shipment saved successfully");

        // Save order tracking logs
        Long currentTime = DateUtils.getTimeInSeconds(LocalDateTime.now());
        List<OrderTrackingEntity> orderTrackings = prepareOrderDataList.stream()
                .filter(ot -> ot.getStatus().equals(ShippingConnectionStatus.SUCCESS))
                .map(data -> OrderTrackingEntity.builder()
                        .packageId(data.getOrderId())
                        .shipmentId(data.getShipmentCode())
                        .content(data.getMessage())
                        .orderStatus(data.getOrderStatus())
                        .time(currentTime)
                        .source(Constants.SOURCE_VIPO)
                        .uniqueKey(String.format("%s_VIPO_%s_%s", data.getOrderId(), data.getOrderStatus(), currentTime))
                        .build()).toList();
        orderTrackingEntityRepository.saveAll(orderTrackings);
        log.info("[prepareOrder] Order tracking logs saved successfully");
    }

    @Override
    public PrintLabelResponse printLabel(PrintLabelRequest request) {
        validatePrintLabelRequest(request);

        VipoUserDetails user = getCurrentUser();

        // Retrieve the list of order packages by IDs
        List<OrderPackageEntity> orderPackages = orderPackageRepository.findByMerchantIdAndIdIn(user.getId(), request.getOrderIds());
        if (ObjectUtils.isEmpty(orderPackages) || orderPackages.size() != request.getOrderIds().size()) {
            throw new VipoNotFoundException(Constants.ORDER_PACKAGE_NOT_FOUND);
        }

        // Validate the status of the order packages
        boolean allWaiting = orderPackages.stream()
                .allMatch(op -> SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.equals(op.getSellerOrderStatus()));
        if (!allWaiting) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.ORDER_NOT_IN_STATUS_ORDER_SHIPMENT_CONNECTION_SUCCESS);
        }

        // Validate if the paperSize and copies exist in the map
        log.info("[printLabel] PaperSize: {}, Copies: {}", request.getPaperSize(), request.getCopies());
        String key = PaperSize.getKey(
                PaperSize.valueOf(request.getPaperSize()),
                Copies.valueOf(request.getCopies())
        );
        log.info("[printLabel] Key: {}", key);
        if (ObjectUtils.isEmpty(key)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST,
                    Constants.INVALID_PAPER_SIZE_OR_NUMBER_OF_COPIES);
        }

        // Extract order codes (EVTP) from the order packages
        List<OrderShipmentEntity> orderShipmentEntities = orderShipmentEntityRepository
                .findByPackageIdInAndCreateOrderStatus(request.getOrderIds(), ShippingConnectionStatus.SUCCESS);

        if (ObjectUtils.isEmpty(orderShipmentEntities) || orderShipmentEntities.size() != request.getOrderIds().size()) {
            throw new VipoNotFoundException(Constants.ORDER_SHIPMENT_NOT_FOUND);
        }

        Map<Long, OrderShipmentEntity> orderShipmentEntityMap = orderShipmentEntities.stream()
                .collect(Collectors.toMap(OrderShipmentEntity::getPackageId, entity -> entity));

        // Create a map of orderPackages for quick access
        Map<Long, OrderPackageEntity> orderPackagesMap = orderPackages.stream()
                .collect(Collectors.toMap(OrderPackageEntity::getId, Function.identity()));

        // Sort and get the shipment codes based on the sortBy and sortDirection
        List<String> shipmentCodeList = sortAndGetShipmentCodes(
                request.getOrderIds(),
                PrintLabelSortBy.valueOf(request.getSortBy()),
                PrintLabelSortDirection.valueOf(request.getSortDirection()),
                orderPackagesMap,
                orderShipmentEntityMap
        );
        log.info("[printLabel] ShipmentCodeList: {}", shipmentCodeList);

        try {
            PrintLabelOrderRequest printLabelOrderRequest = PrintLabelOrderRequest.builder()
                    .orderArray(shipmentCodeList)
                    .build();

            // Call the external service to print the label and handle the response
            PrintLabelOrderResponse printLabelOrderResponse = evtpOrderService.printLabelOrder(printLabelOrderRequest);
            if (printLabelOrderResponse.getStatus() == 200 && !printLabelOrderResponse.isError() && !ObjectUtils.isEmpty(printLabelOrderResponse.getMessage())) {
                if (request.getConfirmPrintedStatus()) {
                    orderShipmentEntityRepository.updateIsPrinted(shipmentCodeList, true);
                }
                savePrintLabelOrderActivityHistory(request);
                String printCode = printLabelOrderResponse.getMessage();
                return PrintLabelResponse.builder()
                        .link(printLabelOrderDomainUrl.concat(PaperSize.getValue(key).replace("${code}", printCode)))
                        .build();
            }
            throw new VipoBusinessException();
        } catch (Exception e) {
            log.error("[printLabel] Failed to print label for order IDs: {}", request.getOrderIds(), e);
            throw new VipoBusinessException();
        }
    }

    private void validatePrintLabelRequest(PrintLabelRequest request) {
        if (ObjectUtils.isEmpty(request.getOrderIds())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.ORDER_IDS_NOT_EMPTY);
        }

        // Vui lòng in tối thiểu 1 đơn hàng và tối đa 100 đơn hàng
        if (request.getOrderIds().isEmpty() || request.getOrderIds().size() > 100) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_ORDER_MIN_MAX);
        }

        // Xác nhận trạng thái in không được để trống
        if (ObjectUtils.isEmpty(request.getConfirmPrintedStatus())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_STATUS_NOT_EMPTY);
        }

        if (ObjectUtils.isEmpty(request.getPaperSize())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_PAPER_SIZE_NOT_EMPTY);
        }

        // if the paperSize is not in enum type PaperSize
        if (!PaperSize.contains(request.getPaperSize())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_PAPER_SIZE_INVALID);
        }

        if (ObjectUtils.isEmpty(request.getCopies())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_NUMBER_OF_COPIES_NOT_EMPTY);
        }

        // if the copies is not in enum type Copies
        if (!Copies.contains(request.getCopies())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.PRINT_NUMBER_OF_COPIES_INVALID);
        }

        if (ObjectUtils.isEmpty(request.getSortBy()) || ObjectUtils.isEmpty(request.getSortDirection())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.SORT_TYPE_NOT_EMPTY);
        }

        // if the sortBy is not in enum type PrintLabelSortBy or the sortDirection is not in enum type PrintLabelSortDirection
        if (!PrintLabelSortBy.contains(request.getSortBy()) || !PrintLabelSortDirection.contains(request.getSortDirection())) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.SORT_TYPE_INVALID);
        }
    }

    private List<String> sortAndGetShipmentCodes(List<Long> orderIds, PrintLabelSortBy sortBy, PrintLabelSortDirection sortDirection,
                                                 Map<Long, OrderPackageEntity> orderPackagesMap, Map<Long, OrderShipmentEntity> orderShipmentEntityMap) {

        // Create comparator based on sortBy
        Comparator<Long> comparator;
        if (sortBy.equals(PrintLabelSortBy.ORDER_CODE)) {
            comparator = Comparator.<Long, LocalDateTime>comparing((Long id) -> {
                OrderPackageEntity op = orderPackagesMap.get(id);
                return op != null
                        ? DateUtils.convertEpochSecondsToLocalDateTime(op.getCreateTime())
                        : LocalDateTime.MIN;
            }).thenComparingLong(id -> id);
        } else {
            comparator = Comparator.<Long, LocalDateTime>comparing((Long id) -> {
                OrderShipmentEntity os = orderShipmentEntityMap.get(id);
                return os != null ? os.getShipmentDate() : LocalDateTime.MIN;
            }).thenComparingLong(id -> id);
        }

        // Reverse the comparator if sortDirection is DESC
        if (sortDirection.equals(PrintLabelSortDirection.DESC)) {
            comparator = comparator.reversed();
        }

        // Sort orderIds based on comparator
        List<Long> sortedOrderIds = orderIds.stream()
                .sorted(comparator)
                .toList();

        // Get shipmentCodeList based on sortedOrderIds
        List<String> shipmentCodeList = sortedOrderIds.stream()
                .map(id -> {
                    OrderShipmentEntity os = orderShipmentEntityMap.get(id);
                    return os != null ? os.getShipmentCode() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return shipmentCodeList;
    }

    private void savePrintLabelOrderActivityHistory(PrintLabelRequest request) {
        List<OrderActivityHistoryEntity> histories = request.getOrderIds().stream()
                .map(orderId -> OrderActivityHistoryEntity.builder()
                        .orderId(orderId)
                        .type(ActivityType.PRINT_LABEL)
                        .details(PrintLabelData.builder()
                                .orderId(orderId)
                                .paperSize(PaperSize.valueOf(request.getPaperSize()))
                                .copies(Copies.valueOf(request.getCopies()))
                                .confirmPrintedStatus(request.getConfirmPrintedStatus())
                                .build())
                        .beforeState(null)
                        .afterState(null)
                        .build())
                .collect(Collectors.toList());
        orderActivityHistoryEntityRepository.saveAll(histories);
        log.info("[printLabel] Print label order activity history saved successfully");
    }

    @Override
    @Transactional
    public OrderRefuseCancelResponse cancelOrder(List<OrderRefuseCancelRequest> request) {
        /* validate refuse request*/
        Long merchantId = getCurrentUser().getId();

        List<OrderPackageEntity> orderPackages
                = orderPackageRepository.findAllByMerchantIdAndIdIn(
                merchantId, request.stream().map(r -> Long.parseLong(r.getOrderId())).toList()
        );
        // in case we could not find any order package
        if (ObjectUtils.isEmpty(orderPackages)) {
            return OrderRefuseCancelResponse.builder()
                    .failed(
                            request.stream().map(
                                    refuseRequest
                                            -> OrderRefuseCancelInfo.builder()
                                            .orderCode(refuseRequest.getOrderId())
                                            .reasonNote(Constants.ORDER_NOT_FOUND)
                                            .build()
                            ).toList()
                    )
                    .build();
        }
        //check if any requests that are not found in order packages
        List<OrderRefuseCancelRequest> notFoundOrderPackagesRequest = request.stream()
                .filter(r -> orderPackages.stream().noneMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))))
                .toList();
        //form the fail list
        List<OrderRefuseCancelInfo> failList
                = notFoundOrderPackagesRequest.stream().map(
                refuseRequest
                        -> OrderRefuseCancelInfo.builder()
                        .orderCode(refuseRequest.getOrderId())
                        .reasonCode(Constants.ORDER_NOT_FOUND)
                        .build()
        ).collect(Collectors.toList());

        //get all requests that are found
        List<OrderRefuseCancelRequest> foundedRequests = request.stream()
                .filter(r -> orderPackages.stream().anyMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))))
                .toList();

        //validate the status of the order packages
        List<OrderPackageEntity> validStatusOrderPackageEntities = orderPackages.stream()
                .filter(
                        op -> SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION.equals(op.getSellerOrderStatus())
                                || SellerOrderStatus.ORDER_PREPARED.equals(op.getSellerOrderStatus())
                                || SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.equals(op.getSellerOrderStatus())
                )
                .toList();
        Map<Long, String> orderPackageIdToOrderCode = orderPackages.stream().collect(Collectors.toMap(
                OrderPackageEntity::getId, OrderPackageEntity::getOrderCode, (existing, replacement) -> existing
        ));

        //form the fail list
        List<OrderRefuseCancelInfo> notValidSellerOrderStatusOrderPackages
                = foundedRequests.stream().filter(
                r -> validStatusOrderPackageEntities.stream().noneMatch(
                        op -> op.getId().equals(Long.parseLong(r.getOrderId()))
                )
        ).map(
                foundRequest -> OrderRefuseCancelInfo.builder()
                        .orderCode(orderPackageIdToOrderCode.get(Long.valueOf(foundRequest.getOrderId())))
                        .reasonNote(Constants.STATUS_ORDER_CHANGED)
                        .build()
        ).toList();
        failList.addAll(notValidSellerOrderStatusOrderPackages);

        //form the valid list
        List<OrderRefuseCancelRequest> validRequests
                = foundedRequests.stream().filter(
                r -> validStatusOrderPackageEntities.stream()
                        .anyMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))
                        )).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(validRequests))
            return OrderRefuseCancelResponse.builder().failed(failList).build();
        OrderRefuseCancelResponse response = refuseAndCancelOrder(validRequests, true);
        failList.addAll(response.getFailed());
        response.setFailed(failList);
        return response;
    }

    private OrderRefuseCancelResponse refuseAndCancelOrder(List<OrderRefuseCancelRequest> request, boolean isCancel) {
        // Extract the order IDs from the request list and convert them to Long values
        List<Long> orderIds = request.stream().map(s -> Long.parseLong(s.getOrderId())).toList();

        // Fetch all order packages using the extracted order IDs
        List<OrderPackageEntity> orderPackageList = orderPackageRepository.findAllById(orderIds);

        // Determine the action status based on whether it's a cancel or reject operation
        SellerOrderStatus actionStatus = isCancel ? SellerOrderStatus.ORDER_CANCELLED_BY_SELLER : SellerOrderStatus.SELLER_REJECTED_ORDER;

        // Filter the order packages that can be processed (not refused by the buyer and not cancelled by the seller)
        List<OrderPackageEntity> orderCanNotRefuseList = orderPackageList.stream()
                .filter(o ->
                        // Exclude orders that have already been refused by the buyer
                        OrderConstant.REFUSE_STATUS_BY_BUYER.contains(o.getOrderStatus())
                                // Include orders that were previously cancelled by the seller
                                || o.getSellerOrderStatus().equals(actionStatus)
                )
                .toList();

        // Initialize lists to store out-of-stock SKU IDs and order activity history entities
        List<Long> oosOrderSkuIds = new ArrayList<>();
        List<OrderActivityHistoryEntity> activityHistories = new ArrayList<>();

        // Define the status and reason strings for the completed operation
        String statusCompleted = isCancel ? OrderConstant.STATUS_CANCELED : OrderConstant.STATUS_REJECTED;
        String reasonCompleted = isCancel ? OrderConstant.ORDER_CANCEL_COMPLETED : OrderConstant.ORDER_REJECT_COMPLETED;
        String reasonFailed = OrderConstant.STATUS_ORDER_CHANGED;
        String orderStatus = isCancel ? OrderConstant.ORDER_STATUS_SELLER_CANCEL_ORDER : OrderConstant.ORDER_STATUS_SELLER_REJECT_ORDER;
        ActivityType activityType = isCancel ? ActivityType.ORDER_CANCEL : ActivityType.ORDER_REJECT;
        // Create a response object to store completed and failed results
        OrderRefuseCancelResponse res = new OrderRefuseCancelResponse();

        // Process each order in the request
        for (OrderRefuseCancelRequest order : request) {
            // Parse the order ID from the request
            Long orderId = Long.parseLong(order.getOrderId());

            // Find the corresponding order package from the list using the order ID
            Optional<OrderPackageEntity> orderPackage = orderPackageList.stream().filter(o -> o.getId().equals(orderId)).findFirst();

            // Check if the order package exists and can be processed (not in refuse or cancel status)
            if (orderPackage.isPresent() && !orderCanNotRefuseList.contains(orderPackage.get())) {
                // Clone the original order package for activity logging
                OrderPackageEntity orderPackageClone = cloneOrderPackage(orderPackage.get());

                // Update the order package status based on the action (cancel or reject)
                OrderPackageEntity orderPackageUpdate = orderPackage.get();
                orderPackageUpdate.setSellerOrderStatus(actionStatus);
                orderPackageUpdate.setOrderStatus(orderStatus);

                // Handle the case where the reason for cancel/reject is "out of stock"
                if (order.getReasonCode().equals(OrderRefuseCancelType.OUT_OF_STOCK)) {
                    // Ensure that SKU IDs are selected for out-of-stock rejection
                    if (CollectionUtils.isEmpty(order.getSkuIds())) {
                        // Add the failed order to the response if no SKU IDs are provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, OrderConstant.REQUIRED_SELECT_SKU_OFS));
                        continue;
                    }
                    // Add the out-of-stock SKU IDs to the list
                    oosOrderSkuIds.addAll(order.getSkuIds());
                    orderPackageUpdate.setCancelNote(OrderConstant.SELLER_REJECT_CANCEL_OUT_OF_STOCK);
                } else {
                    // Ensure that a reason note is provided for cancel/reject
                    if (DataUtils.isNullOrEmpty(order.getReasonNote())) {
                        // Add the failed order to the response if no reason note is provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, String.format(OrderConstant.ORDER_CANCEL_REJECT_REASON_NOT_BLANK, isCancel ? OrderConstant.ORDER_CANCEL_NAME : OrderConstant.ORDER_REJECT_NAME)));
                        continue;
                    }
                    // Set the provided reason note for the cancel/reject action
                    orderPackageUpdate.setCancelNote(order.getReasonNote());
                }
                orderPackageUpdate.setCancelTime(DateUtils.convertMilTimeToSecond(System.currentTimeMillis()));

                // Record the activity history for this action
                activityHistories.add(OrderActivityHistoryEntity.builder()
                        .orderId(orderId)
                        .type(activityType)
                        .beforeState(orderPackageClone)
                        .afterState(cloneOrderPackage(orderPackageUpdate))
                        .build());

                orderPackageUpdate.setOrderStatus(OrderConstant.ORDER_STATUS_SELLER_CANCEL_ORDER);
                // Save the updated order package to the repository
                orderPackageRepository.save(orderPackageUpdate);

                // Add the completed result to the response
                res.setCompleted(new OrderRefuseCancelInfo(orderPackageUpdate.getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), statusCompleted, reasonCompleted));
            } else {
                // Add the failed order to the response if the package cannot be processed
                res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, reasonFailed));
            }
        }

        // If any out-of-stock SKU IDs were processed, reset the quantities in the inventory
        if (!CollectionUtils.isEmpty(oosOrderSkuIds)) {
            productSellerSkuRepository.resetQuantityByIdIn(oosOrderSkuIds);
        }

        // Save all the activity history entities to the repository
        if (!CollectionUtils.isEmpty(activityHistories)) {
            orderActivityHistoryEntityRepository.saveAll(activityHistories);
        }

        // Return the response with completed and failed orders
        return res;
    }

    @Override
    @Transactional
    public OrderRefuseCancelResponse refuseOrder(List<OrderRefuseCancelRequest> request) {
        /* validate refuse request*/
        Long merchantId = getCurrentUser().getId();

        List<OrderPackageEntity> orderPackages
                = orderPackageRepository.findAllByMerchantIdAndIdIn(
                merchantId, request.stream().map(r -> Long.parseLong(r.getOrderId())).toList()
        );
        // in case we could not find any order package
        if (ObjectUtils.isEmpty(orderPackages)) {
            return OrderRefuseCancelResponse.builder()
                    .failed(
                            request.stream().map(
                                    refuseRequest
                                            -> OrderRefuseCancelInfo.builder()
                                            .orderCode(refuseRequest.getOrderId())
                                            .reasonNote(Constants.ORDER_NOT_FOUND)
                                            .build()
                            ).toList()
                    )
                    .build();
        }
        //check if any requests that are not found in order packages
        List<OrderRefuseCancelRequest> notFoundOrderPackagesRequest = request.stream()
                .filter(r -> orderPackages.stream().noneMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))))
                .toList();
        //form the fail list
        List<OrderRefuseCancelInfo> failList
                = notFoundOrderPackagesRequest.stream().map(
                refuseRequest
                        -> OrderRefuseCancelInfo.builder()
                        .orderCode(refuseRequest.getOrderId())
                        .reasonCode(Constants.ORDER_NOT_FOUND)
                        .build()
        ).collect(Collectors.toList());

        //get all requests that are found
        List<OrderRefuseCancelRequest> foundedRequests = request.stream()
                .filter(r -> orderPackages.stream().anyMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))))
                .toList();

        //validate the status of the order packages
        List<OrderPackageEntity> validStatusOrderPackageEntities = orderPackages.stream()
                .filter(op -> SellerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION.equals(op.getSellerOrderStatus()))
                .toList();

        Map<Long, String> orderPackageIdToOrderCode = orderPackages.stream().collect(Collectors.toMap(
                OrderPackageEntity::getId, OrderPackageEntity::getOrderCode, (existing, replacement) -> existing
        ));

        //form the fail list
        List<OrderRefuseCancelInfo> notValidSellerOrderStatusOrderPackages
                = foundedRequests.stream().filter(
                r -> validStatusOrderPackageEntities.stream().noneMatch(
                        op -> op.getId().equals(Long.parseLong(r.getOrderId()))
                )
        ).map(
                foundRequest -> OrderRefuseCancelInfo.builder()
                        .orderCode(orderPackageIdToOrderCode.get(Long.valueOf(foundRequest.getOrderId())))
                        .reasonNote(Constants.STATUS_ORDER_CHANGED)
                        .build()
        ).toList();
        failList.addAll(notValidSellerOrderStatusOrderPackages);

        //form the valid list
        List<OrderRefuseCancelRequest> validRequests
                = foundedRequests.stream().filter(
                r -> validStatusOrderPackageEntities.stream()
                        .anyMatch(op -> op.getId().equals(Long.parseLong(r.getOrderId()))
                        )).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(validRequests))
            return OrderRefuseCancelResponse.builder().failed(failList).build();

        //todo: sao lại viết chung vào 1 hàm, tách hàm, nếu mai sau có cập nhật gì vào business thì sao
        OrderRefuseCancelResponse response = refuseAndCancelOrder(validRequests, false);
        failList.addAll(response.getFailed());
        response.setFailed(failList);
        return response;
    }

    @Override
    public List<SellerOrderStatusResponse> getAllOrderSellerManagementStatus() {
        // Get the current user's details
        VipoUserDetails user = getCurrentUser();

        // Count the number of seller orders by MerchantId
        List<SellerOrderStatusProjection> sellerOrderStatus = repo.countSellerOrderbyMerchantId(user.getId());

        // Create a list of seller order status responses
        List<SellerOrderStatusResponse> res = new LinkedList<>();

        // Add the "All" status with the total number of orders to the response list
        res.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                sellerOrderStatus.stream().mapToLong(SellerOrderStatusProjection::getToltalOrder).sum(), null, true));

        // Iterate through each order status and add to the response list
        res.addAll(sellerOrderStatus.stream().map(sos -> {
            // Create a new seller order status response object
            SellerOrderStatusResponse orderStatus = new SellerOrderStatusResponse();
            orderStatus.setTotalOrder(sos.getToltalOrder()); // Set the total number of orders
            //todo: remove order count for RETURN_REFUND_CANCEL | IN_TRANSIT | DELIVERED status because it is not ready in phase 5
            List<Integer> tabNotReady = List.of(
                    OrderFilterTab.RETURN_REFUND_CANCEL.getValue()
//                    ,OrderFilterTab.IN_TRANSIT.getValue(),
//                    OrderFilterTab.DELIVERED.getValue()
                    );
            if (tabNotReady.contains(sos.getStatusGroup())) {
                orderStatus.setTotalOrder(0L);
                orderStatus.setAccessible(false); //todo: enable when the phase supports
            }

            orderStatus.setName(OrderFilterTab.getLableFromValue(sos.getStatusGroup())); // Set the status name
            orderStatus.setCode(OrderFilterTab.getFromValue(sos.getStatusGroup()).toString()); // Set the status code
            // If the status is "Waiting for shipment", add child statuses
            if (sos.getStatusGroup().equals(OrderFilterTab.WAITING_SHIPMENT.getValue())) {
                // Count the number of waiting shipment orders by MerchantId
                List<SellerOrderStatusResponse> waitingShipmentStatus = repo.countWaitingShipmentSellerOrderbyMerchantId(user.getId(), OrderFilterTab.WAITING_SHIPMENT.getValue());

                // Create a list of child statuses
                List<SellerOrderStatusResponse> wSS = new LinkedList<>();
                wSS.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                        waitingShipmentStatus.stream().mapToLong(SellerOrderStatusResponse::getTotalOrder).sum(), null, true));
                Long totalOrder;
                Optional<SellerOrderStatusResponse> getTotalOfOSCS = waitingShipmentStatus.stream().filter(s -> s.getCode().equals(SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.toString())).findFirst();
                if (getTotalOfOSCS.isPresent()) {
                    totalOrder = getTotalOfOSCS.get().getTotalOrder();
                } else {
                    totalOrder = 0L;
                }
                List<SellerOrderStatusResponse> childs = new ArrayList<>();
                childs.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                        waitingShipmentStatus.stream().mapToLong(SellerOrderStatusResponse::getTotalOrder).sum(), null, true));
                childs.addAll(
                        waitingShipmentStatus.stream().filter(s -> !s.getCode().equals(SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.toString())).map(b -> {
                            if (b.getCode().equals(SellerOrderStatus.ORDER_PREPARED.toString())) {
                                b.setTotalOrder(b.getTotalOrder() + totalOrder);
                            }
                            return b;
                        }).toList());
                // Set the child statuses for the "Waiting for shipment" status
                orderStatus.setChildStatus(childs);
            }

            return orderStatus; // Return the seller order status response object
        }).toList());

        return res; // Return the list of seller order status responses
    }

    @Override
    public List<SellerOrderStatusResponse> getAllOrderSellerManagementStatusV2(){

        // Get the current user's details
        VipoUserDetails user = getCurrentUser();

        // Count the number of seller orders by MerchantId
        List<SellerOrderStatusProjection> sellerOrderStatus = repo.countSellerOrderbyMerchantIdV2(user.getId());


        // Create a list of seller order status responses
        List<SellerOrderStatusResponse> res = new LinkedList<>();

        // Add the "All" status with the total number of orders to the response list
        res.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                sellerOrderStatus.stream().mapToLong(SellerOrderStatusProjection::getToltalOrder).sum(), null, true));

        for(OrderFilterTab tab : OrderFilterTab.values()){
            SellerOrderStatusResponse orderStatus = new SellerOrderStatusResponse();
           Optional<SellerOrderStatusProjection> sellerStatus = sellerOrderStatus.stream().filter(sos -> sos.getStatusGroup().equals(tab.getValue())).findFirst();

           if(sellerStatus.isPresent()){
               orderStatus.setTotalOrder(sellerStatus.get().getToltalOrder()); // Set the total number of orders
               if(tab.equals(OrderFilterTab.WAITING_SHIPMENT)){
                   // Count the number of waiting shipment orders by MerchantId
                   List<SellerOrderStatusResponse> waitingShipmentStatus = repo.countWaitingShipmentSellerOrderbyMerchantId(user.getId(), OrderFilterTab.WAITING_SHIPMENT.getValue());

                   // Create a list of child statuses
                   List<SellerOrderStatusResponse> wSS = new LinkedList<>();
                   wSS.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                           waitingShipmentStatus.stream().mapToLong(SellerOrderStatusResponse::getTotalOrder).sum(), null, true));
                   Long totalOrder;
                   Optional<SellerOrderStatusResponse> getTotalOfOSCS = waitingShipmentStatus.stream().filter(s -> s.getCode().equals(SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.toString())).findFirst();
                   if (getTotalOfOSCS.isPresent()) {
                       totalOrder = getTotalOfOSCS.get().getTotalOrder();
                   } else {
                       totalOrder = 0L;
                   }
                   List<SellerOrderStatusResponse> childs = new ArrayList<>();
                   childs.add(new SellerOrderStatusResponse(null, OrderConstant.SELLER_TAB_ALL,
                           waitingShipmentStatus.stream().mapToLong(SellerOrderStatusResponse::getTotalOrder).sum(), null, true));
                   childs.addAll(
                           waitingShipmentStatus.stream().filter(s -> !s.getCode().equals(SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS.toString())).map(b -> {
                               if (b.getCode().equals(SellerOrderStatus.ORDER_PREPARED.toString())) {
                                   b.setTotalOrder(b.getTotalOrder() + totalOrder);
                               }
                               return b;
                           }).toList());
                   // Set the child statuses for the "Waiting for shipment" status
                   orderStatus.setChildStatus(childs);
               }

           }else{
               orderStatus.setTotalOrder(0L);
               if(tab.equals(OrderFilterTab.WAITING_SHIPMENT)){
                   List<String> tabNotReady = List.of(
                           OrderConstant.SELLER_TAB_ALL,
                           SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION.getDescription(),
                           SellerOrderStatus.ORDER_PREPARED.getDescription()
                   );
                   orderStatus.setChildStatus(tabNotReady.stream().map(status -> new SellerOrderStatusResponse(
                           null,
                           status ,
                          0L,
                           null,
                           true)).toList());
               }
           }
            orderStatus.setName(tab.getLable()); // Set the status name
            orderStatus.setCode(tab.toString()); // Set the status code
            //todo: remove order count for RETURN_REFUND_CANCEL | IN_TRANSIT | DELIVERED status because it is not ready in phase 5
            List<OrderFilterTab> tabNotReady = List.of(
                    OrderFilterTab.RETURN_REFUND_CANCEL
//                    ,OrderFilterTab.IN_TRANSIT
//                    OrderFilterTab.DELIVERED
            );

            if (tabNotReady.contains(tab)) {
                orderStatus.setTotalOrder(0L);
                orderStatus.setAccessible(false); //todo: enable when the phase supports
            }

            res.add(orderStatus);
        }

        return res; // Return the list of seller order status responses

    }

    private List<String> rootStatusWithUnearnedRevenue() {
        return Arrays.asList(
                RootStatusEnum.WAIT_FOR_PAY.getCode(),
                RootStatusEnum.WAIT_FOR_PROCESS.getCode(),
                RootStatusEnum.WAIT_FOR_DELIVERY.getCode(),
                RootStatusEnum.DELIVERING.getCode(),
                RootStatusEnum.SUCCESSFUL_DELIVERY.getCode()
        );
    }

    private List<OrderStatusEntity> statusCodes(List<String> statusCodes) {
        return orderStatusRepository.findAllStatusCodeByInsRootCode(statusCodes);
    }

    private Page<OrderPackageResponse> getPageOfOrderPackages(List<String> statusCodes, Long merchantId, SearchOrderByKeywordRequest request) {
        Pageable pageRequest = PageRequest.of(request.getPageNo() - 1, request.getPageSize());
        return repo.getOrderPackageByFilters(
                statusCodes,
                DateUtils.convertMilTimeToSecond(request.getStartDate()),
                DateUtils.convertMilTimeToSecond(request.getEndDate()),
                merchantId,
                request.getSearchQuery(),
                request.getSearchBy(),
                pageRequest
        );
    }

    @Transactional
    public OrderRefuseCancelResponse rejectOrders(List<OrderRefuseCancelRequest> request) {
        Long merchantId = getCurrentUser().getId();
        // Extract the order IDs from the request list and convert them to Long values
        List<Long> orderIds = request.stream().map(s -> Long.parseLong(s.getOrderId())).toList();

        // Fetch all order packages using the extracted order IDs
        List<OrderPackageEntity> orderPackageList = orderPackageRepository.findByMerchantIdAndIdIn(merchantId, orderIds);

        // Determine the action status based on whether it's a cancel or reject operation
        SellerOrderStatus actionStatus = SellerOrderStatus.SELLER_REJECTED_ORDER;

        List<SellerOrderStatus> statusList = List.of(SellerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION);

        // Filter the order packages that can be processed (not refused by the buyer and not cancelled by the seller)
        List<OrderPackageEntity> orderCanNotRefuseList = orderPackageList.stream()
                .filter(o ->
                        // Exclude orders that have already been refused by the buyer
                        OrderConstant.REFUSE_STATUS_BY_BUYER.contains(o.getOrderStatus())
                                // Include orders that were previously cancelled by the seller
                                || o.getSellerOrderStatus().equals(actionStatus) ||
                                !statusList.contains(o.getSellerOrderStatus())
                )
                .toList();

        // Initialize lists to store out-of-stock SKU IDs and order activity history entities
        List<Long> oosOrderSkuIds = new ArrayList<>();
        List<PackageLogEntity> packageLogs = new ArrayList<>();
        List<OrderActivityHistoryEntity> activityHistories = new ArrayList<>();
        // Create a response object to store completed and failed results
        OrderRefuseCancelResponse res = new OrderRefuseCancelResponse();

        // Process each order in the request
        for (OrderRefuseCancelRequest order : request) {
            // Parse the order ID from the request
            Long orderId = Long.parseLong(order.getOrderId());

            // Find the corresponding order package from the list using the order ID
            Optional<OrderPackageEntity> orderPackage = orderPackageList.stream().filter(o -> o.getId().equals(orderId)).findFirst();

            // Check if the order package exists and can be processed (not in refuse or cancel status)
            if (orderPackage.isPresent() && !orderCanNotRefuseList.contains(orderPackage.get())) {
                // Clone the original order package for activity logging
                OrderPackageEntity orderPackageClone = cloneOrderPackage(orderPackage.get());

                // Update the order package status based on the action (cancel or reject)
                OrderPackageEntity orderPackageUpdate = orderPackage.get();
                orderPackageUpdate.setSellerOrderStatus(actionStatus);
                orderPackageUpdate.setOrderStatus(OrderConstant.ORDER_STATUS_SELLER_REJECT_ORDER);

                // Handle the case where the reason for cancel/reject is "out of stock"
                if (order.getReasonCode().equals(OrderRefuseCancelType.OUT_OF_STOCK)) {
                    // Ensure that SKU IDs are selected for out-of-stock rejection
                    if (CollectionUtils.isEmpty(order.getSkuIds())) {
                        // Add the failed order to the response if no SKU IDs are provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, OrderConstant.REQUIRED_SELECT_SKU_OFS));
                        continue;
                    }
                    // Add the out-of-stock SKU IDs to the list
                    oosOrderSkuIds.addAll(order.getSkuIds());
                    orderPackageUpdate.setCancelNote(OrderConstant.SELLER_REJECT_CANCEL_OUT_OF_STOCK);
                } else {
                    // Ensure that a reason note is provided for cancel/reject
                    if (DataUtils.isNullOrEmpty(order.getReasonNote())) {
                        // Add the failed order to the response if no reason note is provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, String.format(OrderConstant.ORDER_CANCEL_REJECT_REASON_NOT_BLANK, OrderConstant.ORDER_REJECT_NAME)));
                        continue;
                    }
                    // Set the provided reason note for the cancel/reject action
                    orderPackageUpdate.setCancelNote(order.getReasonNote());
                }
                orderPackageUpdate.setCancelTime(DateUtils.convertMilTimeToSecond(System.currentTimeMillis()));
                // Record the activity history for this action
                OrderPackageEntity orderPackage023 = cloneOrderPackage(orderPackageUpdate);
                activityHistories.add(OrderActivityHistoryEntity.builder()
                        .orderId(orderId)
                        .type(ActivityType.ORDER_REJECT)
                        .beforeState(orderPackageClone)
                        .afterState(orderPackage023)
                        .build());

                orderPackageUpdate.setOrderStatus(OrderConstant.ORDER_STATUS_SELLER_CANCEL_ORDER);
                // Save the updated order package to the repository
                orderPackageRepository.save(orderPackageUpdate);
                activityHistories.add(OrderActivityHistoryEntity.builder()
                        .orderId(orderId)
                        .type(ActivityType.ORDER_REJECT)
                        .beforeState(orderPackage023)
                        .afterState(orderPackageUpdate)
                        .build());

                // Add the completed result to the response
                res.setCompleted(new OrderRefuseCancelInfo(orderPackageUpdate.getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_REJECTED,  OrderConstant.ORDER_REJECT_COMPLETED));
                // add package log
                packageLogs.add(new PackageLogEntity(
                        orderPackageUpdate.getSellerOrderStatus().getDescription(),
                        null,
                        JsonMapperUtils.writeValueAsString(orderPackageUpdate),
                        merchantId,
                        orderPackageUpdate.getCustomerId(),
                        1,
                        orderPackageUpdate.getId()
                ));
            } else {
                // Add the failed order to the response if the package cannot be processed
                res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, OrderConstant.STATUS_ORDER_CHANGED));
            }
        }

        // If any out-of-stock SKU IDs were processed, reset the quantities in the inventory
        if (!CollectionUtils.isEmpty(oosOrderSkuIds)) {
            productSellerSkuRepository.resetQuantityByIdIn(oosOrderSkuIds);
        }

        // Save all the activity history entities to the repository
        if (!CollectionUtils.isEmpty(activityHistories)) {
            orderActivityHistoryEntityRepository.saveAll(activityHistories);
        }

        // save package logs
        savePackageLog(packageLogs);

        // Return the response with completed and failed orders
        return res;
    }

    @Transactional
    public OrderRefuseCancelResponse cancelOrders(List<OrderRefuseCancelRequest> request) {
        Long merchantId = getCurrentUser().getId();
        // Extract the order IDs from the request list and convert them to Long values
        List<Long> orderIds = request.stream().map(s -> Long.parseLong(s.getOrderId())).toList();

        // Fetch all order packages using the extracted order IDs
        List<OrderPackageEntity> orderPackageList = orderPackageRepository.findAllByMerchantIdAndIdIn(merchantId, orderIds);

        // Determine the action status based on whether it's a cancel or reject operation

        List<SellerOrderStatus> statusList = List.of(SellerOrderStatus.WAITING_FOR_ORDER_PREPARATION, SellerOrderStatus.ORDER_PREPARED, SellerOrderStatus.ORDER_SHIPMENT_CONNECTION_SUCCESS);

        // Filter the order packages that can be processed (not refused by the buyer and not cancelled by the seller)
        List<OrderPackageEntity> orderCanNotRefuseList = orderPackageList.stream()
                .filter(o ->
                        // Exclude orders that have already been refused by the buyer
                        OrderConstant.REFUSE_STATUS_BY_BUYER.contains(o.getOrderStatus())
                                // Include orders that were previously cancelled by the seller
                                || o.getSellerOrderStatus().equals(SellerOrderStatus.ORDER_CANCELLED_BY_SELLER) ||
                                !statusList.contains(o.getSellerOrderStatus())
                )
                .toList();

        // Initialize lists to store out-of-stock SKU IDs and order activity history entities
        List<Long> oosOrderSkuIds = new ArrayList<>();
        List<PackageLogEntity> packageLogs = new ArrayList<>();
        List<OrderActivityHistoryEntity> activityHistories = new ArrayList<>();

        OrderRefuseCancelResponse res = new OrderRefuseCancelResponse();

        // Process each order in the request
        for (OrderRefuseCancelRequest order : request) {
            // Parse the order ID from the request
            Long orderId = Long.parseLong(order.getOrderId());

            // Find the corresponding order package from the list using the order ID
            Optional<OrderPackageEntity> orderPackage = orderPackageList.stream().filter(o -> o.getId().equals(orderId)).findFirst();

            // Check if the order package exists and can be processed (not in refuse or cancel status)
            if (orderPackage.isPresent() && !orderCanNotRefuseList.contains(orderPackage.get())) {
                // Clone the original order package for activity logging
                OrderPackageEntity orderPackageClone = cloneOrderPackage(orderPackage.get());

                // Update the order package status based on the action (cancel or reject)
                OrderPackageEntity orderPackageUpdate = orderPackage.get();
                orderPackageUpdate.setSellerOrderStatus(SellerOrderStatus.ORDER_CANCELLED_BY_SELLER);
                orderPackageUpdate.setOrderStatus(OrderConstant.ORDER_STATUS_SELLER_CANCEL_ORDER);

                // Handle the case where the reason for cancel/reject is "out of stock"
                if (order.getReasonCode().equals(OrderRefuseCancelType.OUT_OF_STOCK)) {
                    // Ensure that SKU IDs are selected for out-of-stock rejection
                    if (CollectionUtils.isEmpty(order.getSkuIds())) {
                        // Add the failed order to the response if no SKU IDs are provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, OrderConstant.REQUIRED_SELECT_SKU_OFS));
                        continue;
                    }
                    // Add the out-of-stock SKU IDs to the list
                    oosOrderSkuIds.addAll(order.getSkuIds());
                    orderPackageUpdate.setCancelNote(OrderConstant.SELLER_REJECT_CANCEL_OUT_OF_STOCK);
                } else {
                    // Ensure that a reason note is provided for cancel/reject
                    if (DataUtils.isNullOrEmpty(order.getReasonNote())) {
                        // Add the failed order to the response if no reason note is provided
                        res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED, String.format(OrderConstant.ORDER_CANCEL_REJECT_REASON_NOT_BLANK, OrderConstant.ORDER_CANCEL_NAME)));
                        continue;
                    }
                    // Set the provided reason note for the cancel/reject action
                    orderPackageUpdate.setCancelNote(order.getReasonNote());
                }
                orderPackageUpdate.setCancelTime(DateUtils.convertMilTimeToSecond(System.currentTimeMillis()));
                // Save the updated order package to the repository
                orderPackageRepository.save(orderPackageUpdate);

                // Record the activity history for this action
                activityHistories.add(OrderActivityHistoryEntity.builder()
                        .orderId(orderId)
                        .type(ActivityType.ORDER_CANCEL)
                        .beforeState(orderPackageClone)
                        .afterState(cloneOrderPackage(orderPackageUpdate))
                        .build());

                // Add the completed result to the response
                res.setCompleted(new OrderRefuseCancelInfo(orderPackageUpdate.getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_CANCELED, OrderConstant.ORDER_CANCEL_COMPLETED));
                // add package log
                packageLogs.add(new PackageLogEntity(
                        orderPackageUpdate.getSellerOrderStatus().getDescription(),
                        null,
                        JsonMapperUtils.writeValueAsString(orderPackageUpdate),
                        merchantId,
                        orderPackageUpdate.getCustomerId(),
                        1,
                        orderPackageUpdate.getId()
                ));
            } else {
//                String reasonMsg = OrderConstant.STATUS_ORDER_CHANGED;
//                if(orderPackage.isPresent() && (OrderConstant.REFUSE_STATUS_BY_BUYER.contains(orderPackage.get().getOrderStatus()))){
//                    reasonMsg = OrderConstant.CAN_NOT_CANCEL_WHEN_ORDER_CANCELED;
//                }
                // Add the failed order to the response if the package cannot be processed
                res.setFailed(new OrderRefuseCancelInfo(orderPackage.get().getOrderCode(), order.getSkuIds(), order.getReasonCode().name(), OrderConstant.STATUS_FAILED,  OrderConstant.STATUS_ORDER_CHANGED));
            }
        }

        // If any out-of-stock SKU IDs were processed, reset the quantities in the inventory
        if (!CollectionUtils.isEmpty(oosOrderSkuIds)) {
            productSellerSkuRepository.resetQuantityByIdIn(oosOrderSkuIds);
        }

        // Save all the activity history entities to the repository
        if (!CollectionUtils.isEmpty(activityHistories)) {
            orderActivityHistoryEntityRepository.saveAll(activityHistories);
        }

        // save package logs
        savePackageLog(packageLogs);

        // Return the response with completed and failed orders
        return res;
    }



    private List<OrderDetailsResponse.PackageProduct> getPackageProductList(List<PackageProductEntity> productEntities, List<PriceAdjustmentSkuData.SkuDetails> skuDetails) {
        List<Long> listLazbaoSkuId = productEntities.stream().map(PackageProductEntity::getLazbaoSkuId).map(Long::valueOf).toList();
        Map<String, Boolean> productSellerSkus = productSellerSkuRepository.findAllByIdIn(listLazbaoSkuId).stream()
                .collect(Collectors.toMap(
                        p -> String.valueOf(p.getId()),
                        p -> p.getStock() == 0
                ));

        return productEntities.stream().map(pp -> {
            boolean isOutOfStock = productSellerSkus.getOrDefault(pp.getLazbaoSkuId(), false);
            OrderDetailsResponse.PackageProduct res = OrderDetailsResponse.PackageProduct.builder()
                    .id(pp.getId())
                    .productId(pp.getProductId())
                    .merchantId(pp.getMerchantId())
                    .sellerOpenId(pp.getSellerOpenId())
                    .skuId(pp.getLazbaoSkuId())
                    .image(pp.getSkuImageUrl())
                    .name(pp.getName())
                    .spec(OrderMapper.convertJsonToSpecResponse(pp.getSpecMap()))
                    .unitPrice(pp.getSkuPrice())
                    .quantity(pp.getQuantity())
                    .totalPrice(pp.getSkuPrice().multiply(BigDecimal.valueOf(pp.getQuantity())))
                    .isOutOfStock(isOutOfStock)
                    .build();
            if (!CollectionUtils.isEmpty(skuDetails)) {
                // TODO: HNam kiem tra lai phan nay
                skuDetails.stream().filter(sku -> sku.getSkuId().equals(pp.getLazbaoSkuId())).findFirst().ifPresent(sku -> {
                    res.setUnitPrice(ObjectUtils.isEmpty(sku.getPriceAfterAdjustment())
                            ? pp.getSkuPrice() : sku.getPriceAfterAdjustment());
                    res.setUnitPriceBeforeAdjustment(ObjectUtils.isEmpty(sku.getPriceBeforeAdjustment())
                            ? null : sku.getPriceBeforeAdjustment());
                    res.setTotalPriceBeforeAdjustment(ObjectUtils.isEmpty(sku.getPriceBeforeAdjustment())
                            ? null : sku.getPriceBeforeAdjustment().multiply(BigDecimal.valueOf(pp.getQuantity())));
                    res.setTotalPrice(ObjectUtils.isEmpty(sku.getPriceAfterAdjustment())
                            ? res.getTotalPrice() : sku.getPriceAfterAdjustment().multiply(BigDecimal.valueOf(pp.getQuantity())));
                });
            }
            return res;
        }).toList();
    }

    public BigDecimal getPriceAdjustmentTotal(Long orderId) {
        List<PriceAdjustmentHistoryResponse> values = getPriceAdjustmentHistory(orderId);
        if (!CollectionUtils.isEmpty(values)) {
            // If the first entry is of type PriceAdjustmentSkuData, process the individual SKU price adjustments
            if (values.get(0).getDetailsData() instanceof PriceAdjustmentSkuData) {
                PriceAdjustmentSkuData data = (PriceAdjustmentSkuData) values.get(0).getDetailsData();
                BigDecimal totalAfterAdjustment = BigDecimal.ZERO;
                for (PriceAdjustmentSkuData.SkuDetails detail : data.getSkus()) {
                    totalAfterAdjustment = totalAfterAdjustment.add(detail.getPriceBeforeAdjustment().subtract(detail.getPriceAfterAdjustment()).multiply(BigDecimal.valueOf(detail.getQuantity())));
                }
                return totalAfterAdjustment;
            } else {
                PriceAdjustmentTotalData data = (PriceAdjustmentTotalData) values.get(0).getDetailsData();
                return data.getAdjustedAmount();
            }
        }
        return BigDecimal.ZERO;
    }

    private void savePackageLog(List<PackageLogEntity> data){
        if(!CollectionUtils.isEmpty(data)){
            packageLogEntityRepository.saveAll(data);
        }
    }

    public boolean isVietnamMerchant(VipoUserDetails merchantinfo) {
        if (DataUtils.isNullOrEmpty(merchantinfo)) {
            return false;
        }
        String country = merchantRepository.getMerchantCountryCodeByMerchantId(merchantinfo.getId());
        return CountryEnum.VIETNAM.getCode().equalsIgnoreCase(country);
    }

}
