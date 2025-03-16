package com.vtp.vipo.seller.common.dto.response.withdrawalrequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExportWithdrawalRequestListResponse {

    String fileName;

    ContentDisposition contentDisposition;

    String fallbackFileName;

    String contentType;

    Long contentLength;

    byte[] resource;


}
