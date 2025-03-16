/***************************************************************************
 * Copyright 2018 by VIETIS - All rights reserved.                *    
 **************************************************************************/
package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */

public class VipoUnAuthorizationException extends VipoBusinessException {

    private static final long serialVersionUID = 1537715339494843375L;

    public VipoUnAuthorizationException() {
        super(BaseExceptionConstant.VIPO_UNAUTHORIZED, BaseExceptionConstant.VIPO_UNAUTHORIZED_DESCRIPTION);
    }
    
    public VipoUnAuthorizationException(String message) {
        super(BaseExceptionConstant.VIPO_UNAUTHORIZED, message);
    }

    public VipoUnAuthorizationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
