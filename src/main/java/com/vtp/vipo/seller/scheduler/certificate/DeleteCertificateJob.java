package com.vtp.vipo.seller.scheduler.certificate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dao.repository.ProductCertificateEntityRepository;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.AmazonS3Service;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@DisallowConcurrentExecution
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteCertificateJob implements Job {
    ProductCertificateEntityRepository productCertificateEntityRepository;

    AmazonS3Service amazonS3Service;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("DELETE CERTIFICATE JOB STARTED");

        // Tim chung chi chua gan voi san pham da bi xoa va thoi gian tao chung chi lon hon 1 ngay
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<ProductCertificateEntity> certificateEntities = productCertificateEntityRepository
                .findOldDeletedCertificates(date);
        log.info("[DeleteCertificateJob] Found {} certificates to delete", certificateEntities.size());

        if (certificateEntities.isEmpty()) {
            log.info("[DeleteCertificateJob] No certificate to delete");
            return;
        }

        certificateEntities.forEach(certificate -> {
            List<StorageInfoDTO> storageInfoDTOS = JsonMapperUtils.convertJsonToObject(certificate.getStorageInfo(),
                    new TypeReference<>() {
                    });
            if (!ObjectUtils.isEmpty(storageInfoDTOS)) {
                log.info("[DeleteCertificateJob] Deleting certificate file from storage - certificateId: {}", certificate.getId());
                for (StorageInfoDTO storageInfoDTO : storageInfoDTOS) {
                    amazonS3Service.deleteFile(storageInfoDTO);
                }
            }
            log.info("[DeleteCertificateJob] Certificate file deleted from storage - certificateId: {}", certificate.getId());
        });
    }
}
