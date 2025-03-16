package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.RequestConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Set;

@Slf4j
public final class RequestUtils {

    private static final String VERSION = "version";

    private static final String OPERA = "opera";

    private RequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getTokenFromRequest(HttpServletRequest request) {
        String authenticationHeader = request.getHeader(Constants.HEADER);
        if (ObjectUtils.isEmpty(authenticationHeader)) {
            authenticationHeader = request.getHeader(Constants.HEADER2);
        }
        return authenticationHeader;
    }

    public static String extractOs(HttpServletRequest request) {
        var os = StringUtils.EMPTY;
        if (request == null) {
            return os;
        }
        var userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("windows")) {
            os = "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            os = "Mac";
        } else if (userAgent.toLowerCase().contains("x11")) {
            os = "Unix";
        } else if (userAgent.toLowerCase().contains("android")) {
            os = "Android";
        } else if (userAgent.toLowerCase().contains("iphone")) {
            os = "IPhone";
        } else {
            os = "UnKnown, More-Info: " + userAgent;
        }
        return os;
    }

    public static String extractBrowser(HttpServletRequest request) {
        var browser = StringUtils.EMPTY;
        if (request == null) {
            return browser;
        }
        var userAgent = request.getHeader("User-Agent").toLowerCase();
        if (userAgent.contains("msie")) {
            var substring = userAgent.substring(userAgent.indexOf("msie")).split(";")[0];
            browser =
                    substring.split(StringUtils.SPACE)[0].replace("msie", "IE")
                            + "-"
                            + substring.split(StringUtils.SPACE)[1];
        } else if (userAgent.contains("safari") && userAgent.contains(VERSION)) {
            browser =
                    (userAgent.substring(userAgent.indexOf("safari")).split(StringUtils.SPACE)[0])
                            .split("/")[0]
                            + "-"
                            + (userAgent.substring(userAgent.indexOf(VERSION)).split(StringUtils.SPACE)[0])
                            .split("/")[1];
        } else if (userAgent.contains("opr") || userAgent.contains(OPERA)) {
            if (userAgent.contains(OPERA)) {
                browser =
                        (userAgent.substring(userAgent.indexOf(OPERA)).split(StringUtils.SPACE)[0])
                                .split("/")[0]
                                + "-"
                                + (userAgent.substring(userAgent.indexOf(VERSION)).split(StringUtils.SPACE)[0])
                                .split("/")[1];
            } else if (userAgent.contains("opr")) {
                browser =
                        ((userAgent.substring(userAgent.indexOf("opr")).split(StringUtils.SPACE)[0])
                                .replace("/", "-"))
                                .replace("opr", "Opera");
            }
        } else if (userAgent.contains("chrome")) {
            browser =
                    (userAgent.substring(userAgent.indexOf("chrome")).split(StringUtils.SPACE)[0])
                            .replace("/", "-");
        } else if ((userAgent.contains("mozilla/7.0"))
                || (userAgent.contains("netscape6"))
                || (userAgent.contains("mozilla/4.7"))
                || (userAgent.contains("mozilla/4.78"))
                || (userAgent.contains("mozilla/4.08"))
                || (userAgent.contains("mozilla/3"))) {
            browser = "Netscape-?";
        } else if (userAgent.contains("firefox")) {
            browser =
                    (userAgent.substring(userAgent.indexOf("firefox")).split(StringUtils.SPACE)[0])
                            .replace("/", "-");
        } else if (userAgent.contains("rv")) {
            browser = "IE-" + userAgent.substring(userAgent.indexOf("rv") + 3, userAgent.indexOf(")"));
        } else {
            browser = "UnKnown, More-Info: " + userAgent;
        }
        return browser;
    }

    public static String extractClientIpAddress(HttpServletRequest request) {
        for (String header : RequestConstant.HEADERS_TO_TRY) {
            var ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static boolean matches(HttpServletRequest request, Set<String> excludePatterns) {
        if (StringUtils.isEmpty(request.getContextPath())) {
            return matches(request.getRequestURI(), excludePatterns);
        }
        return matches(
                request.getRequestURI().substring(request.getContextPath().length()), excludePatterns);
    }

    public static boolean matches(String lookupPath, Set<String> excludePatterns) {
        PathMatcher pathMatcherToUse = new AntPathMatcher();
        if (!CollectionUtils.isEmpty(excludePatterns)) {
            for (String pattern : excludePatterns) {
                if (pathMatcherToUse.match(pattern, lookupPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean existedRequestBody(Object o) {
        try {
            var method = ((HandlerMethod) o).getMethod();
            var annotations = method.getParameterAnnotations();
            for (Annotation[] annotation : annotations) {
                for (Annotation tmp : annotation) {
                    if (tmp instanceof RequestBody) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public static String extractAuthentication(HttpServletRequest servletRequest) {
        return servletRequest.getHeader(RequestConstant.AUTHORIZATION);
    }

    public static String extractToken(HttpServletRequest servletRequest) {
        var auth = extractAuthentication(servletRequest);
        if (auth != null) {
            if (auth.startsWith(RequestConstant.BEARER_PREFIX)) {
                return auth.replace(RequestConstant.BEARER_PREFIX, StringUtils.EMPTY);
            }
            if (auth.startsWith(RequestConstant.BASIC_PREFIX)) {
                return auth.replace(RequestConstant.BASIC_PREFIX, StringUtils.EMPTY);
            }
            return auth;
        }
        return null;
    }

    public static String extractRequestId() {
        return ThreadContext.get(RequestConstant.REQUEST_ID);
    }

    public static String extractSessionId() {
        return ThreadContext.get(RequestConstant.SESSION_ID);
    }

    public static Locale extractLocale() {
        var lang = ThreadContext.get(RequestConstant.CLIENT_LANG);
        return lang == null ? Locale.ENGLISH : new Locale(lang);
    }

}
