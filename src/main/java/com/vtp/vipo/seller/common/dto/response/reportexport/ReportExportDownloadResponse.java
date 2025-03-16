package com.vtp.vipo.seller.common.dto.response.reportexport;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;

@Builder
@Data
public class ReportExportDownloadResponse {

    String fileName;

    String contentDispositionValue;

    ContentDisposition contentDisposition;

    String fallbackFileName;

    String contentType;

    Long contentLength;

    InputStreamResource resource;

}
