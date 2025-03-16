package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.RootStatusEnum;
import com.vtp.vipo.seller.common.dto.request.*;
import com.vtp.vipo.seller.common.dto.request.order.OrderFilterRequest;
import com.vtp.vipo.seller.common.dto.request.order.OrderRefuseCancelRequest;
import com.vtp.vipo.seller.common.dto.response.OrderPackageResponse;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.ExcelServiceUtils;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.OrderService;
import com.vtp.vipo.seller.services.order.OrderPackageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.vtp.vipo.seller.common.dto.request.order.ApproveOrderPackagesRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController extends BaseController<OrderService> {

    private final OrderPackageService orderPackageService;

    private final ExcelServiceUtils excelServiceUtils;

    @GetMapping("/status")
    public ResponseEntity<?> getAllOrderStatus() {
        return toResult(service.getAllRootOrderStatus());
    }

    @GetMapping("/keyword-search")
    public ResponseEntity<?> getAllKeywordSearch() {
        return toResult(service.getAllKeywordSearch());
    }

    @PostMapping("/orders-by-status")
    public ResponseEntity<?> getListOrder(@RequestBody SearchOrderByKeywordRequest request) {
        if (!ObjectUtils.isEmpty(request.getOrderStatus()) && !request.getOrderStatus().equals(RootStatusEnum.WAIT_FOR_PAY.getCode())) {
            return toResult(service.getAllByOrderStatus(request));
        }
        if(!ObjectUtils.isEmpty(request.getOrderStatus()) && request.getOrderStatus().equals(RootStatusEnum.WAIT_FOR_PAY.getCode())){
            return toResult(service.getWaitForPayOrders(request));
        }
        throw new VipoNotFoundException();
    }

    @GetMapping("/detail")
    public ResponseEntity<?> detail(@RequestParam("code") String code) {
        return toResult(service.getOrderByCode(code));
    }

    @PostMapping("/revenue")
    public ResponseEntity<?> revenue(@RequestBody SearchOrderByKeywordRequest request) {
        return toResult(service.getAllRevenue(request));
    }

    @PostMapping("/download-excel-revenue")
    public ResponseEntity<Resource> exportRevenue(@RequestBody SearchOrderByKeywordRequest request) {
        ByteArrayInputStream file = excelServiceUtils.export(service.exportRevenue(request), OrderPackageResponse.class);
        LocalDate localDate = LocalDate.now();

        Date utilDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        String fileName = "excel_revenue_" + DateUtils.toDateString(utilDate, DateUtils.DD_MM_YYYY_HH_MM_SS) + ".xlsx";
        InputStreamResource in = new InputStreamResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.parseMediaType(Constants.HTTP_RESPONSE_HEADER_CONTENT_TYPE_EXCEL)).body(in);
    }

    @PostMapping("/total-unearned-revenue")
    public ResponseEntity<?> unearnedRevenue(@RequestBody SearchOrderByKeywordRequest request) {
        return toResult(service.sumOrdersByMerchant(request.getOrderStatus()));
    }

    @PostMapping("/total-recognized-revenue")
    public ResponseEntity<?> recognizedRevenue(@RequestBody SearchOrderByKeywordRequest request) {
        return toResult(service.sumOrdersByMerchant(request.getOrderStatus()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long orderId) {
        return ResponseUtils.success(service.getOrderDetails(orderId));
    }

    @PostMapping("/adjust-price/total")
    public ResponseEntity<?> adjustPriceByTotal(@Valid @RequestBody AdjustPriceByTotalRequest request) {
        service.adjustPriceByTotal(request);
        return ResponseUtils.success();
    }

    @PostMapping("/adjust-price/sku")
    public ResponseEntity<?> adjustPriceBySku(@Valid @RequestBody AdjustPriceBySkuRequest request) {
        service.adjustPriceBySku(request);
        return ResponseUtils.success();
    }

    @GetMapping("/{orderId}/price-adjustments")
    public ResponseEntity<?> getPriceAdjustmentHistory(@PathVariable Long orderId) {
        return ResponseUtils.success(service.getPriceAdjustmentHistory(orderId));
    }

    @GetMapping("/adjust-price/total")
    public ResponseEntity<?> getPriceAdjustmentTotalDetail(@RequestParam Long orderId, @RequestParam Long adjustmentHistoryId) {
        return ResponseUtils.success(service.getPriceAdjustmentTotalDetail(orderId, adjustmentHistoryId));
    }

    @GetMapping("/adjust-price/sku")
    public ResponseEntity<?> getPriceAdjustmentSkuDetail(@RequestParam Long orderId, @RequestParam Long adjustmentHistoryId) {
        return ResponseUtils.success(service.getPriceAdjustmentSkuDetail(orderId, adjustmentHistoryId));
    }

    @PostMapping("/prepare")
    public ResponseEntity<?> prepareOrder(@RequestBody PrepareOrderRequest request) {
        return ResponseUtils.success(service.prepareOrder(request));
    }

    @PostMapping("/print-label")
    public ResponseEntity<?> printLabel(@RequestBody PrintLabelRequest request) {
        return ResponseUtils.success(service.printLabel(request));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> searchOrder(@RequestBody OrderFilterRequest request
    ) {
        return ResponseUtils.success(service.searchOrder(request));
    }

    /**
     * Hủy đơn hàng
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestBody @Valid @NotEmpty List<OrderRefuseCancelRequest> request){
       return ResponseUtils.success(service.cancelOrders(request));
    }

    /**
     * Từ chối đơn hàng
     */
    @PostMapping("/refuse")
    public ResponseEntity<?> rejectOrder(@RequestBody  @Valid @NotEmpty List<OrderRefuseCancelRequest> request){
        return ResponseUtils.success(service.rejectOrders(request));
    }

    @GetMapping("/seller-management-status")
    public ResponseEntity<?> getAllOrderSellerManagementStatus() {
        return toResult(service.getAllOrderSellerManagementStatusV2());
    }

    /**
     * Duyệt đơn hàng
     * Handles the approval of multiple order packages.
     *
     * <p>This endpoint processes a request to approve one or more order packages.
     * It delegates the approval logic to the {@code orderPackageService} and returns
     * a standardized success response upon completion.</p>
     *
     * @param approveOrderPackagesRequest the request payload containing details of the order packages to approve
     * @return a {@link ResponseEntity} containing the result of the approval operation
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveOrderPackages(
            @RequestBody ApproveOrderPackagesRequest approveOrderPackagesRequest
    ) {
        return ResponseUtils.success(orderPackageService.approveOrderPackages(approveOrderPackagesRequest));
    }

}
