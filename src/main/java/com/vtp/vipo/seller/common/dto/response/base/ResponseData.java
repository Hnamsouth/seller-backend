package com.vtp.vipo.seller.common.dto.response.base;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vtp.vipo.seller.VipoSellerApplication;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.common.utils.RequestUtils;
import lombok.Getter;
import lombok.ToString;
import com.vtp.vipo.seller.common.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * This class represents the response data of a service operation. It contains the server time, zone
 * info, service name, session ID, request ID, response code, message, error description, and the
 * actual data. The class uses the Lombok library to generate getters and a toString method for the
 * fields. The class implements Serializable, which means it can be converted to a byte stream and
 * restored from it. The @Serial annotation is used to indicate that the serialVersionUID field is a
 * serial version UID for the Serializable class. The @ToString annotation is used to generate a
 * toString method for the class. The @Getter annotation is used to generate getters for all the
 * fields.
 * <p>
 *
 * This is an adapted version for Vipo system
 *
 * @param <T> the type of the actual data
 * @author haidv
 * @version 1.0
 */
@ToString
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResponseData<T> {

    /**
     * The server time when the response is created. It is formatted as a string using the pattern
     * defined in the DateConstant class.
     */
    private final String serverTime;

    /**
     * The zone info of the server. It is obtained from the default time zone of the JVM.
     */
    private final String zoneInfo;

    /**
     * The name of the service that creates the response. It is obtained from the BusinessApplication
     * class.
     */
    private final String service;

    /**
     * The session ID of the request. It is obtained from the RequestUtils class.
     */
    private final String sessionId;

    /**
     * The request ID of the request. It is obtained from the RequestUtils class.
     */
    private final String requestId;

    /**
     * The response code of the operation. It is "0" for a successful operation and a non-zero string
     * for an error.
     */
    private String code;

    /**
     * The message of the operation. It is "Successful!" for a successful operation and an error
     * message for an error.
     */
    private String message;

    /**
     * The error description of the operation. It is null for a successful operation and a non-null
     * string for an error.
     */
    private String errorDesc;

    /**
     * The actual data of the operation. It is the result of a successful operation and can be null
     * for an error.
     */
    private T data;

    /**
     * Vipo system will use the "status" field for the business response
     */
    private String status;

    /**
     * The default constructor of the class. It initializes the serverTime, zoneInfo, service,
     * sessionId, requestId, code, and message fields.
     */
    public ResponseData() {
        this.code = "0";
        this.serverTime =
                LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(DateUtils.ISO_8601_EXTENDED_DATETIME_FORMAT));
        this.zoneInfo = TimeZone.getDefault().getID();
        this.message = "Successful!";
        this.service = VipoSellerApplication.APPLICATION_NAME;
        this.requestId = RequestUtils.extractRequestId();
        /* In a stateless system, each request is independent, and there is no session information stored on the server
        (or shared across requests). Therefore, sessionId is null in this case */
        this.sessionId = RequestUtils.extractSessionId();
        this.status = BaseExceptionConstant.SUCCESS;
    }

    /**
     * This method is used to set the data field and return the ResponseData object for a successful
     * operation.
     *
     * @param data the result of the operation
     * @return the ResponseData object
     */
    public ResponseData<T> success(T data) {
        this.data = data;
        return this;
    }

    /**
     * This method is used to set the code and message fields and return the ResponseData object for
     * an error.
     *
     * @param code    the error code
     * @param message the error message
     * @return the ResponseData object
     */
    public ResponseData<T> error(String code, String message) {
        this.code = code;
        this.message = message;
        return this;
    }

    /**
     * This method is used to set the code and message fields and return the ResponseData object for
     * an error.
     *
     * @param status  vipo system status
     * @param code    the error code
     * @param message the error message
     * @return the ResponseData object
     */
    public ResponseData<T> error(String status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
        return this;
    }

    /**
     * This method is used to set the code, errorDesc, message, and data fields and return the
     * ResponseData object for an error.
     *
     * @param code      the error code
     * @param errorDesc the error description
     * @param message   the error message
     * @param data      the result of the operation
     * @return the ResponseData object
     */
    public ResponseData<T> error(String code, String errorDesc, String message, T data) {
        this.data = data;
        this.code = code;
        this.message = message;
        this.errorDesc = errorDesc;
        return this;
    }

    /**
     * This method is used to set the code, errorDesc, message, and data fields and return the
     * ResponseData object for an error.
     *
     * @param code      the error code
     * @param errorDesc the error description
     * @param message   the error message
     * @param data      the result of the operation
     * @return the ResponseData object
     */
    public ResponseData<T> error(String status, String code, String errorDesc, String message, T data) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
        this.errorDesc = errorDesc;
        return this;
    }

}
