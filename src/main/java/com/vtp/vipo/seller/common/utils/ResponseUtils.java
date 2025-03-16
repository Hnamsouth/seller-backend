package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.dto.response.base.ResponseData;
import org.springframework.core.io.Resource;
import org.springframework.http.*;

import java.io.IOException;

/**
 * This utility class provides methods to create ResponseEntity objects with ResponseData payload.
 * The ResponseEntity objects are used to return HTTP responses with specific status codes. The
 * ResponseData payload contains the response data, code, message, and other information. The class
 * provides methods to create responses for successful operations and errors. The class also
 * provides a method to create a ResponseData object for an error. The class is final and has a
 * private constructor to prevent instantiation. The IllegalStateException is thrown if an attempt
 * is made to instantiate the class.
 *
 * @author haidv
 * @version 1.0
 */
public final class ResponseUtils {

    /**
     * The private constructor of the utility class. It throws an IllegalStateException to prevent
     * instantiation.
     */
    private ResponseUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for a
     * successful operation. The ResponseData payload does not contain any data.
     *
     * @param <T> the type of the data
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> success() {
        return success(null);
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for a
     * successful operation. The ResponseData payload contains the given data.
     *
     * @param <T> the type of the data
     * @param o the data
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> success(T o) {
        return ResponseEntity.ok(new ResponseData<T>().success(o));
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for a
     * successful operation with a CREATED status. The ResponseData payload does not contain any data.
     *
     * @param <T> the type of the data
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> created() {
        return created(null);
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for a
     * successful operation with a CREATED status. The ResponseData payload contains the given data.
     *
     * @param <T> the type of the data
     * @param o the data
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> created(T o) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseData<T>().success(o));
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for an error.
     * The ResponseData payload contains the given code, error description, message, and status. The
     * ResponseData payload does not contain any data.
     *
     * @param <T> the type of the data
     * @param code the error code
     * @param errorDesc the error description
     * @param message the error message
     * @param status the HTTP status
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> error(
            String code, String errorDesc, String message, HttpStatus status) {
        return error(code, errorDesc, message, null, status);
    }

    /**
     * This method is used to create a ResponseEntity object with a ResponseData payload for an error.
     * The ResponseData payload contains the given code, error description, message, data, and status.
     *
     * @param <T> the type of the data
     * @param code the error code
     * @param errorDesc the error description
     * @param message the error message
     * @param data the data
     * @param status the HTTP status
     * @return the ResponseEntity object
     */
    public static <T> ResponseEntity<ResponseData<T>> error(
            String code, String errorDesc, String message, T data, HttpStatus status) {
        return ResponseEntity.status(status).body(getResponseDataError(code, errorDesc, message, data));
    }

    /**
     * This method is used to create a ResponseData object for an error. The ResponseData object
     * contains the given code, error description, message, and data.
     *
     * @param <T> the type of the data
     * @param code the error code
     * @param errorDesc the error description
     * @param message the error message
     * @param data the data
     * @return the ResponseData object
     */
    public static <T> ResponseData<T> getResponseDataError(
            String code, String errorDesc, String message, T data) {
        return new ResponseData<T>().error(code, errorDesc, message, data);
    }

}
