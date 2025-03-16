package com.vtp.vipo.seller.services.impl;

import com.amazonaws.services.s3.model.S3Object;
import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.RevenueConstant;
import com.vtp.vipo.seller.common.constants.WithdrawalRequestConstant;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalConfigEntity;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestEntity;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestExportEntity;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestLogEntity;
import com.vtp.vipo.seller.common.dao.entity.dto.PrepaymentTransactionData;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestReportTypeEnum;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestItemEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestItemProjection;
import com.vtp.vipo.seller.common.dao.entity.projection.WithdrawalRequestProjection;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.ExportWithdrawalRequestListRequest;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.WithdrawalRequestCreateFilter;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.WithdrawalRequestFilter;
import com.vtp.vipo.seller.common.dto.response.CancelWithdrawRequestResponse;
import com.vtp.vipo.seller.common.dto.response.WithdrawRequestExportDetailsResponse;
import com.vtp.vipo.seller.common.dto.response.WithdrawRequestExportResponse;
import com.vtp.vipo.seller.common.dto.response.WithdrawRequestHistoryResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDownloadResponse;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.*;
import com.vtp.vipo.seller.common.enumseller.StorageType;
import com.vtp.vipo.seller.common.exception.*;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.mapper.WithdrawalRequestMapper;
import com.vtp.vipo.seller.common.utils.*;
import com.vtp.vipo.seller.config.mq.kafka.KafkaTopicConfig;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.WithdrawalRequestService;
import com.vtp.vipo.seller.services.withdraw.ExportReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalRequestServiceImpl extends BaseService<WithdrawalRequestEntity, Long, WithdrawalRequestRepository> implements WithdrawalRequestService {

    private final WithdrawalRequestMapper mapper;

    private final WithdrawalRequestItemRepository withdrawalRequestItemRepository;

    private final OrderPackageRepository orderPackageRepository;

    private final WithdrawalConfigRepository withdrawalConfigRepository;

    private final WithdrawalRequestLogEntityRepository withdrawalRequestLogEntityRepository;

    private final WithdrawalRequestExportEntityRepository withdrawalRequestExportEntityRepository;

    private final AmazonS3Service amazonS3Service;

    private final ExportReportService exportReportService;

    @SuppressWarnings("rawtypes")
    private final KafkaTemplate kafkaTemplate;

    private final KafkaTopicConfig kafkaTopicConfig;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${custom.properties.withdraw-request.maximum-export-time:5m}")
    private Duration maximumWithdrawRequestExportDuration;

    @Value("${custom.properties.withdraw-request.list-export.file-name:DanhSachQuanLyYeuCau.xlsx}")
    private String withdrawalRequestListExportFileName;

    @Override
    public WithdrawalRequestOverviewRes getWithdrawRequestOverview() {
        VipoUserDetails merchantInfo = getCurrentUser();
        log.info("[Get WithdrawRequestOverview by merchantId] {}", merchantInfo.getId());
        LocalDateTime currentDateTime = LocalDateTime.now();

        //TODO: need to update after withdrawal config is ready
        WithdrawalConfigEntity withdrawalConfig = getMerchantWithdrawConfig(merchantInfo.getId());

        Integer requestInMonth = getRequestInMonthByMerchantId(merchantInfo.getId());
        Integer maxRequestInMonth = withdrawalConfig.getMaxWithdrawalAttemptInAMonth();

        Integer numRequested = requestInMonth >= maxRequestInMonth ? 0 : maxRequestInMonth - requestInMonth;

        return WithdrawalRequestOverviewRes.builder()
                // tổng tiền các đơn chưa rút || chưa rút thành công
                .balancePending(getAvailableBalanceByMerchantId(merchantInfo.getId()))
                .withdrawalThisWeek(repo.getTotalWithdrawalByMerchantId(merchantInfo.getId(),
                        DateUtils.getFirstDateOfWeekInSecond(currentDateTime),
                        DateUtils.getTimeInSeconds(currentDateTime)
                ).orElse(BigDecimal.ZERO))
                .withdrawalThisMonth(repo.getTotalWithdrawalByMerchantId(merchantInfo.getId(),
                        DateUtils.getFirstDateOfMonthInSecond(currentDateTime),
                        DateUtils.getTimeInSeconds(currentDateTime)
                ).orElse(BigDecimal.ZERO))
                .totalWithdrawn(repo.getTotalWithdrawalByMerchantId(merchantInfo.getId(), null, null).orElse(BigDecimal.ZERO))
                .remainingWithdrawals(String.format("%s/%s", numRequested, maxRequestInMonth))
                .accountInfo(getMerchantBankAccountInfo(merchantInfo.getId()))
                .canCreateRequest(numRequested != 0)
                .date(DateUtils.getTimeInSeconds(currentDateTime))
                .build();
    }

    @Override
    public PagingRs searchWithdrawalRequests(WithdrawalRequestFilter request) {
        log.info("[search Withdrawal Requests with data]: {}", request);
        PagingRs res = new PagingRs();
        PageRequest pageRequest = PageRequest.of(request.getPageNum(), request.getPageSize());

        VipoUserDetails merchantInfo = getCurrentUser();

        if (!CollectionUtils.isEmpty(request.getWithdrawalRequestStatus()) && request.getWithdrawalRequestStatus().contains(WithdrawRequestStatusEnum.PROCESSING)) {
            List<WithdrawRequestStatusEnum> status = request.getWithdrawalRequestStatus();
            status.add(WithdrawRequestStatusEnum.APPROVED);
            request.setWithdrawalRequestStatus(status);
        }

        Page<WithdrawalRequestProjection> filterWithdrawalRequest = repo.findAllByFilterRequest(
                request.getWithdrawalRequestType(),
                request.getWithdrawalRequestStatus(),
                request.getAmountFrom(),
                request.getAmountTo(),
                DateUtils.convertToStartOfDay(request.getStartDate()),
                DateUtils.convertToEndOfDay(request.getEndDate()),
                merchantInfo.getId(),
                pageRequest
        );

        res.setCurrentPage(request.getPageNum());
        res.setTotalCount(filterWithdrawalRequest.getTotalElements());
        res.setData(filterWithdrawalRequest.stream().map(mapper::projectionToResponse).toList());

        return res;
    }

    @Override
    public WithdrawalRequestDetailResponse getWithdrawalRequestDetail(String id) {
        log.info("[get Withdrawal Request Detail with id ]: {}", id);
        Long merchantId = getCurrentUser().getId();
        Long requestId = Long.valueOf(id);
        if (DataUtils.isNullOrEmpty(requestId))
            throw new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND);

        WithdrawalRequestProjection withdrawalRequest = repo.findByIdAndMerchantId(Long.valueOf(id), merchantId).orElseThrow(
                () -> new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND)
        );

        WithdrawalRequestDetailResponse res = mapper.projectionToDetailResponse(withdrawalRequest);

        //TODO: need to update after withdrawal config is ready
        List<WithdrawalRequestItemProjection> withdrawalRequestItemProjections = withdrawalRequestItemRepository.getWithdrawalRequestItemByRequestId(requestId);

        LinkedList<WithdrawalRequestItem> withdrawalRequestItems = new LinkedList<>(withdrawalRequestItemProjections.stream().map(mapper::convertItemProjectionToItem).toList());

        Map<Long, PrepaymentTransactionData> trans = getPrepaymentTransactionId(withdrawalRequestItems.stream().map(WithdrawalRequestItem::getOrderPackageId).toList());

        if(!CollectionUtils.isEmpty(trans)){
            withdrawalRequestItems.forEach(item -> {
                PrepaymentTransactionData transData = trans.get(item.getOrderPackageId());
                if (ObjectUtils.isNotEmpty(transData))
                    item.setPrepaymentTransactionCode(transData.getPrepaymentTransactionCode());
            });
        }

        withdrawalRequestItems.add(getRowTotalPrice(withdrawalRequestItems));

        res.setFeeColumns(getFeeColumn(withdrawalRequestItems));

        res.setWithdrawalRequestItems(withdrawalRequestItems);

        return res;
    }

    @Override
    public WithdrawalRequestCreateInfoRes getWithdrawalRequestCreateInfo() {
        //TODO: totalRevenue | availableBalance
        Long merchantId = getCurrentUser().getId();
        log.info("[get Withdrawal Request CreateInfo by merchantId]: {}", merchantId);

        WithdrawalConfigEntity withdrawalConfig = getMerchantWithdrawConfig(merchantId);
        return WithdrawalRequestCreateInfoRes.builder()
                // Tông doanh thu trên các đơn đã giao thành công chưa trừ các đơn đã rút (ko bao gồm thuế)
                // => Tông doanh thu trên các đơn ĐÃ rút hoặc CHƯA rút & đã giao thành công
                .totalRevenue(repo.getTotalRevenueByMerchantId(merchantId).stream().reduce(BigDecimal.ZERO, BigDecimal::add))
                // Tông doanh thu trên các đơn đã giao thành công - các đơn đã rút thành công (ko bao gồm thuế)
                // => Tông doanh thu trên các đơn CHƯA rút & đã giao thành công
                //TODO: chờ confirm vs BA xem có trừ các đơn bị âm ko
                .availableBalance(getAvailableBalanceByMerchantId(merchantId))
                .accountInfo(getMerchantBankAccountInfo(merchantId))
                .taxValue(withdrawalConfig.getTax())
                .build();
    }

    @Override
    public PagingRs getOrderPackgeToWithdrawal(WithdrawalRequestCreateFilter request) {
        log.info("[get OrderPackge To Withdrawal with data]: {}", request);
        Long merchantId = getCurrentUser().getId();

        WithdrawalConfigEntity withdrawalConfig = getMerchantWithdrawConfig(merchantId);

        Integer requestInMonth = getRequestInMonthByMerchantId(merchantId);
        if (requestInMonth >= withdrawalConfig.getMaxWithdrawalAttemptInAMonth()) {
            throw new VipoInvalidDataRequestException(String.format(WithdrawalRequestConstant.REQUEST_LIMITED, withdrawalConfig.getMaxWithdrawalAttemptInAMonth()));
        }

        BigDecimal availableBalance = getAvailableBalanceByMerchantId(merchantId);
        if(availableBalance.compareTo(BigDecimal.ZERO) <= 0){
            throw new VipoInvalidDataRequestException(WithdrawalRequestConstant.NO_AVAILABLE_BALANCE);
        }

        PageRequest pageRequest = PageRequest.of(request.getPageNum(), request.getPageSize());

        //TODO: need to update after withdrawal config is ready
        Page<WithdrawalRequestItemProjection> orderPackages = orderPackageRepository.getOrderPackageToWithdrawalByMerchantId(
                merchantId,
                request.getShippingCode(),
                request.getOrderCode(),
                request.getBuyerName(),
                DateUtils.convertToStartOfDayByEpochSeconds(request.getStartDate()),
                DateUtils.convertToEndOfDayByEpochSeconds(request.getEndDate()),
                pageRequest);

        List<WithdrawalRequestItem> orderWithdrawalItems = orderPackages.stream().map(mapper::convertItemProjectionToItem).toList();

        // get transaction code by orderPackgeIds
        Map<Long, PrepaymentTransactionData> trans = getPrepaymentTransactionId(orderPackages.stream().map(WithdrawalRequestItemProjection::getOrderPackageId).toList());

        if(!CollectionUtils.isEmpty(trans)){
            orderWithdrawalItems.forEach(item -> {
                PrepaymentTransactionData transData = trans.get(item.getOrderPackageId());
                if (ObjectUtils.isNotEmpty(transData))
                    item.setPrepaymentTransactionCode(transData.getPrepaymentTransactionCode());
            });
        }

        WithdrawalConfigEntity merchantConfig = getMerchantWithdrawConfig(merchantId);
        WithdrawalPaging res = new WithdrawalPaging();
        res.setCurrentPage(request.getPageNum());
        res.setTotalCount(orderPackages.getTotalElements());
        res.setData(orderWithdrawalItems);
        res.setFeeColumns(getFeeColumn(orderWithdrawalItems));
        res.setTaxValue(merchantConfig.getTax());
        return res;
    }

    @Override
    @Transactional
    public Object createWithdrawalRequest(List<Long> orderPackageIds) {
        log.info("[create new withdraw request with orderPackageIds] {}", orderPackageIds);
        Long merchantId = getCurrentUser().getId();
        //TODO: need to update after withdrawal config is ready
        WithdrawalConfigEntity withdrawalConfig = getMerchantWithdrawConfig(merchantId);

        Integer requestInMonth = getRequestInMonthByMerchantId(merchantId);
        if (requestInMonth >= withdrawalConfig.getMaxWithdrawalAttemptInAMonth()) {
            throw new VipoInvalidDataRequestException(String.format(WithdrawalRequestConstant.REQUEST_LIMITED, withdrawalConfig.getMaxWithdrawalAttemptInAMonth()));
        }

        BigDecimal availableBalance = getAvailableBalanceByMerchantId(merchantId);
        if(availableBalance.compareTo(BigDecimal.ZERO) <= 0){
            throw new VipoInvalidDataRequestException(WithdrawalRequestConstant.NO_AVAILABLE_BALANCE);
        }

        Collection<WithdrawalRequestItemProjection> orderOfCanWithdraw = orderPackageRepository.getOrderPackageToWithdrawalByMerchantIdAndIdIn(merchantId, orderPackageIds);

        if (CollectionUtils.isEmpty(orderOfCanWithdraw)){
            throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAIL_CREATE_WITHDRAW_TOAST, WithdrawalRequestConstant.ORDER_HAVE_REQUESTED);
        }

        if (orderOfCanWithdraw.size() != orderPackageIds.size()) {
            List<Long> orderOfCanWithdrawIds = orderOfCanWithdraw.stream().map(WithdrawalRequestItemProjection::getOrderPackageId).toList();
            List<Long> orderPackageIdsNotValid = orderPackageIds.stream().filter(o -> !orderOfCanWithdrawIds.contains(o)).toList();
            Collection<WithdrawalRequestItemProjection> orderOfCanWithdrawNotValid = orderPackageRepository.getOrderPackageByMerchantIdAndIdIn(merchantId, orderPackageIdsNotValid);

            if(CollectionUtils.isEmpty(orderOfCanWithdrawNotValid)){
                throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAILED_TO_EXECUTE, WithdrawalRequestConstant.ORDER_PACKAGE_INVALID);
            }

            Map<String,  Collection<WithdrawalRequestItem>> res = new HashMap<>();
            res.put("orderValid", mapper.convertListItemProjectionToItems(orderOfCanWithdraw));
            res.put("orderInValid", mapper.convertListItemProjectionToItems(orderOfCanWithdrawNotValid));

            return res;
        }

        //TODO: use redis lock orderOfCanWithdraw to avoid duplicate request
        // after create request success unlock orderOfCanWithdraw

        BigDecimal totalWithdrawAmount = BigDecimal.ZERO;
        List<WithdrawalRequestItemEntity> requestItems = new ArrayList<>();

        for (WithdrawalRequestItemProjection order : orderOfCanWithdraw) {
            if(order.getEstimatedProfit().compareTo(BigDecimal.ZERO) <= 0){
                throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAILED_TO_EXECUTE, WithdrawalRequestConstant.INVALID_AVAILABLE_BALANCE);
            }

            totalWithdrawAmount = totalWithdrawAmount.add(order.getEstimatedProfit());

            BigDecimal withdrawableAmount = order.getEstimatedProfit()
                    .multiply(withdrawalConfig.getTax())
                    .divide(BigDecimal.valueOf(100), 3, RoundingMode.CEILING);

            requestItems.add(WithdrawalRequestItemEntity.builder()
                    .packageId(order.getOrderPackageId())
                    .withdrawAmount(order.getEstimatedProfit())
                    .withdrawableAmount(order.getEstimatedProfit().subtract(withdrawableAmount))
                    .reCreated(Boolean.FALSE)
                    //TODO: need to update after withdrawal config is ready
                    .withdrawableTime(order.getSuccessDeliveryDate() + withdrawalConfig.getWithdrawAfterSecond())
                    .build()
            );
        }

        BigDecimal tax = totalWithdrawAmount
                .multiply(withdrawalConfig.getTax())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.CEILING);

        WithdrawalRequestEntity newRequest = repo.save(WithdrawalRequestEntity.builder()
                .merchantId(merchantId)
                .status(WithdrawRequestStatusEnum.PENDING)
                .totalAmount(totalWithdrawAmount.subtract(tax).setScale(0, RoundingMode.CEILING))
                .taxValue(withdrawalConfig.getTax())
                .tax(tax)
                .type(WithdrawalRequestType.WITHDRAW_TO_ACCOUNT)
                .build());

        updateWithdrawItemReCreated(orderPackageIds);

        log.info("[new withdraw request saved] {}", newRequest);
        try {
            List<WithdrawalRequestItemEntity> newRequestItems = withdrawalRequestItemRepository.saveAll(requestItems.stream().peek(item -> item.setWithdrawalRequestId(newRequest.getId())).toList());

            log.info("[new withdraw request items saved] {}", newRequestItems);
            WithdrawalRequestCreateRes res = mapper.entityToResponse(newRequest);
            res.setWithdrawableItems(newRequestItems.stream().map(mapper::entityToResponse).toList());

            // save withdraw request log
            WithdrawalRequestLogEntity withdrawalRequestLog = withdrawalRequestLogEntityRepository.save(WithdrawalRequestLogEntity.builder()
                    .withdrawalRequestId(newRequest.getId())
                    .beforeData(newRequest)
                    .newStatus(newRequest.getStatus())
                    .build());
            log.info("[new withdraw request log saved] {}", withdrawalRequestLog);
            return res;

        } catch (Exception e) {
            throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAIL_CREATE_WITHDRAW_TOAST, WithdrawalRequestConstant.ORDER_HAVE_REQUESTED);
        }

    }

    private void updateWithdrawItemReCreated(List<Long> orderPackgeIds){
        if(CollectionUtils.isEmpty(orderPackgeIds))
            return;
        withdrawalRequestItemRepository.updateWithdrawItemReCreated(orderPackgeIds);
    }

    private Set<FeeColumn> getFeeColumn(Collection<WithdrawalRequestItem> data) {
        if (CollectionUtils.isEmpty(data)) return Set.of();

        Set<FeeColumn> feeColumns = new HashSet<>();
        for (WithdrawalRequestItem item : data) {
            feeColumns.addAll(item.getPlatformFees().stream().map(FeeColumn::new).toList());
        }

        return feeColumns;
    }

    private WithdrawalRequestItem getRowTotalPrice(List<WithdrawalRequestItem> data) {

        WithdrawalRequestItem rowTotalPrice = new WithdrawalRequestItem();
        Map<String, FeeMap> totalFees = new HashMap<>();

        BigDecimal totalPrePaymentAmount = BigDecimal.ZERO;
        BigDecimal totalCodAmount = BigDecimal.ZERO;
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        BigDecimal totalAdjustmentAmount = BigDecimal.ZERO;
        BigDecimal totalEstimatedProfit = BigDecimal.ZERO;

        for (WithdrawalRequestItem item : data) {
            totalPrePaymentAmount = totalPrePaymentAmount.add(item.getPrepayment());
            totalCodAmount = totalCodAmount.add(item.getCodAmount());
            totalOrderAmount = totalOrderAmount.add(item.getOrderAmount());
            totalAdjustmentAmount = totalAdjustmentAmount.add(item.getAdjustmentPrice());
            totalEstimatedProfit = totalEstimatedProfit.add(item.getEstimatedProfit());
            if (!CollectionUtils.isEmpty(item.getPlatformFees())) {
                for (FeeMap fee : item.getPlatformFees()) {
                    if (!totalFees.containsKey(fee.getColumnCode())) {
                        totalFees.put(fee.getColumnCode(), fee);
                    } else {
                        FeeMap feeData = totalFees.get(fee.getColumnCode()).clone();
                        feeData.setFeeValue(feeData.getFeeValue().add(fee.getFeeValue()));
                        totalFees.put(fee.getColumnCode(), feeData);
                    }
                }
            }
        }
        rowTotalPrice.setPrepayment(totalPrePaymentAmount);
        rowTotalPrice.setCodAmount(totalCodAmount);
        rowTotalPrice.setOrderAmount(totalOrderAmount);
        rowTotalPrice.setAdjustmentPrice(totalAdjustmentAmount);
        rowTotalPrice.setEstimatedProfit(totalEstimatedProfit);
        rowTotalPrice.setPlatformFees(totalFees.values());
        return rowTotalPrice;
    }

    private Integer getRequestInMonthByMerchantId(Long merchantId) {

        LocalDateTime currentDateTime = LocalDateTime.now();

        Long firstDayOfMonth = DateUtils.getFirstDateOfMonthInSecond(currentDateTime);
        Long lastDayOfMonth = DateUtils.getLastDateOfMonthInSecond(currentDateTime);

        Integer numRequested = repo.getRemainingWithdrawalsByMerchantId(merchantId, firstDayOfMonth, lastDayOfMonth);

        return DataUtils.isNullOrEmpty(numRequested) ? 0 : numRequested;

    }

    private boolean canMakeWithdrawRequest(Long merchantId) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        Long firstDayOfMonth = DateUtils.getFirstDateOfMonthInSecond(currentDateTime);
        Long lastDayOfMonth = DateUtils.getLastDateOfMonthInSecond(currentDateTime);

        Boolean canMakeRequest = repo.checkWithdrawTimes(merchantId, firstDayOfMonth, lastDayOfMonth);

        return DataUtils.isNullOrEmpty(canMakeRequest) || canMakeRequest;

    }

    @Transactional
    @Override
    public CancelWithdrawRequestResponse cancelWithdrawRequest(String withdrawRequestId) {
        log.info("Cancel withdraw request with id: {}", withdrawRequestId);
        VipoUserDetails user = getCurrentUser();

        // Find the WithdrawalRequestEntity by id and merchantId
        WithdrawalRequestEntity withdrawalRequest = repo.findByIdAndMerchantIdAndDeletedFalse(Long.valueOf(withdrawRequestId), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.WITHDRAW_NOT_FOUND));

        // Check status of the WithdrawalRequestEntity
        if (withdrawalRequest.getStatus().equals(WithdrawRequestStatusEnum.PROCESSING) || withdrawalRequest.getStatus().equals(WithdrawRequestStatusEnum.REJECTED)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, RevenueConstant.CANNOT_CANCEL_PROCESSING_REJECTED);
        }

        if (withdrawalRequest.getStatus().equals(WithdrawRequestStatusEnum.CANCELED)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, RevenueConstant.CANCELLED_REQUEST);
        }

        if (!withdrawalRequest.getStatus().equals(WithdrawRequestStatusEnum.PENDING)) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, RevenueConstant.CANCEL_ONLY_PENDING);
        }

        // Update the status of the WithdrawalRequestEntity
        WithdrawRequestStatusEnum previosStatus = withdrawalRequest.getStatus();
        withdrawalRequest.setStatus(WithdrawRequestStatusEnum.CANCELED);

        repo.save(withdrawalRequest);
        log.info("Cancel withdraw request with id: {} successfully", withdrawRequestId);

        // Add request log
        saveRequestLogAfterCancel(withdrawalRequest, previosStatus);

        return CancelWithdrawRequestResponse.builder()
                .requestId(String.valueOf(withdrawalRequest.getId()))
                .status(withdrawalRequest.getStatus())
                .message(RevenueConstant.CANCEL_WITHDRAW_SUCCESS)
                .build();
    }

    private void saveRequestLogAfterCancel(WithdrawalRequestEntity withdrawalRequest, WithdrawRequestStatusEnum previosStatus) {
        WithdrawalRequestLogEntity logEntity = WithdrawalRequestLogEntity.builder()
                .withdrawalRequestId(withdrawalRequest.getId())
                .previousStatus(previosStatus)
                .newStatus(withdrawalRequest.getStatus())
                .build();

        withdrawalRequestLogEntityRepository.save(logEntity);
    }

    @Override
    public List<WithdrawRequestHistoryResponse> getWithdrawRequestHistory(String withdrawRequestId) {
        log.info("Get withdraw request history with id: {}", withdrawRequestId);
        VipoUserDetails user = getCurrentUser();

        // Find the WithdrawalRequestEntity by id and merchantId
        WithdrawalRequestEntity withdrawalRequest = repo.findByIdAndMerchantIdAndDeletedFalse(Long.valueOf(withdrawRequestId), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.WITHDRAW_NOT_FOUND));

        // Find withdrawal request logs by withdrawalRequestId
        List<WithdrawalRequestLogEntity> withdrawalRequestLogs = withdrawalRequestLogEntityRepository
                .findByWithdrawalRequestIdOrderByCreatedAtDesc(withdrawalRequest.getId());

        return withdrawalRequestLogs.stream().map(logEntity -> WithdrawRequestHistoryResponse.builder()
                .date(DateUtils.getTimeInSeconds(logEntity.getCreatedAt()))
                .withdrawStatus(logEntity.getNewStatus().name())
                .withdrawStatusDescription(logEntity.getNewStatus().getLable())
                .build()).toList();
    }

    @Transactional
    @Override
    public WithdrawRequestExportResponse exportReport(String withdrawRequestId) {
        WithdrawalRequestDetailResponse detail = getWithdrawalRequestDetail(withdrawRequestId);

        /* phase 6: withdrawal request: check if the export has been created before to avoid export too many times */
        Optional<WithdrawalRequestExportEntity> newestWithdrawalRequestExportEntityOptional
                = withdrawalRequestExportEntityRepository.findFirstPendingAndSuccessReport(
                Long.valueOf(withdrawRequestId),
                List.of(WithdrawalRequestExportEnum.COMPLETED, WithdrawalRequestExportEnum.PENDING)
        );

        if (newestWithdrawalRequestExportEntityOptional.isPresent()) {
            WithdrawalRequestExportEntity firstWithdrawRequestExport
                    = newestWithdrawalRequestExportEntityOptional.get();
            if (
                    firstWithdrawRequestExport.getStatus().equals(WithdrawalRequestExportEnum.COMPLETED)
                            || firstWithdrawRequestExport.getCreatedAt()
                            .isAfter(
                                    DateUtils.getCurrentLocalDateTime()
                                            .minus(maximumWithdrawRequestExportDuration)
                            )   //make sure the export did not take too long
            )
                return WithdrawRequestExportResponse.builder()
                        .reportExportId(String.valueOf(firstWithdrawRequestExport.getId()))
                        .build();
        }

        // Create export entity
        WithdrawalRequestExportEntity exportEntity = WithdrawalRequestExportEntity.builder()
                .withdrawalRequestId(Long.valueOf(withdrawRequestId))
                .reportType(WithdrawRequestReportTypeEnum.EXCEL)
                .reportName(RevenueConstant.EXPORT_WITHDRAW_DETAIL_FILE_NAME)
                .filePath(null)
                .exportTime(LocalDateTime.now())
                .status(WithdrawalRequestExportEnum.PENDING)
                .errorMessage(null)
                .storageType(StorageType.S3)
                .storageInfo(null)
                .finishTime(null)
                .build();

        // Save to DB
        WithdrawalRequestExportEntity savedExportEntity = withdrawalRequestExportEntityRepository.save(exportEntity);

        /* phase 6: withdrawal request: push the request to kafka */
        var orderPackageReportExportMsg
                = OrderPackageReportExportMsg.builder()
                .excelFileName(RevenueConstant.EXPORT_WITHDRAW_DETAIL_FILE_NAME)
                .withdrawalRequestReportId(savedExportEntity.getId())
                .merchantId(getCurrentUser().getId())
                .build();

        kafkaTemplate.send(
                kafkaTopicConfig.getOrderPackageExportTopicName()
                , JsonUtils.toJson(new MessageData(orderPackageReportExportMsg))
        );

//        // Process export report
//        exportReportService.processExportReport(detail, savedExportEntity);

        log.info("[exportReport] Export report request completed");
        return WithdrawRequestExportResponse.builder()
                .reportExportId(String.valueOf(savedExportEntity.getId()))
                .build();
    }

//    @Override
//    public WithdrawRequestExportResponse exportReport(String withdrawRequestId) {
//        // Get withdrawal request detail
//        WithdrawalRequestDetailResponse detail = getWithdrawalRequestDetail(withdrawRequestId);
//
//        // Create export entity
//        String fileName = "ChiTietYeuCau" + UUID.randomUUID() + ".xlsx";
//        WithdrawalRequestExportEntity exportEntity = WithdrawalRequestExportEntity.builder()
//                .withdrawalRequestId(Long.valueOf(withdrawRequestId))
//                .reportType(WithdrawRequestReportTypeEnum.EXCEL)
//                .reportName(fileName)
//                .filePath(null)
//                .exportTime(LocalDateTime.now())
//                .status(WithdrawalRequestExportEnum.PENDING)
//                .errorMessage(null)
//                .storageType(StorageType.LOCAL)
//                .storageInfo(null)
//                .finishTime(null)
//                .build();
//
//        // Save export entity
//        WithdrawalRequestExportEntity savedExportEntity =
//                withdrawalRequestExportEntityRepository.save(exportEntity);
//
//        // Process export report
//        exportReportService.processExportReport(detail, savedExportEntity);
//
//        // Return response
//        log.info("[exportReport LOCAL] Export report request completed");
//        return WithdrawRequestExportResponse.builder()
//                .reportExportId(String.valueOf(savedExportEntity.getId()))
//                .build();
//    }

    @Override
    public WithdrawRequestExportDetailsResponse getReportDetails(String withdrawRequestId, String exportId) {
        log.info("Get report details with withdrawRequestId: {} and exportId: {}", withdrawRequestId, exportId);
        VipoUserDetails user = getCurrentUser();

        // Find the WithdrawalRequestEntity by id and merchantId
        WithdrawalRequestEntity withdrawalRequest = repo.findByIdAndMerchantIdAndDeletedFalse(Long.valueOf(withdrawRequestId), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.WITHDRAW_NOT_FOUND));

        // Find the WithdrawalRequestExportEntity by id and withdrawalRequestId
        WithdrawalRequestExportEntity withdrawalRequestExport
                = withdrawalRequestExportEntityRepository.findByIdAndWithdrawalRequestIdAndDeletedFalse(Long.valueOf(exportId), withdrawalRequest.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.REPORT_NOT_FOUND));

        return WithdrawRequestExportDetailsResponse.builder()
                .id(String.valueOf(withdrawalRequestExport.getId()))
                .reportType(withdrawalRequestExport.getReportType())
                .status(withdrawalRequestExport.getStatus())
                .errorMessage(withdrawalRequestExport.getErrorMessage())
                .createdAt(DateUtils.getTimeInSeconds(withdrawalRequestExport.getCreatedAt()))
                .finishTime(DateUtils.getTimeInSeconds(withdrawalRequestExport.getFinishTime()))
                .build();
    }

    @Override
    public ReportExportDownloadResponse downloadReport(String withdrawRequestId, String exportId) {
        log.info("Download report with withdrawRequestId: {} and exportId: {}", withdrawRequestId, exportId);
        VipoUserDetails user = getCurrentUser();

        // Find the WithdrawalRequestEntity by id and merchantId
        WithdrawalRequestEntity withdrawalRequest = repo.findByIdAndMerchantIdAndDeletedFalse(Long.valueOf(withdrawRequestId), user.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.WITHDRAW_NOT_FOUND));

        // Find the WithdrawalRequestExportEntity by id and withdrawalRequestId
        WithdrawalRequestExportEntity withdrawalRequestExport
                = withdrawalRequestExportEntityRepository.findByIdAndWithdrawalRequestIdAndDeletedFalse(Long.valueOf(exportId), withdrawalRequest.getId())
                .orElseThrow(() -> new VipoNotFoundException(RevenueConstant.REPORT_NOT_FOUND));

        // Check status of the WithdrawalRequestExportEntity
        WithdrawalRequestExportEnum reportExportStatus = withdrawalRequestExport.getStatus();
        if (reportExportStatus.equals(WithdrawalRequestExportEnum.PENDING)) {
            throw new VipoInvalidDataRequestException("Still pending");
        }

        if (reportExportStatus.equals(WithdrawalRequestExportEnum.FAILED)) {
            throw new VipoInvalidDataRequestException("Failed to create the file");
        }

        // Check storage type
        StorageType storageType = withdrawalRequestExport.getStorageType();
        if (!storageType.equals(StorageType.S3)) {
            return null; // only supporting s3 storage for now
        }

        // Get storage info
        String storageInfoStr = withdrawalRequestExport.getStorageInfo();
        if (StringUtils.isBlank(storageInfoStr)) {
            throw new VipoInvalidDataRequestException("Failed to create the file");
        }

        StorageInfoDTO storageInfoDTO = JsonMapperUtils.convertJsonToObject(storageInfoStr, StorageInfoDTO.class);
        if (ObjectUtils.isEmpty(storageInfoDTO) || StringUtils.isBlank(storageInfoDTO.getBucketName()) || StringUtils.isBlank(storageInfoDTO.getKey())) {
            throw new VipoInvalidDataRequestException("Failed to create the file");
        }

        // Create file name if not end with .xlsx
        String fileName = withdrawalRequestExport.getReportName();
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName += ".xlsx";
        }
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(StringProcessingUtils.normalizeVietnamese(fileName))
                .build();

        try {
            S3Object s3Object = amazonS3Service.getS3Object(storageInfoDTO.getBucketName(), storageInfoDTO.getKey());
            InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());
            long contentLength = s3Object.getObjectMetadata().getContentLength();
            return ReportExportDownloadResponse.builder()
                    .contentType(RevenueConstant.EXPORT_WITHDRAW_DETAIL_CONTENT_TYPE)
                    .fileName(fileName)
                    .contentDisposition(contentDisposition)
                    .contentLength(contentLength)
                    .resource(resource)
                    .build();
        } catch (IOException e) {
            throw new VipoFailedToExecuteException("Failed to load the excel file");
        }
    }

    /**
     * Hệ thống sẽ tự động tích chọn các đơn mà request trước đó đã chọn.
     * (Trường hợp đơn hàng có trong request từ chối đang nằm trong 1 request khác thì hệ thống sẽ loại bỏ những đơn hàng đó).
     */
    @Override
    public PagingRs reCreateWithdrawalRequest(String id) {
        log.info("[ get order reCreate Withdrawal Request with id]: {}", id);
        //
        Long merchantId = getCurrentUser().getId();
        WithdrawalRequestEntity request = repo.getRequestById(id).orElseThrow(
                () -> new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND)
        );
        if (!WithdrawalRequestConstant.FAIL_STATUS.contains(request.getStatus())) {
            throw new VipoNotFoundException(WithdrawalRequestConstant.RE_CREATE_STATUS_INVALID);
        }

        List<Long> orderPackageInRequest = withdrawalRequestItemRepository.getOrderPackageIdByRequestId(id);

        if (CollectionUtils.isEmpty(orderPackageInRequest)) {
            throw new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND);
        }

        List<Long> orderPackageReCreated = withdrawalRequestItemRepository.getOrderPackageIdReCreatedByRequestIdAndPackageIdIn(orderPackageInRequest);

        List<Long> orderPackageCanMakeRequest = new ArrayList<>();

        if (!CollectionUtils.isEmpty(orderPackageReCreated)) {
            if (orderPackageInRequest.size() == orderPackageReCreated.size()) {
                throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAILED_TO_EXECUTE, WithdrawalRequestConstant.RECREATE_ORDER_HAVE_REQUESTED);
            }
            orderPackageCanMakeRequest.addAll(orderPackageInRequest.stream().filter(o -> !orderPackageReCreated.contains(o)).toList());
        } else {
            orderPackageCanMakeRequest.addAll(orderPackageInRequest);
        }

        Collection<WithdrawalRequestItemProjection> orderPackages = orderPackageRepository.getOrderPackageReCreateByIdIn(merchantId, orderPackageCanMakeRequest);

        List<WithdrawalRequestItem> orderWithdrawalItems = orderPackages.stream().map(mapper::convertItemProjectionToItem).toList();

        Map<Long, PrepaymentTransactionData> trans = getPrepaymentTransactionId(orderWithdrawalItems.stream().map(WithdrawalRequestItem::getOrderPackageId).toList());

        if(!CollectionUtils.isEmpty(trans)){
            orderWithdrawalItems.forEach(item -> {
                PrepaymentTransactionData transData = trans.get(item.getOrderPackageId());
                if (ObjectUtils.isNotEmpty(transData))
                    item.setPrepaymentTransactionCode(transData.getPrepaymentTransactionCode());
            });
        }

        WithdrawalConfigEntity merchantConfig = getMerchantWithdrawConfig(merchantId);

        WithdrawalPaging res = new WithdrawalPaging();
        res.setData(orderWithdrawalItems);
        res.setFeeColumns(getFeeColumn(orderWithdrawalItems));
        res.setTaxValue(merchantConfig.getTax());
        return res;

    }

    @Override
    public Object reCreateWithdrawalRequestV2(String id) {
        log.info("[ get order reCreate Withdrawal Request with id]: {}", id);
        //
        Long merchantId = getCurrentUser().getId();
        WithdrawalRequestEntity request = repo.getRequestById(id).orElseThrow(
                () -> new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND)
        );
        if (!WithdrawalRequestConstant.FAIL_STATUS.contains(request.getStatus())) {
            throw new VipoNotFoundException(WithdrawalRequestConstant.RE_CREATE_STATUS_INVALID);
        }

        List<Long> orderPackageInRequest = withdrawalRequestItemRepository.getOrderPackageIdByRequestId(id);

        if (CollectionUtils.isEmpty(orderPackageInRequest)) {
            throw new VipoNotFoundException(WithdrawalRequestConstant.NOT_FOUND);
        }

        List<Long> orderPackageReCreated = withdrawalRequestItemRepository.getOrderPackageIdReCreatedByRequestIdAndPackageIdIn(orderPackageInRequest);

        List<Long> orderPackageCanMakeRequest = new ArrayList<>();

        if (!CollectionUtils.isEmpty(orderPackageReCreated)) {
            if (orderPackageInRequest.size() == orderPackageReCreated.size()) {
                throw new VipoInvalidDataRequestException(BaseExceptionConstant.FAILED_TO_EXECUTE, WithdrawalRequestConstant.RECREATE_ORDER_HAVE_REQUESTED);
            }
            orderPackageCanMakeRequest.addAll(orderPackageInRequest.stream().filter(o -> !orderPackageReCreated.contains(o)).toList());
        } else {
            orderPackageCanMakeRequest.addAll(orderPackageInRequest);
        }

        Map<String,  Collection<WithdrawalRequestItem>> res = new HashMap<>();
//        v2 detail data
        Collection<WithdrawalRequestItemProjection> orderPackages = orderPackageRepository.getOrderPackageByMerchantIdAndIdInV2(merchantId, orderPackageInRequest);
        List<WithdrawalRequestItem> orderWithdrawalItems = orderPackages.stream().map(mapper::convertItemProjectionToItem).toList();

        Map<Long, PrepaymentTransactionData> trans = getPrepaymentTransactionId(orderWithdrawalItems.stream().map(WithdrawalRequestItem::getOrderPackageId).toList());

        if(!CollectionUtils.isEmpty(trans)){
            orderWithdrawalItems.forEach(item -> {
                PrepaymentTransactionData transData = trans.get(item.getOrderPackageId());
                if (ObjectUtils.isNotEmpty(transData))
                    item.setPrepaymentTransactionCode(transData.getPrepaymentTransactionCode());
            });
        }

        Collection<WithdrawalRequestItem> orderWithdrawalValid = orderWithdrawalItems.stream().filter(o -> orderPackageCanMakeRequest.contains(o.getOrderPackageId())).toList();
        Collection<WithdrawalRequestItem> orderWithdrawalInValid = orderWithdrawalItems.stream().filter(o -> !orderPackageCanMakeRequest.contains(o.getOrderPackageId())).toList();
        res.put("orderValid", orderWithdrawalValid);
        res.put("orderInValid", orderWithdrawalInValid);

        return res;

    }

    private String getMerchantBankAccountInfo(Long merchantId) {

        WithdrawalRequestProjection merchantBankAccountInfo = repo.getBankAccountInfoByMerchantId(merchantId).orElseThrow(
                () -> new VipoNotFoundException(WithdrawalRequestConstant.BANK_ACCOUNT_INFO_NOT_FOUND)
        );
        return WithdrawalRequestMapper.convertAccountInfo(merchantBankAccountInfo);
    }

    private WithdrawalConfigEntity getMerchantWithdrawConfig(Long merchantId){
        return withdrawalConfigRepository.getConfigByMerchantId(merchantId).orElseThrow(
                () -> new VipoNotFoundException("Không tìm thấy cấu hình của Nhà!")
        );
    }

    @Override
    public void markExportAsFailed(OrderPackageReportExportMsg exportMsg, String failedMessage) {
        if (
                ObjectUtils.isEmpty(exportMsg)
                || ObjectUtils.isEmpty(exportMsg.getWithdrawalRequestReportId())
        ) {
           throw new VipoInvalidDataRequestException("Not found the withdrawalRequestReportId from the request: {}",
                    JsonMapperUtils.writeValueAsString(exportMsg));
        }
        WithdrawalRequestExportEntity withdrawalRequestExportEntity
                = withdrawalRequestExportEntityRepository.findById(exportMsg.getWithdrawalRequestReportId())
                .orElse(null);
        if (ObjectUtils.isEmpty(withdrawalRequestExportEntity))
            throw new VipoInvalidDataRequestException(
                    "Not found the withdrawal_request_export.id " + exportMsg.getWithdrawalRequestReportId()
            );

        withdrawalRequestExportEntity.setFinishTime(DateUtils.getCurrentLocalDateTime());
        withdrawalRequestExportEntity.setStatus(WithdrawalRequestExportEnum.FAILED);
        withdrawalRequestExportEntity.setErrorMessage(failedMessage);

        withdrawalRequestExportEntityRepository.save(withdrawalRequestExportEntity);
    }


    @Override
    public ExportWithdrawalRequestListResponse exportWithdrawalRequestList(
            ExportWithdrawalRequestListRequest exportWithdrawalRequestListRequest
    ) {
        // Load the template as a classpath resource
        ClassPathResource templateResource = new ClassPathResource("template/withdrawal_request_list_template.xlsx");

        if (!templateResource.exists()) {
            throw new VipoFailedToExecuteException("Không tìm thấy file mẫu khi xuất excel");
        }

        VipoUserDetails merchantInfo = getCurrentUser();

        // Use try-with-resources to ensure streams are closed automatically
        try (InputStream inputStream = templateResource.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data goes into the first sheet

            // Starting row index (0-based). If the first row is headers, start from 1
            int startRow = 1;

            List<WithdrawalRequestProjection> withdrawalRequestProjections
                    = repo.getAllWithdrawalRequestsByFilter(
                    exportWithdrawalRequestListRequest.getWithdrawalRequestType(),
                    exportWithdrawalRequestListRequest.getWithdrawalRequestStatus(),
                    exportWithdrawalRequestListRequest.getAmountFrom(),
                    exportWithdrawalRequestListRequest.getAmountTo(),
                    DateUtils.convertEpochSecondsToLocalDateTime(exportWithdrawalRequestListRequest.getStartDate()),
                    DateUtils.convertEpochSecondsToLocalDateTime(exportWithdrawalRequestListRequest.getEndDate()),
                    merchantInfo.getId()
            );

            for (WithdrawalRequestProjection withdrawalRequestProjection : withdrawalRequestProjections) {
                Row row = sheet.createRow(startRow++);

                WithdrawalRequestType requestType = withdrawalRequestProjection.getWithdrawalRequestType();
                WithdrawRequestStatusEnum requestStatusEnum = withdrawalRequestProjection.getWithdrawalRequestStatus();
                BigDecimal amount = withdrawalRequestProjection.getAmount();
                LocalDateTime dateTime = withdrawalRequestProjection.getDate();

                // Map data to specific columns
                row.createCell(0).setCellValue(
                        ObjectUtils.isNotEmpty(dateTime) ?
                                DateUtils.toDateString(dateTime, DateUtils.HHmmSSddMMyyyy) : null
                        );
                row.createCell(1).setCellValue(ObjectUtils.isNotEmpty(requestType) ? requestType.getLabel() : null);
                row.createCell(2).setCellValue(
                        ObjectUtils.isNotEmpty(requestStatusEnum) ? requestStatusEnum.getLable() : null
                );
                row.createCell(3).setCellValue(withdrawalRequestProjection.getReason());
                row.createCell(4).setCellValue(
                        ObjectUtils.isNotEmpty(amount) ? amount.setScale(0, RoundingMode.UP).toString() : null
                );
            }

            // Optionally, auto-size columns if not handled in the template
            for (int i = 0; i < 5; i++) { // Adjust based on the number of columns
                sheet.autoSizeColumn(i);
            }

            // Write the workbook to ByteArrayOutputStream
            workbook.write(baos);

            byte[] contentBytes = baos.toByteArray();

            String fileName = withdrawalRequestListExportFileName;
            if (!fileName.toLowerCase().endsWith(".xlsx")) {
                fileName += ".xlsx";
            }
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(StringProcessingUtils.normalizeVietnamese(fileName))
                    .build();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            long contentLength = contentBytes.length;


            return ExportWithdrawalRequestListResponse.builder()
                    .contentType(contentType)
                    .fileName(fileName)
                    .contentDisposition(contentDisposition)
                    .contentLength(contentLength)
                    .resource(contentBytes)
                    .build();
        } catch (IOException e) {
            throw new VipoFailedToExecuteException(e.getLocalizedMessage());
        }
    }

    private Map<Long, PrepaymentTransactionData> getPrepaymentTransactionId(Collection<Long> packageIds) {

        Collection<PrepaymentTransactionData> dataTransaction = repo.getTransactionCodeByOrderPackageIdIn(packageIds);
        if (dataTransaction.isEmpty())
            return Map.of();

        Map<Long, PrepaymentTransactionData> rs = new HashMap<>();

        for (PrepaymentTransactionData trans : dataTransaction) {
            Long key = trans.getPackageId();
            if (rs.containsKey(key)) {
                if (trans.getCreatePaymentTime().after(rs.get(key).getCreatePaymentTime())) {
                    rs.replace(key, trans);
                }
            } else {
                rs.put(key, trans);
            }
        }
        return rs;
    }

    private BigDecimal getAvailableBalanceByMerchantId(Long merchantId) {
        //TODO: chờ confirm vs BA xem có trừ các đơn bị âm ko
        return repo.getAvailableBalanceByMerchantId(merchantId).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
