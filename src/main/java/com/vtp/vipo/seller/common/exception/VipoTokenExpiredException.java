package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */

public class VipoTokenExpiredException extends VipoBusinessException {

   private static final long serialVersionUID = 7641702942202000228L;
    
    public VipoTokenExpiredException() {
        super(BaseExceptionConstant.TOKEN_EXPIRED, BaseExceptionConstant.TOKEN_EXPIRED_DESCRIPTION);
    }
    
    public VipoTokenExpiredException(String message) {
        super(BaseExceptionConstant.TOKEN_EXPIRED, message);
    }
    
}
