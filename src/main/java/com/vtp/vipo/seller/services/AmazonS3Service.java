package com.vtp.vipo.seller.services;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.S3Object;
import com.vtp.vipo.seller.common.dto.StorageInfoDTO;
import com.vtp.vipo.seller.common.dto.response.FileAmazonS3Info;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Service
public interface AmazonS3Service {

    String uploadFile(MultipartFile file, boolean isUploadExcel);

    FileAmazonS3Info getFileInfoByUrl(String url);

    String uploadFile(Path filePath, String bucketName, String key);

    String uploadFilePublic(Path filePath, String bucketName, String key);

    S3Object getS3Object(String bucket, String key) throws IOException;

    DeleteObjectsResult deleteObjects(DeleteObjectsRequest deleteRequest);

    StorageInfoDTO uploadFile(MultipartFile file);

    void deleteFile(StorageInfoDTO storageInfoDTO);

    String uploadFile(InputStream inputStream, long contentLength, String bucketName, String key, String contentType);

    StorageInfoDTO uploadFile(InputStream inputStream, long contentLength, String contentType, String fileName);
}
