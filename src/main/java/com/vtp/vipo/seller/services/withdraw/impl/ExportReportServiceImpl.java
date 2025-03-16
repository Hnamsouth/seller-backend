package com.vtp.vipo.seller.services.withdraw.impl;

import com.vtp.vipo.seller.common.constants.RevenueConstant;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import com.vtp.vipo.seller.common.dao.repository.WithdrawalRequestExportEntityRepository;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestDetailResponse;
import com.vtp.vipo.seller.common.utils.FileUtils;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.withdraw.ExcelService;
import com.vtp.vipo.seller.services.withdraw.ExportReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportReportServiceImpl implements ExportReportService {
    private final AmazonS3Service amazonS3Service;

    private final WithdrawalRequestExportEntityRepository withdrawalRequestExportEntityRepository;

    private final ExcelService excelService;

    @Async
    @Override
    public void processExportReport(WithdrawalRequestDetailResponse detail, WithdrawalRequestExportEntity savedExportEntity) {
        log.info("[processExportReport] Start processing export report for withdrawal request detail: {}", detail.getId());
        try {
            // Create excel file
            ByteArrayOutputStream bos = excelService.createExcelFile(detail);

            // Upload to S3
            InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
            StorageInfoDTO storageInfoDTO = amazonS3Service.uploadFile(
                    inputStream,
                    bos.size(),
                    RevenueConstant.EXPORT_WITHDRAW_DETAIL_CONTENT_TYPE,
                    RevenueConstant.EXPORT_WITHDRAW_DETAIL_FILE_NAME
            );

            // If upload success, update the export entity
            savedExportEntity.setStatus(WithdrawalRequestExportEnum.COMPLETED);
            savedExportEntity.setFilePath(storageInfoDTO.getLink());
            savedExportEntity.setStorageInfo(JsonMapperUtils.writeValueAsString(storageInfoDTO));
        } catch (Exception ex) {
            // If exception, update the export entity
            savedExportEntity.setStatus(WithdrawalRequestExportEnum.FAILED);
            savedExportEntity.setErrorMessage(ex.getMessage());
        } finally {
            // Update finish time
            savedExportEntity.setFinishTime(LocalDateTime.now());
            withdrawalRequestExportEntityRepository.save(savedExportEntity);
        }
        log.info("[processExportReport] Finish processing export report for withdrawal request detail: {}", detail.getId());
    }

//    @Async
//    @Override
//    public void processExportReport(WithdrawalRequestDetailResponse detail, WithdrawalRequestExportEntity savedExportEntity) {
//        log.info("[processExportReport LOCAL] Start processing export report for withdrawal request detail: {}", detail.getId());
//        try {
//            // Create excel file
//            ByteArrayOutputStream bos = excelService.createExcelFile(detail);
//
//            // Update file to local
//            File localFile = FileUtils.uploadFile(bos.toByteArray(), savedExportEntity.getReportName());
//            String localPath = localFile.getAbsolutePath();
//
//            // If success, update export entity
//            savedExportEntity.setStatus(WithdrawalRequestExportEnum.COMPLETED);
//            savedExportEntity.setFilePath(localPath);
//
//            // Update storage info
//            StorageInfoDTO storageInfoDTO = StorageInfoDTO.builder()
//                    .link(localPath)   // link cục bộ
//                    .bucketName(null)  // cục bộ -> không có bucket
//                    .key(savedExportEntity.getReportName())
//                    .build();
//            savedExportEntity.setStorageInfo(JsonMapperUtils.writeValueAsString(storageInfoDTO));
//
//        } catch (Exception ex) {
//            // If exception, update export entity
//            savedExportEntity.setStatus(WithdrawalRequestExportEnum.FAILED);
//            savedExportEntity.setErrorMessage("Lỗi khi export: " + ex.getMessage());
//            log.error("Export or upload file error", ex);
//        } finally {
//            // Update finish time
//            savedExportEntity.setFinishTime(LocalDateTime.now());
//            withdrawalRequestExportEntityRepository.save(savedExportEntity);
//        }
//
//        // Return completed future
//        log.info("[processExportReport LOCAL] Finish processing export report for withdrawal request detail: {}", detail.getId());
//    }
}
