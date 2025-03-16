package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.ProductConstant;
import com.vtp.vipo.seller.common.dto.request.product.ProductCreateUpdateRequest;
import com.vtp.vipo.seller.common.dto.request.product.search.ProductSearchReq;
import com.vtp.vipo.seller.services.CategoryService;
import com.vtp.vipo.seller.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController extends BaseController<CategoryService> {
    @GetMapping("/cbb")
    public ResponseEntity<?> getCbb() {
        return toResult(service.getCbb());
    }
    @PostMapping("insert-category")
    public ResponseEntity<?> insertCategory(@RequestBody @Valid MultipartFile file) {
        return  toResult(BaseExceptionConstant.SUCCESS,
                ProductConstant.CREATE_SUCCESS_DESCRIPTION,
                service.insertCategory(file));
    }
}

