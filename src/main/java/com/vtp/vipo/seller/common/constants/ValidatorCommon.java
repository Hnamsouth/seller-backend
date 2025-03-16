package com.vtp.vipo.seller.common.constants;

import com.vtp.vipo.seller.common.utils.DateUtils;
import org.springframework.util.ObjectUtils;

public class ValidatorCommon {

    public static String validateTime(Long time){
        if (!ObjectUtils.isEmpty(time) && time > 0) {
            return DateUtils.toStringFromLong(time * 1000, DateUtils.HH_MM_DD_MM_YYYY);
        }
        return "";
    }
}
