package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.response.ValidateFileResponse;
import com.vtp.vipo.seller.common.enumseller.FileExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileUtils {

    public static File convert(MultipartFile file) throws IOException {
        File convFile = new File("hubsub_file", file.getOriginalFilename());
        if (!convFile.getParentFile().exists()) {
            log.info("mkdir: {}", convFile.getParentFile().mkdirs());
        }
        FileOutputStream fos = null;

        try {
            if (!convFile.createNewFile()) {
                log.error("Fail to create file!");
            }
            fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            assert fos != null;
            fos.close();
        }

        return convFile;
    }

    public static ValidateFileResponse checkFile(MultipartFile file, boolean isUploadExcel) {

        ValidateFileResponse validateFileResponse = new ValidateFileResponse();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        validateFileResponse.setFileName(originalFilename);
        validateFileResponse.setExtension(fileExtension);
        validateFileResponse.setError(false);

        if (file.isEmpty()) {
            validateFileResponse.setError(true);
            validateFileResponse.setMessage(BaseExceptionConstant.FILE_EMPTY_DESCRIPTION);
            return validateFileResponse;
        }
        List<String> acceptedImageExtensions;
        if (!isUploadExcel) {
            acceptedImageExtensions = new ArrayList<>(
                    Arrays.asList("jpg", "jpeg", "png", "pdf", "mp4"));
        } else {
            acceptedImageExtensions = new ArrayList<>(
                    Arrays.asList("xlsx", "xls"));
        }

        if (!acceptedImageExtensions.contains(fileExtension)) {
            validateFileResponse.setError(true);
            validateFileResponse.setMessage(BaseExceptionConstant.FILE_WRONG_FORMAT);
            return validateFileResponse;
        }

        if (file.getSize() > 5 * 1024 * 1024 && !fileExtension.equalsIgnoreCase("mp4")) { // max size: 5M
            validateFileResponse.setError(true);
            validateFileResponse.setMessage(BaseExceptionConstant.FILE_EXCEED_CAPACITY_ALLOWED);
            return validateFileResponse;
        }

        if (fileExtension.equalsIgnoreCase("mp4") && file.getSize() > 25 * 1024 * 1024) {
            validateFileResponse.setError(true);
            validateFileResponse.setMessage(BaseExceptionConstant.FILE_EXCEED_CAPACITY_ALLOWED);
            return validateFileResponse;
        }

        return validateFileResponse;
    }

    public static String fullyReadFileFromClassPath(String filePath) throws IOException {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Validates if the uploaded file has an allowed extension.
     *
     * @param file The {@link MultipartFile} to validate.
     * @return {@code true} if the file has a valid extension; {@code false} otherwise.
     */
    public static boolean isValidFileExtension(MultipartFile file) {
        if (ObjectUtils.isEmpty(file) || ObjectUtils.isEmpty(file.getOriginalFilename())) {
            return false;
        }

        String extension = getFileExtension(file.getOriginalFilename());
        return FileExtension.isValidExtension(extension);
    }

    /**
     * Retrieves the file extension from the given filename.
     *
     * @param filename The name of the file.
     * @return The file extension without the dot, or an empty string if none exists.
     */
    public static String getFileExtension(String filename) {
        if (ObjectUtils.isEmpty(filename)) {
            return "";
        }

        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1 || lastIndexOfDot == filename.length() - 1) {
            return ""; // No extension found
        }

        return filename.substring(lastIndexOfDot + 1);
    }

    /**
     * Retrieves the size of the file in megabytes (MB).
     *
     * @param file The {@link MultipartFile} whose size is to be determined.
     * @return The size of the file in MB, or {@code -1} if the file is empty or null.
     */
    public static double getFileSizeInMB(MultipartFile file) {
        if (ObjectUtils.isEmpty(file) || file.isEmpty()) {
            return -1;
        }

        long sizeInBytes = file.getSize();
        return (double) sizeInBytes / (1024 * 1024);
    }

    /**
     * Checks if the file size is within the allowed maximum size.
     *
     * @param file      The {@link MultipartFile} to check.
     * @param maxSizeMB The maximum allowed file size in megabytes (MB).
     * @return {@code true} if the file size is less than or equal to the maximum size; {@code false} otherwise.
     */
    public static boolean isFileSizeAllowed(MultipartFile file, double maxSizeMB) {
        double fileSizeMB = getFileSizeInMB(file);
        if (fileSizeMB == -1) {
            return false;
        }
        return fileSizeMB <= maxSizeMB;
    }

    public static boolean isPdf(MultipartFile file) {
        return "application/pdf".equalsIgnoreCase(file.getContentType());
    }

    public static void createDirectory(String name) {
        Path path = Paths.get(name);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                log.error("Cannot create directory: {}", path);
                log.error(e.getMessage());
                throw new RuntimeException("Cannot create directory: " + path);
            }
        }
    }

    public static File uploadFile(byte[] fileContent, String fileName) {
        createDirectory("hubsub_file");
        Path rootPath = Paths.get("hubsub_file");
        Path filePath = rootPath.resolve(fileName);

        try {
            Files.write(filePath, fileContent);
            return filePath.toFile();
        } catch (IOException e) {
            log.error("Cannot upload file: {}", filePath);
            log.error(e.getMessage());
            throw new RuntimeException("Cannot upload file: " + filePath);
        }
    }
}
