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

public class VipoDuplicatedEntityException extends VipoBusinessException {
    
    private static final long serialVersionUID = 7641702942202000228L;
    
    public VipoDuplicatedEntityException() {
        super(BaseExceptionConstant.DUPLICATED_ENTITY, BaseExceptionConstant.DUPLICATED_ENTITY_DESCRIPTION);
    }
    
    public VipoDuplicatedEntityException(String message) {
        super(BaseExceptionConstant.DUPLICATED_ENTITY, message);
    }
    
    public VipoDuplicatedEntityException(String objectName, String message) {
        super(BaseExceptionConstant.DUPLICATED_ENTITY, objectName + ":" + message);
    }
    
}
