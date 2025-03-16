package com.vtp.vipo.seller.common.dto;

import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: hieuhm12
 * Date: 9/23/2024
 */
public class CustomMultipartFile implements MultipartFile {
    private final byte[] fileContent;
    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private static final Map<String, String> MINETYPE = new HashMap<>();

    static {
        MINETYPE.put("7z", "application/x-7z-compressed");
        MINETYPE.put("rar", "application/vnd.rar");
        MINETYPE.put("zip", "application/zip");
        MINETYPE.put("txt", "text/plain");
        MINETYPE.put("ppt", "application/vnd.ms-powerpoint");
        MINETYPE.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MINETYPE.put("doc", "application/msword");
        MINETYPE.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MINETYPE.put("xls", "application/vnd.ms-excel");
        MINETYPE.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MINETYPE.put("pdf", "application/pdf");
        MINETYPE.put("jpeg", "image/jpeg");
        MINETYPE.put("png", "image/png");
        MINETYPE.put("bmp", "image/bmp");
        MINETYPE.put("gif", "image/gif");
    }

    public CustomMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        Assert.hasLength(name, "Name must not be null");
        this.fileName = name;
        this.originalFileName = originalFilename != null ? originalFilename : "";
        this.contentType = contentType;
        this.fileContent = content != null ? content : new byte[0];
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getOriginalFilename() {
        return originalFileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(dest)) {
            outputStream.write(fileContent);
        }
    }

    public CustomMultipartFile(String name, String originalFilename, String contentType, InputStream contentStream) throws IOException {
        this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    }

    public CustomMultipartFile(String name, InputStream contentStream) throws IOException {
        this(name, name, MINETYPE.get(getFileType(name)), FileCopyUtils.copyToByteArray(contentStream));
    }

    private static String getFileType(String input) {
        List<String> splitMagicNumByComma = Arrays.asList(input.trim().split("\\."));
        return splitMagicNumByComma.get(splitMagicNumByComma.size() - 1);
    }
}
