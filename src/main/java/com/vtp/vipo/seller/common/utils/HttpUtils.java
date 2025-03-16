package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.Constants;
import org.springframework.util.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpUtils {

    private HttpUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(Constants.HEADER);
        if (ObjectUtils.isEmpty(authenticationHeader)) {
            authenticationHeader = request.getHeader(Constants.HEADER2);
        }
        return authenticationHeader;
    }

}
