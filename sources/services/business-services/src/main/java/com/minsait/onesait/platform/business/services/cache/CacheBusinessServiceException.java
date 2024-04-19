/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.business.services.cache;

import lombok.Getter;

public class CacheBusinessServiceException extends Exception {

    private static final long serialVersionUID = -192328143236459940L;

    public enum Error {
        CACHE_DOES_NOT_EXIST, CACHE_WAS_NOT_CREATED, NAME_OF_MAP_ALREADY_USED, UNSUPPORTED_TYPE
    }

    @Getter
    private final Error error;
    
    public CacheBusinessServiceException(Error error) {
        super();
        this.error = error;
    }

    public CacheBusinessServiceException(Error error, String message, Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public CacheBusinessServiceException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public CacheBusinessServiceException(Error error, Throwable cause) {
        super(cause);
        this.error = error;
    }
}
