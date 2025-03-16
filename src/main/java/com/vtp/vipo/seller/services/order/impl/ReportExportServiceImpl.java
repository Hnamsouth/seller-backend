package com.vtp.vipo.seller.services.order.impl;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.business.calculator.ExcelColumn;
import com.vtp.vipo.seller.business.calculator.OrderPackageExportRowDTO;
import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.packageproduct.PriceRange;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import com.vtp.vipo.seller.common.dao.entity.projection.PlatformFeeProjection;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportHistoryResponse;
import com.vtp.vipo.seller.common.enumseller.OrderFilterTab;
import com.vtp.vipo.seller.common.enumseller.StorageType;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.business.event.kafka.base.OrderPackageReportExportMsg;
import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.OrderConstant;
import com.vtp.vipo.seller.common.dao.entity.ReportExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.reportexport.S3StorageLocation;
import com.vtp.vipo.seller.common.dao.entity.projection.OrderPackageProjection;
import com.vtp.vipo.seller.common.dao.entity.projections.MyData;
import com.vtp.vipo.seller.common.dto.request.order.report.OrderPackageReportExportRequest;
import com.vtp.vipo.seller.common.dto.response.ReportHistoryProjection;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.order.report.OrderPackageReportExportResponse;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDetailResponse;
import com.vtp.vipo.seller.common.dto.response.reportexport.ReportExportDownloadResponse;
import com.vtp.vipo.seller.common.exception.*;
import com.vtp.vipo.seller.common.utils.*;
import com.vtp.vipo.seller.config.mq.kafka.KafkaTopicConfig;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.mapper.OrderMapper;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.order.ReportExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vtp.vipo.seller.common.dao.entity.enums.reportexport.FileStorageMethodEnum;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportExportServiceImpl extends BaseService<ReportExportEntity, Long, ReportExportRepository> implements ReportExportService {

    final AmazonS3Service amazonS3Service;

    final PackageProductRepository packageProductRepository;

    final SellerOrderStatusRepository sellerOrderStatusRepository;

    final OrderPackageRepository orderPackageRepository;

    final SellerMapper sellerMapper;

    @SuppressWarnings("rawtypes")
    final KafkaTemplate kafkaTemplate;

    final KafkaTopicConfig kafkaTopicConfig;

    final OrderMapper orderMapper;
    private final PlatformFeeDetailRepository platformFeeDetailRepository;

    private final WithdrawalRequestExportEntityRepository withdrawalRequestExportEntityRepository;

    @Value("${custom.properties.s3.vipo-bucket.name}")
    String orderPackageExportBucketName;

    @Value("${custom.properties.s3.vipo-bucket.order-package-export.key-prefix}")
    String orderPackageExportKeyPrefix;

    @Value("${custom.properties.report-export.cleanup.duration:P6M}")
    Duration cleanUpDuration;

    @Value("${custom.properties.excel-workbook.window-size:10}")
    Integer workbookWindowSize;

    @Value("${custom.properties.report-export.sheet-name:Data}")
    String reportExportSheetName;

    private final static long ORDER_PACKAGE_MAX_ROW_NUM_TO_EXPORT = 10000;

    private final static String ORDER_PACKAGE_REPORT_TYPE = "ORDER_PACKAGE";

    private final static int RECORD_BATCH_FETCHING_SIZE = 1000; //determine the number of rows we fetch from the db

    private final ApplicationContext applicationContext;

    private ReportExportServiceImpl getProxy() {
        return applicationContext.getBean(ReportExportServiceImpl.class);
    }

    @Override
    public PagingRs getReportHistory(String reportName, Long fromDate, Long toDate, int pageNum, int pageSize) {
        VipoUserDetails user = getCurrentUser();
        PagingRs res = new PagingRs();
        Pageable pageRequest = PageRequest.of(pageNum, pageSize);
        Page<ReportHistoryProjection> reportHistories = repo.findAllByReportNameAndExportTime(reportName, fromDate, toDate, user.getId(), pageRequest);
        res.setData(reportHistories.getContent());
        res.setTotalCount(reportHistories.getTotalElements());
        res.setCurrentPage(pageNum);
        return res;
    }

    @Override
    public PagingRs getReportHistoryV2(String reportName, Long fromDate, Long toDate, int pageNum, int pageSize) {
        VipoUserDetails user = getCurrentUser();
        PagingRs res = new PagingRs();
        Pageable pageRequest = PageRequest.of(pageNum, pageSize);
        LocalDateTime fromLocalDateTime
                = ObjectUtils.isNotEmpty(fromDate) ? DateUtils.convertEpochSecondsToLocalDateTime(fromDate) : null;
        LocalDateTime toLocalDateTime
                = ObjectUtils.isNotEmpty(toDate) ? DateUtils.convertEpochSecondsToLocalDateTime(toDate) : null;

        Page<ReportExportEntity> reportHistories
                = repo.findAllByReportExportByNameAndTimeRange(
                reportName, fromLocalDateTime, toLocalDateTime, user.getId(),
                ReportExportStatus.NOT_SHOW_IN_HISTORY_STATUS, pageRequest
        );

        List<ReportExportHistoryResponse> responses
                = reportHistories.getContent().stream()
                .map(sellerMapper::toReportExportHistoryResponse)
                        .toList();

        res.setData(responses);
        res.setTotalCount(reportHistories.getTotalElements());
        res.setCurrentPage(pageNum);
        return res;
    }

    @Override
    public ReportHistoryProjection getReportExportById(Long reportId) {
        return repo.findReportExportById(reportId, getCurrentUser().getId()).orElseThrow(
                VipoNotFoundException::new
        );
    }

    /**
     * Processes the report export request for order packages.
     * <p>
     * This method is responsible for handling the report export request. It performs the following:
     * 1. Validates the input parameters to ensure that they conform to the necessary rules.
     * 2. Handles the business logic related to generating the report (currently not implemented).
     * 3. Returns an OrderPackageReportExportResponse that will provide information about the report export process.
     * </p>
     *
     * @param orderPackageReportExportRequest The request object containing the parameters to filter the order package report.
     * @return The response containing details about the report export process (e.g., success status, generated report).
     */
    @Transactional
    @Override
    public OrderPackageReportExportResponse requestReportExport(
            @NotNull @Valid OrderPackageReportExportRequest orderPackageReportExportRequest
    ) {
        // Validate the export request
        validateOrderPackageReportExportRequest(orderPackageReportExportRequest);

        Long merchantId = getCurrentUser().getId();

        String exportFileName = createExcelExportFileNameForOrderPackage(orderPackageReportExportRequest);
        orderPackageReportExportRequest.setExcelFileName(exportFileName);

        String exportReportRequest = JsonMapperUtils.writeValueAsString(orderPackageReportExportRequest);

        var reportExportEntity = ReportExportEntity.builder()
                .reportSubType(ObjectUtils.isEmpty(orderPackageReportExportRequest.getTabCode()) ? OrderConstant.SELLER_TAB_ALL : orderPackageReportExportRequest.getTabCode().getLable())
                        .exportReportRequest(exportReportRequest)
                        .reportType(ORDER_PACKAGE_REPORT_TYPE)
                        .reportFileName(exportFileName)
                        .merchantId(getCurrentUser().getId())
                        .status(ReportExportStatus.pending)
                        .build();

        if(ObjectUtils.isEmpty(repo.save(reportExportEntity)))
            throw new VipoFailedToExecuteException("Failed to export");

        orderPackageReportExportRequest.setReportExportId(reportExportEntity.getId());

        OrderPackageReportExportMsg orderPackageReportExportMsg
                = orderMapper.toOrderPackageReportExportMsg(orderPackageReportExportRequest);
        orderPackageReportExportMsg.setMerchantId(merchantId);

        //todo:set MessageId for the message push to kafka.
        kafkaTemplate.send(
                kafkaTopicConfig.getOrderPackageExportTopicName()
                , JsonUtils.toJson(new MessageData(orderPackageReportExportMsg))
        );

        return OrderPackageReportExportResponse.builder().reportExportId(String.valueOf(reportExportEntity.getId())).build();
    }

    /**
     * Validates the request parameters for exporting the order package report.
     * <p>
     * The validation checks the following conditions:
     * - The "tab" field must be included in the predefined list of valid statuses (SellerOrderStatus.PARENT_SELLER_ORDER_STATUS_STR_LIST).
     * - If "selectedOrderIds" is provided:
     *   - The number of selected order IDs cannot exceed 200.
     *   - The number of rows to export must not exceed 10,000.
     *   - All selected order IDs must exist and share the same parent status corresponding to the "tab" value.
     * - If "selectedOrderIds" is not provided, the date range should be valid (startDate < today <= endDate).
     * </p>
     *
     * @param orderPackageReportExportRequest The request object to validate.
     */
    private void validateOrderPackageReportExportRequest(
            OrderPackageReportExportRequest orderPackageReportExportRequest
    ) {

        Long merchantId = getCurrentUser().getId();

        /* Phase 5: Exporting cancel/refund/reject status is not supported */
        if (
                ObjectUtils.isNotEmpty(orderPackageReportExportRequest.getTabCode())
                        && ObjectUtils.isEmpty(orderPackageReportExportRequest.getSelectedOrderIds())
                        && orderPackageReportExportRequest.getTabCode().equals(OrderFilterTab.RETURN_REFUND_CANCEL)
        )
            throw new VipoInvalidDataRequestException("Not supported");

        /* convert tabCode to tab
        * because two apis was developing in the same time, there is an conflict between naming convention */
        if (ObjectUtils.isEmpty(orderPackageReportExportRequest.getReportExportId())) {
            if (ObjectUtils.isEmpty(orderPackageReportExportRequest.getTabCode()))
                orderPackageReportExportRequest.setTab(null);
            else {
                orderPackageReportExportRequest.setTab(
                        Constants.FILTER_TAB_TO_SELLER_STATUS.get(orderPackageReportExportRequest.getTabCode()).name()
                );
            }
        }

        /* check if  the "tab" field is included in SellerOrderStatus.PARENT_SELLER_ORDER_STATUS_STR_LIST */
        String tab = orderPackageReportExportRequest.getTab();
        boolean notFindAllSellerOrderStatus = StringUtils.isNotBlank(tab); //check if the requested seller order status is ALL
        if (
                notFindAllSellerOrderStatus
                        && !SellerOrderStatus.PARENT_SELLER_ORDER_STATUS_STR_LIST.contains(tab)
        )
            throw new VipoInvalidDataRequestException("Tab xuất báo cáo không hợp lệ");


        //if the "selectedOrderIds" is not blank
        List<Long> requestedOrderPackageIds = orderPackageReportExportRequest.getSelectedOrderIds();
        if (ObjectUtils.isNotEmpty(requestedOrderPackageIds)) {
            /* check if the num of exported rows does not excceed 10000 */
            Long numOfExportedRows
                    = packageProductRepository.countByMerchantIdAndPackageIdIn(merchantId, requestedOrderPackageIds);
            if (ObjectUtils.isEmpty(numOfExportedRows) || numOfExportedRows == 0)
                throw new VipoInvalidDataRequestException("Không có dữ liệu");
            if (numOfExportedRows > ORDER_PACKAGE_MAX_ROW_NUM_TO_EXPORT)
                throw new VipoInvalidDataRequestException("Không xuất báo cáo được do số lượng SKU quá lớn");

            /* check if all these ids exists */
            List<OrderPackageProjection> existedOrderPackageProjections
                    = orderPackageRepository.getIdByMerchantIdAndIdIn(merchantId, requestedOrderPackageIds);

            List<Long> existedOrderPackageIds
                    = existedOrderPackageProjections.stream().map(OrderPackageProjection::getId).toList();
            requestedOrderPackageIds.forEach(id -> {
                if (!existedOrderPackageIds.contains(id))
                    throw new VipoInvalidDataRequestException("Không tìm thấy đơn hàng với id:" + id);
            });

            /* check if they all have the same parent sellerOrderStatus, and equivalent to the "tab" field */
            if (notFindAllSellerOrderStatus) {
                Set<String> parentSellerOrderStatuses
                        = existedOrderPackageProjections
                        .stream()
                        .map(orderPackageProjection -> {
                            String sellerOrderStatusStr = orderPackageProjection.getSellerOrderStatus();
                            /* check if all order package has statuses */
                            if (StringUtils.isBlank(sellerOrderStatusStr))
                                throw new VipoNotFoundException(
                                        "Not found seller order status for the order package with id "
                                                + orderPackageProjection.getId()
                                );

                            SellerOrderStatus sellerOrderStatus = SellerOrderStatus.getByName(sellerOrderStatusStr);
                            if (ObjectUtils.isEmpty(sellerOrderStatus))
                                throw new VipoNotFoundException(
                                        "Not found the seller status " + sellerOrderStatusStr
                                                + " for the oder package id " + orderPackageProjection.getId()
                                );

                            return ObjectUtils.isEmpty(sellerOrderStatus.getParentSellerOrderSattus()) ?
                                    sellerOrderStatus.name()
                                    : sellerOrderStatus.getParentSellerOrderSattus().name();
                        })
                        .collect(Collectors.toSet());

                if (parentSellerOrderStatuses.size() > 1)
                    throw new VipoInvalidDataRequestException(
                            "The provided order package do not have same parent seller order status"
                    );

                if (!parentSellerOrderStatuses.iterator().next().equals(tab))
                    throw new VipoInvalidDataRequestException(
                            "The parent seller order status of provided order packages does not match with the selected tab"
                    );
            }

        } else {
            /* validate the date */
            Long startDateInEpochSecond = orderPackageReportExportRequest.getStartDate();
            Long endDateInEpochSecond = orderPackageReportExportRequest.getEndDate();
            if (ObjectUtils.isNotEmpty(startDateInEpochSecond) && ObjectUtils.isNotEmpty(endDateInEpochSecond)) {
                LocalDateTime startLocalDateTime = DateUtils.getLocalDateTimeFromEpochSecond(startDateInEpochSecond);
                LocalDateTime endLocalDateTime = DateUtils.getLocalDateTimeFromEpochSecond(endDateInEpochSecond);
                if (startLocalDateTime.isAfter(endLocalDateTime))
                    throw new VipoInvalidDataRequestException("Ngày bắt đầu phải trước ngày kết thúc");
                if (endLocalDateTime.minusMonths(1).isAfter(startLocalDateTime))
                    throw new VipoInvalidDataRequestException("Chỉ hỗ trợ trong khoảng 1 tháng");

            }

            /* validate the number of exported rows */
            List<SellerOrderStatus> requestedSellerOrderStatusesStrList = null;
            if (notFindAllSellerOrderStatus)
                requestedSellerOrderStatusesStrList
                        = SellerOrderStatus.PARENT_TO_CHILDREN_SELLER_ORDER_STATUS.get(SellerOrderStatus.getByName(tab));

            /* lower case all the string filter */
            String orderCode = StringProcessingUtils.stripAndLowerCaseTheString(orderPackageReportExportRequest.getOrderCode());
            String buyerName = StringProcessingUtils.stripAndLowerCaseTheString(orderPackageReportExportRequest.getBuyerName());
            String productName = StringProcessingUtils.stripAndLowerCaseTheString(orderPackageReportExportRequest.getProductName());
            String shipmentCode = StringProcessingUtils.stripAndLowerCaseTheString(orderPackageReportExportRequest.getShipmentCode());

            /* set back to the request which is the kafka message later */
            orderPackageReportExportRequest.setOrderCode(orderCode);
            orderPackageReportExportRequest.setBuyerName(buyerName);
            orderPackageReportExportRequest.setProductName(productName);
            orderPackageReportExportRequest.setShipmentCode(shipmentCode);

            Long numOfExportedRows
                    = packageProductRepository.countByMerchantIdAndSellerOrderStatusIn(
                    merchantId,
                    ObjectUtils.isNotEmpty(requestedSellerOrderStatusesStrList) ?
                            requestedSellerOrderStatusesStrList.stream().map(SellerOrderStatus::name).toList()
                            : null,
                    startDateInEpochSecond,
                    endDateInEpochSecond,
                    orderCode,
                    buyerName,
                    productName,
                    shipmentCode
            );

            // allow to export excel even thought there is no row
//            if (ObjectUtils.isEmpty(numOfExportedRows) || numOfExportedRows == 0)
//                throw new VipoInvalidDataRequestException("Không có dữ liệu");
            if (numOfExportedRows > ORDER_PACKAGE_MAX_ROW_NUM_TO_EXPORT)
                throw new VipoInvalidDataRequestException("Không xuất báo cáo được do số lượng SKU quá lớn");


        }

    }

    private String createExcelExportFileNameForOrderPackage(
            OrderPackageReportExportRequest orderPackageReportExportRequest
    ) {
        var fileNameBuilder = new StringBuilder();
        if (DataUtils.isNullOrEmpty(orderPackageReportExportRequest.getTabCode()))
            fileNameBuilder.append("Tất cả");
        else{
//            SellerOrderStatus sellerOrderStatus = SellerOrderStatus.getByName(orderPackageReportExportRequest.getTab());
//            if (ObjectUtils.isNotEmpty(sellerOrderStatus))
            fileNameBuilder.append(orderPackageReportExportRequest.getTabCode().getLable());
        }


        if (
                ObjectUtils.isNotEmpty(orderPackageReportExportRequest.getStartDate())
                || ObjectUtils.isNotEmpty(orderPackageReportExportRequest.getEndDate())
        ) {
            if (!fileNameBuilder.isEmpty())
                fileNameBuilder.append('.');
            fileNameBuilder
                    .append(
                            DateUtils.convertEpochToDateString(
                                    orderPackageReportExportRequest.getStartDate(), DateUtils.yyyyMMdd
                            )
                    )
                    .append('_')
                    .append(
                            DateUtils.convertEpochToDateString(
                                    orderPackageReportExportRequest.getEndDate(), DateUtils.yyyyMMdd
                            )
                    );
        }

        return !fileNameBuilder.isEmpty()? fileNameBuilder.toString() : "report-" + getCurrentUser().getId();

    }


    @Override
    public void exportOrderPackageReport(OrderPackageReportExportMsg exportRequest) throws IOException {

        if (ObjectUtils.isEmpty(exportRequest))
            throw new VipoInvalidDataRequestException("null message content or null report_export.id");

        ReportExportEntity reportExportEntity = null;
        Long reportExportId = exportRequest.getReportExportId();

        /* Phase 6: Withdrawal request: use the same method for both exportation */
        WithdrawalRequestExportEntity withdrawalRequestExportEntity = null;
        Long withdrawalRequestExportId = exportRequest.getWithdrawalRequestReportId();

        if (ObjectUtils.isNotEmpty(reportExportId)) {
            reportExportEntity
                    = repo.findById(reportExportId)
                    .orElseThrow(() -> new VipoInvalidDataRequestException("not found report_export.id = " + reportExportId));
            /* check if the report export request had been handled */
            if (reportExportEntity.getStatus() != ReportExportStatus.pending)
                return;
        } else if (ObjectUtils.isNotEmpty(withdrawalRequestExportId)){
            withdrawalRequestExportEntity
                    = withdrawalRequestExportEntityRepository.findById(withdrawalRequestExportId)
                    .orElseThrow(
                            () -> new VipoInvalidDataRequestException(
                                    "not found withdrawal_request_export.id = " + withdrawalRequestExportId
                            )
                    );
            /* check if the report export request had been handled */
            if (withdrawalRequestExportEntity.getStatus() != WithdrawalRequestExportEnum.PENDING)
                return;
        } else {
            throw new VipoInvalidDataRequestException("Not provided id of report_export or withdrawal_request_export");
        }


        Path excelFilePath = null;
        try {
            excelFilePath = getProxy().generateExcelToTempFile(exportRequest);

            String s3Key = ObjectUtils.isNotEmpty(reportExportId) ?
                    orderPackageExportKeyPrefix  + reportExportId + ".xlsx"
                    :orderPackageExportKeyPrefix  + withdrawalRequestExportId + ".xlsx";

            if (ObjectUtils.isNotEmpty(reportExportId)) {
                reportExportEntity.setStorageType(FileStorageMethodEnum.S3.name());
                reportExportEntity.setStorageInfo(
                        JsonMapperUtils.writeValueAsString(
                                S3StorageLocation.builder().bucket(orderPackageExportBucketName).key(s3Key).build()
                        )
                );
            } else {
                withdrawalRequestExportEntity.setStorageType(StorageType.S3);
            }

            //todo: check the key has been existed
            String excelFileS3URL
                    = amazonS3Service.uploadFile(excelFilePath, orderPackageExportBucketName, s3Key);

            if (StringUtils.isBlank(excelFileS3URL))
                throw new VipoFileException("Failed to get url of the s3 file");
            //todo: use the temp file path in case of failed to upload
            if (ObjectUtils.isNotEmpty(reportExportEntity)) {
                reportExportEntity.setFilePath(excelFileS3URL);
                reportExportEntity.setStatus(ReportExportStatus.completed);
                reportExportEntity.setFinishTime(DateUtils.getCurrentLocalDateTime());
                if (ObjectUtils.isEmpty(repo.save(reportExportEntity)))
                    log.info("failed to save reportExportEntity {} ", reportExportEntity.getId());
                //todo: throw exceptions here
            } else {
                withdrawalRequestExportEntity.setStatus(WithdrawalRequestExportEnum.COMPLETED);
                withdrawalRequestExportEntity.setFinishTime(DateUtils.getCurrentLocalDateTime());
                withdrawalRequestExportEntity.setFilePath(excelFileS3URL);
                withdrawalRequestExportEntity.setStorageInfo(
                        JsonMapperUtils.writeValueAsString(
                                StorageInfoDTO.builder()
                                        .bucketName(orderPackageExportBucketName)
                                        .link(excelFileS3URL)
                                        .key(s3Key)
                                        .build()
                        )
                );
                if (ObjectUtils.isEmpty(withdrawalRequestExportEntityRepository.save(withdrawalRequestExportEntity)))
                    log.info("failed to save reportExportEntity {} ", withdrawalRequestExportEntity.getId());
            }
        } finally {
            // Clean up temporary file
            if (excelFilePath != null) {
                try {
                    Files.deleteIfExists(excelFilePath);
                } catch (IOException e) {
                    // Log the error; in production, use a logging framework
                    log.error("Failed to delete temporary file: {} with error: {}", excelFilePath, e.getLocalizedMessage());
                }
            }
        }

    }

    /**
     * Generates an Excel file with styled headers and writes data in batches.
     *
     * @param exportRequest The export request containing necessary parameters.
     * @return Path to the generated temporary Excel file.
     * @throws IOException If an I/O error occurs during file operations.
     */
    @Transactional(readOnly = true)
    public Path generateExcelToTempFile(OrderPackageReportExportMsg exportRequest) throws IOException {
//        // Define the path for the Excel file
        Path tempFile = Files.createTempFile("export_" + exportRequest.getReportExportId(), ".xlsx");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Initialize SXSSFWorkbook with a window of 100 rows in memory
//        try (InputStream templateInputStream = resource.getInputStream();
//             XSSFWorkbook xssfWorkbook = new XSSFWorkbook(templateInputStream);
//             SXSSFWorkbook workbook = new SXSSFWorkbook(xssfWorkbook, 100); // 100 rows in memory
//             FileOutputStream fileOut = new FileOutputStream(tempFile.toFile())) {
        try (
                SXSSFWorkbook workbook = new SXSSFWorkbook(workbookWindowSize);
                FileOutputStream fileOut = new FileOutputStream(tempFile.toFile())
        ) {

//            Sheet sheet = workbook.getSheetAt(0); //todo: replace with an env var
            Sheet sheet = workbook.createSheet(reportExportSheetName);

            /* Add column for platform fees (this field is dynamic) */
            //get all the platform fee id to name
            Map<Long, String> platformFeeIdToName = getAllPlatformFeeIdToName(exportRequest);
            //get a list of the platform fee that is ordered by platformId
            List<Long> platformFeeIds = platformFeeIdToName.keySet().stream().sorted().toList();

            // Create Header Row with Styles
//            clearSheetData(sheet);
            createHeaderRow(workbook, sheet, platformFeeIds, platformFeeIdToName);

            // Write Data in Batches
//            writeDataInBatches(workbook, sheet, exportRequest);
            writeDataUsingStream(workbook, sheet, exportRequest, platformFeeIds);

//
//            // Auto-size columns for better readability (optional and can be performance-intensive for large sheets)
//            for (ExcelColumn column : ExcelColumn.values()) {
//                sheet.autoSizeColumn(column.getIndex());
//            }

            // Write the workbook to the temporary file
            workbook.write(fileOut);

            // Dispose of temporary files backing this workbook on disk
            workbook.dispose();
        }

        return tempFile;
    }

    private Map<Long, String> getAllPlatformFeeIdToName(OrderPackageReportExportMsg exportRequest) {
        List<PlatformFeeProjection> platformFeeProjections = null;
        if (ObjectUtils.isNotEmpty(exportRequest.getReportExportId())) {
            if (ObjectUtils.isNotEmpty(exportRequest.getSelectedOrderIds())) {
                platformFeeProjections = platformFeeDetailRepository.getDistinctPlatformFeeByMerchantIdAndPackageIdIn(
                        exportRequest.getMerchantId(), exportRequest.getSelectedOrderIds()
                );
            } else {
                Long startDateInEpochSecond = exportRequest.getStartDate();
                Long endDateInEpochSecond = exportRequest.getEndDate();
                Integer requestedTabCode = null;

                if (!DataUtils.isNullOrEmpty(exportRequest.getTabCode())) {
                    requestedTabCode = exportRequest.getTabCode().getValue();
                }

                /* lower case all the string filter */
                String orderCode = exportRequest.getOrderCode();
                String buyerName = exportRequest.getBuyerName();
                String productName = exportRequest.getProductName();
                String shipmentCode = exportRequest.getShipmentCode();

                Long merchantId = exportRequest.getMerchantId();

                platformFeeProjections = platformFeeDetailRepository.getDistinctPlatformFeeByReportFilter(
                        merchantId,
                        requestedTabCode,
                        startDateInEpochSecond,
                        endDateInEpochSecond,
                        orderCode,
                        buyerName,
                        productName,
                        shipmentCode
                );
            }
        } else {    //withdrawalRequestReportId
            platformFeeProjections = platformFeeDetailRepository.getDistinctPlatformFeeByWithdrawalRequestExportId(
                    exportRequest.getWithdrawalRequestReportId()
            );
        }
        if (ObjectUtils.isEmpty(platformFeeProjections)) return Map.of();
        Map<Long, String> response = new HashMap<>();
        for (PlatformFeeProjection platformFee: platformFeeProjections) {
            if  (ObjectUtils.isNotEmpty(platformFee.getPlatformFeeId()) && ObjectUtils.isNotEmpty(platformFee.getFeeName()))
                response.put(platformFee.getPlatformFeeId(), platformFee.getFeeName());
        }

        return response;
    }
    //todo: remove
    /**
     * Creates a header row with specified styles.
     *
     * @param workbook The workbook where styles are defined.
     * @param sheet    The sheet where the header row is created.
     */
    private void createHeaderRow(
            SXSSFWorkbook workbook, Sheet sheet, List<Long> platformFeeIds, Map<Long, String> platformFeeIdToName
    ) {
        // Create a style for the header
        CellStyle headerStyle = workbook.createCellStyle();

        // Set the font to bold
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFont(headerFont);

        // Set the alignment to center
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Set the background color
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Set borders
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        //wrap text
        headerStyle.setWrapText(true);

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(30);

        for (ExcelColumn column : ExcelColumn.values()) {
            if (column != ExcelColumn.PHI_SAN_DYNAMIC) {
                int cellIndex = column.getIndex() < ExcelColumn.PHI_SAN_DYNAMIC.getIndex()?
                        column.getIndex(): column.getIndex() - 1 + platformFeeIds.size();
                Cell cell = headerRow.createCell(cellIndex);
                cell.setCellStyle(headerStyle);

                if (column == ExcelColumn.DOANH_THU_DUKIEN_TREN_DON_14){
                    StringBuilder headerName = new StringBuilder(column.getHeaderName());
                    int dynamicFeeTag = Constants.DYNAMIC_START_FEE_TAG;
                    for (Long ignored : platformFeeIds) {
                        headerName.append(" -(").append(dynamicFeeTag++).append(")");
                    }
                    cell.setCellValue(
                            headerName.toString()
                    );
                } else {
                    cell.setCellValue(column.getHeaderName());
                }

                sheet.setColumnWidth(cellIndex, column.getColumnWidth());
            }
            else {
                int dynamicFeeTag = Constants.DYNAMIC_START_FEE_TAG;
                for (Long platformFeeId : platformFeeIds) {
                    int cellIndex = column.getIndex() + platformFeeIds.indexOf(platformFeeId);
                    String feeName = platformFeeIdToName.get(platformFeeId) + " (" + dynamicFeeTag++ +")";
                    Cell cell = headerRow.createCell(cellIndex);
                    cell.setCellStyle(headerStyle);
                    cell.setCellValue(feeName);
                    sheet.setColumnWidth(cellIndex, column.getColumnWidth());
                }
            }
        }
    }

    /**
     * Clears existing data in the sheet below the header row.
     *
     * @param sheet The sheet to clear.
     */
    private void clearSheetData(Sheet sheet) {
        // Assuming the first row is the header
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i > 0; i--) { // Start from the bottom to avoid shifting issues
            Row row = sheet.getRow(i);
            if (row != null) {
                sheet.removeRow(row);
            }
        }
    }

    private void writeDataInBatches(SXSSFWorkbook workbook, Sheet sheet, OrderPackageReportExportMsg exportRequest) throws IOException {
        /* handle by batch */
        //determine the number of records
        int pageNum = 0;
        int rowNum  = 1;
        Long totalRowNum = getExportedRowNum(exportRequest);
        int totalPage = 0; //todo: handle when total page num =0
        if (ObjectUtils.isEmpty(totalRowNum) || totalRowNum <= 0)
            return;

        totalPage = PagingUtils.calculateTotalPage(RECORD_BATCH_FETCHING_SIZE, totalRowNum);

        CellStyle dataCellStyle = createDataCellStyle(workbook);


        while (pageNum < totalPage) {
            List<OrderPackageExportRowDTO> dataRows = getDataRowsInBatch(exportRequest, pageNum, RECORD_BATCH_FETCHING_SIZE);

            for (OrderPackageExportRowDTO data : dataRows) {
                Row row = sheet.createRow(rowNum++);
                //todo:handle the case multiple package_products in an order_package
                //check if the order_package.id is duplicate, only update some column

                // Iterate through columns in order and populate cells
                for (ExcelColumn column : ExcelColumn.values()) {
                    Object cellValue = column.getValueExtractor().apply(data);
                    createStyledCell(row, column.getIndex(), cellValue, dataCellStyle);
                }
            }
            pageNum++;
        }
    }

    private List<OrderPackageExportRowDTO> getDataRowsInBatch(OrderPackageReportExportMsg exportRequest, int pageNum, int recordBatchFetchingSize) {
        List<MyData> batch = fetchBatch(exportRequest, pageNum, RECORD_BATCH_FETCHING_SIZE);
        if (ObjectUtils.isEmpty(batch))
            return List.of();

        /* OrderPackageExportRowDTO can be duplicated a lot of information, so we determine the first package product
        * row to contains all information of order_package */
        Map<Long, Long> orderPackageIdToFirstSkuId
                = batch.stream().collect(Collectors.toMap(
                MyData::getPackageId,
                MyData::getId,
                (existing, replacement) -> {
                    if (ObjectUtils.isEmpty(existing))
                        return replacement;
                    if (ObjectUtils.isEmpty(replacement))
                        return existing;
                    return existing <= replacement? existing: replacement;
                }
        ));
        List<OrderPackageExportRowDTO> dataRows
                = batch.stream().map(
                data -> {
                    Long packageProductId = data.getId();
                    Long packageId = data.getPackageId();
                    if (
                            ObjectUtils.isEmpty(packageProductId)
                                    && ObjectUtils.isEmpty(packageId)
                    )
                        throw new VipoInvalidDataRequestException();
                    Long firstPackageId = orderPackageIdToFirstSkuId.get(packageId);
                    if (
                            ObjectUtils.isEmpty(firstPackageId)
                    )
                        throw new VipoInvalidDataRequestException();
                    if (firstPackageId.equals(packageProductId)) {
                        OrderPackageExportRowDTO orderPackageExportRowDTO
                                = orderMapper.toOrderPackageExportRowDTOPrimary(data);
                        SellerOrderStatus sellerOrderStatus
                                = SellerOrderStatus.getByName(orderPackageExportRowDTO.getSellerOrderStatusName());
                        orderPackageExportRowDTO.setSellerOrderStatusDescription(
                                ObjectUtils.isNotEmpty(sellerOrderStatus) ? sellerOrderStatus.getDescription() : null
                        );
                        orderPackageExportRowDTO.setFirstSkuRow(true);

                        //Tổng giá bán (3)
                        BigDecimal price
                                = ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getPrice()) ?
                                orderPackageExportRowDTO.getPrice() : BigDecimal.ZERO;
                        BigDecimal negotiatedAmount
                                = ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getNegotiatedAmount()) ?
                                orderPackageExportRowDTO.getNegotiatedAmount() : BigDecimal.ZERO;

//                        orderPackageExportRowDTO.setPriceMinusNegotiatedAmount(price.subtract(negotiatedAmount));
                        return orderPackageExportRowDTO;
                    }
                    else {
                        return orderMapper.toOrderPackageExportRowDTOOther(data);
                    }
                }
        ).toList();


        //calculate the total field accross sku
        Map<Long, OrderPackageExportRowDTO> packageIdToSumPackageFieldDTO
                = dataRows.stream().collect(Collectors.toMap(
                OrderPackageExportRowDTO::getPackageId,
                row -> OrderPackageExportRowDTO.builder()
                        .totalSkuWeight(ObjectUtils.isNotEmpty(row.getSkuWeight()) ? row.getSkuWeight() : 0)
                        .totalSkuQuantity(ObjectUtils.isNotEmpty(row.getSkuQuantity()) ? row.getSkuQuantity() : 0)
                        .build(),
                (existing, replacement) -> {
                    existing.setTotalSkuWeight(existing.getSkuWeight() + replacement.getSkuWeight());
                    existing.setTotalSkuQuantity(existing.getTotalSkuQuantity() + replacement.getSkuWeight());
                    return existing;
                }
        ));

        dataRows.stream().filter(OrderPackageExportRowDTO::isFirstSkuRow)
                .forEach(
                        dataRow -> {
                            //update total sku weight
                            OrderPackageExportRowDTO summary = packageIdToSumPackageFieldDTO.get(dataRow.getPackageId());
                            if (ObjectUtils.isEmpty(summary))
                                return;
                            orderMapper.updateOrderPackageExportRowDTO(summary, dataRow);
                        }
                );

        return dataRows;
    }

    /**
     * Writes data to the Excel sheet using streaming.
     *
     * @param workbook      The SXSSFWorkbook instance.
     * @param sheet         The sheet to write data into.
     * @param exportRequest The export request containing necessary parameters.
     * @throws IOException If an I/O error occurs during data writing.
     */
    private void writeDataUsingStream(
            SXSSFWorkbook workbook, Sheet sheet, OrderPackageReportExportMsg exportRequest, List<Long> platformFeeIds
    ) {
        // Initialize row number (assuming header is at row 0)
        AtomicInteger rowNum = new AtomicInteger(1);

        // Create cell style once (optionally, you can clone styles from the template)
        CellStyle dataCellStyle = createDataCellStyle(workbook);

        final OrderPackageExportRowDTO[] firstRowRef = {null};
        // Obtain a stream of data from the repository
        try (Stream<OrderPackageExportRowDTO> dataStream = getDataStream(exportRequest, platformFeeIds)) {
            dataStream.forEach(data -> {
                if (data.isFirstSkuRow()) {
                    firstRowRef[0] = data;
                }

                data.setRowNum(rowNum.getAndIncrement());

                // Create a new row
                Row row = sheet.createRow(data.getRowNum());

                // Populate cells
                for (ExcelColumn column : ExcelColumn.values()) {

                    if (column != ExcelColumn.PHI_SAN_DYNAMIC) {
                        int index = column.getIndex() < ExcelColumn.PHI_SAN_DYNAMIC.getIndex() ?
                                column.getIndex() : column.getIndex() - 1 + platformFeeIds.size();
                        Object cellValue = column.getValueExtractor().apply(data);
                        createStyledCell(row, index, cellValue, dataCellStyle);
                    } else {
                        for (Long platformFeeId : platformFeeIds) {
                            int index = column.getIndex() + platformFeeIds.indexOf(platformFeeId);
                            Object cellValue = null;
                            if (ObjectUtils.isNotEmpty(data.getPlatformFeeMap()))
                                cellValue = data.getPlatformFeeMap().get(platformFeeId);
                            createStyledCell(row, index, cellValue, dataCellStyle);
                        }
                    }

                }

                //if the data is not the first row of the order package, we need to sum up some field
                if (!data.isFirstSkuRow()) {
                    OrderPackageExportRowDTO summary = firstRowRef[0];
                    if (
                            ObjectUtils.isNotEmpty(data.getSkuWeight())
                                    && ObjectUtils.isNotEmpty(data.getSkuQuantity())
                    ) {
                        summary.setTotalSkuWeight(
                                NumUtils.sumLongs(
                                        summary.getTotalSkuWeight(), data.getSkuWeight() * data.getSkuQuantity()
                                )
                        );
                    }
                    if (ObjectUtils.isNotEmpty(data.getSkuQuantity()))
                        summary.setTotalSkuQuantity(NumUtils.sumLongs(summary.getTotalSkuQuantity(), data.getSkuQuantity()));

                    summary.setTotalPlatformDiscountFromProduct(
                            NumUtils.sumBigDecimals(
                                    summary.getTotalPlatformDiscountFromProduct(),
                                    data.getPlatformDiscountFromProduct()
                            )
                    );
                    summary.setExpectedRevenue(//doanh thu dự kiến trên đơn bị trừ đi bởi tiền chiết khấu cho từng sản phẩm
                            NumUtils.minusBigDecimals(
                                    summary.getExpectedRevenue(), data.getPlatformDiscountFromProduct()
                            )
                    );

                    Row sumaryRow = sheet.getRow(summary.getRowNum());
                    for (ExcelColumn column : ExcelColumn.UPDATED_COLUMN_WHEN_SUM_UP) {
                        if (column != ExcelColumn.PHI_SAN_DYNAMIC) {
                            int index = column.getIndex() < ExcelColumn.PHI_SAN_DYNAMIC.getIndex() ?
                                    column.getIndex() : column.getIndex() - 1 + platformFeeIds.size();
                            Object cellValue = column.getValueExtractor().apply(summary);
                            createStyledCell(sumaryRow, index, cellValue, dataCellStyle);
                        } else {
                            for (Long platformFeeId : platformFeeIds) {
                                int index = column.getIndex() + platformFeeIds.indexOf(platformFeeId);
                                Object cellValue = null;
                                if (ObjectUtils.isNotEmpty(summary.getPlatformFeeMap()))
                                    cellValue = summary.getPlatformFeeMap().get(platformFeeId);
                                createStyledCell(sumaryRow, index, cellValue, dataCellStyle);

                            }
                        }
                    }
                }
            });
        }
    }

    private Stream<OrderPackageExportRowDTO> getDataStream(OrderPackageReportExportMsg exportRequest, List<Long> platformFeeIds) {
        Stream<MyData> dataStream = getStream(exportRequest);
        final Long[] firstPackageProductId = {null};

        return dataStream
                .filter(data -> ObjectUtils.isNotEmpty(data.getPackageId()))
                .map(
                        data -> {
                            Long firstPackageId = firstPackageProductId[0];

                            /* indicate the first sku of the order package */
                            boolean isFirstRow
                                    = ObjectUtils.isEmpty(firstPackageId) || !data.getPackageId().equals(firstPackageId);

                            OrderPackageExportRowDTO orderPackageExportRowDTO = null;
                            if (isFirstRow) {
                                firstPackageProductId[0] = data.getPackageId();

                                orderPackageExportRowDTO
                                        = orderMapper.toOrderPackageExportRowDTOPrimary(data);
//                        SellerOrderStatus sellerOrderStatus
//                                = SellerOrderStatus.getByName(orderPackageExportRowDTO.getSellerOrderStatus());
//                        orderPackageExportRowDTO.setSellerOrderStatusDescription(
//                                ObjectUtils.isNotEmpty(sellerOrderStatus) ? sellerOrderStatus.getDescription() : null
//                        );

                                orderPackageExportRowDTO.setFirstSkuRow(true);

                                // Tổng giá bán (3); tiền hàng gốc
                                BigDecimal originPrice = orderPackageExportRowDTO.getOriginTotalSkuPrice();
                                BigDecimal price = orderPackageExportRowDTO.getTotalSkuPrice();    //tiền hàng cuối sau khi điều chỉnh giá
                                // Tiền đàm phán trên tổng đơn hàng (5): tiền hàng gốc - tiền hàng sau khi điều chỉnh
                                orderPackageExportRowDTO.setTotalNegotiatedAmount(NumUtils.minusBigDecimals(originPrice, price));
                                // Phí vận chuyển mà người mua trả (9)
                                if (
                                        ObjectUtils.isEmpty(orderPackageExportRowDTO.getTotalDomesticShippingFee())
                                                || orderPackageExportRowDTO.getTotalDomesticShippingFee().compareTo(BigDecimal.ZERO) <= 0
                                )
                                    orderPackageExportRowDTO.setTotalDomesticShippingFee(orderPackageExportRowDTO.getTotalShippingFee());
                                BigDecimal shipmentFee
                                        = DataUtils.getBigDecimal(orderPackageExportRowDTO.getTotalDomesticShippingFee());

                                // Tổng giá trị đơn hàng (VND) (6) = (3) - (5)
                                // tương đương với price ở bên trên

                                // Tổng số tiền người mua thanh toán (10)
                                orderPackageExportRowDTO.setTotalPaidBuyer(price.add(shipmentFee));

                                // Doanh thu dự kiến trên đơn (15) = (6) - (7) - (12) - (13) - (14)
                                orderPackageExportRowDTO.setExpectedRevenue(price);

                                //trừ đi trừ đi tất cả các phí sàn trên đơn hàng
                                if (StringUtils.isNotBlank(data.getPlatformFeeStr())) {
                                    TypeReference<Map<Long, BigDecimal>> typeRef = new TypeReference<>() {
                                    };
                                    Map<Long, BigDecimal> platformFeeIdToValue
                                            = JsonMapperUtils.convertJsonToObject(data.getPlatformFeeStr(), typeRef);
                                    orderPackageExportRowDTO.setPlatformFeeMap(platformFeeIdToValue);
                                    BigDecimal totalPlatformFee
                                            = ObjectUtils.isNotEmpty(platformFeeIdToValue) ?
                                            NumUtils.sumBigDecimals(platformFeeIdToValue.values())
                                            : BigDecimal.ZERO;
                                    orderPackageExportRowDTO.setExpectedRevenue(
                                            NumUtils.minusBigDecimals(
                                                    orderPackageExportRowDTO.getExpectedRevenue(),
                                                    totalPlatformFee
                                            )
                                    );
                                }

                                /* for sum up later */
                                orderPackageExportRowDTO.setTotalSkuQuantity(orderPackageExportRowDTO.getSkuQuantity());    //tổng số lượng sku
                                orderPackageExportRowDTO.setTotalSkuWeight(
                                        ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuQuantity())
                                                && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuWeight()) ?
                                                orderPackageExportRowDTO.getSkuWeight() * orderPackageExportRowDTO.getSkuQuantity()
                                                : 0
                                );        //tổng trọng lượng sku (kg)


                                //Tiền đàm phán trên tổng đơn hàng (5)
                                if (
                                        ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getNegotiatedAmount())
                                                && orderPackageExportRowDTO.getNegotiatedAmount().compareTo(BigDecimal.ZERO) > 0
                                ) {
                                    orderPackageExportRowDTO.setTotalNegotiatedDeductionAmount(
                                            orderPackageExportRowDTO.getNegotiatedAmount()
                                    );
                                } else {
                                    orderPackageExportRowDTO.setTotalNegotiatedDeductionAmount(
                                            orderPackageExportRowDTO.getOriginTotalSkuPrice().subtract(
                                                    orderPackageExportRowDTO.getTotalSkuPrice()
                                            )
                                    );
                                }

                                /* hide column using seller order status */
                                hideSomeColumnBasedOnSellerOrderStatus(orderPackageExportRowDTO);

                                /* set up data for column "Loại đơn hàng" */
                                if (ObjectUtils.isEmpty(data.getIsChangePrice()) || data.getIsChangePrice() == 0) { //TH chưa điều chỉnh giá
                                    orderPackageExportRowDTO.setOrderType(Constants.NO_PRICE_ADJUSTED_ORDER_PACKAGE_TYPE);
                                } else {
                                    orderPackageExportRowDTO.setOrderType(Constants.PRICE_ADJUSTED_ORDER_PACKAGE_TYPE);
                                }
                            } else {
                                orderPackageExportRowDTO = orderMapper.toOrderPackageExportRowDTOOther(data);
                            }


                            //phí chiết khấu sàn trên từng sản phẩm
                            //trừ đi triết khấu từng sản phẩm cho sàn
                            BigDecimal platformDiscountRate
                                    = ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSellerPlatformDiscountRate()) ?
                                    orderPackageExportRowDTO.getSellerPlatformDiscountRate()
                                    : orderPackageExportRowDTO.getPlatformDiscountRate();
                            if (
                                    ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuPrice())
                                            && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuQuantity())
                                            && ObjectUtils.isNotEmpty(platformDiscountRate)
                            ) {
                                orderPackageExportRowDTO.setPlatformDiscountFromProduct(
                                        ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSellerPlatformDiscountAmount()) ?
                                                orderPackageExportRowDTO.getSellerPlatformDiscountAmount()
                                                : orderPackageExportRowDTO.getSkuPrice()
                                                .multiply(BigDecimal.valueOf(orderPackageExportRowDTO.getSkuQuantity()))
                                                .multiply(platformDiscountRate)
                                                .divide(BigDecimal.valueOf(100), RoundingMode.UP)
                                );
                                if (isFirstRow) {
                                    orderPackageExportRowDTO.setTotalPlatformDiscountFromProduct(
                                            orderPackageExportRowDTO.getPlatformDiscountFromProduct()
                                    );
                                    orderPackageExportRowDTO.setExpectedRevenue(
                                            NumUtils.minusBigDecimals(
                                                    orderPackageExportRowDTO.getExpectedRevenue(),
                                                    orderPackageExportRowDTO.getPlatformDiscountFromProduct()
                                            )
                                    );
                                }
                            }

                            //col 19: giá gốc and col 20: người bán trợ giá
                            if (ObjectUtils.isEmpty(orderPackageExportRowDTO.getPriceRanges())) {
                                if (ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuPrice())
                                        && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuQuantity())) {
                                    BigDecimal totalSkuPrice = orderPackageExportRowDTO.getSkuPrice()
                                            .multiply(BigDecimal.valueOf(orderPackageExportRowDTO.getSkuQuantity()));
                                    orderPackageExportRowDTO.setOriginTotalSkuPriceWithoutPriceRange(totalSkuPrice);
                                }
                            } else {
                                PriceRange firstPriceRange = orderPackageExportRowDTO.getPriceRanges()
                                        .stream()
                                        .findFirst()
                                        .orElse(null);
                                if (ObjectUtils.isNotEmpty(firstPriceRange)
                                        && ObjectUtils.isNotEmpty(firstPriceRange.getSystemCurrencyPrice())
                                        && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuPrice())
                                        && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuQuantity())) {
                                    BigDecimal systemCurrencyTotal = firstPriceRange.getSystemCurrencyPrice()
                                            .multiply(BigDecimal.valueOf(orderPackageExportRowDTO.getSkuQuantity()));
                                    BigDecimal skuTotal = orderPackageExportRowDTO.getSkuPrice()
                                            .multiply(BigDecimal.valueOf(orderPackageExportRowDTO.getSkuQuantity()));
                                    orderPackageExportRowDTO.setOriginTotalSkuPriceWithoutPriceRange(systemCurrencyTotal);
                                    orderPackageExportRowDTO.setPriceRangeDeduction(systemCurrencyTotal.subtract(skuTotal));
                                }
                            }

                            //Tiền đàm phán trên SKU (4)
                            if (
                                    ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuPrice())
                                            && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuQuantity())
                                            && ObjectUtils.isNotEmpty(orderPackageExportRowDTO.getSkuNegotiatedAmount())
                                            && orderPackageExportRowDTO.getSkuNegotiatedAmount().compareTo(BigDecimal.ZERO) > 0
                            ) {
                                orderPackageExportRowDTO.setNegotiatedDeductionAmountOnSku(
                                        orderPackageExportRowDTO.getSkuPrice()
                                                .subtract(orderPackageExportRowDTO.getSkuNegotiatedAmount())
                                                .multiply(BigDecimal.valueOf(orderPackageExportRowDTO.getSkuQuantity()))
                                );
                            }

                            return orderPackageExportRowDTO;
                        }
                );

    }

    private void hideSomeColumnBasedOnSellerOrderStatus(OrderPackageExportRowDTO orderPackageExportRowDTO) {
        if (
                ObjectUtils.isEmpty(orderPackageExportRowDTO)
                        || ObjectUtils.isEmpty(orderPackageExportRowDTO.getSellerOrderStatus())
        )
            return;
        SellerOrderStatus sellerOrderStatus
                = SellerOrderStatus.getByName(orderPackageExportRowDTO.getSellerOrderStatus());
        if (ObjectUtils.isEmpty(sellerOrderStatus))
            return;
        if (Constants.NOT_PAID_SELLER_ORDER_STATUSES.contains(sellerOrderStatus)) {
            orderPackageExportRowDTO.setCreateTime(null);
        }
        if (
                !Constants.SUCCESS_TO_CONNECT_DELIVERY_SELLER_ORDER_STATUSES
                        .contains(sellerOrderStatus)
        ) {
            orderPackageExportRowDTO.setShipmentCode(null);
            orderPackageExportRowDTO.setCarrierName(null);
            orderPackageExportRowDTO.setExpectedDeliveryTime(null);
            orderPackageExportRowDTO.setTotalShippingFee(null);
        }

        if (!Constants.SUCCESS_TO_PREPARE_ORDER_PACKAGE.contains(sellerOrderStatus)) {
            orderPackageExportRowDTO.setShipmentMethod(null);
            orderPackageExportRowDTO.setShipmentDate(null);
        }

        if (SellerOrderStatus.ORDER_COMPLETED != sellerOrderStatus) {
            orderPackageExportRowDTO.setPaymentMethod(null);
            orderPackageExportRowDTO.setDeliverySuccessTime(null);
            orderPackageExportRowDTO.setTotalPaidBuyer(null);
            orderPackageExportRowDTO.setTotalDomesticShippingFee(null);
        }

        if (
                SellerOrderStatus.WAITING_FOR_PAYMENT == sellerOrderStatus
                || ObjectUtils.isEmpty(orderPackageExportRowDTO.getPaymentTime())
                || orderPackageExportRowDTO.getPaymentTime() == 0
        ) {
            orderPackageExportRowDTO.setPrepayment(null);
        }
    }

    private Stream<MyData> getStream(OrderPackageReportExportMsg exportRequest) {
        if (ObjectUtils.isNotEmpty(exportRequest.getReportExportId())) {
            if (ObjectUtils.isNotEmpty(exportRequest.getSelectedOrderIds())) {
                //todo: use the Carrier table
                return packageProductRepository.getByMerchantIdAndPackageIdInUsingStream(
                        exportRequest.getMerchantId(), exportRequest.getSelectedOrderIds()
                );
            } else {
                Long startDateInEpochSecond = exportRequest.getStartDate();
                Long endDateInEpochSecond = exportRequest.getEndDate();
                Integer requestedTabCode = null;

                if(!DataUtils.isNullOrEmpty(exportRequest.getTabCode())){
                    requestedTabCode =  exportRequest.getTabCode().getValue();
                }

                /* lower case all the string filter */
                String orderCode = exportRequest.getOrderCode();
                String buyerName = exportRequest.getBuyerName();
                String productName = exportRequest.getProductName();
                String shipmentCode = exportRequest.getShipmentCode();

                Long merchantId = exportRequest.getMerchantId();

                return packageProductRepository.getByReportFilterUsingStream(
                        merchantId,
                        requestedTabCode,
                        startDateInEpochSecond,
                        endDateInEpochSecond,
                        orderCode,
                        buyerName,
                        productName,
                        shipmentCode
                );
            }
        } else { //withdrawalRequestReportId presents
            return packageProductRepository.getByWithdrawalRequestExportId(
                    exportRequest.getWithdrawalRequestReportId()
            );
        }
    }

    /**
     * Creates a CellStyle for data rows with thin black borders.
     *
     * @param workbook The workbook where styles are defined.
     * @return A CellStyle with thin black borders.
     */
    private CellStyle createDataCellStyle(SXSSFWorkbook workbook) {
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Font vietnameseFont = workbook.createFont();
        vietnameseFont.setFontName("Arial Unicode MS");
        dataCellStyle.setFont(vietnameseFont);
        return dataCellStyle;
    }

    private CellStyle createDataCellStyle(SXSSFWorkbook workbook, Short colorIndex) {
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        dataCellStyle.setFillBackgroundColor(colorIndex);

        return dataCellStyle;
    }

    /**
     * Creates a styled cell with the given value and style.
     *
     * @param row          The row where the cell is created.
     * @param columnIndex  The index of the column.
     * @param value        The value to set in the cell.
     * @param cellStyle    The CellStyle to apply.
     */
    private void createStyledCell(Row row, int columnIndex, Object value, CellStyle cellStyle) {
        Cell cell = row.createCell(columnIndex);
        if (ObjectUtils.isEmpty(value)) {
            cell.setCellValue("");
            cell.setCellStyle(cellStyle);
            return;
        }
        if (value instanceof String)
            cell.setCellValue((String) value);
        if (value instanceof RichTextString)
            cell.setCellValue((RichTextString) value);
        if (value instanceof LocalDateTime)
            cell.setCellValue(DateUtils.toDateString((LocalDateTime) value, DateUtils.saleSDateCheckoutFormat));
        if (value instanceof LocalDate)
            cell.setCellValue((LocalDate) value);
        if (value instanceof Date)
            cell.setCellValue((Date) value);
        if (value instanceof Calendar)
            cell.setCellValue((Calendar) value);
        if (value instanceof Number)
            cell.setCellValue(((Number) value).doubleValue());
        if (value instanceof Boolean)
            cell.setCellValue((Boolean) value);

        cell.setCellStyle(cellStyle);
    }

    private List<MyData> fetchBatch(
            OrderPackageReportExportMsg exportRequest, int pageNum, int batchSize
    ) {
        Pageable pageable = PageRequest.of(pageNum, batchSize);
        if (ObjectUtils.isNotEmpty(exportRequest.getSelectedOrderIds())) {
            //todo: use the Carrier table
            return packageProductRepository.getByMerchantIdAndPackageIdIn(
                    exportRequest.getMerchantId(), exportRequest.getSelectedOrderIds(), pageable
            );
        }

        else {
            Long startDateInEpochSecond = exportRequest.getStartDate();
            Long endDateInEpochSecond = exportRequest.getEndDate();
            List<SellerOrderStatus> requestedSellerOrderStatusesStrList = null;
            String tab = exportRequest.getTab();
            if (StringUtils.isNotBlank(exportRequest.getTab()))
                requestedSellerOrderStatusesStrList
                        = SellerOrderStatus.PARENT_TO_CHILDREN_SELLER_ORDER_STATUS.get(SellerOrderStatus.getByName(tab));

            /* lower case all the string filter */
            String orderCode = exportRequest.getOrderCode();
            String buyerName = exportRequest.getBuyerName();
            String productName = exportRequest.getProductName();
            String shipmentCode = exportRequest.getShipmentCode();

            Long merchantId = exportRequest.getMerchantId();

            return packageProductRepository.getByReportFilter(
                    merchantId,
                    requestedSellerOrderStatusesStrList,
                    startDateInEpochSecond,
                    endDateInEpochSecond,
                    orderCode,
                    buyerName,
                    productName,
                    shipmentCode,
                    pageable
            );
        }

    }


    private Long getExportedRowNum(OrderPackageReportExportMsg exportRequest) {
        if (ObjectUtils.isNotEmpty(exportRequest.getSelectedOrderIds())) {
            return packageProductRepository.countByMerchantIdAndPackageIdIn(
                    exportRequest.getMerchantId(), exportRequest.getSelectedOrderIds()
            );
        } else {
            Long startDateInEpochSecond = exportRequest.getStartDate();
            Long endDateInEpochSecond = exportRequest.getEndDate();
            List<SellerOrderStatus> requestedSellerOrderStatusesStrList = null;
            String tab = exportRequest.getTab();
            if (StringUtils.isNotBlank(exportRequest.getTab()))
                requestedSellerOrderStatusesStrList
                        = SellerOrderStatus.PARENT_TO_CHILDREN_SELLER_ORDER_STATUS.get(SellerOrderStatus.getByName(tab));

            /* lower case all the string filter */
            String orderCode = exportRequest.getOrderCode();
            String buyerName = exportRequest.getBuyerName();
            String productName = exportRequest.getProductName();
            String shipmentCode = exportRequest.getShipmentCode();

            Long merchantId = exportRequest.getMerchantId();

            return packageProductRepository.countByMerchantIdAndSellerOrderStatusIn(
                    merchantId,
                    requestedSellerOrderStatusesStrList.stream().map(SellerOrderStatus::name).toList(),
                    startDateInEpochSecond,
                    endDateInEpochSecond,
                    orderCode,
                    buyerName,
                    productName,
                    shipmentCode
            );

        }
    }


    @Override
    public ReportExportDownloadResponse downloadFile(@NotNull Long reportExportId) {
        Long merchantId = getCurrentUser().getId();
        ReportExportEntity reportExport = repo.findByIdAndMerchantIdAndDeleted(reportExportId, merchantId, false)
                .orElseThrow(() -> new VipoNotFoundException("ReportExport not found for ID: " + reportExportId));

        ReportExportStatus reportExportStatus = reportExport.getStatus();
        if (reportExportStatus.equals(ReportExportStatus.pending))
            throw new VipoInvalidDataRequestException("Still pending");
        if (reportExportStatus.equals(ReportExportStatus.failed))
            throw new VipoInvalidDataRequestException("Failed to create the file");

        String storageType = reportExport.getStorageType();
        if (StringUtils.isBlank(storageType) && !storageType.equals(FileStorageMethodEnum.S3.name()))
            return null;    //only supporting s3 storage for now
        String storageInfoStr = reportExport.getStorageInfo();
        if (StringUtils.isBlank(storageInfoStr))   //case mark success but no storage info is stored
            throw new VipoInvalidDataRequestException("Failed to create the file");
        var s3StorageLocation = JsonMapperUtils.convertJsonToObject(storageInfoStr, S3StorageLocation.class);
        if (
                ObjectUtils.isEmpty(s3StorageLocation)
                || StringUtils.isBlank(s3StorageLocation.getBucket())
                || StringUtils.isBlank(s3StorageLocation.getKey())
        )
            throw new VipoInvalidDataRequestException("Failed to create the file");

        String fileName = reportExport.getReportFileName();
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName += ".xlsx";
        }
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(StringProcessingUtils.normalizeVietnamese(fileName))
                .build();

        try {
            S3Object s3Object
                    = amazonS3Service.getS3Object(s3StorageLocation.getBucket(), s3StorageLocation.getKey());


//            String contentType = s3Object.getObjectMetadata().getContentType();
//            if (StringUtils.isBlank(contentType)) {
//                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//            }
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            var resource = new InputStreamResource(s3Object.getObjectContent());

            long contentLength = s3Object.getObjectMetadata().getContentLength();

            return ReportExportDownloadResponse.builder()
                    .contentType(contentType)
                    .fileName(fileName)
                    .contentDisposition(contentDisposition)
                    .contentLength(contentLength)
                    .resource(resource)
                    .build();
        } catch (IOException e) {
            throw new VipoFailedToExecuteException("Failed to load the excel file");
        }


    }

    @Override
    public ReportExportDetailResponse getReportExportDetail(Long reportExportId) {
        Long merchantId = getCurrentUser().getId();
        ReportExportEntity reportExport
                = repo.findByIdAndMerchantIdAndDeleted(reportExportId, merchantId, false)
                .orElseThrow(() -> new VipoNotFoundException("ReportExport not found for ID: " + reportExportId));

        return orderMapper.toReportExportDetailResponse(reportExport);
    }

    @Transactional
    public void deleteOldReportOccasionally() {
        // Calculate the cutoff date (e.g., 6 months ago)
        LocalDateTime cutoffDate = DateUtils.getCurrentLocalDateTime().minus(cleanUpDuration);

        log.info("Starting cleanup of old report exports.");

        List<ReportExportEntity> batch = new ArrayList<>();
        final int BATCH_SIZE = 1000; // Adjust based on S3 limits and performance

        try (Stream<ReportExportEntity> stream = repo.streamOldReports(cutoffDate)) {
            stream.forEach(report -> {
                batch.add(report);
                if (batch.size() >= BATCH_SIZE) {
                    deleteBatch(batch);
                    batch.clear();
                }
            });
        }

        // Delete any remaining reports
        if (!batch.isEmpty()) {
            deleteBatch(batch);
        }

        log.info("Cleanup of old report exports completed.");
    }

    private void deleteBatch(List<ReportExportEntity> batch) {
        if (batch.isEmpty()) return;

        // Group reports by bucket
        Map<String, List<String>> bucketToKeysMap = batch.stream()
                .filter(report -> StringUtils.isNotBlank(report.getStorageType()) && report.getStorageType().equals(FileStorageMethodEnum.S3.name()))
                .map(report -> {
                    String storageInfoStr = report.getStorageInfo();
                    if (StringUtils.isBlank(storageInfoStr)) {
                        log.warn("Failed to get the S3 file info for report_export.id {}", report.getId());
                        return null;
                    }
                    S3StorageLocation s3StorageLocation = JsonMapperUtils.convertJsonToObject(storageInfoStr, S3StorageLocation.class);
                    if (ObjectUtils.isEmpty(s3StorageLocation) || StringUtils.isBlank(s3StorageLocation.getBucket()) || StringUtils.isBlank(s3StorageLocation.getKey())) {
                        log.warn("Failed to get the S3 file info for report_export.id {}", report.getId());
                        return null;
                    }
                    return new AbstractMap.SimpleEntry<>(s3StorageLocation.getBucket(), s3StorageLocation.getKey());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (Map.Entry<String, List<String>> entry : bucketToKeysMap.entrySet()) {
            String bucketName = entry.getKey();
            List<String> keys = entry.getValue();

            List<DeleteObjectsRequest.KeyVersion> keyVersions = keys.stream()
                    .map(DeleteObjectsRequest.KeyVersion::new)
                    .collect(Collectors.toList());

            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keyVersions)
                    .withQuiet(false);

            try {
                DeleteObjectsResult result = amazonS3Service.deleteObjects(deleteRequest);
                List<DeleteObjectsResult.DeletedObject> deleted = result.getDeletedObjects();
                log.info("Deleted {} objects from bucket {}", deleted.size(), bucketName);

                // Mark records as deleted in the database
                // Assuming you have a way to map keys back to reports
                // This might require additional queries or a different approach
                // For simplicity, let's iterate through the batch
                batch.forEach(report -> {
                    String storageInfoStr = report.getStorageInfo();
                    if  (StringUtils.isBlank(storageInfoStr))
                        return;
                    S3StorageLocation s3StorageLocation = JsonMapperUtils.convertJsonToObject(storageInfoStr, S3StorageLocation.class);
                    if ( ObjectUtils.isNotEmpty(s3StorageLocation)
                            && ObjectUtils.isNotEmpty(s3StorageLocation.getBucket())
                            && s3StorageLocation.getBucket().equals(bucketName)
                            && ObjectUtils.isNotEmpty(s3StorageLocation.getKey())
                            && s3StorageLocation.getKey().equals(report.getStorageInfo())) {
                        report.setDeleted(true);
                    }
                });

                repo.saveAll(batch);
            } catch (Exception e) {
                log.error("Failed to bulk delete objects from bucket {}. Error: {}", bucketName, e.getMessage());
                // TODO: Implement retry logic or move to a dead-letter queue
            }
        }
    }


    @Override
    public void markExportReportAsFailed(OrderPackageReportExportMsg content, String errorMsg) {
        if (ObjectUtils.isEmpty(content) || ObjectUtils.isEmpty(content.getReportExportId())) {
            log.info("The message does not contain the reportExportId");
            return;
        }

        ReportExportEntity reportExportEntity = repo.findById(content.getReportExportId())
                .orElse(null);

        if (ObjectUtils.isEmpty(reportExportEntity)) {
            log.info("The message does not contain the reportExportId");
            return;
        }

        reportExportEntity.setStatus(ReportExportStatus.failed);
        reportExportEntity.setErrorMessage(errorMsg);
        reportExportEntity.setFinishTime(DateUtils.getCurrentLocalDateTime());
        repo.save(reportExportEntity);
    }

}
