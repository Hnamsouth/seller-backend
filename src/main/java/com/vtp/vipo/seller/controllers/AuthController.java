package com.vtp.vipo.seller.controllers;

import com.vtp.vipo.seller.common.BaseController;
import com.vtp.vipo.seller.common.dto.request.auth.*;
import com.vtp.vipo.seller.common.dto.response.AuthResponse;
import com.vtp.vipo.seller.common.dto.response.base.ResponseData;
import com.vtp.vipo.seller.common.utils.ResponseUtils;
import com.vtp.vipo.seller.services.authen.AuthenticationService;
import com.vtp.vipo.seller.services.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController<MerchantService> {

    private final AuthenticationService authenticationService;

    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return toResult(service.login(loginRequest));
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestType2 signUpRequest) {
        return toResult(service.register(signUpRequest));
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<?> refreshTokenUser(@Valid @RequestBody TokenRefreshRequest request) {
        return toResult(service.refreshToken(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePasswordUser(@Valid @RequestBody ChangePasswordRequest request) {
        return toResult(service.changePassword(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return toResult(service.logout());
    }

    @GetMapping("/public/country")
    public ResponseEntity<?> getCountry() {
        return toResult(service.getCbb());
    }

    /**
     * Handles user authentication requests and generates a token upon successful authentication.
     *
     * <p>This endpoint accepts a JSON payload containing the user's login credentials, validates it,
     * and returns an authentication token if the credentials are correct.</p>
     * @param loginInput the {@link AuthenticationInput} containing the user's credentials (authentication_type and
     *                   credential).
     * @return a {@link ResponseEntity} containing a {@link ResponseData} object that wraps the {@link AuthResponse}.
     * @see AuthenticationService#authenticate(AuthenticationInput)
     */
    @PostMapping("/token")
    public ResponseEntity<ResponseData<AuthResponse>> authenticate(@Validated @RequestBody AuthenticationInput loginInput) {
        return ResponseUtils.success(authenticationService.authenticate(loginInput));
    }

}
