package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.dto.request.MerchantRequest;
import com.vtp.vipo.seller.common.dto.request.merchant.MerchantRequestV2;
import com.vtp.vipo.seller.services.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/merchant")
public class MerchantController extends BaseController<MerchantService> {

    @PostMapping("/changeInfo")
    public ResponseEntity<?> changeInfo(@Valid @RequestBody MerchantRequest request) {
        return toResult(service.changeInfo(request));
    }

    @GetMapping("/getInfo")
    public ResponseEntity<?> getInfo() {
        return toResult(service.getInfo());
    }


    /* VIPO-3903: Upload E-Contract: new APIs for merchant */

    @GetMapping("/v2/getInfo")
    public ResponseEntity<?> getInfoV2() {
        return toResult(service.getInfoV2());
    }

    @GetMapping("/v2/contract/history")
    public ResponseEntity<?> getMerchantContractHistory(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return toResult(service.getMerchantContractHistory(pageNum, pageSize));
    }

    @GetMapping("/v2/contract/terms/accept")
    public ResponseEntity<?> acceptMerchantTerms() {
        return toResult(service.acceptMerchantTerms());
    }

    @PostMapping("/v2/changeInfo")
    public ResponseEntity<?> changeInfoV2(@Valid @RequestBody MerchantRequestV2 request) {
        return toResult(service.changeInfoV2(request));
    }

}
