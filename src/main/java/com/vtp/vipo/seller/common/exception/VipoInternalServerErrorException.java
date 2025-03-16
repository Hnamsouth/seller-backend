package com.vtp.vipo.seller.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VipoInternalServerErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public VipoInternalServerErrorException() {
    }

    public VipoInternalServerErrorException(String message) {
        super(message);
    }

    public VipoInternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
