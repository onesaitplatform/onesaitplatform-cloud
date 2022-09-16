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
package com.minsait.onesait.platform.controlpanel;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.github.dandelion.core.web.DandelionFilter;
import com.github.dandelion.core.web.DandelionServlet;
import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;
import com.minsait.onesait.platform.commons.ssl.SSLUtil;
import com.minsait.onesait.platform.controlpanel.converter.YamlHttpMessageConverter;
import com.minsait.onesait.platform.controlpanel.security.CheckSecurityFilter;
import com.minsait.onesait.platform.controlpanel.security.encryption.PasswordEncryptionManager;
import com.minsait.onesait.platform.interceptor.CorrelationInterceptor;
import com.minsait.onesait.platform.metrics.manager.MetricsNotifier;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableAsync
@EnableJpaRepositories(basePackages = "com.minsait.onesait.platform.config.repository")
@ComponentScan(basePackages = { "com.ibm.javametrics.spring", "com.minsait.onesait.platform" }, lazyInit = true)
// @EnableAutoConfiguration(exclude = { MetricsDropwizardAutoConfiguration.class
// })
public class ControlPanelWebApplication implements WebMvcConfigurer {

	@Autowired
	private PasswordEncryptionManager passwordManager;

	@Configuration
	@Profile("default")
	@ComponentScan(basePackages = { "com.ibm.javametrics.spring", "com.minsait.onesait.platform" }, lazyInit = true)
	static class LocalConfig {
	}

	@Value("${onesaitplatform.locale.default:en}")
	@Getter
	@Setter
	private String defaultLocale;

	@Value("${onesaitplatform.analytics.dataflow.version:3.10.0}")
	private String streamsetsVersion;

	@Autowired
	private CorrelationInterceptor logInterceptor;
	@Autowired
	private CheckSecurityFilter securityCheckInterceptor;

	@Autowired
	private ApplicationContext appCtx;

	public static void main(String[] args) throws Exception {
		try {
			SpringApplication.run(ControlPanelWebApplication.class, args);
		} catch (final BeanCreationException ex) {
			final Throwable realCause = unwrap(ex);
			log.error("Error on startup", realCause);
		} catch (final Exception e) {
			log.error("Error on startup", e);
		}
	}

	public static Throwable unwrap(Throwable ex) {
		if (ex != null && BeanCreationException.class.isAssignableFrom(ex.getClass())) {
			return unwrap(ex.getCause());
		} else {
			return ex;
		}
	}

	@Autowired
	public void initializeMetricsNotifier() {
		appCtx.getBean(MetricsNotifier.class);
	}

	@Bean
	public TaskExecutor taskExecutor() {
		final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setQueueCapacity(100);
		executor.setMaxPoolSize(50);
		executor.initialize();
		return executor;
	}

	@Bean
	public ResourceBundleMessageSource messageSource() {
		final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		// resourceBundleMessageSource.setBasename("i18n/messages");
		resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/project");
		resourceBundleMessageSource.setDefaultEncoding("UTF-8");
		return resourceBundleMessageSource;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
		registry.addResourceHandler("/notebooks/app/**").addResourceLocations("classpath:/static/notebooks/");
		registry.addResourceHandler("/dataflow/{instance}/app/**")
		.addResourceLocations("classpath:/static/dataflow/" + streamsetsVersion + "/");
		registry.addResourceHandler("/dataflow/app/**")
		.addResourceLocations("classpath:/static/dataflow/" + streamsetsVersion + "/");
		registry.addResourceHandler("/gitlab/**").addResourceLocations("classpath:/static/gitlab/");
	}

	/**
	 * Exports the all endpoint metrics like those implementing
	 * {@link PublicMetrics}.
	 */
	// TO-DO review this
	// @Bean
	// public MetricsEndpointMetricReader
	// metricsEndpointMetricReader(MetricsEndpoint metricsEndpoint) {
	// return new MetricsEndpointMetricReader(metricsEndpoint);
	// }

	/**
	 * Dandelion Config Beans
	 */

	@Bean
	public DandelionFilter dandelionFilter() {
		return new DandelionFilter();
	}

	@Bean
	public ServletRegistrationBean<DandelionServlet> dandelionServletRegistrationBean() {
		return new ServletRegistrationBean<>(new DandelionServlet(), "/dandelion-assets/*");
	}

	/**
	 * Locale Context Beans
	 */
	@Bean
	public LocaleResolver localeResolver() {
		final SessionLocaleResolver slr = new SessionLocaleResolver();
		final Locale locale = new Locale(defaultLocale);
		slr.setDefaultLocale(locale);
		return slr;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		final LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		lci.setIgnoreInvalidLocale(true);
		return lci;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
		// registry.addInterceptor(logInterceptor);
		registry.addInterceptor(securityCheckInterceptor);

	}

	private static final String MSJ_SSL_ERROR = "Error configuring SSL verification in Control Panel";

	@Bean
	@Conditional(ControlPanelAvoidSSLVerificationCondition.class)
	String sslUtil() {
		try {
			SSLUtil.turnOffSslChecking();
		} catch (final KeyManagementException | NoSuchAlgorithmException e) {
			log.error(MSJ_SSL_ERROR, e);
			throw new GenericRuntimeOPException(MSJ_SSL_ERROR, e);
		}

		return "OK";
	}

	@Bean(name = MultipartFilter.DEFAULT_MULTIPART_RESOLVER_BEAN_NAME)
	public CommonsMultipartResolver filterMultipartResolver() {
		final CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(-1);
		return multipartResolver;
	}

	@Bean
	public FilterRegistrationBean<CharacterEncodingFilter> filterRegistrationBean() {
		final FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
		final CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setForceEncoding(true);
		characterEncodingFilter.setEncoding("UTF-8");
		registrationBean.setFilter(characterEncodingFilter);
		registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registrationBean;
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new YamlHttpMessageConverter<>());
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorPathExtension(false);
	}

}