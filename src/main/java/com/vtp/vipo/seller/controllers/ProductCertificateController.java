package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.certificate.ProductCertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductCertificateController extends BaseController<ProductCertificateService> {
    @PostMapping("/product/certificate")
    public ResponseEntity<?> addCertificate(@RequestParam(name = "files") List<MultipartFile> files) {
        return toResult(service.addCertificate(null, files));
    }

    @DeleteMapping("/product/certificate/{certificateId}")
    public ResponseEntity<?> delelteCertificate(@PathVariable String certificateId) {
        service.deleteCertificate(Long.valueOf(certificateId));
        return ResponseUtils.success();
    }

    @GetMapping("/product/{productId}/certificate")
    public ResponseEntity<?> getCertificatesByProduct(@PathVariable Long productId,
                                                      @RequestParam(required = false, defaultValue = Constants.PAGE_DEFAULT) int page,
                                                      @RequestParam(required = false, defaultValue = Constants.PAGE_SIZE_DEFAULT) int pageSize) {
        return toResult(service.getCertificatesByProduct(productId, page, pageSize));
    }

    @PostMapping("/product/{productId}/certificate")
    public ResponseEntity<?> addCertificateToProduct(@PathVariable Long productId, @RequestParam(name = "files") List<MultipartFile> files) {
        return toResult(service.addCertificateToProduct(productId, files));
    }

    @DeleteMapping("/product/{productId}/certificate/{certificateId}")
    public ResponseEntity<?> removeCertificateFromProduct(@PathVariable Long productId,
                                                          @PathVariable String certificateId) {
        service.removeCertificateFromProduct(productId, Long.valueOf(certificateId));
        return ResponseUtils.success();
    }
}
