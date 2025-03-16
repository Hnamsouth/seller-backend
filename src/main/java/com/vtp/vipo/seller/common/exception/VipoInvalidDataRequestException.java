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

public class VipoInvalidDataRequestException extends VipoBusinessException {

    private static final long serialVersionUID = 7641702942202000228L;

    public VipoInvalidDataRequestException() {
        super(BaseExceptionConstant.INVALID_DATA_REQUEST, BaseExceptionConstant.INVALID_DATA_REQUEST_DESCRIPTION);
    }

    public VipoInvalidDataRequestException(String message) {
        super(BaseExceptionConstant.INVALID_DATA_REQUEST, message);
    }

    public VipoInvalidDataRequestException(String status, String message) {
        super(BaseExceptionConstant.INVALID_DATA_REQUEST, message);
        this.setStatus(status);
    }

}