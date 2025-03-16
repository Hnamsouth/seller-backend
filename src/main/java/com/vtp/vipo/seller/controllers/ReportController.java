package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.dto.request.ReportRequest;
import com.vtp.vipo.seller.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/application/info")
public class ReportController extends BaseController<ReportService> {

    @PostMapping
    public ResponseEntity<?> reportFromMOIT(@Valid @RequestBody ReportRequest request){
        return ResponseEntity.ok(service.reportFromMOIT(request));
    }

}
