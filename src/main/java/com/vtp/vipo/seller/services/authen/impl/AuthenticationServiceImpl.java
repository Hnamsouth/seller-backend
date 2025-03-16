package com.vtp.vipo.seller.services.authen.impl;

import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dao.entity.MerchantNewEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerOpenEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.ContractStatus;
import com.vtp.vipo.seller.common.dao.entity.enums.merchant.MerchantContractType;
import com.vtp.vipo.seller.common.dao.repository.MerchantNewRepository;
import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.dao.repository.SellerOpenRepository;
import com.vtp.vipo.seller.common.dto.request.auth.AuthenticationInput;
import com.vtp.vipo.seller.common.dto.response.AuthResponse;
import com.vtp.vipo.seller.common.mapper.SellerMapper;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.config.security.jwt.JwtTokenService;
import com.vtp.vipo.seller.services.authen.AuthenticationContext;
import com.vtp.vipo.seller.services.authen.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the {@link AuthenticationService} interface.
 * <p>
 * This service handles user authentication and OAuth2 account linking.
 * It provides methods to authenticate users, generate JWT tokens, and manage
 * external authentication providers (e.g., Google, Facebook).
 * </p>
 *
 * @author haidv
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationServiceImpl implements AuthenticationService {

    // Authentication context to handle authentication strategy logic
    final AuthenticationContext authenticationContext;

    final JwtTokenService jwtTokenService;
    private final MerchantNewRepository merchantNewRepository;

    @Value("${security.vipo.exp.jwt-refresh}")
    private Long refreshTokenDurationMs;

    final MerchantRepository merchantRepository;

    final SellerOpenRepository sellerOpenRepository;

    final SellerMapper sellerMapper;

    /**
     * Authenticates a user based on the provided input.
     * <p>
     * The method validates the credentials using the authentication context,
     * generates JWT access and refresh tokens, and returns authentication output
     * containing token details and user information.
     * </p>
     *
     * @param input The authentication input containing identifier, credential, and authentication type.
     * @return An {@link AuthResponse} object with tokens and user details.
     */
    @Transactional
    @Override
    public AuthResponse authenticate(AuthenticationInput input) {

        MerchantEntity merchant = authenticationContext.authenticate(
                input.getIdentifier(), input.getCredential(), input.getAuthenticationType()
        );

        //todo: when consider merchant_new table can replace merchant table, no need to copy 1:1 between 2 tables
        /* VIPO-3903: Upload E-Contract:  */
        MerchantNewEntity merchantNew = getMerchantNewFromMerchant(merchant);

        String sellerOpenId = sellerOpenRepository.getSellerOpenIdByMerchantId(merchant.getId());

        // copy from MerchantServiceImpl#login
        String jwt = jwtTokenService.generateTokenFromUserInfo(
                new VipoUserDetails(
                        merchant.getId(),
                        merchant.getContactPhone(),
                        merchant.getPassword(),
                        merchant.getRefreshToken(),
                        merchant.getCountryId(),
                        sellerOpenId,
                        merchant.getContactEmail(),
                        merchant.getName())
        );
        String refreshToken = generateRefreshToken(merchant, jwt);
        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .phone(merchant.getContactPhone())
                .build();

    }

    /**
     * Copy from MerchantServiceImpl#generateRefreshToken
     */
    private String generateRefreshToken(MerchantEntity merchant, String jwt) {
        String refreshToken = UUID.randomUUID().toString();
        merchant.setRefreshToken(refreshToken);
        merchant.setExpiredAt(DateUtils.convertMilTimeToSecond(System.currentTimeMillis() + refreshTokenDurationMs));
//        merchant.setAccessToken(jwt);
        merchantRepository.save(merchant);
        return refreshToken;
    }


    /**
     * VIPO-3903: Upload E-Contract: keep same data between merchant and merchant_new for ensuring different development
     * flow occur
     */
    private MerchantNewEntity getMerchantNewFromMerchant(MerchantEntity merchant) {

        MerchantNewEntity merchantNew = merchantNewRepository.findById(merchant.getId()).orElse(null);

        if (ObjectUtils.isNotEmpty(merchantNew))
            return merchantNew;

        MerchantNewEntity newMerchant = sellerMapper.toMerchantNewEntity(merchant);
        newMerchant.setContractStatus(ContractStatus.NO_CONTRACT);
        newMerchant.setEnabledEditCountry(true);
        newMerchant.setType(MerchantContractType.NO_CONTRACT);

        return merchantNewRepository.save(newMerchant);

    }

}
