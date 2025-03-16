package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.WithdrawalRequestConstant;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawRequestStatusEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestType;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.ExportWithdrawalRequestListRequest;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.WithdrawalRequestCreateFilter;
import com.vtp.vipo.seller.common.dto.request.withdrawalrequest.WithdrawalRequestFilter;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDownloadResponse;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.ExportWithdrawalRequestListResponse;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestCreateRes;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.WithdrawalRequestService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/revenue/withdrawal")
public class WithdrawalRequestController extends BaseController<WithdrawalRequestService> {

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {
        return toResult(service.getWithdrawRequestOverview());
    }

    @PostMapping("/list")
    public ResponseEntity<?> getWithdrawalRequests(@RequestBody WithdrawalRequestFilter request) {
        return toResult(service.searchWithdrawalRequests(request));
    }

    @GetMapping("/request/{id}")
    public ResponseEntity<?> getWithdrawalRequestDetail(@PathVariable String id){
        return toResult(service.getWithdrawalRequestDetail(id));
    }

    @GetMapping("/create/info")
    public ResponseEntity<?> getWithdrawalRequestCreateInfo() {
        return toResult(service.getWithdrawalRequestCreateInfo());
    }

    @PostMapping("/order-package/list")
    public ResponseEntity<?> getOrderPackageList(@RequestBody WithdrawalRequestCreateFilter request) {
        return toResult(service.getOrderPackgeToWithdrawal(request));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createWithdrawalRequest(@RequestBody List<Long> orderPackageIds) {
        Object result = service.createWithdrawalRequest(orderPackageIds);
        if(result instanceof WithdrawalRequestCreateRes) {
            return toResult(result);
        }else {
            return toResult(BaseExceptionConstant.FAIL_CREATE_WITHDRAW, WithdrawalRequestConstant.ORDER_HAVE_REQUESTED , result);
        }
    }

    @GetMapping("/re-create/{id}")
    public ResponseEntity<?> createWithdrawalRequest(@PathVariable String id) {
        //TODO: re open this when re-create v2 is ready
        return toResult(service.reCreateWithdrawalRequestV2(id));
//        return toResult(service.reCreateWithdrawalRequest(id));
    }

    /*
     * Huy yeu cau rut tien
     * */
    @PostMapping("/request/{id}/cancel")
    public ResponseEntity<?> cancelWithdrawRequest(@PathVariable String id) {
        return ResponseUtils.success(service.cancelWithdrawRequest(id));
    }

    /*
     * Xem lich su tac dong len yeu cau rut tien
     * */
    @GetMapping("/request/{id}/history")
    public ResponseEntity<?> getWithdrawRequestHistory(@PathVariable String id) {
        return ResponseUtils.success(service.getWithdrawRequestHistory(id));
    }

    /*
     * Xuất báo cáo
     * */
    @PostMapping("/request/{id}/export")
    public ResponseEntity<?> exportReport(@PathVariable String id) {
        return ResponseUtils.success(service.exportReport(id));
    }


    /*
     * Lấy thông tin chi tiết của báo cáo
     * */
    @GetMapping("/request/{requestId}/export/{exportId}")
    public ResponseEntity<?> getReportDetails(@PathVariable String requestId, @PathVariable String exportId) {
        return ResponseUtils.success(service.getReportDetails(requestId, exportId));
    }

    /*
     * Download báo cáo
     * */
    @GetMapping("/request/{requestId}/export/{exportId}/download")
    public ResponseEntity<InputStreamResource> downloadReport(@PathVariable String requestId, @PathVariable String exportId) {
        ReportExportDownloadResponse downloadResponse = service.downloadReport(requestId, exportId);
        if (ObjectUtils.isEmpty(downloadResponse)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(downloadResponse.getContentType()));
        headers.setContentDisposition(downloadResponse.getContentDisposition());

        logger.info("Content disposition is {} ", headers.getContentDisposition());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(downloadResponse.getContentLength())
                .body(downloadResponse.getResource());
    }

    @GetMapping("/list/export")
    public ResponseEntity<byte[]> exportWithdrawalRequestList (
            @RequestParam(required = false) WithdrawalRequestType withdrawalRequestType,
            @RequestParam(required = false) List<WithdrawRequestStatusEnum> withdrawalRequestStatus,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate
    ) {


        ExportWithdrawalRequestListResponse exportResponse
                = service.exportWithdrawalRequestList(
                        ExportWithdrawalRequestListRequest.builder()
                                .withdrawalRequestType(withdrawalRequestType)
                                .withdrawalRequestStatus(withdrawalRequestStatus)
                                .amountFrom(amountFrom)
                                .amountTo(amountTo)
                                .startDate(startDate)
                                .endDate(endDate)
                                .build()
        );

        if (ObjectUtils.isEmpty(exportResponse)) {
            throw new VipoFailedToExecuteException("Thất bại khi xuất file excel!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exportResponse.getContentType()));
        headers.setContentDisposition(exportResponse.getContentDisposition());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(exportResponse.getContentLength())
                .body(exportResponse.getResource());
    }

}
