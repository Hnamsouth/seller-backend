package com.vtp.vipo.seller.common.exception;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dto.response.VTPVipoResponse;
import com.vtp.vipo.seller.common.utils.Translator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;

import java.nio.file.AccessDeniedException;

/**
 * Author : Le Quang Dat </br>
 * Email: quangdat0993@gmail.com</br>
 * Jan 20, 2024
 */
@RestControllerAdvice
@Slf4j
public class VipoExceptionHandler extends ResponseEntityExceptionHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String getErrorMessage(Exception ex) {
        StringBuilder errorMessage = new StringBuilder();
        if (ex instanceof MethodArgumentNotValidException) {
            var exception = (MethodArgumentNotValidException) ex;
            exception.getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String message = error.getDefaultMessage();
                errorMessage.append(fieldName).append(": ").append(message).append(". ");
            });
        }
        return errorMessage.toString();
    }

    /**
     * Handle number format exception.
     *
     * @param request the request
     * @param e       the e
     * @return the response entity
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleNumberFormatException(final HttpServletRequest request,
                                                                               final Exception e) {

        logger.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(BaseExceptionConstant.INVALID_DATA_REQUEST);
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoInvalidDataRequestException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleInvalidInputException(final HttpServletRequest request,
                                                                               final Exception e) {

        log.info(e.getMessage(), e);

        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoInvalidDataRequestException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoDuplicatedEntityException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleDuplicatedEntityException(final HttpServletRequest request,
                                                                                   final Exception e) {
        log.info(e.getMessage(), e);

        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoDuplicatedEntityException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoNotMatchDataException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleNotMatchException(final HttpServletRequest request,
                                                                           final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoNotMatchDataException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoNotFoundException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleNotFoundException(final HttpServletRequest request,
                                                                           final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoNotFoundException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoUnAuthorizationException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleUnauthorizedException(Exception e, WebRequest request) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoUnAuthorizationException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoFailedToExecuteException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleBluuFailedToExecuteException(final HttpServletRequest request,
                                                                                      final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoFailedToExecuteException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(VipoBusinessException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleBluuBusinessException(final HttpServletRequest request,
                                                                               final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoBusinessException) e).getStatus());
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<VTPVipoResponse<String>> handleException(final HttpServletRequest request,
                                                                   final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(BaseExceptionConstant.UNKNOWN_ERROR);
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleRuntimeException(final HttpServletRequest request,
                                                                          final Exception e) {

        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(BaseExceptionConstant.UNKNOWN_ERROR);
        response.setMessage(e.getMessage());
        // Kiểm tra nếu ngoại lệ là VipoBusinessException bị gói bên trong
        if (e.getCause() instanceof VipoBusinessException) {
            VipoBusinessException vipoException = (VipoBusinessException) e.getCause();
            response.setMessage(vipoException.getMessage());
            response.setStatus(vipoException.getStatus());
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<VTPVipoResponse<String>> NotAcceptException(final HttpServletRequest request,
                                                                      final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(BaseExceptionConstant.FAILED_TO_CALL);
        response.setMessage(BaseExceptionConstant.FAILED_TO_CALL_DESCRIPTION);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        log.info(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON).body("Access denied");
    }

    @ExceptionHandler({VipoConnectionTimeoutException.class})
    public ResponseEntity<Object> handleConnectionTimeoutException(Exception e, WebRequest request) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(((VipoConnectionTimeoutException) e).getStatus());
        response.setMessage(e.getMessage());
        return  ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<VTPVipoResponse<String>> badCredentialsException(final HttpServletRequest request,
                                                                           final Exception e) {
        log.info(e.getMessage(), e);
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        response.setStatus(BaseExceptionConstant.BAD_CREDENTIAL);
        response.setMessage(BaseExceptionConstant.BAD_CREDENTIAL_DESCRIPTION);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.info(ex.getMessage(), ex);
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.info(ex.getLocalizedMessage(), ex);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(new VTPVipoResponse<>(BaseExceptionConstant.INVALID_DATA_REQUEST,
                getErrorMessage(ex).toString(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<VTPVipoResponse<String>> handleConstraintViolationException(final HttpServletRequest request, final ConstraintViolationException e) {
        VTPVipoResponse<String> response = new VTPVipoResponse<>();
        // Bạn có thể duyệt qua các vi phạm ràng buộc (constraint violations) và lấy thông tin chi tiết
        StringBuilder errorMessage = new StringBuilder();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errorMessage.append(violation.getMessage()).append(" ");
        }

        response.setStatus("02");  // Bạn có thể tùy chỉnh trạng thái theo yêu cầu
        response.setMessage(errorMessage.toString().strip());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(response);
    }

//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<VTPVipoResponse<String>> handleConstraintViolationException(final HttpServletRequest request, final ConstraintViolationException e) {
//        VTPVipoResponse<String> response = new VTPVipoResponse<>();
//
//        // Bạn có thể duyệt qua các vi phạm ràng buộc (constraint violations) và lấy thông tin chi tiết
//        StringBuilder errorMessage = new StringBuilder();
//        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
//            errorMessage.append(violation.getMessage()).append(" ");
//        }
//
//        response.setStatus("00");  // Bạn có thể tùy chỉnh trạng thái theo yêu cầu
//        response.setMessage(errorMessage.toString());
//
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response);
//    }

}
