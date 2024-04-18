/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.report.custom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRResourcesUtil;
import net.sf.jasperreports.repo.DefaultRepositoryService;
import net.sf.jasperreports.repo.RepositoryContext;
import net.sf.jasperreports.repo.ResourceInfo;
import net.sf.jasperreports.repo.StandardResourceInfo;

@Slf4j
public class CustomRepositoryService extends DefaultRepositoryService {

	public CustomRepositoryService(JasperReportsContext jasperReportsContext) {
		super(jasperReportsContext);
	}

	@Override
	public InputStream getInputStream(RepositoryContext context, String uri) {

		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, dummyTrustManager, new java.security.SecureRandom());
			final HostnameVerifier allHostsValid = (hostname, session) -> true;
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			URL url = JRResourcesUtil.createURL(uri, urlHandlerFactory);
			if (url != null) {
				return JRLoader.getInputStream(url);
			}

			final File file = resolveFile(context, uri);
			if (file != null) {
				return JRLoader.getInputStream(file);
			}

			url = JRResourcesUtil.findClassLoaderResource(uri, classLoader);
			if (url != null) {
				return JRLoader.getInputStream(url);
			}
		} catch (final JRException | KeyManagementException e) {
			throw new JRRuntimeException(e);
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public ResourceInfo getResourceInfo(RepositoryContext context, String location) {

		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, dummyTrustManager, new java.security.SecureRandom());
			final HostnameVerifier allHostsValid = (hostname, session) -> true;
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (final Exception e1) {
			// NO-OP
		}

		// detecting URLs
		final URL url = JRResourcesUtil.createURL(location, urlHandlerFactory);
		if (url != null) {
			// not supporting paths relative to URLs
			return null;
		}

		if (fileResolver != null) {
			// not dealing with file resolvers
			return null;
		}

		final File file = resolveFile(context, location);
		if (file != null) {
			try {
				// resolving to real path to eliminate .. and .
				final Path path = file.toPath().toRealPath();
				return StandardResourceInfo.from(path);
			} catch (final IOException e) {
				log.warn("Failed to resolve real path for file " + file, e);

				// using the paths as present in the File object
				return StandardResourceInfo.from(file);
			}
		}

		// TODO lucianc classloader resources
		return null;
	}

	TrustManager[] dummyTrustManager = new TrustManager[] { new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	} };

}
