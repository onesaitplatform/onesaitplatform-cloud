/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.onelog;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.Getter;
import lombok.Setter;


public class GelfTcpTlsAppender extends GelfTcpAppender {

    /**
     * If {@code true}, trust all TLS certificates (even self signed certificates).
     */
	@Getter
	@Setter
    private boolean trustAllCertificates;

    @Override
    protected SSLSocketFactory initSocketFactory() {
        if (trustAllCertificates) {
            addWarn("Enable trustAllCertificates - don't use this in production!");
            try {
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, OneLogUtils.buildNoopTrustManagers(), new SecureRandom());
                return context.getSocketFactory();
            } catch (final NoSuchAlgorithmException | KeyManagementException e) {
                throw new IllegalStateException(e);
            }
        }
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

}
