package com.vtp.vipo.seller.services.authen.impl.strategies;

import com.vtp.vipo.seller.business.feign.VtpApiClient;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dao.entity.MerchantLogEntity;
import com.vtp.vipo.seller.common.dao.entity.MerchantGroupEntity;
import com.vtp.vipo.seller.common.dao.entity.MerchantLogEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerOpenEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.MerchantLogActionEnum;
import com.vtp.vipo.seller.common.dao.repository.MerchantLogRepository;
import com.vtp.vipo.seller.common.dao.repository.MerchantGroupRepository;
import com.vtp.vipo.seller.common.dao.entity.enums.MerchantLogActionEnum;
import com.vtp.vipo.seller.common.dao.repository.MerchantLogRepository;
import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.dao.repository.SellerOpenRepository;
import com.vtp.vipo.seller.common.enumseller.AllowMultipleStores;
import com.vtp.vipo.seller.common.enumseller.MerchantInactiveStatus;
import com.vtp.vipo.seller.common.enumseller.MerchantStatusEnum;
import com.vtp.vipo.seller.common.exception.VipoFailedToExecuteException;
import com.vtp.vipo.seller.common.exception.enums.VipoAuthenticationException;
import com.vtp.vipo.seller.common.utils.JsonMapperUtils;
import com.vtp.vipo.seller.services.authen.AuthenticationStrategy;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

/**
 * Authentication strategy implementation for VTP SSO (Single Sign-On).
 *
 * <p>This class handles the authentication process for users via VTP's SSO service.
 * It uses {@link VtpApiClient} to communicate with the external SSO API and manages
 * user data in the local database using {@link MerchantEntity}
 * </p>
 */
@Validated
@Component("VTP_SSO")
@RequiredArgsConstructor
public class VtpSsoStrategy implements AuthenticationStrategy {

    private final VtpApiClient vtpApiClient;

    private final MerchantRepository merchantRepository;

    private final SellerOpenRepository sellerOpenRepository;

    private final MerchantLogRepository merchantLogRepository;

    private final MerchantGroupRepository merchantGroupRepository;

    // Parameter used to indicate the source of the update request (specific to VIPO).
    private static final int VTP_SSO_UPDATE_USER_SOURCE_PARAM_FOR_VIPO = 5;

    /**
     * Authenticates a user via VTP's SSO.
     *
     * <p>This method calls the SSO API to validate the user's credentials and retrieves
     * their information. If the user doesn't exist in the local database, it creates
     * a new user and associates them with the VTP provider.</p>
     *
     * @param identifier The user identifier (not used in this implementation).
     * @param credential The user's SSO token.
     * @return The authenticated {@link MerchantEntity}.
     * @throws VipoAuthenticationException if the SSO API response indicates an error or invalid credentials.
     */
    @Transactional
    @Override
    public MerchantEntity authenticate(String identifier, String credential) {
        // Call the SSO API to update and validate user credentials.
        SsoUpdateUserResponse ssoUpdateUserResponse = vtpApiClient.updateUser(
                SsoUpdateUserRequest.builder()
                        .tokenSSO(credential)
                        .source(VTP_SSO_UPDATE_USER_SOURCE_PARAM_FOR_VIPO)
                        .build()
        );

        // Validate the response and throw an exception if the response is invalid or indicates an error.
        if (
                ObjectUtils.isEmpty(ssoUpdateUserResponse)
                        || (ObjectUtils.isNotEmpty(ssoUpdateUserResponse.getError()) && ssoUpdateUserResponse.getError())
                        || ObjectUtils.isEmpty(ssoUpdateUserResponse.getData())
                        || ObjectUtils.isEmpty(ssoUpdateUserResponse.getData().getUserId())
        ) {
            throw VipoAuthenticationException.INVALID_OAUTH2_CREDENTIAL.asException();
        }

        // Extract user data from the response.
        SsoUpdateUserResponse.DataResponse dataResponse = ssoUpdateUserResponse.getData();
        SsoUpdateUserResponse.UserInfoResponse userInfoResponse = dataResponse.getUserInfoResponse();

        /* Extract phone number from dataResponse or userInfoResponse. */
        String phone = null;
        if (StringUtils.isNotBlank(dataResponse.getPhone())) {
            phone = dataResponse.getPhone();
        } else if (ObjectUtils.isNotEmpty(userInfoResponse)) {
            phone = userInfoResponse.getPhone();
        }

        MerchantEntity user = merchantRepository.findFirstByVtpUserId(dataResponse.getUserId());

        if (ObjectUtils.isEmpty(user) && StringUtils.isNotBlank(phone)) {
            user = merchantRepository.findByContactPhone(phone); //todo: check if this rule still usable
            if (ObjectUtils.isNotEmpty(user)) {
                user.setVtpUserId(dataResponse.getUserId());
                if (ObjectUtils.isEmpty(merchantRepository.save(user)))
                    throw new VipoFailedToExecuteException("Failed to link vtp account to current user");
            }
        }

        // If the user doesn't exist in the local database, create and save a new user.
        if (ObjectUtils.isEmpty(user)) {
            user = merchantRepository.save(toMerchantEntity(dataResponse));
            sellerOpenRepository.save(SellerOpenEntity.builder()
                    .platformType(null)
                    .merchantId(user.getId())
                    .sellerOpenId(genSellerOpenId())
                    .build());

            /* Store a merchant_log entry when a merchant first appears in the system. */
            merchantLogRepository.save(
                    MerchantLogEntity.builder()
                            .merchantId(user.getId())
                            .action(MerchantLogActionEnum.MERCHANT_CREATED.name())
                            .note(MerchantLogActionEnum.MERCHANT_CREATED.getMsg())
                            .data(JsonMapperUtils.writeValueAsString(user))
                            .build()
            );

        }

        return user;
    }

    /**
     * copy from Merchant Service Impl
     */
    private String genSellerOpenId() {
        return String.format("vipo-seller-%s", UUID.randomUUID());
    }

    /**
     * Maps the SSO API response to a {@link MerchantEntity} entity.
     *
     * @param dataResponse The {@link SsoUpdateUserResponse.DataResponse} containing user information.
     * @return A {@link MerchantEntity} entity populated with the provided data.
     */
    private MerchantEntity toMerchantEntity(@NotNull SsoUpdateUserResponse.DataResponse dataResponse) {
        SsoUpdateUserResponse.UserInfoResponse userInfoResponse = dataResponse.getUserInfoResponse();
        boolean isNotNullUserInfoResponse = ObjectUtils.isNotEmpty(userInfoResponse);

        /* Extract email from dataResponse or userInfoResponse. */
        String email = null;
        if (StringUtils.isNotBlank(dataResponse.getEmail())) {
            email = dataResponse.getEmail();
        } else if (ObjectUtils.isNotEmpty(userInfoResponse)) {
            email = userInfoResponse.getEmail();
        }

        /* Extract phone number from dataResponse or userInfoResponse. */
        String phone = null;
        if (StringUtils.isNotBlank(dataResponse.getPhone())) {
            phone = dataResponse.getPhone();
        } else if (ObjectUtils.isNotEmpty(userInfoResponse)) {
            phone = userInfoResponse.getPhone();
        }
        //note: set default merchant group . need to update later
        List<MerchantGroupEntity> merchantGroupEntities = merchantGroupRepository.findAll();
        Long firstMerchantGroupEntity
                = ObjectUtils.isNotEmpty(merchantGroupEntities) ?
                merchantGroupEntities.get(0).getId() : null;

        /* Build and return the User entity. */
        return MerchantEntity.builder()
                .vtpUserId(dataResponse.getUserId())
                .contactEmail(email)
                .contactPhone(phone)
                .name(
                        isNotNullUserInfoResponse
                                ? (
                                StringUtils.isNotBlank(userInfoResponse.getDisplayName())
                                        ? userInfoResponse.getDisplayName()
                                        : userInfoResponse.getLastName() + " " + userInfoResponse.getFirstName()
                        )
                                : null
                )
                .provinceId(isNotNullUserInfoResponse ? userInfoResponse.getProvinceId() : null)
                .districtId(isNotNullUserInfoResponse ? userInfoResponse.getDistrictId() : null)
                .wardId(isNotNullUserInfoResponse ? userInfoResponse.getWardsId() : null)
                .address(isNotNullUserInfoResponse ? userInfoResponse.getHomeNo() : null)
                .fullAddress(isNotNullUserInfoResponse ? userInfoResponse.getAddress() : null)
                .status(MerchantStatusEnum.PENDING_APPROVAL.getValue())
                .inactive(MerchantInactiveStatus.ACTIVE.getValue())
                .countryId(Constants.VIETNAM_COUNTRY_ID)
                .isPolicyTerms(true)
                .allowMultipleStores(AllowMultipleStores.NOT_ALLOWED.getValue())
                .merchantGroupId(firstMerchantGroupEntity)
                .build();
    }


}