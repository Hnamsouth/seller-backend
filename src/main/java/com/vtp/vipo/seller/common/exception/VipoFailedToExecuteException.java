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

public class VipoFailedToExecuteException extends VipoBusinessException {

    private static final long serialVersionUID = 7641702942202000228L;

    public VipoFailedToExecuteException() {
        super(BaseExceptionConstant.FAILED_TO_EXECUTE, BaseExceptionConstant.FAILED_TO_EXECUTE_DESCRIPTION);
    }

    public VipoFailedToExecuteException(String message) {
        super(BaseExceptionConstant.FAILED_TO_EXECUTE, message);
    }

    public VipoFailedToExecuteException(String objectName, String message) {
        super(BaseExceptionConstant.FAILED_TO_EXECUTE, objectName + ":" + message);
    }

}
