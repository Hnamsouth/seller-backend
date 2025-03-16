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

public class VipoNotFoundException extends VipoBusinessException {

    private static final long serialVersionUID = 7641702942202000228L;
    
    public VipoNotFoundException() {
        super(BaseExceptionConstant.NOT_FOUND_ENTITY, BaseExceptionConstant.NOT_FOUND_ENTITY_DESCRIPTION);
    }
    
    public VipoNotFoundException(String message) {
        super(BaseExceptionConstant.NOT_FOUND_ENTITY, message);
    }
    
}
