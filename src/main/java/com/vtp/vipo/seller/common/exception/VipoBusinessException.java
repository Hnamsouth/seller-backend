package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.utils.Translator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VipoBusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected String status;

    protected Object data;

    public VipoBusinessException(String status) {
        super();
        this.status = status;
    }

    public VipoBusinessException(String status, String data, String message) {
        super(data + ": " + message);
        this.status = status;
    }
    public VipoBusinessException(ErrorCodeResponse errorCodeResponse) {
        super(errorCodeResponse.getMessageI18N());
        this.status = errorCodeResponse.getErrorCode();
    }
    public VipoBusinessException(ErrorCodeResponse errCode, String... params) {
        super(Translator.toLocale(errCode.getMessage(), params));
        this.status = errCode.getErrorCode();
    }
    public VipoBusinessException(String errCode, String message) {
        super(Translator.toLocale(message));
        this.status = errCode;
    }
}
