package com.vtp.vipo.seller.services.evtp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.dto.request.TokenEvtpRequest;
import com.vtp.vipo.seller.common.dto.response.TokenEvtpResponse;
import com.vtp.vipo.seller.common.utils.APIUtils;
import com.vtp.vipo.seller.services.evtp.EvtpTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service("evtpTokenService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvtpTokenServiceImpl implements EvtpTokenService {

    @Value("${evtp.api.get-short-term-partner-token}")
    String getShortTermPartnerTokenApiUrl;

    @Value("${evtp.api.get-long-term-partner-token}")
    String getLongTermPartnerTokenApiUrl;

    @Value("${evtp.api.get-client-token}")
    String getClientTokenApiUrl;

    final APIUtils apiUtils;

    @Override
    public TokenEvtpResponse getShortTermPartnerToken(TokenEvtpRequest request) {
        log.info("[EvtpTokenService getShortTermPartnerToken] Request: {}", request);
        TokenEvtpResponse response = apiUtils.callApiSendJson(
                getShortTermPartnerTokenApiUrl,
                HttpMethod.POST,
                null,
                null,
                null,
                request,
                new TypeReference<TokenEvtpResponse>() {
                }
        );
        log.info("[EvtpTokenService getShortTermPartnerToken] Response: {}", response);
        return response;
    }

    @Override
    public TokenEvtpResponse getLongTermPartnerToken(TokenEvtpRequest request, String shortTermToken) {
        log.info("[EvtpTokenService getLongTermPartnerToken] Request: {}, shortTermToken: {}", request, shortTermToken);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Token", shortTermToken);

        TokenEvtpResponse response = apiUtils.callApiSendJson(
                getLongTermPartnerTokenApiUrl,
                HttpMethod.POST,
                null,
                headers,
                null,
                request,
                new TypeReference<TokenEvtpResponse>() {
                }
        );
        log.info("[EvtpTokenService getLongTermPartnerToken] Response: {}", response);
        return response;
    }

    @Override
    public TokenEvtpResponse getClientToken(TokenEvtpRequest request, String longTermToken) {
        log.info("[EvtpTokenService getClientToken] Request: {}, longTermToken: {}", request, longTermToken);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Token", longTermToken);

        TokenEvtpResponse response = apiUtils.callApiSendJson(
                getClientTokenApiUrl,
                HttpMethod.POST,
                null,
                headers,
                null,
                request,
                new TypeReference<TokenEvtpResponse>() {
                }
        );
        log.info("[EvtpTokenService getClientToken] Response: {}", response);
        return response;
    }
}



