package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.ProductConstant;
import com.vtp.vipo.seller.common.dto.request.product.ProductCreateUpdateRequest;
import com.vtp.vipo.seller.common.dto.request.product.approve.ApproveProductRequest;
import com.vtp.vipo.seller.common.dto.request.product.search.ProductSearchReq;
import com.vtp.vipo.seller.common.dto.request.product.update.*;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoUnAuthorizationException;
import com.vtp.vipo.seller.services.ProductService;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController extends BaseController<ProductService> {
    /*Tìm kiếm*/
    @PostMapping("/search")
    @ApiOperation(value = "Tìm kiếm", notes = "Tìm kiếm sản phẩm khách hàng đã tạo")
    public ResponseEntity<?> search(@RequestBody @Valid ProductSearchReq request,
                                    @PageableDefault(sort = "updateTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return toResult(service.search(request, pageable));
    }

    /*Tạo nháp*/
    @ApiOperation(value = "Tạo nháp", notes = "Tạo nháp sản phẩm nháp khong gửi duyệt")
    @PostMapping("/create-draft")
    public ResponseEntity<?> createDraft(@Valid @RequestBody ProductCreateUpdateRequest request) {
        return toResult(BaseExceptionConstant.SUCCESS,
                ProductConstant.CREATE_SUCCESS_DESCRIPTION,
                service.saveDraft(request));
    }

    /**
     * HA note:
     *
     * Khi tạo sản phẩm mới sẽ gọi API này
     */
    /*Tạo gửi duyệt*/
    @ApiOperation(value = "Tạo gửi duyệt", notes = "Tạo nháp sản phẩm")
    @PostMapping("/create-send-approve")
    public ResponseEntity<?> createSendApprove(@Valid @RequestBody ProductCreateUpdateRequest request) {
        return toResult(BaseExceptionConstant.SUCCESS,
                ProductConstant.CREATE_SUCCESS_DESCRIPTION,
                service.saveSendApprove(request));
    }

    /*Xem chi tiet*/
    @GetMapping("/detail/{id}")
    @ApiOperation(value = "Xem chi tiết", notes = "Xem chi tiết sản phẩm khách hàng tạo")
    public ResponseEntity<?> getDetail(@PathVariable @Param("id") Long id) {
        return toResult(service.getDetail(id));
    }

    /*Xóa*/
    @PutMapping("/delete-product")
    @ApiOperation(value = "Xóa sản phẩm", notes = "Xóa sản phẩm sản phẩm khách hàng tạo")
    public ResponseEntity<?> deleteProduct(@Valid @RequestBody PauseStopSellingProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.DELETE_SUCCESS_DESCRIPTION,
                service.deleteProduct(productRequest));
    }

    /*yêu cau duyet lại*/
    @GetMapping("request-review/{id}")
    @ApiOperation(value = "yêu cau duyet lại", notes = "Xóa sản phẩm sản phẩm khách hàng tạo")
    public ResponseEntity<?> requestReview(@PathVariable @Param("id") Long id) {
        return toResult(service.requestReview(id));
    }

    /*Sửa YC duyệt*/
    @PutMapping("update-request")
    @ApiOperation(value = "Sửa YC duyệt", notes = "Sửa YC duyệt sản phẩm sản phẩm khách hàng tạo")
    public ResponseEntity<?> updateRequest(@Valid @RequestBody ProductCreateUpdateRequest request) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateRequest(request));
    }

    /*Hủy yêu cầu duyệt*/
    @GetMapping("cancel-approval-request/{id}")
    @ApiOperation(value = "Hủy yêu cầu duyệt", notes = "Hủy yêu cầu duyệt sản phẩm khách")
    public ResponseEntity<?> cancelApprovalRequest(@PathVariable @Param("id") Long id) {
        return toResult(service.cancelApprovalRequest(id));
    }

    /*Hủy nháp*/
    @GetMapping("cancel-draft/{id}")
    @ApiOperation(value = "Hủy nháp", notes = "Hủy nháp sản phẩm khách")
    public ResponseEntity<?> cancelDraft(@PathVariable @Param("id") Long id) {
        return toResult(service.cancelDraft(id));
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái 7: "Đang sửa sản phẩm", ấn "Cập nhật", r sẽ gọi API này
     */
    /*Sửa nháp*/
    @PutMapping("/update-draft")
    @ApiOperation(value = "Sửa nháp", notes = "Sửa nháp sản phẩm khách")
    public ResponseEntity<?> updateDraft(@Valid @RequestBody ProductCreateUpdateRequest productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateDraft(productRequest));
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái đang bán, ấn "sửa toàn bộ thông tin", Rồi ấn "Lưu nháp", r sẽ gọi API này
     */
    /*Sửa toàn bộ TT nháp*/
    @PutMapping("/update-total-draft")
    @ApiOperation(value = "Sửa toàn bộ TT nháp", notes = "Sửa toàn bộ TT nháp sản phẩm khách")
    public ResponseEntity<?> updateTotalDraft(@Valid @RequestBody ProductCreateUpdateRequest productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateTotal(productRequest, true));
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái đang bán, ấn "sửa toàn bộ thông tin", Rồi ấn "Cập nhật", sẽ gọi API này
     */
    /*Sửa toàn bộ TT*/
    @PutMapping("/update-total")
    @ApiOperation(value = "Sửa toàn bộ TT", notes = "Sửa toàn bộ TT sản phẩm khách")
    public ResponseEntity<?> updateTotal(@Valid @RequestBody ProductCreateUpdateRequest productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateTotal(productRequest, false));
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái đang bán, ấn "sửa thông tin cơ bản" sẽ gọi API này
     */
    @PutMapping("/update-base-info")
    @ApiOperation(value = "Sửa thông tin cơ bản", notes = "Sửa thông tin cơ bản sản phẩm khách")
    public ResponseEntity<?> updateBaseInfo(@Valid @RequestBody UpdateBaseInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateBaseInfo(productRequest, false));
    }

    @PutMapping("/update-base-info-draft")
    @ApiOperation(value = "Sửa thông tin cơ bản nhap", notes = "Sửa nhap thông tin cơ bản sản phẩm khách")
    public ResponseEntity<?> updateBaseInfoDraft(@Valid @RequestBody UpdateBaseInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateBaseInfo(productRequest, false));
    }

    @PutMapping("/update-selling-info")
    @ApiOperation(value = "Sửa thông tin cơ bản", notes = "Sửa thông tin bán hàng sản phẩm khách")
    public ResponseEntity<?> updateSellingInfo(@Valid @RequestBody UpdateSellingInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateSellingInfo(productRequest, false));
    }

    @PutMapping("/update-selling-info-draft")
    @ApiOperation(value = "Sửa thông tin cơ bản nhap", notes = "Sửa nhap thông tin bán hàng sản phẩm khách")
    public ResponseEntity<?> updateSellingInfoDraft(@Valid @RequestBody UpdateSellingInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateSellingInfo(productRequest, true));
    }

    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái "đang bán", ấn "sửa thông số" sẽ gọi API này
     */
    @PutMapping("/update-spec-info")
    @ApiOperation(value = "Sửa thông số", notes = "Sửa thông số sản phẩm khách")
    public ResponseEntity<?> updateSpecInfo(@Valid @RequestBody UpdateSpecInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateSpecInfo(productRequest, false));
    }

    @PutMapping("/update-spec-info-draft")
    @ApiOperation(value = "Sửa thông số nhap", notes = "Sửa thông số sản phẩm khách map")
    public ResponseEntity<?> updateSpecInfoDraft(@Valid @RequestBody UpdateSpecInfoProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateSpecInfo(productRequest, true));
    }

    /*Sửa thông tin phân loại*/
    /**
     * HA note:
     *
     * Sản phẩm đang ở trạng thái "đang bán", ấn "Sửa thông tin phân loại" sẽ gọi API này
     */
    @PutMapping("/update-attribute")
    @ApiOperation(value = "Sửa thông tin phân loại", notes = "Sửa thông tin phân loại sản phẩm khách")
    public ResponseEntity<?> updateAttribute(@Valid @RequestBody UpdateAttributeProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateAttribute(productRequest, false));
    }

    /*Sửa thông tin phân loại nhap*/
    @PutMapping("/update-attribute-draft")
    @ApiOperation(value = "Sửa thông tin phân loại nhap", notes = "Sửa thông tin phân loại sản phẩm khách nhap")
    public ResponseEntity<?> updateAttributeDraft(@Valid @RequestBody UpdateAttributeProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateAttribute(productRequest, true));
    }

    /*tiếp tục bán*/
    @PutMapping("/continue-selling")
    @ApiOperation(value = "tiếp tục bán", notes = "tiếp tục bán sản phẩm")
    public ResponseEntity<?> continueSelling(@Valid @RequestBody ContinueSellingProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.continueSelling(productRequest));
    }

    /*dừng bán*/
    @PutMapping("/pause-selling")
    @ApiOperation(value = "dừng bán", notes = "dừng bán sản phẩm")
    public ResponseEntity<?> pauseSelling(@Valid @RequestBody PauseStopSellingProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.pauseSelling(productRequest));
    }

    /*ngừng bán*/
    @PutMapping("/stop-selling")
    @ApiOperation(value = "ngừng bán", notes = "ngừng bán sản phẩm")
    public ResponseEntity<?> stopSelling(@Valid @RequestBody PauseStopSellingProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.stopSelling(productRequest));
    }

    /*Xem chi tiet bang tam*/
    @GetMapping("/detail-temporary/{id}")
    @ApiOperation(value = "Xem chi tiết bang tam", notes = "Xem chi tiết bang tam sản phẩm khách hàng tạo")
    public ResponseEntity<?> getDetailTemporary(@PathVariable @Param("id") Long id) {
        var res = service.getDetailTemporary(id);
        if(ObjectUtils.isEmpty(res)){
            throw new VipoBusinessException(ErrorCodeResponse.ERROR_EMPTY_TEMP_RECORD);
        }
        return toResult(res);
    }

    /*Cập nhật tồn kho*/
    @PutMapping("/update-stock")
    @ApiOperation(value = "Cập nhật tồn kho", notes = "Cập nhật tồn kho sản phẩm")
    public ResponseEntity<?> updateStock(@Valid @RequestBody ContinueSellingProductReq productRequest) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.updateStock(productRequest));
    }

    @GetMapping("/create-template")
    @ApiOperation(value = "Lấy file mẫu tạo mới", notes = "Lấy file mẫu tạo mới sản phẩm")
    public ResponseEntity<?> getCreateTemple() {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.getCreateTemplate());
    }

    @PostMapping("/create-by-file")
    @ApiOperation(value = "Tạo mới theo file", notes = "Tạo mới sản phẩm theo file")
    public ResponseEntity<?> createByFile(@Valid @RequestBody @Param("file") MultipartFile file) {
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.createByFile(file));
    }
    @GetMapping("/get-template")
    @ApiOperation(value = "Lấy file mẫu tạo mới", notes = "Lấy file mẫu tạo mới sản phẩm")
    public ResponseEntity<?> getTemple() {
        return service.getTemplate();
    }


    @PostMapping("/approve-temp-product")
    @ApiOperation(value = "Lấy file mẫu tạo mới", notes = "Lấy file mẫu tạo mới sản phẩm")
    public ResponseEntity<?> approveTempProduct(
            HttpServletRequest request, @RequestBody ApproveProductRequest approveProductRequest
    ) {
        String key = request.getHeader("credential");
        if (StringUtils.isBlank(key) || !key.equals("vipo-cms"))
            throw new VipoUnAuthorizationException();
        return toResult(BaseExceptionConstant.SUCCESS, ProductConstant.UPDATE_SUCCESS_DESCRIPTION,
                service.approveProduct(approveProductRequest));
    }

}

