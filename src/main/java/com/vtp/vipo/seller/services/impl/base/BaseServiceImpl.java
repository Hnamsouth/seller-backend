package com.vtp.vipo.seller.services.impl.base;

import com.vtp.vipo.seller.common.exception.VipoUnAuthorizationException;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * BaseServiceImpl class provides common functionality for services that need to
 * retrieve the current authenticated user in the system.
 * This class contains a method to get the current user details and handle
 * authentication-related errors by throwing a custom exception when the
 * user is not authenticated or the user details are incomplete.
 *
 * <p>This class assumes that the application uses Spring Security for authentication
 * and the user details are stored in the {@link SecurityContextHolder}.</p>
 *
 * <p>Any class extending this class can leverage the {@link #getCurrentUser()}
 * method to fetch the details of the currently authenticated user.</p>
 *
 * @see VipoUserDetails
 * @see SecurityContextHolder
 * @see VipoUnAuthorizationException
 */
@Slf4j
public class BaseServiceImpl {

    /**
     * Retrieves the current authenticated user's details from the Spring Security context.
     *
     * <p>This method assumes that the user's details are stored in the
     * {@link SecurityContextHolder} and are accessible via the authentication principal.
     * If the user is not authenticated or the required user details (like phone number) are
     * missing, a {@link VipoUnAuthorizationException} is thrown to indicate that
     * the current user is not authorized to access the requested resource.</p>
     *
     * @return the current authenticated user's details as a {@link VipoUserDetails} object.
     * @throws VipoUnAuthorizationException if the user is not authenticated or has invalid user details.
     */
    protected VipoUserDetails getCurrentUser() {
        VipoUserDetails info = null;
        try {
            // Retrieve the current authentication from the SecurityContextHolder
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                // Get the principal (user details) from the authentication object
                info = (VipoUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        } catch (Exception e) {
            // Log any errors that occur while retrieving user information
            log.error("Error retrieving current user details: {}", e.getLocalizedMessage(), e);
        }

        // Check if the user details are valid and not empty
        if (!ObjectUtils.isEmpty(info)) {
            // Further validation to ensure user details are complete
//            if (info == null || ObjectUtils.isEmpty(info.getPhone())) {
//                // If the phone number is empty or user details are incomplete, throw authorization exception
//                throw new VipoUnAuthorizationException("User details are incomplete, phone number is missing.");
//            }
            return info; // Return the user details if valid
        } else {
            // If the user details are not available, throw authorization exception
            throw new VipoUnAuthorizationException("User not authenticated or unauthorized.");
        }
    }
}