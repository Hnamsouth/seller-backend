package com.vtp.vipo.seller.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.dto.response.SpecProp;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConvertSpecProd {
    public static List<SpecProp> buildSpecList(String specReq) {
        Map<String, String> specMap = JsonMapperUtils.convertJsonToObject(specReq, new TypeReference<>() {
        });
        List<SpecProp> spec = new ArrayList<>();
        if (!ObjectUtils.isEmpty(specMap)) {
            specMap.forEach((key, value) -> {
                SpecProp skuProp = new SpecProp();
                skuProp.setPropName(key);
                skuProp.setPropValue(value);
                spec.add(skuProp);
            });
        }
        return spec;
    }
}
