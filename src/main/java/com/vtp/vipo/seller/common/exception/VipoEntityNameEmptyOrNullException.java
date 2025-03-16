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

public class VipoEntityNameEmptyOrNullException extends VipoBusinessException {

    private static final long serialVersionUID = 7641702942202000228L;

    public VipoEntityNameEmptyOrNullException() {
        super(BaseExceptionConstant.ENTITY_NAME_EMPTY_OR_NULL, BaseExceptionConstant.ENTITY_NAME_EMPTY_OR_NULL_DESCRIPTION);
    }

    public VipoEntityNameEmptyOrNullException(String message) {
        super(BaseExceptionConstant.ENTITY_NAME_EMPTY_OR_NULL, message);
    }

    public VipoEntityNameEmptyOrNullException(String objectName, String message) {
        super(BaseExceptionConstant.ENTITY_NAME_EMPTY_OR_NULL, objectName + ":" + message);
    }

}