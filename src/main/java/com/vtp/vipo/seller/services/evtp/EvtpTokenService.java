package com.vtp.vipo.seller.services.evtp;

import com.vtp.vipo.seller.common.dto.request.TokenEvtpRequest;
import com.vtp.vipo.seller.common.dto.response.TokenEvtpResponse;

public interface EvtpTokenService {
    TokenEvtpResponse getShortTermPartnerToken(TokenEvtpRequest request);

    TokenEvtpResponse getLongTermPartnerToken(TokenEvtpRequest request, String shortTermToken);

    TokenEvtpResponse getClientToken(TokenEvtpRequest request, String longTermToken);
}

