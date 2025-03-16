package com.vtp.vipo.seller.services.certificate.impl;

import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dao.repository.ProductCertificateEntityRepository;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.certificate.PDFConversionService;
import com.vtp.vipo.seller.services.certificate.UploadImagePDFService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadImagePDFServiceImpl implements UploadImagePDFService {
    ProductCertificateEntityRepository productCertificateEntityRepository;

    PDFConversionService pdfConversionService;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> processPdfImages(ProductCertificateEntity certificate, MultipartFile pdfFile, List<StorageInfoDTO> storageInfoDTOS) {
        try {
            log.info("[processPdfImages] Starting processing PDF images for Certificate ID: {}", certificate.getId());
            List<StorageInfoDTO> pdfImages = pdfConversionService.extractPdfToImages(pdfFile).join();

            if (!pdfImages.isEmpty()) {
                // Update certificate with extracted images
                List<String> imageLinks = new ArrayList<>(pdfImages.stream().map(StorageInfoDTO::getLink).toList());
                storageInfoDTOS.addAll(pdfImages);

                // Update certificate
                certificate.setStorageInfo(JsonMapperUtils.writeValueAsString(storageInfoDTOS));
                certificate.setRepresentedImageLinks(JsonMapperUtils.writeValueAsString(imageLinks));

                // Save certificate
                productCertificateEntityRepository.save(certificate);
                log.info("[processPdfImages] Done processing PDF images for Certificate ID: {}", certificate.getId());
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("[processPdfImages] Error processing PDF images for Certificate ID: {}", certificate.getId(), e);
            return CompletableFuture.completedFuture(null);
        }
    }
}
