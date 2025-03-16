package com.vtp.vipo.seller.services.certificate;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dto.response.UploadCertificateResponse;
import com.vtp.vipo.seller.common.utils.FileUtils;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileValidationService {
    public ValidationResult validateFiles(List<MultipartFile> files) {
        List<UploadCertificateResponse.FailData> failList = new ArrayList<>();
        List<MultipartFile> validFiles = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            List<String> errors = validateFile(file);

            if (errors.isEmpty()) {
                validFiles.add(file);
            } else {
                failList.add(UploadCertificateResponse.FailData.builder()
                        .index(i)
                        .name(getFileName(file))
                        .message(errors)
                        .build());
            }
        }

        return new ValidationResult(validFiles, failList);
    }

    private List<String> validateFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (ObjectUtils.isEmpty(file)) {
            errors.add(Constants.FILE_NOT_EXIST);
            return errors;
        }

        if (!FileUtils.isValidFileExtension(file)) {
            errors.add(Constants.FILE_INVALID_EXTENSION);
        }

        if (!FileUtils.isFileSizeAllowed(file, Constants.MAX_UPLOAD_CERTIFICATE_SIZE_MB)) {
            errors.add(Constants.FILE_EXCEED_SIZE);
        }

        return errors;
    }

    private String getFileName(MultipartFile file) {
        return (ObjectUtils.isEmpty(file) || ObjectUtils.isEmpty(file.getOriginalFilename())) ? Constants.UNKNOWN_FILE : file.getOriginalFilename();
    }

    // Inner class to hold validation results
    public record ValidationResult(List<MultipartFile> validFiles, List<UploadCertificateResponse.FailData> failList) {
    }
}
