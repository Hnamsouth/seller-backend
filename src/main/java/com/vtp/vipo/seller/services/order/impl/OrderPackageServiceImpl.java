package com.vtp.vipo.seller.services.order.impl;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.OrderConstant;
import com.vtp.vipo.seller.common.dao.entity.OrderActivityHistoryEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderPackageEntity;
import com.vtp.vipo.seller.common.dao.entity.OrderTrackingEntity;
import com.vtp.vipo.seller.common.dao.entity.PackageLogEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderPackageEvent;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.projections.SellerPackageProductProjection;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.request.order.ApproveOrderPackagesRequest;
import com.vtp.vipo.seller.common.dto.response.order.ApproveOrderPackagesResponse;
import com.vtp.vipo.seller.common.dto.response.order.OrderPackageResAfterSellerAction;
import com.vtp.vipo.seller.common.enumseller.ActivityType;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.services.impl.base.BaseServiceImpl;
import com.vtp.vipo.seller.services.order.BuyerOrderStatusService;
import com.vtp.vipo.seller.services.order.OrderPackageService;
import com.vtp.vipo.seller.services.order.SellerOrderStatusService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OrderPackageService} that handles operations related to order packages,
 * specifically approving multiple order packages based on provided requests.
 *
 * <p>This service interacts with repositories to fetch and persist order package data,
 * utilizes state machine services to manage order status transitions, and constructs
 * appropriate responses indicating the success or failure of each approval operation.</p>
 *
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class OrderPackageServiceImpl extends BaseServiceImpl implements OrderPackageService {

    final OrderPackageRepository orderPackageRepository;

    final PackageProductRepository packageProductRepository;

    final SellerOrderStatusService sellerOrderStatusService;

    final BuyerOrderStatusService buyerOrderStatusService;

    final ApplicationContext applicationContext;

    final OrderActivityHistoryEntityRepository orderActivityHistoryEntityRepository;

    final OrderTrackingEntityRepository orderTrackingEntityRepository;

    final PackageLogEntityRepository packageLogEntityRepository;

    /**
     * Retrieves a proxy instance of {@link OrderPackageServiceImpl} from the application context.
     *
     * <p>This is useful for invoking methods that require transactional proxies or other AOP features.</p>
     *
     * @return a proxy instance of {@link OrderPackageServiceImpl}
     */
    private OrderPackageServiceImpl getProxy() {
        return applicationContext.getBean(OrderPackageServiceImpl.class);
    }

    /**
     * Approves multiple order packages based on the provided request.
     *
     * <p>This method performs the following steps:
     * <ol>
     *     <li>Retrieves the current user's merchant information.</li>
     *     <li>Fetches the requested order packages from the repository.</li>
     *     <li>Identifies and marks any requested order packages that are not found.</li>
     *     <li>Filters out order packages that are not in an eligible state for approval.</li>
     *     <li>Validates the presence of package products associated with the eligible order packages.</li>
     *     <li>Checks for SKU stock availability within the package products.</li>
     *     <li>Approves eligible order packages and constructs the response.</li>
     * </ol>
     * </p>
     *
     * @param approveOrderPackagesRequest the request payload containing details of the order packages to approve
     * @return an {@link ApproveOrderPackagesResponse} object containing the results of the approval operation
     */
    @Override
    public ApproveOrderPackagesResponse approveOrderPackages(
            @NotNull @Valid ApproveOrderPackagesRequest approveOrderPackagesRequest
    ) {

        // Retrieve the currently authenticated user's merchant information
        VipoUserDetails merchantInfo = getCurrentUser();

        // Extract the list of order package IDs to be approved from the request
        List<Long> requestedOrderIds = approveOrderPackagesRequest.getOrderPackageIds();

        // Fetch the order package entities that belong to the merchant and match the requested IDs
        List<OrderPackageEntity> orderPackageEntities
                = orderPackageRepository.findByMerchantIdAndIdIn(merchantInfo.getId(), requestedOrderIds);

        // If no order packages are found, mark all requested IDs as failed with appropriate reason
        if (ObjectUtils.isEmpty(orderPackageEntities))
            return ApproveOrderPackagesResponse.builder()
                    .failList(
                            requestedOrderIds.stream()
                                    .map(
                                            orderPackageId
                                                    -> buildSellerStatusActionResponse(
                                                    orderPackageId,
                                                    null,
                                                    false,
                                                    Constants.ORDER_PACKAGE_NOT_FOUND
                                            )
                                    ).toList()
                    )
                    .build();

        // Extract the IDs of the found order packages
        List<Long> foundedOrderIds
                = orderPackageEntities.stream().map(OrderPackageEntity::getId).toList();

        // Initialize the fail list with order packages that were requested but not found
        List<OrderPackageResAfterSellerAction> failList = new ArrayList<>(requestedOrderIds.stream()
                .filter(requestedId -> !foundedOrderIds.contains(requestedId))
                .map(
                        requestedId
                                -> buildSellerStatusActionResponse(
                                requestedId,
                                null,
                                false,
                                Constants.ORDER_PACKAGE_NOT_FOUND
                        )
                ).toList());

        // Identify and mark order packages that are not in the 'WAITING_FOR_SELLER_CONFIRMATION' status
        failList.addAll(
                orderPackageEntities.stream()
                        .filter(
                                orderPackageEntity
                                        -> ObjectUtils.isEmpty(orderPackageEntity.getSellerOrderStatus())
                                        || !orderPackageEntity.getSellerOrderStatus()
                                        .equals(SellerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION)
                        ).map(
                                orderPackageEntity
                                        -> buildSellerStatusActionResponse(
                                        orderPackageEntity, false, OrderConstant.STATUS_ORDER_CHANGED
                                )
                        )
                        .toList()
        );

        // Filter out order packages that are eligible for approval based on their current status
        List<OrderPackageEntity> validStatusForApprovalOrderPackages
                = orderPackageEntities.stream()
                .filter(
                        orderPackageEntity
                                -> ObjectUtils.isNotEmpty(orderPackageEntity.getSellerOrderStatus())
                                && orderPackageEntity.getSellerOrderStatus()
                                .equals(SellerOrderStatus.WAITING_FOR_SELLER_CONFIRMATION)
                )
                .toList();

        // If no order packages are eligible for approval, return the fail list
        if (ObjectUtils.isEmpty(validStatusForApprovalOrderPackages))
            return ApproveOrderPackagesResponse.builder().failList(failList).build();

        // Retrieve package product projections for the eligible order packages
        List<SellerPackageProductProjection> sellerPackageProductProjections
                = packageProductRepository.getPackageProducByPackageIdIn(
                validStatusForApprovalOrderPackages.stream().map(OrderPackageEntity::getId).toList()
        );

        // If no package products are found, mark the eligible order packages as failed
        if (ObjectUtils.isEmpty(sellerPackageProductProjections)) {
            failList.addAll(
                    validStatusForApprovalOrderPackages.stream()
                            .map(
                                    orderPackageEntity -> buildSellerStatusActionResponse(
                                            orderPackageEntity,
                                            false,
                                            Constants.NO_PACKAGE_PRODUCTS_IN_THE_ORDER_PACKAGE_WARNING
                                    )
                            )
                            .toList()
            );
            return ApproveOrderPackagesResponse.builder().failList(failList).build();
        }

        // Extract package IDs that have associated package products
//        List<Long> sellerPackageProductPackageIds
//                = sellerPackageProductProjections.stream()
//                .filter(sellerPackageProductProjection -> ObjectUtils.isNotEmpty(sellerPackageProductProjection.getSkuId()))
//                .map(SellerPackageProductProjection::getPackageId).toList();
//
//        // Mark order packages without associated package products as failed
//        failList.addAll(
//                validStatusForApprovalOrderPackages.stream()
//                        .filter(orderPackageEntity -> !sellerPackageProductPackageIds.contains(orderPackageEntity.getId()))
//                        .map(
//                                orderPackageEntity -> buildSellerStatusActionResponse(
//                                        orderPackageEntity,
//                                        false,
//                                        Constants.NO_PACKAGE_PRODUCTS_IN_THE_ORDER_PACKAGE_WARNING
//                                )
//                        )
//                        .toList()
//        );
//
//        // Filter out order packages that have associated package products and are eligible for approval
//        List<OrderPackageEntity> foundedOrderPackages = validStatusForApprovalOrderPackages.stream()
//                .filter(orderPackageEntity -> sellerPackageProductPackageIds.contains(orderPackageEntity.getId()))
//                .toList();
//
//        if (ObjectUtils.isEmpty(foundedOrderPackages))
//            return ApproveOrderPackagesResponse.builder().failList(failList).build();

        // Map to track package IDs to SKU IDs that are out of stock
        Map<Long, List<Long>> packageIdToOutOfStockSkuId = sellerPackageProductProjections.stream()
                .filter(sellerPackageProductProjection ->
                        ObjectUtils.isNotEmpty(sellerPackageProductProjection.getSkuId())
                        &&
                        (
                                ObjectUtils.isEmpty(sellerPackageProductProjection.getSkuStock())
                                || sellerPackageProductProjection.getSkuStock() <= 0
                        )
                )
                .collect(Collectors.toMap(
                        SellerPackageProductProjection::getPackageId,
                        sellerPackageProductProjection -> List.of(sellerPackageProductProjection.getSkuId()),
                        (existing, replacement) -> {
                            List<Long> mergedList = new ArrayList<>(existing);
                            mergedList.addAll(replacement);
                            return mergedList;
                        }
                ));

        // Initialize list to hold order packages eligible for approval
        List<OrderPackageEntity> eligibleForApprovalOrderPackage = new ArrayList<>();
        for (OrderPackageEntity orderPackageUnit : validStatusForApprovalOrderPackages) {
            List<Long> outOfStockSkuIds = packageIdToOutOfStockSkuId.get(orderPackageUnit.getId());
            if (ObjectUtils.isEmpty(outOfStockSkuIds))
                eligibleForApprovalOrderPackage.add(orderPackageUnit);
            else
                failList.add(
                        buildSellerStatusActionResponse(
                                orderPackageUnit,
                                false,
                                "Đơn hàng có sản phẩm hết hàng không thể duyệt"
                        )
                );
        }

        // If no order packages are eligible after SKU stock check, return the fail list
        if (ObjectUtils.isEmpty(eligibleForApprovalOrderPackage))
            return ApproveOrderPackagesResponse.builder().failList(failList).build();

        // Approve eligible order packages using a transactional proxy method
        List<OrderPackageResAfterSellerAction> approveResultList
                = getProxy().approveOrderPackage(eligibleForApprovalOrderPackage);

        // Extract successfully approved order packages
        List<OrderPackageResAfterSellerAction> successList
                = approveResultList.stream()
                .filter(result -> ObjectUtils.isNotEmpty(result.getSuccess()) && Boolean.TRUE.equals(result.getSuccess()))
                .toList();

        // Add failed approval results to the fail list
        failList.addAll(
                approveResultList.stream()
                        .filter(result -> ObjectUtils.isEmpty(result.getSuccess()) || Boolean.FALSE.equals(result.getSuccess()))
                        .toList()
        );

        // Construct and return the response with both success and fail lists
        return ApproveOrderPackagesResponse.builder().successList(successList).failList(failList).build();
    }

    /**
     * Builds a response object representing the result of a seller action on an order package.
     *
     * @param id      the unique identifier of the order package
     * @param orderCode the code of the order
     * @param success indicates whether the action was successful
     * @param reason  the reason for failure, if any
     * @return an instance of {@link OrderPackageResAfterSellerAction} encapsulating the response details
     */
    private OrderPackageResAfterSellerAction buildSellerStatusActionResponse(
            Long id, String orderCode, boolean success, String reason
    ) {
        return
                OrderPackageResAfterSellerAction.builder()
                        .id(id)
                        .orderCode(orderCode)
                        .success(success)
                        .reason(reason)
                        .build();
    }

    /**
     * Builds a response object representing the result of a seller action on an order package.
     *
     * @param orderPackageEntity the order package entity involved in the action
     * @param success            indicates whether the action was successful
     * @param reason             the reason for failure, if any
     * @return an instance of {@link OrderPackageResAfterSellerAction} encapsulating the response details
     */
    private OrderPackageResAfterSellerAction buildSellerStatusActionResponse(
            @NotNull OrderPackageEntity orderPackageEntity, boolean success, String reason
    ) {
        return OrderPackageResAfterSellerAction.builder()
                .id(orderPackageEntity.getId())
                .orderCode(orderPackageEntity.getOrderCode())
                .success(success)
                .reason(reason)
                .build();
    }

    /**
     * Approves a list of eligible order packages.
     *
     * <p>This method updates the statuses of the provided order packages to {@link SellerOrderPackageEvent#APPROVED},
     * handles any exceptions during the update process, and persists the changes to the repository.</p>
     *
     * @param orderPackageEntities the list of order packages eligible for approval
     * @return a list of {@link OrderPackageResAfterSellerAction} objects representing the result of each approval
     */
    @Transactional
    public List<OrderPackageResAfterSellerAction> approveOrderPackage(
            @NotEmpty List<OrderPackageEntity> orderPackageEntities
    ) {
        List<OrderPackageResAfterSellerAction> response = new ArrayList<>();
        List<OrderPackageEntity> successUpdateStatusOrderPackage = new ArrayList<>();
        List<OrderActivityHistoryEntity> activityHistories = new ArrayList<>();
        List<PackageLogEntity> packageLogs = new ArrayList<>();
        for (OrderPackageEntity orderPackage : orderPackageEntities) {
            try {
                String beforeUpdate = JsonMapperUtils.writeValueAsString(orderPackage);
                updateOrderPackageStatusesByEvent(orderPackage, SellerOrderPackageEvent.APPROVED);
                successUpdateStatusOrderPackage.add(orderPackage);
                //note: add a row in order_activity_history
                activityHistories.add(OrderActivityHistoryEntity.builder()
                        .orderId(orderPackage.getId())
                        .type(ActivityType.SELLER_APPROVE)
                        .beforeState(beforeUpdate)
                        .afterState(JsonMapperUtils.writeValueAsString(orderPackage))
                        .build());
                packageLogs.add(new PackageLogEntity(
                        OrderConstant.SELLER_APPROVED_ORDER,
                        null,
                        JsonMapperUtils.writeValueAsString(orderPackage),
                        orderPackage.getMerchantId(),
                        orderPackage.getCustomerId(),
                        1,
                        orderPackage.getId()));
            } catch (VipoFailedToExecuteException vipoFailedToExecuteException) {
                response.add(
                        buildSellerStatusActionResponse(
                                orderPackage, false, vipoFailedToExecuteException.getMessage()
                        )
                );
            }
        }

        if (ObjectUtils.isEmpty(successUpdateStatusOrderPackage))
            return response;

        long timeInSeconds = DateUtils.getCurrentTimeInSeconds();
        List<OrderTrackingEntity> orderTrackingEntities
                = successUpdateStatusOrderPackage.stream()
                .map(
                        orderPackageEntity -> OrderTrackingEntity.builder()
                        .packageId(orderPackageEntity.getId())
                        .content(Constants.SELLER_APPROVE_ORDER_PACKAGE)
                        .orderStatus(orderPackageEntity.getOrderStatus())
                        .time(timeInSeconds)
                        .uniqueKey(String.format("%d_VIPO_%s_%d", orderPackageEntity.getId(), orderPackageEntity.getOrderStatus(), timeInSeconds))
                        .source(Constants.VIPO_SOURCE)
                        .build()
                ).toList();

        try {
            orderPackageRepository.saveAll(successUpdateStatusOrderPackage);
            orderActivityHistoryEntityRepository.saveAll(activityHistories);
            orderTrackingEntityRepository.saveAll(orderTrackingEntities);
            packageLogEntityRepository.saveAll(packageLogs);
            response.addAll(
                    successUpdateStatusOrderPackage.stream()
                            .map(orderPackageEntity
                                    -> buildSellerStatusActionResponse(orderPackageEntity, true, OrderConstant.SELLER_ORDER_APPROVED))
                            .toList()
            );
        } catch (OptimisticLockingFailureException optimisticLockingFailureException) {
            response.addAll(
                    successUpdateStatusOrderPackage.stream()
                            .map(orderPackageEntity
                                    -> buildSellerStatusActionResponse(
                                            orderPackageEntity,
                                    false,
                                    "Có sự cập nhật ở các đơn hàng, vui lòng thử lại!"
                                    )
                            )
                            .toList()
            );
        }

        return response;
    }

    /**
     * Updates the statuses of an order package based on a specific event.
     *
     * <p>This method interacts with both seller and buyer order status services
     * to handle the state transitions triggered by the event.</p>
     *
     * @param orderPackageEntity        the order package entity to be updated
     * @param sellerOrderPackageEvent the event triggering the status update
     */
    private void updateOrderPackageStatusesByEvent(
            @NotNull OrderPackageEntity orderPackageEntity, @NotNull SellerOrderPackageEvent sellerOrderPackageEvent
    ) {
        sellerOrderStatusService.updateSellerOrderStatusByEvent(orderPackageEntity, sellerOrderPackageEvent);
        buyerOrderStatusService.updateBuyerOrderStatusByEvent(orderPackageEntity, sellerOrderPackageEvent);
    }

}
