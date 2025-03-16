package com.vtp.vipo.seller.common.dto.response;

import java.sql.Timestamp;

/**
 * Interface representing a report history record.
 * Provides methods to retrieve information about a specific report, including its ID, type, name,
 * export time, status, download URL, and any error messages associated with it.
 */
public interface ReportHistoryProjection {

    /**
     * @return The unique ID of the report
     */
    String getReportId();

    /**
     * @return The type of the report follwing by {@link com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus}
     */
    String getReportType();

    /**
     * @return A description of the report type
     */
    String getReportTypeDesc();

    /**
     * @return The name of the report
     */
    String getReportName();

    /**
     * @return The timestamp when the report was exported
     */
    Long getExportTime();

    /**
     * @return The status of the report {@link com.vtp.vipo.seller.common.dao.entity.enums.ReportExportStatus}
     */
    String getStatus();

    /**
     * @return The URL where the report can be downloaded
     */
    String getDownloadUrl();

    /**
     * @return The error message, if any, associated with the report generation or export
     */
    String getErrorMessage();

}

