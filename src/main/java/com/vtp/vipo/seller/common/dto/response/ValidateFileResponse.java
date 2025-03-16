package com.vtp.vipo.seller.common.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidateFileResponse {

    private Boolean error;

    private String message;

    private String fileName;

    private String extension;

}
