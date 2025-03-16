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

public class VipoNotMatchDataException extends VipoBusinessException {

   private static final long serialVersionUID = 7641702942202000228L;
    
    public VipoNotMatchDataException() {
        super(BaseExceptionConstant.NOT_MATCH_DATA, BaseExceptionConstant.NOT_MATCH_DATA_DESCRIPTION);
    }
    
    public VipoNotMatchDataException(String message) {
        super(BaseExceptionConstant.NOT_MATCH_DATA, message);
    }
    
    public VipoNotMatchDataException(String objectName, String message) {
        super(BaseExceptionConstant.NOT_MATCH_DATA, objectName + ":" + message);
    }
    
}
