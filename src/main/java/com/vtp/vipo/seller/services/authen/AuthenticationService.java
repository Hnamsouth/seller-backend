package com.vtp.vipo.seller.services.authen;

import com.vtp.vipo.seller.common.dto.request.auth.AuthenticationInput;
import com.vtp.vipo.seller.common.dto.response.AuthResponse;

/**
 * Interface for handling user authentication services.
 * <p>
 * Provides methods to authenticate users and link external OAuth2 accounts
 * to existing user accounts in the system.
 * </p>
 *
 * @author haidv
 * @version 1.0
 */
public interface AuthenticationService {

    /**
     * Authenticates a user based on the provided input credentials.
     * <p>
     * The implementation should validate the input and return an appropriate
     * {@link AuthResponse} containing authentication details such as
     * tokens or session information.
     * </p>
     *
     * @param loginInput The input containing user credentials, such as username and password.
     * @return An {@link AuthResponse} object containing authentication details.
     */
    AuthResponse authenticate(AuthenticationInput loginInput);

}
