package com.vtp.vipo.seller.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.response.VTPVipoResponse;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */
@RestController
public abstract class BaseController<S> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected S service;


    protected <D> ResponseEntity<VTPVipoResponse<D>> toResult(D t) {
        VTPVipoResponse<D> response = new VTPVipoResponse<D>();
        response.setStatus(BaseExceptionConstant.SUCCESS);
        response.setMessage(BaseExceptionConstant.SUCCESS_DESCRIPTION);
        response.setData(t);
        return new ResponseEntity<VTPVipoResponse<D>>(response, HttpStatus.OK);
    }

    protected <D> ResponseEntity<VTPVipoResponse<D>> toResult(String status, String message, D t) {
        VTPVipoResponse<D> response = new VTPVipoResponse<D>();
        response.setStatus(status);
        response.setMessage(message);
        response.setData(t);
        return new ResponseEntity<VTPVipoResponse<D>>(response, HttpStatus.OK);
    }

}
