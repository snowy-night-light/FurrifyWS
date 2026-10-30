/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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