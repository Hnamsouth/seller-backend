package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.request.product.ProductCreateUpdateRequest;
import com.vtp.vipo.seller.common.dto.request.product.approve.ApproveProductRequest;
import com.vtp.vipo.seller.common.dto.request.product.search.ProductSearchReq;
import com.vtp.vipo.seller.common.dto.request.product.update.*;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.product.approve.ApproveProductResponse;
import com.vtp.vipo.seller.common.dto.response.product.create.ImportByFileResultRes;
import com.vtp.vipo.seller.common.dto.response.product.detail.ProductDetailResponse;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    PagingRs search(ProductSearchReq request, Pageable pageable);

    ProductDetailResponse getDetail(Long id);

    String deleteProduct(PauseStopSellingProductReq req);

    String updateTotal(ProductCreateUpdateRequest productCreateUpdateRequest, boolean isDraft);

    String saveSendApprove(ProductCreateUpdateRequest productCreateUpdateRequest);

    String requestReview(Long id);

    String cancelApprovalRequest(Long id);

    String cancelDraft(Long id);

    String saveDraft(ProductCreateUpdateRequest productCreateUpdateRequest);

    /**
     * Sản phẩm đang ở trạng thái "đang bán" và "đã duyệt kiểm tạo", ấn "sửa thông tin cơ bản" sẽ gọi Hàm này
     *
     * This method is called when a product in the "selling" and "approved" status
     * needs to update its basic information. It updates information like category,
     * name, image, and description. If not in draft mode, it changes the product's
     * status to "adjust pending".
     *
     * @param request The request object containing the updated base information for the product.
     * @param isDraft A boolean indicating if the product is in draft status or not.
     * @return A success description message indicating the result of the operation.
     */
    String updateBaseInfo(UpdateBaseInfoProductReq request, boolean isDraft);

    String updateSellingInfo(UpdateSellingInfoProductReq request, boolean isDraft);

    String updateSpecInfo(UpdateSpecInfoProductReq request, boolean isDraft);

    String updateDraft(ProductCreateUpdateRequest request);

    String updateAttribute(UpdateAttributeProductReq request, boolean isDraft);

    String continueSelling(ContinueSellingProductReq product);

    String pauseSelling(PauseStopSellingProductReq req);

    String stopSelling(PauseStopSellingProductReq req);

    ProductDetailResponse getDetailTemporary(Long id);

    String updateStock(ContinueSellingProductReq product);

    String getCreateTemplate();

    ImportByFileResultRes createByFile(MultipartFile multipartFile);

    ResponseEntity<InputStreamResource> getTemplate();

    String updateRequest(ProductCreateUpdateRequest request);

    ApproveProductResponse approveProduct(@Valid ApproveProductRequest approveProductRequest);
}
