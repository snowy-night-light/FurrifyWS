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
package ws.furrify.core.exception.handler;

import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ws.furrify.core.exception.Errors;

@RestControllerAdvice
public class GlobalFeignExceptionHandler {

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ProblemDetail handleNoFallbackAvailableException(NoFallbackAvailableException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                Errors.SERVICE_TEMPORARILY_UNAVAILABLE.getErrorMessage()
        );
    }

    @ExceptionHandler(feign.FeignException.ServiceUnavailable.class)
    public ProblemDetail handleServiceUnavailable(feign.FeignException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                Errors.SERVICE_TEMPORARILY_UNAVAILABLE.getErrorMessage()
        );
    }
}