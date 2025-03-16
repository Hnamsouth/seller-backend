package com.vtp.vipo.seller.services.certificate;

import com.vtp.vipo.seller.common.dto.response.UploadCertificateResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductCertificateService {
    UploadCertificateResponse addCertificate(Long productId, List<MultipartFile> files);

    void deleteCertificate(Long certificateId);

    PagingRs getCertificatesByProduct(Long productId, int page, int pageSize);

    UploadCertificateResponse addCertificateToProduct(Long productId, List<MultipartFile> files);

    void removeCertificateFromProduct(Long productId, Long certificateId);
}
