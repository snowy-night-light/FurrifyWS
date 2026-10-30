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