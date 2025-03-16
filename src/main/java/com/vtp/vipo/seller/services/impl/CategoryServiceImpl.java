package com.vtp.vipo.seller.services.impl;


import com.vtp.vipo.seller.common.BaseService;
import com.vtp.vipo.seller.common.dao.entity.*;
import com.vtp.vipo.seller.common.dao.repository.*;
import com.vtp.vipo.seller.common.dto.response.cbb.ComboboxRes;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.common.utils.DataUtils;
import com.vtp.vipo.seller.common.utils.FileUtils;
import com.vtp.vipo.seller.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("categoryService")
@RequiredArgsConstructor
public class CategoryServiceImpl extends BaseService<CategoryEntity, Long, CategoryRepository> implements CategoryService {

    @Override
    public List<ComboboxRes> getCbb() {
        List<CategoryEntity> categories = repo.fetchCategoriesWithLanguage(getLocale());
        Map<Long, ComboboxRes> categoryMap = new HashMap<>();
        for (CategoryEntity category : categories) {
            ComboboxRes comboboxRes = new ComboboxRes();
            comboboxRes.setKey(category.getId());
            comboboxRes.setValue(category.getName());
            comboboxRes.setSubs(new ArrayList<>());
            categoryMap.put(category.getId(), comboboxRes);
        }
        for (CategoryEntity category : categories) {
            if (category.getParentId() != null && category.getParentId() != 0) {
                ComboboxRes child = categoryMap.get(category.getId());
                ComboboxRes parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.getSubs().add(child);
                }
            }
        }
        return categoryMap.values().stream()
                .filter(c -> categories.stream()
                        .noneMatch(cat -> cat.getId().equals(c.getKey()) && cat.getParentId() != null
                                && cat.getParentId() != 0)) // Filter only root categories
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String insertCategory(MultipartFile file) {
        if (!file.getContentType().equals("text/plain"))
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
        Map<String, CategoryEntity> mapId = repo.findAll().stream().collect(Collectors.toMap(
                i -> i.getName().toLowerCase(), Function.identity()
        ));
        List<CategoryEntity> inssertList = new ArrayList<>();
        try (
                InputStreamReader isr = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("|")) {
                    String[] arr = line.split("\\|");
                    if (arr.length != 3) {
                        throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
                    }
                    CategoryEntity categoryL = mapId.get(arr[0].trim().toLowerCase());
                    if(DataUtils.isNullOrEmpty(categoryL)){
                        throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_FIELD);
                    }
                    CategoryEntity category = CategoryEntity.builder()
                            .name(arr[2].trim())
                            .code(arr[1].trim())
                            .parentId(categoryL.getId())
                            .icon(categoryL.getIcon())
                            .affiliateMinComission(0F)
                            .position(0)
                            .published(0)
                            .build();
                    inssertList.add(category);
                }
            }
        } catch (Exception e) {
            throw new VipoBusinessException(ErrorCodeResponse.IO_EXCEPTION);
        }
        repo.saveAll(inssertList);
        return "";
    }
}
