package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VipoAuthenticationFailureHandler extends VipoBusinessException {

    private static final long serialVersionUID = 1L;

    public VipoAuthenticationFailureHandler() {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, BaseExceptionConstant.CONNECTION_TIMEOUT_DESCRIPTION);
    }

    public VipoAuthenticationFailureHandler(String message) {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, message);
    }

    public VipoAuthenticationFailureHandler(String objectName, String message) {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, objectName + ":" + message);
    }
}
