package com.vtp.vipo.seller.config.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for managing the API version.
 * <p>
 * This interceptor intercepts HTTP requests and extracts the API version from the request headers.
 * It sets the API version in a thread-local holder (ApiVersionHolder), allowing the version to be accessed
 * during request processing. After request completion, the API version is cleared from the thread-local holder.
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    // Constant for the header key used to retrieve the API version from the request
    private static final String HEADER_API_VERSION = "API-Version";

    /**
     * This method is invoked before the request is handled by a controller.
     * It extracts the API version from the request header and stores it in a thread-local holder.
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param handler the handler (usually a controller method) that will process the request
     * @return true to allow the request to proceed to the controller, false to prevent further processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Retrieve the API version from the request header
        String version = request.getHeader(HEADER_API_VERSION);
        if (StringUtils.isBlank(version)) {
            ApiVersionHolder.setApiVersion(ApiVersion.LEGACY);
            return true;
        }

        // Set the extracted API version in the thread-local holder for the current thread
        ApiVersion currentVersion = ApiVersion.getVersionFromCode(version.strip());
        ApiVersionHolder.setApiVersion(ObjectUtils.isNotEmpty(currentVersion) ? currentVersion : ApiVersion.getNewestVersion());

        // Continue processing the request
        return true;
    }

    /**
     * This method is invoked after the request has been processed by the handler.
     * It clears the API version from the thread-local holder to avoid memory leaks.
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param handler the handler that processed the request
     * @param ex any exception thrown during request processing (null if no exception occurred)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clear the API version from the thread-local holder after request processing
        ApiVersionHolder.clear();
    }
}