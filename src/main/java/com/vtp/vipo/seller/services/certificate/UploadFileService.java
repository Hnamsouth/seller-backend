package com.vtp.vipo.seller.services.certificate;

import com.vtp.vipo.seller.common.dto.response.UploadCertificateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface UploadFileService {
    CompletableFuture<UploadCertificateResponse.SuccessData> uploadFile(Long productId, MultipartFile file);
}
