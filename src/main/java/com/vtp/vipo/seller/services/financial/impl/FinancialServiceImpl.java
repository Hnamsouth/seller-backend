package com.vtp.vipo.seller.services.financial.impl;

import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.FinancialConstant;
import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialExportStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.FinancialReportType;
import com.vtp.vipo.seller.common.dao.entity.enums.financial.PeriodType;
import com.vtp.vipo.seller.common.dao.entity.enums.reportexport.FileStorageMethodEnum;
import com.vtp.vipo.seller.common.dao.entity.enums.reportexport.S3StorageLocation;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.request.financial.FinancialReportRequest;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.financial.FinancialReportResponse;
import com.vtp.vipo.seller.common.dto.response.financial.TimeRange;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.exception.VipoInvalidDataRequestException;
import com.vtp.vipo.seller.common.exception.VipoNotFoundException;
import com.vtp.vipo.seller.common.mapper.ProductMapper;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.common.utils.*;
import com.vtp.vipo.seller.config.mq.kafka.KafkaTopicConfig;
import com.vtp.vipo.seller.config.mq.kafka.MessageData;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.config.versioning.ApiVersion;
import com.vtp.vipo.seller.config.versioning.ApiVersionUtils;
import com.vtp.vipo.seller.financialstatement.common.dto.OrderReportDTO;
import com.vtp.vipo.seller.financialstatement.common.dto.ProductSumaryDTO;
import com.vtp.vipo.seller.financialstatement.common.dto.TopProductDTO;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportInfoRequest;
import com.vtp.vipo.seller.financialstatement.common.dto.request.RevenueReportExportMsg;
import com.vtp.vipo.seller.financialstatement.common.dto.response.RevenueReportExportInfoResponse;
import com.vtp.vipo.seller.financialstatement.common.enums.FinancialReportExportType;
import com.vtp.vipo.seller.financialstatement.export.SuccessOrderPackageJRRsultSetDataSource;
import com.vtp.vipo.seller.financialstatement.export.WithdrawRequestJRResultSetDataSource;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.financial.FinancialService;
import com.vtp.vipo.seller.services.financial.FinancialValidator;
import com.vtp.vipo.seller.services.financial.PeriodCalculator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import lombok.AccessLevel;
import com.vtp.vipo.seller.services.financial.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinancialServiceImpl extends BaseService<ProductEntity, Long, ProductRepository> implements FinancialService {

    final ProductRepository productRepository;

    final RevenueReportEntityRepository revenueReportEntityRepository;

    final TopProductReportEntityRepository topProductReportEntityRepository;

    final ChartTopProductReportEntityRepository chartTopProductReportEntityRepository;

    final FinancialReportEntityRepository financialReportEntityRepository;

    final AggregateRevenueService aggregateRevenueService;

    final RevenueReportService revenueReportService;

    final TotalVisitsService totalVisitsService;

    final ProductMapper productMapper;

    final ProductDailyAnalyticEntityRepository productDailyAnalyticEntityRepository;

    final MerchantRepository merchantRepository;

    final WardRepository wardRepository;

    final MerchantPaymentCardRepository merchantPaymentCardRepository;

    final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate jdbcTemplateNameParams;

    final OrderPackageRepository orderPackageRepository;

    private final WithdrawalRequestRepository withdrawalRequestRepository;

    final SellerMapper sellerMapper;

    final RevenueReportExportEntityRepository revenueReportExportEntityRepository;

    @SuppressWarnings("rawtypes")
    private final KafkaTemplate kafkaTemplate;

    private final KafkaTopicConfig kafkaTopicConfig;

    private final AmazonS3Service amazonS3Service;
    private final WithdrawalRequestItemRepository withdrawalRequestItemRepository;

    private final MerchantNewRepository merchantNewRepository;

    @Value("${custom.properties.financial-statement.maximum-export-time:5m}")
    private Duration maximumFinancialStatementExportDuration;

    @Value("${custom.properties.s3.vipo-bucket.name}")
    String vipoBucketName;

    @Value("${custom.properties.s3.vipo-bucket.revenue-export.key-prefix:revenue-export-}")
    String revenueExportPrefix;

    @Value("${custom.properties.financial-statement.maximum-export-order-package-num:1000000}")
    private Long maximumExportOrderPackageNum;

    @Value("${custom.properties.financial-statement.maximum-withdrawal-item-num:1000000}")
    private Long maximumWithdrawalItemNum;

    /**
     * Filters products based on the given pagination parameters and search keyword.
     * <p>
     * This method retrieves the current user's details, validates that the keyword (if provided)
     * does not exceed 150 characters, and queries the product repository with the specified pagination.
     * The result is then mapped to a {@link PagingRs} response.
     * </p>
     *
     * @param page     The current page number (1-indexed).
     * @param pageSize The number of records per page.
     * @param keyword  The search keyword. Must not exceed 150 characters.
     * @return A {@link PagingRs} object containing the total count, product data, and current page.
     * @throws VipoBusinessException if the keyword length exceeds 150 characters.
     */
    @Override
    public PagingRs filterProduct(Integer page, Integer pageSize, String keyword) {
        log.info("[Financial filterProduct] Filter product with page: {}, pageSize: {}, keyword: {}", page, pageSize, keyword);
        VipoUserDetails user = getCurrentUser();

        // Validate keyword length: maximum allowed is 150 characters.
        if (ObjectUtils.isNotEmpty(keyword) && keyword.trim().length() > 150) {
            log.error("[Financial filterProduct] Keyword length is invalid");
            throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, "Từ khóa tìm kiếm cho phép tối đa 150 ký tự");
        }

        // Create a pageable instance for pagination.
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        // Query products based on the current user's ID and the keyword.
        Page<ProductEntity> pageData = productRepository.filterProduct(user.getId(), keyword, pageable);
        // Map the results to the PagingRs response.
        return PagingRs.builder()
                .totalCount(pageData.getTotalElements())
                .data(productMapper.toProductBaseResponseList(pageData.getContent()))
                .currentPage(page)
                .build();
    }

    /**
     * Exports a revenue report based on the given financial report request.
     * <p>
     * This method validates the request, builds the time range for the report, and then attempts
     * to find an existing report for the current period. If found, it builds and returns a response
     * from the existing report; otherwise, it aggregates new revenue data, saves the report, and returns it.
     * </p>
     *
     * @param request The financial report request.
     * @return A {@link FinancialReportResponse} containing the revenue report data.
     */
    @Override
    public FinancialReportResponse exportReportRevenue(FinancialReportRequest request) {
        log.info("[Financial exportReportRevenue] Export report revenue with request: {}", request);
        VipoUserDetails user = getCurrentUser();

        // Validate the financial report request.
        FinancialValidator.validateFinancialReportRequest(request);

        // Build the time range for the report based on the request.
        TimeRange timeRange = buildTimeRange(request);
        LocalDate currentStartDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate();
        LocalDate currentEndDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate();
        log.info("[Financial exportReportRevenue] Time range: {} - {}", currentStartDate, currentEndDate);

        // Attempt to find an existing report for the current period.
        Optional<RevenueReportEntity> existingReport = findReportForCurrentPeriod(user.getId(), request, timeRange);

        if (existingReport.isPresent()) {
            log.info("[Financial exportReportRevenue] Report exists in the current period");
            // Build and return a response based on the existing report.
            return buildResponseFromExistingReport(existingReport.get(), user.getId());
        }

        // If no report exists, aggregate new revenue data and save the new report.
        log.info("[Financial exportReportRevenue] Report does not exist in the current period");
        FinancialReportResponse response = aggregateRevenueService.aggregateRevenueData(request, user.getId(), timeRange);
        Long savedReportId = revenueReportService.save(response, user.getId(), timeRange);
        log.info("[Financial exportReportRevenue] Saved report revenue with reportId: {}", savedReportId);
        response.setRevenueReportId(String.valueOf(savedReportId));

        return response;
    }

    /**
     * Calculates the time range based on the given financial report request.
     *
     * @param request The financial report request.
     * @return A {@link TimeRange} representing the calculated time range.
     */
    private TimeRange buildTimeRange(FinancialReportRequest request) {
        return PeriodCalculator.calculateTimeRange(request);
    }

    /**
     * Attempts to find an existing revenue report for the current period.
     * <p>
     * This method extracts the current start and end dates from the provided time range, determines the
     * period type from the request, and then searches for a report matching the merchant ID, period type,
     * and date range.
     * </p>
     *
     * @param userId    The merchant's identifier.
     * @param request   The financial report request.
     * @param timeRange The time range for the report.
     * @return An {@link Optional} containing the found {@link RevenueReportEntity}, or empty if not found.
     */
    private Optional<RevenueReportEntity> findReportForCurrentPeriod(Long userId, FinancialReportRequest request, TimeRange timeRange) {
        LocalDate currentStartDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate();
        LocalDate currentEndDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate();
        PeriodType periodType = PeriodType.valueOf(request.getFilterType().toUpperCase());
        return findReportByPeriod(userId, periodType, currentStartDate, currentEndDate);
    }

    /**
     * Finds a revenue report by merchant ID, period type, and the specified period start and end dates.
     * <p>
     * Note: In Phase 6, productId will be used. Currently, this method only filters by merchantId,
     * period type, and period dates.
     * </p>
     *
     * @param merchantId The merchant's identifier.
     * @param filterType The period type.
     * @param start      The start date of the period.
     * @param end        The end date of the period.
     * @return An {@link Optional} containing the found {@link RevenueReportEntity}, or empty if not found.
     */
    private Optional<RevenueReportEntity> findReportByPeriod(Long merchantId, PeriodType filterType, LocalDate start, LocalDate end) {
        return revenueReportEntityRepository.findByMerchantIdAndPeriodTypeAndPeriodStartAndPeriodEnd(merchantId, filterType, start, end);
    }

//    private Optional<RevenueReportEntity> findReportByPeriod(Long merchantId, String filterType, long start, long end) {
//        return revenueReportEntityRepository.findByMerchantIdAndPeriodTypeAndPeriodStartAndPeriodEnd(merchantId, filterType, start, end);
//    }

    /**
     * Builds a {@link FinancialReportResponse} from an existing revenue report.
     * <p>
     * This method retrieves the associated top product report data, chart data, financial report details,
     * and total visits data, then converts the combined information into a {@link FinancialReportResponse}
     * using the {@link FinancialReportConverter}.
     * </p>
     *
     * @param reportEntity The existing revenue report entity.
     * @param userId       The merchant's identifier.
     * @return A {@link FinancialReportResponse} built from the existing report data.
     */
    private FinancialReportResponse buildResponseFromExistingReport(RevenueReportEntity reportEntity, Long userId) {
        Long reportId = reportEntity.getId();

        // Retrieve top product report data, ordered by ranking.
        List<TopProductReportEntity> topProductReports = topProductReportEntityRepository.findByReportIdOrderByRanking(reportId);

        // Retrieve chart data for top products, ordered by display order.
        List<ChartTopProductReportEntity> chartTopProductReports = chartTopProductReportEntityRepository.findByReportIdOrderByDisplayOrder(reportId);

        // Retrieve financial report details, ordered by display order.
        List<FinancialReportEntity> financialReports = financialReportEntityRepository.findByReportIdOrderByDisplayOrder(reportId);

        // Retrieve total visits data for the merchant.
        FinancialReportResponse.TotalVisitsData totalVisitsData = totalVisitsService.getTotalVisitsAllTime(userId);

        // Convert and combine the data into a FinancialReportResponse.
        return FinancialReportConverter.convertToResponse(
                reportEntity,
                topProductReports,
                chartTopProductReports,
                financialReports,
                totalVisitsData
        );
    }

    /**
     * Retrieves product IDs for a given merchant based on the financial report request.
     * <p>
     * If the request contains a non-empty list of product IDs, it verifies that all the provided
     * product IDs exist for the merchant (and that none are deleted). If any product is missing or
     * if the count does not match, a {@link VipoBusinessException} is thrown. Otherwise, if no product
     * IDs are specified in the request, all product IDs for the merchant are retrieved.
     * </p>
     *
     * @param request    The financial report request containing product ID filters (if any).
     * @param merchantId The merchant's identifier.
     * @return A list of product IDs for the merchant.
     * @throws VipoBusinessException if some product IDs are not found.
     */
    private List<Long> getProductIds(FinancialReportRequest request, Long merchantId) {
        log.info("[Financial getProductIds] Get product ids with request: {}, merchantId: {}", request, merchantId);
        List<Long> productIds = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(request.getProductIds())) {
            // Query products by the provided IDs for the merchant.
            List<ProductEntity> products = productRepository.findByIdInAndMerchantIdAndIsDeleted(request.getProductIds(), merchantId, 0);
            // Validate that all requested product IDs are found.
            if (products.isEmpty() || products.size() != request.getProductIds().size()) {
                throw new VipoBusinessException(BaseExceptionConstant.INVALID_DATA_REQUEST, FinancialConstant.PRODUCT_NOT_FOUND);
            }
            productIds = request.getProductIds();
        } else {
            // Retrieve all product IDs for the merchant if none are specified in the request.
            productIds = productRepository.findProductIdsByMerchantId(merchantId);
        }
        return productIds;
    }

    @Override
    public RevenueReportExportInfoResponse getExportRevenueReport(RevenueReportExportInfoRequest exportInfoRequest) {
        Long merchantId = getCurrentUser().getId();

        MerchantEntity merchantEntity = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new VipoNotFoundException("Merchant not found"));

        Map<String, Object> reportData = new HashMap<>();


        /* set customer info */
        reportData.put(
                "customerName",
                StringProcessingUtils.returnDefaultWhenNull(merchantEntity.getName(), Constants.NO_CUSTOMER_DATA_SHOWN)
        );
        reportData.put(
                "customerAddressDetail",
                StringProcessingUtils.returnDefaultWhenNull(merchantEntity.getAddress(), Constants.NO_CUSTOMER_DATA_SHOWN)
        );

        if (ObjectUtils.isNotEmpty(merchantEntity.getWardId())) {
            wardRepository.findDetailByMerchantId(Long.valueOf(merchantEntity.getWardId()))
                    .ifPresent(c -> {
                        reportData.put("customerWard", c.getCustomerWard());
                        reportData.put("customerDistrict", c.getCustomerDistrict());
                        reportData.put("customerProvince", c.getCustomerProvince());
                    });
        }
        if (ObjectUtils.isEmpty(reportData.get("customerWard"))) {
            reportData.put("customerWard", Constants.NO_CUSTOMER_DATA_SHOWN);
            reportData.put("customerDistrict", Constants.NO_CUSTOMER_DATA_SHOWN);
            reportData.put("customerProvince", Constants.NO_CUSTOMER_DATA_SHOWN);
        }

        /* set report period */
        LocalDateTime now = DateUtils.getCurrentLocalDateTime();
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedFirstDay = firstDayOfMonth.format(formatter);
        String formattedToday = now.format(formatter);
        reportData.put("reportStartDate", formattedFirstDay);
        reportData.put("reportEndDate", formattedToday);

        /* set payment card info */
        String customerAccount = null;
        if (StringUtils.isNotBlank(merchantEntity.getContactPhone())) {
            customerAccount = merchantEntity.getContactPhone();
        } else if (StringUtils.isNotBlank(merchantEntity.getContactEmail())) {
            customerAccount = merchantEntity.getContactEmail();
        }
        reportData.put(
                "customerAccount",
                StringProcessingUtils.returnDefaultWhenNull(customerAccount, Constants.NO_CUSTOMER_DATA_SHOWN)
        );

        merchantPaymentCardRepository
                .findFirstByMerchantIdOrderByIsDefaultDescUpdateTimeDesc(merchantId)
                .ifPresent(card -> {
                    reportData.put("customerBankAccountName", card.getAccountOwner());
                    reportData.put("customerBankAccount", card.getAccountNumber());
                    reportData.put("customerBankName", card.getBankCode());
                    reportData.put("customerBankBranchName", card.getBranch());
                });

        if (ObjectUtils.isEmpty(reportData.get("customerBankAccountName"))) {
            reportData.put("customerBankAccountName", Constants.NO_CUSTOMER_DATA_SHOWN);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankAccount"))) {
            reportData.put("customerBankAccount", Constants.NO_CUSTOMER_DATA_SHOWN);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankName"))) {
            reportData.put("customerBankName", Constants.NO_CUSTOMER_DATA_SHOWN);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankBranchName"))) {
            reportData.put("customerBankBranchName", Constants.NO_CUSTOMER_DATA_SHOWN);
        }

        JRSwapFileVirtualizer virtualizer = null;


        try (
                // 1) Get Connection (closes automatically when block ends)
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                // 2) Prepare Statement (closes automatically)
                PreparedStatement preparedStatement = connection.prepareStatement(
                        """
                        SELECT
                            @row_number := @row_number + 1 AS sequenceNum,
                            wr.updatedAt AS time,
                            op.orderCode AS orderPackageCode,
                            wri.withdrawAmount AS amount   
                        FROM withdrawal_request_item wri
                                 JOIN (SELECT @row_number := 0) init
                            LEFT JOIN withdrawal_request wr ON wri.withdrawalRequestId = wr.id
                            LEFT JOIN order_package op ON wri.packageId = op.id
                        WHERE 
                            wr.merchantId = ?
                            AND wr.status = 'SUCCESS'
                        ORDER BY wr.updatedAt DESC
                        """,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                )
        ) {
            /* set the icon */
            String iconPath = new ClassPathResource("images/vipo-icon.png")
                    .getFile().getAbsolutePath();
            reportData.put("vipoIconUrl", iconPath);

            // Enable MySQL streaming
            connection.setAutoCommit(false);

            // 3) Set parameter
            preparedStatement.setLong(1, 221);

            // 4) Execute Query (ResultSet also closes automatically in try-with-resources)
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                // Create a Jasper DataSource from our custom ResultSet
                JRDataSource dataSource = new WithdrawRequestJRResultSetDataSource(resultSet);

                // Create swap file & virtualizer
                JRSwapFile swapFile = new JRSwapFile("src/main/resources/tmp", 4096, 200);
                virtualizer = new JRSwapFileVirtualizer(50, swapFile, true);

                // Put the virtualizer & dataset into report parameters
                reportData.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
                reportData.put("transferedPaymentSumaryDataset", dataSource);

                // Compile, fill, and export the report
                JasperReport jasperReport = JasperCompileManager.compileReport(
                        "src/main/resources/templates/vipo-financial-report.jrxml"
                );

                JasperPrint jasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        reportData,
                        new JREmptyDataSource()
                );

                JasperExportManager.exportReportToPdfFile(
                        jasperPrint,
                        "src/main/resources/tmp/report.pdf"
                );
            }

        } catch (SQLException e) {
            throw new VipoFailedToExecuteException("Failed to execute query " + e.getLocalizedMessage());
        } catch (JRException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Clean up virtualizer manually
            if (virtualizer != null) {
                virtualizer.cleanup();
            }
        }

        // Return your response (currently null as per your original code)
        return null;
    }

    @Transactional
    @Override
    public RevenueReportExportInfoResponse getExportRevenueReportReal(RevenueReportExportInfoRequest exportInfoRequest) {
        Long merchantId = getCurrentUser().getId();

        /* get the revenue report */
        FinancialReportRequest request = sellerMapper.toFinancialReportRequest(exportInfoRequest);
        // Validate request
        FinancialValidator.validateFinancialReportRequest(request);
        // Create time range based on request
        TimeRange timeRange = PeriodCalculator.calculateTimeRange(request);
        LocalDate currentStartDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentStartSec()).toLocalDate();
        LocalDate currentEndDate = DateUtils.getLocalDateTimeFromEpochSecond(timeRange.getCurrentEndSec()).toLocalDate();
        PeriodType periodType = PeriodType.valueOf(request.getFilterType().toUpperCase());
        // Find report in current period
        RevenueReportEntity report = findReportByPeriod(
                merchantId,
                periodType,
                currentStartDate,
                currentEndDate
        ).orElseThrow(() -> new VipoNotFoundException("Report not found"));

        /* validate the number of order package */
        Long startEpochSecond = DateUtils.convertLocalDateToEpochSeconds(report.getPeriodStart());
        Long endEpochSecond = DateUtils.convertLocalDateToEpochSeconds(report.getPeriodEnd());
        validateRevenueReportExportRequest(merchantId, startEpochSecond, endEpochSecond);

        /* Get the revenue_report_export:
        - find the nearest export that has the nearest createdAtTime and has the status completed and pending (the
        completed status has the priority)
         - if the report is not in pending or completed status, then create an export request
         - if the report is pending and the createdAtTime is over the maximumFinancialStatementExportDuration, then
            create an export request
         - else return
         */
        Optional<RevenueReportExportEntity> exportEntityOptional
                = revenueReportExportEntityRepository.findFirstByReportIdAndPeriodStartAndPeriodEndAndStatusInOrderByCreatedAtDescStatusAsc(
                report.getId(), report.getPeriodStart(), report.getPeriodEnd() , Arrays.asList(FinancialExportStatus.COMPLETED, FinancialExportStatus.PENDING)
        );
        RevenueReportExportEntity exportEntity = null;
        if (exportEntityOptional.isPresent()) {
            exportEntity = exportEntityOptional.get();
            if (
                    (
                            exportEntity.getStatus() == FinancialExportStatus.COMPLETED
                                    || exportEntity.getCreatedAt().plus(maximumFinancialStatementExportDuration)
                                    .isAfter(LocalDateTime.now())
                    )
                    &&
                    (
                            ObjectUtils.isNotEmpty(exportEntity.getPeriodEnd())
                                    && (
                                    exportEntity.getPeriodEnd().isEqual(report.getPeriodEnd())
                                            || exportEntity.getPeriodEnd().isAfter(report.getPeriodEnd())
                            )
                    )
            ) {
                return RevenueReportExportInfoResponse.builder()
                        .reportType(FinancialReportExportType.PDF)
                        .status(exportEntity.getStatus())
                        .reportUrl(exportEntity.getFilePath())
                        .build();
            }
        }

        if (ObjectUtils.isEmpty(exportEntity) || exportEntity.getStatus() == FinancialExportStatus.FAILED)
            exportEntity = RevenueReportExportEntity.builder()
                    .reportId(report.getId())
                    .reportType(FinancialReportType.PDF)
                    .reportName("Báo cáo doanh thu " + report.getId()) //todo: replace with more meaningful name
                    .status(FinancialExportStatus.PENDING)
                    .periodStart(report.getPeriodStart())
                    .periodEnd(report.getPeriodEnd())
                    .build();

        if (ObjectUtils.isEmpty(exportEntity.getId())) {
            exportEntity = revenueReportExportEntityRepository.save(exportEntity);
        }

        kafkaTemplate.send(
                kafkaTopicConfig.getRevenueReportExportTopicName()
                , JsonUtils.toJson(
                        new MessageData(
                                RevenueReportExportMsg.builder()
                                        .revenueReportExportId(exportEntity.getId())
                                        .financialReportRequest(request)
                                        .version(ApiVersionUtils.getCurrentVersion().getVersionCode())
                                        .build()
                        )
                )
        );

        return RevenueReportExportInfoResponse.builder()
                .reportType(FinancialReportExportType.PDF)
                .status(exportEntity.getStatus())
                .reportUrl(exportEntity.getFilePath())
                .build();
    }

    private void validateRevenueReportExportRequest(Long merchantId, Long startEpochSecond, Long endEpochSecond) {

        /* limit the order package rows num at 1M */
        Long totalOrder
                = orderPackageRepository.countFinishedOrderPackage(
                        merchantId, startEpochSecond, endEpochSecond
        );

        if (totalOrder > maximumExportOrderPackageNum) {
            throw new VipoInvalidDataRequestException(Constants.REVENUE_REPORT_EXCEED_ORDER_PACKAGE_LIMIT);
        }

        /* limit the withdrawal_request_item at 1M */
        Long totalWithdrawalRequestItem = withdrawalRequestItemRepository.countWithdrawalRequestItem(
                merchantId, startEpochSecond, endEpochSecond
        );
        if (totalWithdrawalRequestItem > maximumWithdrawalItemNum) {
            throw new VipoInvalidDataRequestException(Constants.REVENUE_REPORT_EXCEED_ORDER_PACKAGE_LIMIT);
        }

    }

    /**
     * Export the revenue report the pdf and upload public link to the storage
     */
    @Override
    public void exportRevenueReport(RevenueReportExportMsg content) {

        validateRevenueReportExport(content);

        /* get the revenue report export entity */
        RevenueReportExportEntity exportEntity
                = revenueReportExportEntityRepository.findById(content.getRevenueReportExportId())
                .orElseThrow(() -> new VipoInvalidDataRequestException("Not found revenue_report_export.id"));
        RevenueReportEntity report
                = revenueReportEntityRepository.findById(exportEntity.getReportId())
                .orElseThrow(() -> new VipoInvalidDataRequestException("Report not found"));

        /* Export the revenue report */
        // find the merchant
        long merchantId = report.getMerchantId();
        MerchantEntity merchantEntity = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new VipoNotFoundException("Merchant not found"));
        Map<String, Object> reportData = new HashMap<>();

        /* set customer info */
        reportData.put(
                "customerName",
                StringProcessingUtils.returnDefaultWhenNull(merchantEntity.getName(), Constants.NO_CUSTOMER_DATA_SHOWN)
        );
        reportData.put(
                "customerAddressDetail",
                StringProcessingUtils.returnDefaultWhenNull(merchantEntity.getAddress(), Constants.NO_CUSTOMER_DATA_SHOWN)
        );

        if (ObjectUtils.isNotEmpty(merchantEntity.getWardId())) {
            wardRepository.findDetailByMerchantId(merchantId)
                    .ifPresent(c -> {
                        if (StringUtils.isNotBlank(c.getCustomerWard()))
                            reportData.put(
                                    "customerWard",
                                    StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(
                                            c.getCustomerWard()
                                    )
                            );
                        if (StringUtils.isNotBlank(c.getCustomerDistrict()))
                            reportData.put(
                                    "customerDistrict",
                                    StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(
                                            c.getCustomerDistrict()
                                    )
                            );

                        if (StringUtils.isNotBlank(c.getCustomerProvince()))
                            reportData.put(
                                    "customerProvince",
                                    StringProcessingUtils.capitalizeUperCaseFirstLetterEachWord(
                                            c.getCustomerProvince()
                                    )
                            );
                    });
        }

        if (ObjectUtils.isEmpty(reportData.get("customerWard"))) {
            reportData.put("customerWard", Constants.NO_CUSTOMER_DATA_SHOWN);
            reportData.put("customerDistrict", Constants.NO_CUSTOMER_DATA_SHOWN);
            reportData.put("customerProvince", Constants.NO_CUSTOMER_DATA_SHOWN);
        }

        // set report period
        LocalDate endDate = exportEntity.getPeriodEnd();
        LocalDate startDate = exportEntity.getPeriodStart();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedStartDay = startDate.format(formatter);
        String formattedEndDay = endDate.format(formatter);
        reportData.put("reportStartDate", formattedStartDay);
        reportData.put("reportEndDate", formattedEndDay);
        //convert the period to epoch second
        ZoneId systemZone = ZoneId.systemDefault();
        long startEpochSecond = startDate.atStartOfDay(systemZone).toEpochSecond();
        ZonedDateTime endOfDay = endDate.atTime(LocalTime.MAX).atZone(systemZone);
        long endEpochSecond = endOfDay.toEpochSecond();

        /* set payment card info */
        String customerAccount = "";
        if (StringUtils.isNotBlank(merchantEntity.getContactPhone())) {
            customerAccount = merchantEntity.getContactPhone();
        } else if (StringUtils.isNotBlank(merchantEntity.getContactEmail())) {
            customerAccount = merchantEntity.getContactEmail();
        }
        reportData.put(
                "customerAccount",
                StringProcessingUtils.returnDefaultWhenNull(customerAccount, Constants.TEMPORARY_NO_DATA_PLACEHOLDER)
        );

        boolean isEContractEnabled
                = StringUtils.isNotBlank(content.getVersion())
                && ApiVersion.getVersionFromCode(content.getVersion()).isVersionAtLeast(ApiVersion.V1_3_0);

        if (isEContractEnabled) {
            MerchantNewEntity merchantNew = merchantNewRepository.findById(merchantId)
                    .orElseThrow(() -> new VipoNotFoundException("Merchant not found"));
            reportData.put("customerBankAccountName", merchantNew.getBankOwner());
            reportData.put("customerBankAccount", merchantNew.getBankNumber());
            reportData.put("customerBankName", merchantNew.getBankCode());
            reportData.put("customerBankBranchName", merchantNew.getBankBranch());
        } else {
            merchantPaymentCardRepository
                    .findFirstByMerchantIdOrderByIsDefaultDescUpdateTimeDesc(merchantId)
                    .ifPresent(card -> {
                        reportData.put("customerBankAccountName", card.getAccountOwner());
                        reportData.put("customerBankAccount", card.getAccountNumber());
                        reportData.put("customerBankName", card.getBankCode());
                        reportData.put("customerBankBranchName", card.getBranch());
                    });
        }


        if (ObjectUtils.isEmpty(reportData.get("customerBankAccountName"))) {
            reportData.put("customerBankAccountName", Constants.TEMPORARY_NO_DATA_PLACEHOLDER);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankAccount"))) {
            reportData.put("customerBankAccount", Constants.TEMPORARY_NO_DATA_PLACEHOLDER);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankName"))) {
            reportData.put("customerBankName", Constants.TEMPORARY_NO_DATA_PLACEHOLDER);
        }
        if (ObjectUtils.isEmpty(reportData.get("customerBankBranchName"))) {
            reportData.put("customerBankBranchName", Constants.TEMPORARY_NO_DATA_PLACEHOLDER);
        }

        /* set the icon */

//        try (InputStream iconStream = new ClassPathResource("images/vipo-icon.png").getInputStream()) {
//            // Tiếp tục xử lý với iconStream
//            iconPath = getFilePath(iconStream);
//            // Nếu bạn cần tải lên một dịch vụ hoặc lưu vào đâu đó, bạn có thể đọc từ InputStream
//        } catch (IOException e) {
//            throw new VipoInvalidDataRequestException(e.getLocalizedMessage());
//        }

        InputStream iconStream = null;
        try {
            iconStream = new ClassPathResource("images/vipo-icon.png").getInputStream();
            reportData.put("vipoIconUrl", iconStream);
        } catch (IOException e) {
            throw new VipoInvalidDataRequestException(e.getLocalizedMessage());
        }

        /* get the financial report info */
        FinancialReportRequest request = content.getFinancialReportRequest();
        TimeRange timeRange = buildTimeRange(request);
        FinancialReportResponse response = aggregateRevenueService.aggregateRevenueData(request, merchantId, timeRange);

        /* chart data */
        //tổng doanh thu sản phẩm
        BigDecimal totalProductRevenue = response.getChartData().getTotalRevenue();
        if (ObjectUtils.isNotEmpty(totalProductRevenue)) {
            reportData.put(
                    "productRevenueSum",
                    NumUtils.formatBigDecimalToVNDFormat(totalProductRevenue) + " đ"
            );
        }

        //pie chart
        List<FinancialReportResponse.ChartDataItem> chartData = response.getChartData().getData();
        reportData.put(
                "productSumaryDataset", new JRBeanCollectionDataSource(chartData.stream()
                        .map(product -> new ProductSumaryDTO(
                                StringProcessingUtils.trimLongString(product.getLabel(), 25),
                                product.getPercentage().doubleValue()))
                        .toList())

        );

        /* top product */
        reportData.put("hasProductData", ObjectUtils.isNotEmpty(response.getTopProductReport()));
//        reportData.put(
//                "topProductDataset",
//                new JRBeanCollectionDataSource(response.getTopProductReport().stream().map(
//                        topProduct -> TopProductDTO.builder()
//                                .sequenceNum(topProduct.getRanking())
//                                .productName(topProduct.getProductName())
//                                .quantity(topProduct.getQuantitySold())
//                                .originalPrice(
//                                        ObjectUtils.isNotEmpty(topProduct.getRevenueBeforeNegotiation()) ?
//                                         topProduct.getRevenueBeforeNegotiation()
//                                                 .setScale(0, RoundingMode.HALF_UP).toString()
//                                                : "0"
//                                )
//                                .negotiatedPrice(
//                                        NumUtils.minusBigDecimals(
//                                                topProduct.getRevenueBeforeNegotiation(), topProduct.getRevenueAfterNegotiation()
//                                        ).setScale(0, RoundingMode.HALF_UP).toString()
//                                )
//                                .finalPrice(
//                                        ObjectUtils.isNotEmpty(topProduct.getRevenueAfterNegotiation()) ?
//                                        topProduct.getRevenueAfterNegotiation()
//                                                .setScale(0, RoundingMode.HALF_UP).toString()
//                                                : "0"
//                                )
//                                .revenue(
//                                        ObjectUtils.isNotEmpty(topProduct.getRevenueAfterNegotiation()) ?
//                                                topProduct.getRevenueAfterNegotiation()
//                                                        .setScale(0, RoundingMode.HALF_UP).toString()
//                                                : "0"
//                                )
//                                .revenuePercentage(topProduct.getRevenuePercentage().toString() + " %")
//                                .build()
//                ).toList())
//        );

        if (ObjectUtils.isNotEmpty(response.getTopProductReport())) {
            List<TopProductDTO> topProducts = new ArrayList<>();
            for (FinancialReportResponse.TopProduct topProduct : response.getTopProductReport()) {
                /* the product info */
                topProducts.add(
                        TopProductDTO.builder()
                                .sequenceNum(String.valueOf(topProduct.getRanking()))
                                .productName(
                                        topProduct.getProductName() + "\n" + "Mã sản phẩm: " + topProduct.getProductCode()
                                )
                                .quantity(topProduct.getQuantitySold())
                                .originalPrice(
                                        ObjectUtils.isNotEmpty(topProduct.getRevenueBeforeNegotiation()) ?
                                                NumUtils.formatBigDecimalToVNDFormat(
                                                        topProduct.getRevenueBeforeNegotiation()
                                                ) + " đ" : "0 đ"
                                )
                                .finalPrice(
                                        ObjectUtils.isNotEmpty(topProduct.getRevenueAfterNegotiation()) ?
                                                NumUtils.formatBigDecimalToVNDFormat(topProduct.getRevenueAfterNegotiation()) + " đ"
                                                : "0 đ"
                                )
                                .revenuePercentage(topProduct.getRevenuePercentage().toString() + " %")
                                .build()
                );
                if (ObjectUtils.isNotEmpty(topProduct.getSkus())) {
                    topProducts.addAll(
                            topProduct.getSkus().stream().map(
                                    sku -> TopProductDTO.builder()
                                            .productName(sku.getLabel())
                                            .quantity(sku.getQuantitySold())
                                            .originalPrice(
                                                    ObjectUtils.isNotEmpty(sku.getRevenueBeforeNegotiation()) ?
                                                            NumUtils.formatBigDecimalToVNDFormat(sku.getRevenueBeforeNegotiation()) + " đ"
                                                            : "0 đ"
                                            )
                                            .finalPrice(
                                                    ObjectUtils.isNotEmpty(sku.getRevenueAfterNegotiation()) ?
                                                            NumUtils.formatBigDecimalToVNDFormat(sku.getRevenueAfterNegotiation()) + " đ"
                                                            : "0 đ"
                                            )
                                            .build()
                            ).toList()
                    );
                }
            }

            reportData.put("topProductDataset", new JRBeanCollectionDataSource(topProducts));
        }

        // orderPackageDataset : Báo cáo thu thập
//        reportData.put("orderPackageDataset", new JRBeanCollectionDataSource(getOrderHaveRequest(merchantId, startEpochSecond, endEpochSecond)));

        //Số đơn
        Long totalOrder = orderPackageRepository.countFinishedOrderPackage(merchantId, startEpochSecond, endEpochSecond);
        reportData.put("orderPackageQuantity", totalOrder);
        reportData.put("hasOrderData", ObjectUtils.isNotEmpty(totalOrder) && totalOrder > 0);

        //Tổng kết thanh toán đã chuyển
        BigDecimal totalTransferedPayment
                = withdrawalRequestRepository.sumTotalTransferedPayment(merchantId, startEpochSecond, endEpochSecond);
        reportData.put("hasWithdrawal", ObjectUtils.isNotEmpty(totalTransferedPayment) && totalTransferedPayment.compareTo(BigDecimal.ZERO) > 0);
        reportData.put(
                "totalCompletePaymentAmount",
                ObjectUtils.isNotEmpty(totalTransferedPayment) ?
                        NumUtils.formatBigDecimalToVNDFormat(totalTransferedPayment)
                        : ""
        );

        //export to pdf
        JRSwapFileVirtualizer virtualizer = null;
        Path tempFile = null;
        try (
                // 1) Get Connection (closes automatically when block ends)
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                // 2) Prepare Statement (closes automatically)
                PreparedStatement withdrawRequestPreparedStatement = connection.prepareStatement(
                        """
                                SELECT @row_number := @row_number + 1 AS sequenceNum,
                                       wr.withdrawSuccessTime         AS time,
                                       op.orderCode                   AS orderPackageCode,
                                       wri.withdrawableAmount         AS amount
                                FROM withdrawal_request_item wri
                                         JOIN (SELECT @row_number := 0) init
                                         LEFT JOIN withdrawal_request wr ON wri.withdrawalRequestId = wr.id
                                         LEFT JOIN order_package op ON wri.packageId = op.id
                                WHERE wr.merchantId = ?
                                  AND wr.status = 'SUCCESS'
                                  AND wr.withdrawSuccessTime >= ?
                                  AND wr.withdrawSuccessTime <= ?
                                ORDER BY wr.updatedAt DESC
                                """,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                );

                //todo: check if this query is optimal
                PreparedStatement finishedOrderPackagePreparedStatement = connection.prepareStatement(
                        """
                                select *
                                from (select @row_number := @row_number + 1                                                AS sequenceNum,
                                             op.orderCode                                                                  as orderPackageCode,
                                             IF(
                                                     (op.deliverySuccessTime + wc.withdrawAfterSecond) >= ? and (op.deliverySuccessTime + wc.withdrawAfterSecond) <= ?,
                                                     (op.deliverySuccessTime + wc.withdrawAfterSecond),
                                                     op.deliverySuccessTime
                                             ) as time,
                                             op.price - coalesce(sum(pfd.feeValue), 0) -
                                             coalesce(sum(pp.sellerPlatformDiscountAmount), 0)                             as amount,
                                             IF(
                                                     (op.deliverySuccessTime + wc.withdrawAfterSecond) >= ? and (op.deliverySuccessTime + wc.withdrawAfterSecond) <= ?,
                                                     'Đơn có thể rút',
                                                     'Đã thành công'
                                             )                                                           as status
                                      from order_package op
                                               JOIN (SELECT @row_number := 0) init
                                               join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount,
                                                            ppd.packageId
                                                     from package_product ppd
                                                     group by ppd.packageId) pp on pp.packageId = op.id
                                               join merchant m on op.merchantId = m.id
                                               left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                                               left join platform_fee_detail pfd on pfd.packageId = op.id
                                      where op.id not in (select wri.packageId
                                                          from withdrawal_request_item wri
                                                                   join withdrawal_request wr on wr.id = wri.withdrawalRequestId
                                                          where wr.merchantId = ?
                                                            and wr.status in ('PENDING', 'PROCESSING', 'APPROVED', 'SUCCESS')
                                                          group by wri.packageId)
                                        and op.merchantId = ?
                                        and op.orderStatus = '501'
                                        and ((op.deliverySuccessTime >= ? and op.deliverySuccessTime <= ?)
                                          or ((op.deliverySuccessTime + wc.withdrawAfterSecond) >= ? and
                                              (op.deliverySuccessTime + wc.withdrawAfterSecond) <= ?))
                                      group by op.id) as t
                                order by time desc
                                """,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                )
        ) {

            /* Withdraw Request */
            // Enable MySQL streaming
            connection.setAutoCommit(false);
            // 3) Set parameter
            withdrawRequestPreparedStatement.setLong(1, merchantId);
            withdrawRequestPreparedStatement.setLong(2, startEpochSecond);
            withdrawRequestPreparedStatement.setLong(3, endEpochSecond);

            finishedOrderPackagePreparedStatement.setLong(1, startEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(2, endEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(3, startEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(4, endEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(5, merchantId);
            finishedOrderPackagePreparedStatement.setLong(6, merchantId);
            finishedOrderPackagePreparedStatement.setLong(7, startEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(8, endEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(9, startEpochSecond);
            finishedOrderPackagePreparedStatement.setLong(10, endEpochSecond);
//            finishedOrderPackagePreparedStatement.setLong(9, startEpochSecond);

            // 4) Execute Query (ResultSet also closes automatically in try-with-resources)
            try (
                    ResultSet withdrawRequestResultSet = withdrawRequestPreparedStatement.executeQuery();
                    ResultSet orderPackageResultSet = finishedOrderPackagePreparedStatement.executeQuery();
//                    InputStream reportStream
//                            = getClass().getResourceAsStream("/templates/vipo-financial-report.jrxml")
                    InputStream reportStream
                            = getClass().getResourceAsStream("/templates/vipo-financial-report.jasper")
            ) {
                log.info("[Financial export PDF Report Revenue] get template file: {}", reportStream);

                // Create a Jasper DataSource from our custom ResultSet
                JRDataSource dataSource = new WithdrawRequestJRResultSetDataSource(withdrawRequestResultSet);
                JRDataSource orderPackageDataSource = new SuccessOrderPackageJRRsultSetDataSource(orderPackageResultSet);
                // Create swap file & virtualizer
                String systemTempDir = System.getProperty("java.io.tmpdir");
                JRSwapFile swapFile = new JRSwapFile(systemTempDir, 4096, 200);
                virtualizer = new JRSwapFileVirtualizer(50, swapFile, true);

                // Put the virtualizer & dataset into report parameters
                reportData.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
                reportData.put("transferedPaymentSumaryDataset", dataSource);
                reportData.put("orderPackageDataset", orderPackageDataSource);
//                reportData.put("transferedPaymentSumaryDataset", new JRBeanCollectionDataSource(getOrderTranferred(merchantId, startEpochSecond, endEpochSecond)));

//                JasperReport jasperReport = null;
//
//                try {
//                    // Compile, fill, and export the report
//                    log.info("[Financial export PDF Report Revenue] compile report: {}", reportStream);
//                     jasperReport = JasperCompileManager.compileReport(reportStream);
//                } catch (Exception e) {
//                    throw new VipoFailedToExecuteException(e.getLocalizedMessage());
//                }

                log.info("[Financial export PDF Report Revenue] fill report data: {}", reportData);
//                JasperPrint jasperPrint = JasperFillManager.fillReport(
//                        jasperReport,
//                        reportData,
//                        new JREmptyDataSource()
//                );
                JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, reportData, new JREmptyDataSource());
                log.info("[Financial export PDF Report Revenue] export report to pdf");
                tempFile = Files.createTempFile("report-", ".pdf");
                JasperExportManager.exportReportToPdfFile(jasperPrint, tempFile.toString());
                log.info("[Financial export PDF Report Revenue] upload s3");
                /* Upload to S3 */
                String s3Key = revenueExportPrefix + merchantId
                        + "_" + formattedStartDay
                        + "_" + formattedEndDay
                        + "_" + exportEntity.getId();

                String excelFileS3URL
                        = amazonS3Service.uploadFilePublic(tempFile, vipoBucketName, s3Key);

                exportEntity.setFilePath(excelFileS3URL);
                exportEntity.setStorageType(FileStorageMethodEnum.S3.name());
                exportEntity.setStorageInfo(
                        JsonMapperUtils.writeValueAsString(
                                S3StorageLocation.builder().bucket(vipoBucketName).key(s3Key).build()
                        )
                );
                exportEntity.setExportTime(DateUtils.getCurrentLocalDateTime());
                exportEntity.setStatus(FinancialExportStatus.COMPLETED);
                revenueReportExportEntityRepository.save(exportEntity);
            } catch (IOException e) {
                throw new VipoFailedToExecuteException(e.getLocalizedMessage());
            }

        } catch (SQLException e) {
            throw new VipoFailedToExecuteException("Failed to execute query " + e.getLocalizedMessage());
        } catch (JRException e) {
            throw new RuntimeException(e);
        } finally {
            if (iconStream != null) {
                try {
                    iconStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // Clean up virtualizer manually
            if (virtualizer != null) {
                virtualizer.cleanup();
            }

            /* Delete the temp file */
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                    log.info("Temporary file deleted: {}", tempFile);
                } catch (IOException e) {
                    // Log error if deletion fails.
                    log.warn("Failed to delete temporary file: {}", tempFile, e);
                }
            }
        }
    }

    public Collection<OrderReportDTO> getOrderHaveRequest(long merchantId, long startDate, long endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                select 
                @row_number := @row_number + 1 AS sequenceNum,
                op.orderCode                                                                                  as orderPackageCode,
                       DATE_FORMAT(FROM_UNIXTIME((op.deliverySuccessTime + wc.withdrawAfterSecond)), '%d/%m/%Y')     as time,
                       op.price - coalesce(sum(pfd.feeValue), 0) - coalesce(sum(pp.sellerPlatformDiscountAmount), 0) as amount,
                       IF(wri.packageId is not null, 'Đã thành công', 'Đơn có thể rút')                              as status
                from order_package op
                JOIN (SELECT @row_number := 0) init
                         join (select sum(ppd.sellerPlatformDiscountAmount) as sellerPlatformDiscountAmount, ppd.packageId
                               from package_product ppd
                               group by ppd.packageId) pp on pp.packageId = op.id
                         join merchant m on op.merchantId = m.id
                         left join withdrawal_config wc on wc.merchantGroupId = m.merchantGroupId
                         left join platform_fee_detail pfd on pfd.packageId = op.id
                         left join (select wri.packageId
                                    from withdrawal_request_item wri
                                             join withdrawal_request wr on wr.id = wri.withdrawalRequestId
                                    where wri.createdBy = :merchantId
                                      and wr.status = 'SUCCESS'
                                    group by wri.packageId) wri on wri.packageId = op.id
                
                where op.merchantId = :merchantId
                  and op.orderStatus = '501'
                  and from_unixtime((op.deliverySuccessTime + wc.withdrawAfterSecond)) <= current_timestamp
                  and (:startTime is null or (op.deliverySuccessTime + wc.withdrawAfterSecond) >= :startTime)
                  and (:endTime is null or (op.deliverySuccessTime + wc.withdrawAfterSecond) <= :endTime)
                group by op.id, (op.deliverySuccessTime + wc.withdrawAfterSecond)
                order by (op.deliverySuccessTime + wc.withdrawAfterSecond) desc
                """);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("merchantId", merchantId);
        parameters.put("startTime", startDate);
        parameters.put("endTime", endDate);

        jdbcTemplateNameParams.query(sql.toString(), parameters, new DataClassRowMapper<>(OrderReportDTO.class));

        return jdbcTemplateNameParams.query(sql.toString(), parameters, new DataClassRowMapper<>(OrderReportDTO.class));
    }

    public Collection<OrderReportDTO> getOrderTranferred(long merchantId, long startDate, long endDate) {
        StringBuilder sql = new StringBuilder();
        sql.append("""
                SELECT
                 @row_number := @row_number + 1 AS sequenceNum,
                            wr.withdrawSuccessTime AS time,
                            op.orderCode AS orderPackageCode,
                            wri.withdrawableAmount AS amount   
                        FROM withdrawal_request_item wri
                                 JOIN (SELECT @row_number := 0) init
                            LEFT JOIN withdrawal_request wr ON wri.withdrawalRequestId = wr.id
                            LEFT JOIN order_package op ON wri.packageId = op.id
                        WHERE 
                            wr.merchantId = :merchantId
                            AND wr.status = 'SUCCESS'
                            AND wr.withdrawSuccessTime >= :startTime
                            AND wr.withdrawSuccessTime <= :endTime
                        ORDER BY wr.updatedAt DESC
                """);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("merchantId", merchantId);
        parameters.put("startTime", startDate);
        parameters.put("endTime", endDate);

        return jdbcTemplateNameParams.query(sql.toString(), parameters, new DataClassRowMapper<>(OrderReportDTO.class));
    }

    private void validateRevenueReportExport(RevenueReportExportMsg content) {
        if (ObjectUtils.isEmpty(content) || ObjectUtils.isEmpty(content.getRevenueReportExportId()))
            throw new VipoInvalidDataRequestException("Not found revenue_report_export.id");
        if (ObjectUtils.isEmpty(content.getFinancialReportRequest()))
            throw new VipoInvalidDataRequestException("Not found financial_report_request");
    }

    private String getFilePath(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("vipo_icon_", ".png");
        try(OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new VipoInvalidDataRequestException(e.getLocalizedMessage());
        }
        return tempFile.getAbsolutePath();  // Đường dẫn tệp tạm thời
    }
}
