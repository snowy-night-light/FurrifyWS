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

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import static ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor.KEYCLOAK_INTERNAL_CONFIG_ID;
import static ws.furrify.core.config.feign.SmartOAuth2FeignRequestInterceptor.PRINCIPAL_SUFFIX;

public class OAuth2FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new ErrorDecoder.Default();
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String principalName;

    public OAuth2FeignErrorDecoder(OAuth2AuthorizedClientService authorizedClientService, String appName) {
        this.authorizedClientService = authorizedClientService;
        this.principalName = appName + PRINCIPAL_SUFFIX;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 401) {
            authorizedClientService.removeAuthorizedClient(KEYCLOAK_INTERNAL_CONFIG_ID, principalName);

            return new RetryableException(
                    response.status(),
                    "Unauthorized",
                    response.request().httpMethod(),
                    (Long) null,
                    response.request()
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}