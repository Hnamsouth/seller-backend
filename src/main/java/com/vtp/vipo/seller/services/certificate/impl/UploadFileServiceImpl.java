package com.vtp.vipo.seller.services.certificate.impl;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dao.repository.ProductCertificateEntityRepository;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.UploadCertificateResponse;
import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import com.vtp.vipo.seller.common.enumseller.StorageType;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.FileUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.certificate.UploadFileService;
import com.vtp.vipo.seller.services.certificate.UploadImagePDFService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadFileServiceImpl implements UploadFileService {
    ProductCertificateEntityRepository productCertificateEntityRepository;

    UploadImagePDFService uploadImagePDFService;

    AmazonS3Service amazonS3Service;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<UploadCertificateResponse.SuccessData> uploadFile(Long productId, MultipartFile file) {
        log.info("[uploadFile] START uploading file - productId: {}, fileName: {}", productId, file.getOriginalFilename());
        try {
            // Upload original file
            StorageInfoDTO storageInfoDTO = amazonS3Service.uploadFile(file);

            // Prepare image links
            List<String> imageLinks = new ArrayList<>();
            imageLinks.add(storageInfoDTO.getLink());

            // Prepare storageInfo list
            List<StorageInfoDTO> updatedStorageInfo = new ArrayList<>();
            updatedStorageInfo.add(storageInfoDTO);

            // Prepare certificate entity
            ProductCertificateEntity certificate = ProductCertificateEntity.builder()
                    .productId(productId)
                    .name(file.getOriginalFilename())
                    .storageType(StorageType.S3)
                    .storageInfo(JsonMapperUtils.writeValueAsString(Collections.singletonList(storageInfoDTO)))
                    .fileSize(file.getSize())
                    .fileLink(storageInfoDTO.getLink())
                    .representedImageLinks(JsonMapperUtils.writeValueAsString(imageLinks))
                    .contentType(file.getContentType())
                    .status(CertificateStatus.INACTIVE)
                    .build();

            // Save certificate
            ProductCertificateEntity savedCertificate = productCertificateEntityRepository.save(certificate);
            log.info("[uploadFile] Certificate saved - ID: {}", savedCertificate.getId());

            if (FileUtils.isPdf(file)) {
                log.info("[uploadFile] Triggering PDF image extraction for Certificate ID: {}", savedCertificate.getId());
                CompletableFuture<Void> pdfProcessingFuture = uploadImagePDFService.processPdfImages(savedCertificate, file, updatedStorageInfo);
                pdfProcessingFuture
                        .thenRun(() -> log.info("[uploadFile] Successfully processed PDF images for Certificate ID: {}", savedCertificate.getId()))
                        .exceptionally(ex -> {
                            log.error("[uploadFile] Failed to process PDF images for Certificate ID: {}", savedCertificate.getId(), ex);
                            return null;
                        });
            }

            log.info("[uploadFile] DONE uploading file - productId: {}, fileName: {}", productId, file.getOriginalFilename());
            return CompletableFuture.completedFuture(
                    UploadCertificateResponse.SuccessData.builder()
                            .id(String.valueOf(savedCertificate.getId()))
                            .name(savedCertificate.getName())
                            .link(savedCertificate.getFileLink())
                            .status(savedCertificate.getStatus())
                            .build()
            );

        } catch (Exception e) {
            log.error("[uploadFile] Error uploading file: {}", file.getOriginalFilename(), e);
            throw new VipoBusinessException(BaseExceptionConstant.FAILED_TO_EXECUTE,
                    "Failed to upload file: " + file.getOriginalFilename());
        }
    }
}
