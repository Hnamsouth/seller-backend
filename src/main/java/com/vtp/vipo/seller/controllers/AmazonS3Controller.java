package com.vtp.vipo.seller.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.services.AmazonS3Service;


@RestController
@RequestMapping("/s3")
public class AmazonS3Controller extends BaseController<AmazonS3Service> {

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam(name = "file") MultipartFile file) {
       return toResult(service.uploadFile(file,false));
    }
    @PostMapping("/upload-excel")
    public ResponseEntity<?> uploadExcel(@RequestParam(name = "file") MultipartFile file) {
        return toResult(service.uploadFile(file,true));
    }

}
