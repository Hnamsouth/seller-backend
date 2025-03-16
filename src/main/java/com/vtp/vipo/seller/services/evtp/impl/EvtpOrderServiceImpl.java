package com.vtp.vipo.seller.services.evtp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vtp.vipo.seller.common.dto.request.CreateFullOrderEvtpRequest;
import com.vtp.vipo.seller.common.dto.request.CreateSimplifiedOrderEvtpRequest;
import com.vtp.vipo.seller.common.dto.request.PrintLabelOrderRequest;
import com.vtp.vipo.seller.common.dto.request.ServiceInfoEvtpRequest;
import com.vtp.vipo.seller.common.dto.response.CreateOrderEvtpResponse;
import com.vtp.vipo.seller.common.dto.response.PrintLabelOrderResponse;
import com.vtp.vipo.seller.common.dto.response.ServiceInfoEvtpResponse;
import com.vtp.vipo.seller.common.utils.APIUtils;
import com.vtp.vipo.seller.services.evtp.EvtpOrderService;
import com.vtp.vipo.seller.services.evtp.EvtpTokenManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

@Slf4j
@Service("evtpOrderService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvtpOrderServiceImpl implements EvtpOrderService {

    @Value("${evtp.api.create-full-order}")
    String createFullOrderApiUrl;

    @Value("${evtp.api.create-simplified-order}")
    String createSimplifiedOrderApiUrl;

    @Value("${evtp.api.print-label-order}")
    String printLabelOrderApiUrl;

    @Value("${evtp.token}")
    String evtpToken;

    @Value("${evtp.api.get-service-info}")
    String getServiceInfoApiUrl;

    final EvtpTokenManager evtpTokenManager;

    final APIUtils apiUtils;

    @Override
    public CreateOrderEvtpResponse createFullOrder(CreateFullOrderEvtpRequest request) {
        log.info("[EvtpOrderService createFullOrder] Create full order request: {}", request);
        MultiValueMap<String, String> additionalHeaders = new LinkedMultiValueMap<>();
        additionalHeaders.add("Token", evtpToken);
        CreateOrderEvtpResponse response = apiUtils.callApiSendJson(
                createFullOrderApiUrl,
                HttpMethod.POST,
                null,
                additionalHeaders,
                null,
                request,
                new TypeReference<CreateOrderEvtpResponse>() {
                }
        );

        log.info("[EvtpOrderService createFullOrder] Create full order response: {}", response);
        return response;
    }

    @Override
    public CreateOrderEvtpResponse createSimplifiedOrder(CreateSimplifiedOrderEvtpRequest request) {
        log.info("[EvtpOrderService createSimplifiedOrder] Create simplified order request: {}", request);
        CreateOrderEvtpResponse response = callApiWithClientToken(
                createSimplifiedOrderApiUrl,
                HttpMethod.POST,
                request,
                new TypeReference<CreateOrderEvtpResponse>() {
                }
        );
        log.info("[EvtpOrderService createSimplifiedOrder] Create simplified order response: {}", response);
        return response;
    }

    @Override
    public PrintLabelOrderResponse printLabelOrder(PrintLabelOrderRequest request) {
        log.info("[EvtpOrderService printLabelOrder] Print label order request: {}", request);
        MultiValueMap<String, String> additionalHeaders = new LinkedMultiValueMap<>();
        additionalHeaders.add("Token", evtpToken);
        PrintLabelOrderResponse response = apiUtils.callApiSendJson(
                printLabelOrderApiUrl,
                HttpMethod.POST,
                null,
                additionalHeaders,
                null,
                request,
                new TypeReference<PrintLabelOrderResponse>() {
                }
        );

        log.info("[EvtpOrderService printLabelOrder] Print label order response: {}", response);
        return response;
    }

    @Override
    public List<ServiceInfoEvtpResponse> getServiceInfo(ServiceInfoEvtpRequest request) {
        log.info("[EvtpOrderService getServiceInfo] Get service info request: {}", request);
        MultiValueMap<String, String> additionalHeaders = new LinkedMultiValueMap<>();
        additionalHeaders.add("Token", evtpToken);
        List<ServiceInfoEvtpResponse> response = apiUtils.callApiSendJson(
                getServiceInfoApiUrl,
                HttpMethod.POST,
                null,
                additionalHeaders,
                null,
                request,
                new TypeReference<List<ServiceInfoEvtpResponse>>() {
                }
        );

        log.info("[EvtpOrderService getServiceInfo] Print label order response: {}", response);
        return response;
    }

    /**
     * Generic method to call API with client token. If 401 occurs, refetch token and retry once.
     *
     * @param url     API URL
     * @param method  HttpMethod (GET, POST, ...)
     * @param payload request body object
     * @param typeRef TypeReference of response
     * @param <T>     Payload type
     * @param <R>     Response type
     * @return Response object of type R
     */
    private <T, R> R callApiWithClientToken(String url, HttpMethod method, T payload, TypeReference<R> typeRef) {
        log.info("[EvtpOrderService callApiWithClientToken] Call API with client token: {}", url);

        // Lấy token qua EvtpTokenManager để đảm bảo có token hợp lệ
        String clientToken = evtpTokenManager.getClientToken();
        log.info("[EvtpOrderService callApiWithClientToken] Token: {}", clientToken);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Token", clientToken);

        try {
            return apiUtils.callApiSendJson(url, method, null, headers, null, payload, typeRef);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Refresh token
                evtpTokenManager.refreshClientToken();
                headers.set("Token", evtpTokenManager.getClientToken());
                // Gọi lại
                return apiUtils.callApiSendJson(url, method, null, headers, null, payload, typeRef);
            } else {
                throw e;
            }
        }
    }
}

