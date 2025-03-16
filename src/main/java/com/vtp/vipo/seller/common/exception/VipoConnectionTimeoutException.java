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

public class VipoConnectionTimeoutException extends VipoBusinessException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public VipoConnectionTimeoutException() {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, BaseExceptionConstant.CONNECTION_TIMEOUT_DESCRIPTION);
    }
    
    public VipoConnectionTimeoutException(String message) {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, message);
    }
    
    public VipoConnectionTimeoutException(String objectName, String message) {
        super(BaseExceptionConstant.CONNECTION_TIMEOUT, objectName + ":" + message);
    }
}
