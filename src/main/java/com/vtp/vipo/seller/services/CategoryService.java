package com.vtp.vipo.seller.services;

import com.vtp.vipo.seller.common.dto.request.product.ProductCreateUpdateRequest;
import com.vtp.vipo.seller.common.dto.request.product.search.ProductSearchReq;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import com.vtp.vipo.seller.common.dto.response.product.detail.ProductDetailResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    List<ComboboxRes> getCbb();
    String insertCategory(MultipartFile file);
}
