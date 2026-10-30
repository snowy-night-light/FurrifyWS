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

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ws.furrify.core.utils.SecurityContextUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SmartOAuth2FeignRequestInterceptorTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextUtils.clearFeignClientSecurityContext();
    }

    @Test
    void propagatesOwnerHeaderWhenForwardingBearerToken() {
        UUID ownerId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer inbound-token");
        request.addHeader(SecurityContextUtils.USER_ID_HEADER, ownerId.toString());
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        RequestTemplate template = new RequestTemplate();
        SmartOAuth2FeignRequestInterceptor interceptor = new SmartOAuth2FeignRequestInterceptor(
                mock(OAuth2AuthorizedClientManager.class),
                "worker"
        );

        interceptor.apply(template);

        assertEquals("Bearer inbound-token", template.headers().get(HttpHeaders.AUTHORIZATION).iterator().next());
        assertEquals(ownerId.toString(), template.headers().get(SecurityContextUtils.USER_ID_HEADER).iterator().next());
    }
}
