package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
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
import jakarta.validation.Valid;

import java.util.List;

public interface WithdrawalRequestService {

    /**
     * Retrieves an overview of withdrawal requests.
     * <p>
     * This method fetches and provides a summary of the withdrawal requests from the system,
     * including details such as total amount withdrawn, pending balance, etc.
     *
     * @return A {@link WithdrawalRequestOverviewRes} object containing an overview of withdrawal requests.
     */
    WithdrawalRequestOverviewRes getWithdrawRequestOverview();

    /**
     * Searches for withdrawal requests based on the specified filter criteria.
     * <p>
     * This method performs a search operation for withdrawal requests that match the given filter criteria, such as date range, status, etc.
     * The results are paginated to support efficient data retrieval.
     *
     * @param request a {@link WithdrawalRequestFilter} object containing the filter criteria for the search operation.
     * @return A {@link PagingRs} object containing the paginated results of the search operation.
     * @throws IllegalArgumentException if the filter criteria are invalid.
     */
    PagingRs searchWithdrawalRequests(@Valid WithdrawalRequestFilter request);


    /**
     * Retrieves the details of a specific withdrawal request.
     * <p>
     * This method fetches detailed information about a withdrawal request based on the provided ID.
     *
     * @param id the unique identifier of the withdrawal request.
     * @return A {@link WithdrawalRequestDetailResponse} object containing the details of the specified withdrawal request.
     * @throws IllegalArgumentException if the provided ID is invalid.
     */
    WithdrawalRequestDetailResponse getWithdrawalRequestDetail(String id);

    /**
     * Retrieves information required to create a new withdrawal request.
     * <p>
     * This method provides the necessary data and context required to initiate the creation of a new withdrawal request.
     *
     * @return A {@link WithdrawalRequestCreateInfoRes} object containing the information needed to create a new withdrawal request.
     */
    WithdrawalRequestCreateInfoRes getWithdrawalRequestCreateInfo();

    /**
     * Retrieves order packages eligible for withdrawal based on the specified filter criteria.
     * <p>
     * This method performs a search operation for order packages that are eligible for withdrawal, matching the given filter criteria.
     * The results are paginated to support efficient data retrieval.
     *
     * @param request a {@link WithdrawalRequestCreateFilter} object containing the filter criteria for the search operation.
     * @return A {@link PagingRs} object containing the paginated results of order packages eligible for withdrawal.
     * @throws IllegalArgumentException if the filter criteria are invalid.
     */
    PagingRs getOrderPackgeToWithdrawal(@Valid WithdrawalRequestCreateFilter request);

    Object createWithdrawalRequest(List<Long> orderPackageIds);

    CancelWithdrawRequestResponse cancelWithdrawRequest(String withdrawRequestId);

    List<WithdrawRequestHistoryResponse> getWithdrawRequestHistory(String withdrawRequestId);

    WithdrawRequestExportResponse exportReport(String withdrawRequestId);

    WithdrawRequestExportDetailsResponse getReportDetails(String withdrawRequestId, String exportId);

    ReportExportDownloadResponse downloadReport(String withdrawRequestId, String exportId);

    PagingRs reCreateWithdrawalRequest(String id);

    Object reCreateWithdrawalRequestV2(String id);

    void markExportAsFailed(OrderPackageReportExportMsg exportMsg, String failedMessage);

    ExportWithdrawalRequestListResponse exportWithdrawalRequestList(ExportWithdrawalRequestListRequest exportWithdrawalRequestListRequest);
}
