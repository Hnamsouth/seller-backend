package com.vtp.vipo.seller.services.certificate.impl;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.CustomMultipartFile;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.services.AmazonS3Service;
import com.vtp.vipo.seller.services.certificate.PDFConversionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PDFConversionServiceImpl implements PDFConversionService {
    AmazonS3Service amazonS3Service;

    @Override
    @Async("taskExecutor")
    public CompletableFuture<List<StorageInfoDTO>> extractPdfToImages(MultipartFile pdfFile) {
        List<StorageInfoDTO> storageInfoDTOS = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(pageImage, "png", os);
                byte[] imageBytes = os.toByteArray();

                String imageFileName = "pdf_page_" + (pageIndex + 1) + ".png";
                MultipartFile imageMultipartFile = new CustomMultipartFile(
                        imageFileName,
                        imageFileName,
                        "image/png",
                        imageBytes
                );

                StorageInfoDTO storageInfoDTO = amazonS3Service.uploadFile(imageMultipartFile);
                storageInfoDTOS.add(storageInfoDTO);
            }
        } catch (IOException e) {
            log.error("Error converting PDF to images: {}", pdfFile.getOriginalFilename(), e);
            throw new VipoBusinessException(BaseExceptionConstant.FAILED_TO_EXECUTE,
                    "Failed to convert PDF to images: " + pdfFile.getOriginalFilename());
        }

        return CompletableFuture.completedFuture(storageInfoDTOS);
    }
}
