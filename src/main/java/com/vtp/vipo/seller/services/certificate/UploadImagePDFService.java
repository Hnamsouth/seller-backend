package com.vtp.vipo.seller.services.certificate;

import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UploadImagePDFService {
    CompletableFuture<Void> processPdfImages(ProductCertificateEntity certificate, MultipartFile pdfFile, List<StorageInfoDTO> storageInfoDTOS);
}
