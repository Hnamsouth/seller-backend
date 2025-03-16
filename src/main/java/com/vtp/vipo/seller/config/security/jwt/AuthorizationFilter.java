package com.vtp.vipo.seller.config.security.jwt;

import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.exception.VipoUnAuthorizationException;
import com.vtp.vipo.seller.config.security.VipoUserDetails;
import com.vtp.vipo.seller.config.security.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    public AuthorizationFilter(JwtTokenService tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        VipoUserDetails user = null;
        try {
            user = validateTokenAndReturnUserDetails(request);
            setUpSpringAuthentication(user);
        } catch (ExpiredJwtException e) {
            response.setHeader(BaseExceptionConstant.VIPO_STATUS_HEADER, BaseExceptionConstant.EXPIRED_VIPO_TOKEN);
        } catch (VipoUnAuthorizationException | UnsupportedJwtException | MalformedJwtException |
                 SignatureException e) {
            response.setHeader(BaseExceptionConstant.VIPO_STATUS_HEADER, BaseExceptionConstant.VIPO_INVALID_TOKEN);
        } catch (Exception e) {
            logger.error(Constants.UNKNOWN_ERROR_VALIDATING_JWT_TOKEN, e);
        }
        finally {
            chain.doFilter(request, response);
        }
    }

    private VipoUserDetails validateTokenAndReturnUserDetails(HttpServletRequest request) {
        VipoUserDetails info = null;
        try {
            String jwtToken = parseJwt(request);
            if (StringUtils.hasText(jwtToken)) {
                info = tokenProvider.getUserInfoFromToken(jwtToken);
//                info = (VipoUserDetails) userDetailsService.loadUserByUsername(info.getId().toString());
                if (ObjectUtils.isEmpty(info)) return null;
                return info;
            } else {
                throw new VipoUnAuthorizationException(BaseExceptionConstant.VIPO_INVALID_TOKEN, BaseExceptionConstant.VIPO_INVALID_TOKEN_DESCRIPTION);
            }
        } finally {
            if (info == null || ObjectUtils.isEmpty(info)) {
                request.getSession().setAttribute(Constants.LOGGED_USER, info);
            } else {
                request.getSession().removeAttribute(Constants.LOGGED_USER);
            }
        }
    }

    /**
     * Add user info to Spring context
     */
    private void setUpSpringAuthentication(VipoUserDetails user) {
        if (ObjectUtils.isEmpty(user)) {
            SecurityContextHolder.clearContext();
        } else {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

}
