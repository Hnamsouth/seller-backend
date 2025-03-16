package com.vtp.vipo.seller.services.impl;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.FileAmazonS3Info;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.utils.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.response.ValidateFileResponse;
import com.vtp.vipo.seller.common.exception.VipoFileException;
import com.vtp.vipo.seller.services.AmazonS3Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AmazonS3ServiceImpl implements AmazonS3Service {

    //todo: replace by S3Client entirely later
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile multipartFile, boolean isUploadExcel) {
        try {
            ValidateFileResponse validateFileResponse = FileUtils.checkFile(multipartFile, isUploadExcel);
            if (Boolean.TRUE.equals(validateFileResponse.getError())) throw new VipoFileException(validateFileResponse.getMessage());
            String filePath = DateUtils.toDateString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS);
            String fileName = UUID.randomUUID().toString() + "." + validateFileResponse.getExtension();
            String key = bucketName + "/" + filePath + "/" + fileName;

            PutObjectResult putObjectResult = amazonS3.putObject(new PutObjectRequest(bucketName, key,
                    FileUtils.convert(multipartFile)).withCannedAcl(CannedAccessControlList.PublicRead));
            log.info("PutObjectResult: {}", JsonMapperUtils.writeValueAsString(putObjectResult));
            if (ObjectUtils.isEmpty(putObjectResult))
                throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
            URL imageS3Url = amazonS3.getUrl(bucketName, key);
//            return imageS3Url.toString().replace("https", "http");
            return imageS3Url.toString();
        } catch (Exception ex) {
            throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
        }

    }

    @Override
    public FileAmazonS3Info getFileInfoByUrl(String urlString) {
        String contentType;
        BigDecimal fileSizeMb;
        String key;
        try {
            URL url = new URL(urlString);
            // Lấy đường dẫn (path) từ URL
            String path = url.getPath();
            // Decode URL để chuyển đổi ký tự mã hóa như %2F thành ký tự tương ứng
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            String splitChar = bucketName + "/";
            String[] decodedPathSplit = decodedPath.split(splitChar);
            key = bucketName + decodedPathSplit[decodedPathSplit.length - 1];
            ObjectMetadata metadata = amazonS3.getObjectMetadata(bucketName, key);
            if (DataUtils.isNullOrEmpty(metadata)) {
                throw new VipoBusinessException(ErrorCodeResponse.COMMON_NOT_FOUND_ID, urlString);
            }
            // Lấy kích thước file (byte) và chuyển đổi sang MB
            long fileSizeBytes = metadata.getContentLength();
            fileSizeMb = new BigDecimal(fileSizeBytes)
                    .divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_UP);
            contentType = metadata.getContentType();
        } catch (IOException e) {
            // Xử lý khi có lỗi IO
            log.error(StackTraceUtil.stackTrace(e));
            throw new VipoBusinessException(ErrorCodeResponse.IO_EXCEPTION);
        } catch (Exception e) {
            log.error(StackTraceUtil.stackTrace(e));
            if (e.getCause().toString().equals("com.amazonaws.http.timers.client.SdkInterruptedException")
                    || e.getCause().toString().contains("org.apache.http.conn.ConnectTimeoutException")) {
                throw new VipoBusinessException(ErrorCodeResponse.CONNECTION_TIMEOUT, "AmazonS3");
            }
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_URL, urlString);
        }
        return FileAmazonS3Info.builder().fileSizeMB(fileSizeMb).contentType(contentType).build();
    }

    @Override
    public String uploadFile(Path filePath, String bucketName, String key) {
        //todo: limit the time out for uploading excel
        PutObjectResult putObjectResult = amazonS3.putObject(bucketName, key, filePath.toFile());
        if (ObjectUtils.isEmpty(putObjectResult))
            throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
        URL fileURL = amazonS3.getUrl(bucketName, key);
        return fileURL.toString();
    }

    @Override
    public String uploadFilePublic(Path filePath, String bucketName, String key) {
        //todo: limit the time out for uploading excel
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, filePath.toFile())
                .withCannedAcl(CannedAccessControlList.PublicRead);
        PutObjectResult putObjectResult = amazonS3.putObject(putRequest);
        if (ObjectUtils.isEmpty(putObjectResult))
            throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
        URL fileURL = amazonS3.getUrl(bucketName, key);
        return fileURL.toString();
    }

    @Override
    public S3Object getS3Object(String bucket, String key) {
        return amazonS3.getObject(bucketName, key);
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteRequest) {
        return amazonS3.deleteObjects(deleteRequest);
    }

    @Override
    public StorageInfoDTO uploadFile(MultipartFile file) {
        try {
            String filePath = DateUtils.toDateString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS);
            String fileName = UUID.randomUUID() + "." + FileUtils.getFileExtension(file.getOriginalFilename());
            String key = bucketName + "/" + filePath + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            PutObjectResult putObjectResult = amazonS3.putObject(putObjectRequest);

            if (ObjectUtils.isEmpty(putObjectResult)) {
                throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
            }

            URL imageS3Url = amazonS3.getUrl(bucketName, key);
            return StorageInfoDTO.builder()
                    .link(imageS3Url.toString())
                    .bucketName(bucketName)
                    .key(key)
                    .build();
        } catch (Exception e) {
            throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
        }
    }

    @Override
    public void deleteFile(StorageInfoDTO storageInfoDTO) {
        try {
            amazonS3.deleteObject(storageInfoDTO.getBucketName(), storageInfoDTO.getKey());
        } catch (Exception e) {
            throw new VipoFileException(BaseExceptionConstant.DELETE_FILE_FAIL);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, long contentLength,
                             String bucketName, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);
        metadata.setContentType(contentType);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata);
        amazonS3.putObject(putObjectRequest);
        return amazonS3.getUrl(bucketName, key).toString();
    }

    public StorageInfoDTO uploadFile(InputStream inputStream, long contentLength, String contentType, String fileName) {
        try {
            String filePath = DateUtils.toDateString(new Date(), DateUtils.YYYY_MM_DD_HH_MM_SS);
            String key = bucketName + "/" + filePath + "/" + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);
            metadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);
            PutObjectResult putObjectResult = amazonS3.putObject(putObjectRequest);

            log.info("[uploadFile] putObjectResult: {}", JsonMapperUtils.writeValueAsString(putObjectResult));
            if (ObjectUtils.isEmpty(putObjectResult)) {
                throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
            }

            URL imageS3Url = amazonS3.getUrl(bucketName, key);
            return StorageInfoDTO.builder()
                    .link(imageS3Url.toString())
                    .bucketName(bucketName)
                    .key(key)
                    .build();
        } catch (Exception e) {
            throw new VipoFileException(BaseExceptionConstant.UPLOAD_FILE_FAIL);
        }
    }

}
