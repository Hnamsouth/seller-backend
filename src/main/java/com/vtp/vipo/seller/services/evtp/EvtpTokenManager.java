package com.vtp.vipo.seller.services.evtp;

import com.vtp.vipo.seller.common.dto.request.TokenEvtpRequest;
import com.vtp.vipo.seller.common.dto.response.TokenEvtpResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component("evtpTokenManager")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvtpTokenManager {

    String shortTermToken;
    String longTermToken;
    String clientToken;

    final EvtpTokenService evtpTokenService;

    @Value("${evtp.account.username}")
    String username;

    @Value("${evtp.account.password}")
    String password;

    public EvtpTokenManager(EvtpTokenService evtpTokenService) {
        this.evtpTokenService = evtpTokenService;
    }

    private TokenEvtpRequest buildTokenRequest() {
        return TokenEvtpRequest.builder()
                .username(username)
                .password(password)
                .build();
    }

    public String getShortTermToken() {
        if (ObjectUtils.isEmpty(shortTermToken)) {
            fetchShortTermToken();
        }
        return shortTermToken;
    }

    public String getLongTermToken() {
        if (ObjectUtils.isEmpty(longTermToken)) {
            // Đảm bảo shortTermToken trước
            getShortTermToken();
            fetchLongTermToken();
        }
        return longTermToken;
    }

    public String getClientToken() {
        if (ObjectUtils.isEmpty(clientToken)) {
            // Đảm bảo longTermToken trước
            getLongTermToken();
            fetchClientToken();
        }
        return clientToken;
    }

    public void refreshClientToken() {
        clientToken = null;
        try {
            fetchClientToken();
        } catch (VipoBusinessException e) {
            // LongTermToken có thể hết hạn
            longTermToken = null;
            fetchLongTermToken();
            fetchClientToken();
        }
    }

    public void refreshLongTermToken() {
        longTermToken = null;
        try {
            fetchLongTermToken();
        } catch (VipoBusinessException e) {
            // ShortTermToken hết hạn
            shortTermToken = null;
            fetchShortTermToken();
            fetchLongTermToken();
        }
        // Sau khi refresh longTerm xong, refresh clientToken
        clientToken = null;
        fetchClientToken();
    }

    public void refreshShortTermToken() {
        shortTermToken = null;
        fetchShortTermToken();

        longTermToken = null;
        fetchLongTermToken();

        clientToken = null;
        fetchClientToken();
    }

    private void fetchShortTermToken() {
        TokenEvtpResponse response = evtpTokenService.getShortTermPartnerToken(buildTokenRequest());
        validateResponse(response);
        shortTermToken = response.getData().getToken();
    }

    private void fetchLongTermToken() {
        TokenEvtpResponse response = evtpTokenService.getLongTermPartnerToken(buildTokenRequest(), getShortTermToken());
        validateResponse(response);
        longTermToken = response.getData().getToken();
    }

    private void fetchClientToken() {
        TokenEvtpResponse response = evtpTokenService.getClientToken(buildTokenRequest(), getLongTermToken());
        validateResponse(response);
        clientToken = response.getData().getToken();
    }

    private void validateResponse(TokenEvtpResponse response) {
        if (ObjectUtils.isEmpty(response) ||
                ObjectUtils.isEmpty(response.getData()) ||
                ObjectUtils.isEmpty(response.getData().getToken())) {
            throw new VipoBusinessException();
        }
    }
}


