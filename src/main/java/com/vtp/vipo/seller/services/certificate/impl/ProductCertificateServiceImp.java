package com.vtp.vipo.seller.services.certificate.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dao.entity.ProductEntity;
import com.vtp.vipo.seller.common.dao.repository.ProductCertificateEntityRepository;
import com.vtp.vipo.seller.common.dao.repository.ProductRepository;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.UploadCertificateResponse;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.mapper.ProductCertificateMapper;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.certificate.FileValidationService;
import com.vtp.vipo.seller.services.certificate.ProductCertificateService;
import com.vtp.vipo.seller.services.certificate.UploadFileService;
import com.vtp.vipo.seller.services.impl.base.BaseServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCertificateServiceImp extends BaseServiceImpl implements ProductCertificateService {
    ProductCertificateEntityRepository productCertificateEntityRepository;

    ProductRepository productRepository;

    ProductCertificateMapper productCertificateMapper;

    AmazonS3Service amazonS3Service;

    FileValidationService fileValidationService;

    UploadFileService uploadFileService;

    @Override
    public UploadCertificateResponse addCertificate(Long productId, List<MultipartFile> files) {
        log.info("[addCertificate] START adding certificate - productId: {}, fileSize: {}", productId, files.size());
        if (ObjectUtils.isEmpty(files)) {
            throw new VipoNotFoundException(Constants.NO_FILE_UPLOADED);
        }

        // Validate files
        FileValidationService.ValidationResult validationResult = fileValidationService.validateFiles(files);

        // Upload valid files asynchronously
        List<CompletableFuture<UploadCertificateResponse.SuccessData>> uploadFutures = validationResult.validFiles().stream()
                .map(file -> uploadFileService.uploadFile(productId, file))
                .toList();

        // Wait for all uploads to complete
        List<UploadCertificateResponse.SuccessData> successList = uploadFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        log.info("[addCertificate] DONE adding certificate - productId: {}", productId);
        return UploadCertificateResponse.builder()
                .successList(successList)
                .failList(validationResult.failList())
                .build();
    }

    @Override
    public void deleteCertificate(Long certificateId) {
        log.info("[deleteCertificate] Deleting certificate - certificateId: {}", certificateId);
        // Fetch the certificate based on certificateId
        ProductCertificateEntity certificate = productCertificateEntityRepository.findByIdAndDeletedFalse(certificateId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.CERTIFICATE_NOT_FOUND));

        // Delete the certificate
        productCertificateEntityRepository.delete(certificate);
        log.info("[deleteCertificate] Certificate deleted - certificateId: {}", certificateId);

        // Delete the certificate file from storage (aws s3)
        deleteCertificateFiles(certificate);
        log.info("[deleteCertificate] DONE");
    }

    /**
     * Deletes the certificate files from AWS S3 based on the provided certificate information.
     *
     * @param certificate The ProductCertificateEntity containing storage information.
     */
    @Async
    protected void deleteCertificateFiles(ProductCertificateEntity certificate) {
        List<StorageInfoDTO> storageInfoDTOS = JsonMapperUtils.convertJsonToObject(certificate.getStorageInfo(),
                new TypeReference<>() {
                });
        if (!ObjectUtils.isEmpty(storageInfoDTOS)) {
            log.info("[deleteCertificateFiles] Deleting certificate file from storage - certificateId: {}", certificate.getId());
            for (StorageInfoDTO storageInfoDTO : storageInfoDTOS) {
                amazonS3Service.deleteFile(storageInfoDTO);
            }
        }
        log.info("[deleteCertificateFiles] Certificate file deleted from storage - certificateId: {}", certificate.getId());
    }

    @Override
    public PagingRs getCertificatesByProduct(Long productId, int page, int pageSize) {
        log.info("[getCertificatesByProduct] Fetching certificates by product - productId: {}, page: {}, pageSize: {}", productId, page, pageSize);
        VipoUserDetails user = getCurrentUser();

        // Fetch the product based on orderId
        ProductEntity product = productRepository.findByIdAndMerchantIdAndIsDeleted(productId, user.getId(), Constants.PRODUCT_NOT_DELETED);
        if (ObjectUtils.isEmpty(product)) {
            throw new VipoNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        // Fetch certificates by product
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
        Page<ProductCertificateEntity> pageData = productCertificateEntityRepository.findByProductIdAndDeletedFalse(productId, pageable);
        return PagingRs.builder()
                .totalCount(pageData.getTotalElements())
                .data(productCertificateMapper.toResponseList(pageData.getContent()))
                .currentPage(page)
                .build();
    }

    @Override
    public UploadCertificateResponse addCertificateToProduct(Long productId, List<MultipartFile> files) {
        log.info("[addCertificateToProduct] Adding certificate to product - productId: {}", productId);
        VipoUserDetails user = getCurrentUser();

        // Fetch the product based on orderId
        ProductEntity product = productRepository.findByIdAndMerchantIdAndIsDeleted(productId, user.getId(), Constants.PRODUCT_NOT_DELETED);
        if (ObjectUtils.isEmpty(product)) {
            throw new VipoNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        // Check max certificate
        List<ProductCertificateEntity> certificates = productCertificateEntityRepository.findCertificatesByProduct(productId);
        int newCertificateCount = certificates.size() + files.size();
        log.info("[addCertificateToProduct] Certificate count - productId: {}, newCertificateCount: {}", productId, newCertificateCount);
        if (newCertificateCount > Constants.MAX_UPLOAD_CERTIFICATE_COUNT) {
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, Constants.MAX_CERTIFICATE_PER_PRODUCT_EXCEEDED);
        }

        return addCertificate(productId, files);
    }

    @Override
    public void removeCertificateFromProduct(Long productId, Long certificateId) {
        log.info("[removeCertificateFromProduct] Removing certificate from product - productId: {}, certificateId: {}", productId, certificateId);
        VipoUserDetails user = getCurrentUser();

        // Fetch the product based on orderId
        ProductEntity product = productRepository.findByIdAndMerchantIdAndIsDeleted(productId, user.getId(), Constants.PRODUCT_NOT_DELETED);
        if (ObjectUtils.isEmpty(product)) {
            throw new VipoNotFoundException(Constants.PRODUCT_NOT_FOUND);
        }

        // Fetch the certificate based on certificateId
        ProductCertificateEntity certificate = productCertificateEntityRepository.findByIdAndProductIdAndDeletedFalse(certificateId, productId)
                .orElseThrow(() -> new VipoNotFoundException(Constants.CERTIFICATE_NOT_FOUND));

        // Delete the certificate
        certificate.setDeleted(true);
        productCertificateEntityRepository.save(certificate);
        log.info("[removeCertificateFromProduct] Certificate removed from product - productId: {}, certificateId: {}", productId, certificateId);

        // Delete the certificate file from storage (aws s3)
        deleteCertificateFiles(certificate);
        log.info("[removeCertificateFromProduct] DONE");
    }
}
