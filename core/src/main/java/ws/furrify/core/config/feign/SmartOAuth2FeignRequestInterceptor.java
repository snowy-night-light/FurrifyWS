/*
 * furrify-core - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.core.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.Optional;
import java.util.UUID;

public class SmartOAuth2FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SmartOAuth2FeignRequestInterceptor.class);

    protected static final String KEYCLOAK_INTERNAL_CONFIG_ID = "keycloak-internal";
    protected static final String PRINCIPAL_SUFFIX = "-client";

    private final OAuth2AuthorizedClientManager serviceOAuth2AuthorizedClientManager;
    private final String principalName;

    public SmartOAuth2FeignRequestInterceptor(
            OAuth2AuthorizedClientManager serviceOAuth2AuthorizedClientManager,
            String appName) {

        this.serviceOAuth2AuthorizedClientManager = serviceOAuth2AuthorizedClientManager;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes =
                RequestContextHolder.getRequestAttributes();

        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            String ownerId =
                    servletRequestAttributes.getRequest()
                            .getHeader(SecurityContextUtils.USER_ID_HEADER);

            if (ownerId != null && !ownerId.isBlank()) {
                template.header(SecurityContextUtils.USER_ID_HEADER, ownerId);
            }

            String authorization =
                    servletRequestAttributes.getRequest()
                            .getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization != null && authorization.startsWith("Bearer ")) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
                addCurrentSubjectHeaderIfMissing(template);
                return;
            }
        }

        OAuth2AuthorizeRequest authorizeRequest =
                OAuth2AuthorizeRequest
                        .withClientRegistrationId(KEYCLOAK_INTERNAL_CONFIG_ID)
                        .principal(principalName)
                        .build();

        OAuth2AuthorizedClient authorizedClient = null;
        int maxAttempts = 3;
        for (int attempts = 1; attempts <= maxAttempts; attempts++) {
            try {
                synchronized (this) {
                    authorizedClient = serviceOAuth2AuthorizedClientManager.authorize(authorizeRequest);
                }
                break;
            } catch (Exception e) {
                if (attempts == maxAttempts) {
                    throw e;
                }
                log.warn("Failed to obtain OAuth2 token (attempt {}/{}). Retrying... Cause: {}", attempts, maxAttempts, e.getMessage());
                try {
                    Thread.sleep((long) 1000 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting to retry OAuth2 authorization", ie);
                }
            }
        }

        if (authorizedClient == null) {
            throw new IllegalStateException(
                    "Unable to obtain service access token for client '"
                            + KEYCLOAK_INTERNAL_CONFIG_ID + "'"
            );
        }

        template.header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + authorizedClient.getAccessToken().getTokenValue()
        );

        log.debug("Using service token for Feign call to {}, expires: {}",
                template.url(), authorizedClient.getAccessToken().getExpiresAt());

        addCurrentSubjectHeaderIfMissing(template);
    }

    private void addCurrentSubjectHeaderIfMissing(RequestTemplate template) {
        if (template.headers().containsKey(SecurityContextUtils.USER_ID_HEADER)) {
            return;
        }

        resolveCurrentSubject().ifPresent(subject ->
                template.header(SecurityContextUtils.USER_ID_HEADER, subject.toString())
        );
    }

    private Optional<UUID> resolveCurrentSubject() {
        try {
            return SecurityContextUtils.getCurrentSubject();
        } catch (AccessDeniedException | IllegalArgumentException | IllegalStateException e) {
            log.debug("Could not resolve current subject for Feign owner propagation: {}", e.getMessage());
            return Optional.empty();
        }
    }
}