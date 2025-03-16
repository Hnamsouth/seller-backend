package com.vtp.vipo.seller.config.security.jwt;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dto.response.VTPVipoResponse;
import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component("customAuthenticationEntryPoint")
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        VTPVipoResponse<?> responseBody = new VTPVipoResponse<>();

        /* Case no token */
        if(response.getHeader(BaseExceptionConstant.VIPO_STATUS_HEADER).equals(BaseExceptionConstant.EXPIRED_VIPO_TOKEN)) {
            responseBody.setStatus(BaseExceptionConstant.EXPIRED_VIPO_TOKEN);
            responseBody.setMessage(!ObjectUtils.isEmpty(response.getHeader(Constants.MESSAGE))? response.getHeader(Constants.MESSAGE) : BaseExceptionConstant.EXPIRED_VIPO_TOKEN_DESCRIPTION);
        } else if(response.getHeader(BaseExceptionConstant.VIPO_STATUS_HEADER).equals(BaseExceptionConstant.VIPO_INVALID_TOKEN)) {
            responseBody.setStatus(BaseExceptionConstant.VIPO_INVALID_TOKEN);
            responseBody.setMessage(BaseExceptionConstant.VIPO_INVALID_TOKEN_DESCRIPTION);
        } else { //Unknown error
            responseBody.setStatus(BaseExceptionConstant.UNKNOWN_ERROR);
            responseBody.setMessage(BaseExceptionConstant.UNKNOWN_ERROR_DESCRIPTION);
        }

        response.setContentType(Constants.HTTP_RESPONSE_HEADER_CONTENT_TYPE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpServletResponse.SC_OK);// set 200
        response.getWriter().write(new JSONObject(responseBody).toString());
        response.setHeader(Constants.MESSAGE, null);
    }

}

