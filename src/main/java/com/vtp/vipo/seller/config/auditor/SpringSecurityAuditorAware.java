package com.vtp.vipo.seller.config.auditor;

import com.vtp.vipo.seller.config.security.VipoUserDetails;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This class is responsible for providing the current auditor (i.e., the current user) for auditing purposes.
 * It implements the {@link AuditorAware} interface from Spring Data to return the user ID that is currently authenticated
 * within the security context.
 * <p>
 * It is used by Spring Data JPA to set the 'createdBy' and 'updatedBy' fields when auditing entities.
 */
@Component
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_AUDITOR = "vipo-seller";

    /**
     * Retrieves the current auditor (user ID) from the SecurityContextHolder.
     * This method is called by Spring Data JPA for auditing purposes.
     *
     * @return an {@link Optional} containing the current auditor's user ID if authenticated, or an empty {@link Optional} if not.
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (ObjectUtils.isEmpty(authentication) || !authentication.isAuthenticated()) {
                // No authenticated user found; return system auditor
                return Optional.of(SYSTEM_AUDITOR);
            }

            Object principal = authentication.getPrincipal();

            if (!(principal instanceof VipoUserDetails)) {
                // Principal is not of type VipoUserDetails; return system auditor
                return Optional.of(SYSTEM_AUDITOR);
            }

            VipoUserDetails vipoUserDetails = (VipoUserDetails) principal;

            if (ObjectUtils.isEmpty(vipoUserDetails) || ObjectUtils.isEmpty(vipoUserDetails.getId())) {
                // User details or user ID are missing; return system auditor
                return Optional.of(SYSTEM_AUDITOR);
            }

            // Return the user ID as the auditor
            return Optional.of(String.valueOf(vipoUserDetails.getId()));
        } catch (Exception exception) {
            // In case of any unexpected exceptions, default to system auditor
            return Optional.of(SYSTEM_AUDITOR);
        }
    }
}

