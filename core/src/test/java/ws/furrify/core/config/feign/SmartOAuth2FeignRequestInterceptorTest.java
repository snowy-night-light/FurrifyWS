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
