package com.vtp.vipo.seller.services.certificate;

import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PDFConversionService {
    CompletableFuture<List<StorageInfoDTO>> extractPdfToImages(MultipartFile pdfFile);
}
