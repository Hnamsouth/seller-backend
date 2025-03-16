package com.vtp.vipo.seller.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class APIUtils {

    private final Environment environment;

    public <T> T callApiSendJson(String apiUrl, HttpMethod method, HttpServletRequest extHeaders, MultiValueMap<String, String> additionalHeaders, Map<String, Object> queryParams, Object body, TypeReference<T> reference) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<JsonNode> responseEntity;
            HttpHeaders headers = new HttpHeaders();
            if (!ObjectUtils.isEmpty(extHeaders)) {
                headers.setAll(Collections.list(extHeaders.getHeaderNames())
                        .stream()
                        .collect(Collectors.toMap(h -> h, extHeaders::getHeader)));
                headers.put("accept-encoding", Collections.singletonList("identity"));
            }

            if (!ObjectUtils.isEmpty(additionalHeaders)) {
                headers.addAll(additionalHeaders);
            }

//            String[] activeProfiles = environment.getActiveProfiles();
//
//            /* Set proxy to restTempalte */
//            if (activeProfiles.length > 0) {
//                String highestPriorityProfile = activeProfiles[activeProfiles.length - 1];
//                if (highestPriorityProfile.equals("local")) {
//                    log.info("Setting proxy for local profile");
//                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.60.117.113", 8080));
//                    requestFactory.setProxy(proxy);
//                    restTemplate.setRequestFactory(requestFactory);
//                }
//            }

            headers.put("Content-Type", Collections.singletonList("application/json"));
            URI uri = buildUriWithQueryParams(apiUrl, queryParams);
            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
            responseEntity = restTemplate.exchange(uri.toString(), method, requestEntity, JsonNode.class);
            return JsonMapperUtils.convertJsonToObject(JsonMapperUtils.writeValueAsString(responseEntity.getBody()), reference);
        } catch (HttpStatusCodeException e) {
            log.info("[HttpStatusCodeException] Api Exception: {}", e.getLocalizedMessage());
            throw e;
        } catch (Exception e) {
            log.info("Api Exception: {}", e.getLocalizedMessage());
            throw new ResourceAccessException(BaseExceptionConstant.FAILED_TO_CALL_DESCRIPTION);
        }
    }

    // Build the URI with query parameters
    private URI buildUriWithQueryParams(String baseUrl, Map<String, Object> queryParams) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        if (!Objects.isNull(queryParams)) {
            queryParams.forEach(uriBuilder::queryParam);
        }
        return uriBuilder.build().toUri();
    }

}