/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.storage.EmptyStorageFactory;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.controlpanel.interceptor.BearerTokenFilter;
import com.minsait.onesait.platform.controlpanel.interceptor.MicrosoftTeamsTokenFilter;
import com.minsait.onesait.platform.controlpanel.interceptor.X509CertFilter;
import com.minsait.onesait.platform.controlpanel.interceptor.XOpAPIKeyFilter;
import com.minsait.onesait.platform.controlpanel.security.xss.XSSFilter;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.security.ri.ConfigDBDetailsService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@Slf4j
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	public static final String LOGIN_STR = "/login";
	public static final String BLOCK_PRIOR_LOGIN = "block_prior_login";
	public static final String INVALIDATE_SESSION_FORCED = "INVALIDATE_SESSION_FORCED";
	public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
	public static final String BLOCK_PRIOR_LOGIN_PARAMS = "block_prior_login_params";

	private static final String SAML = "saml";
	private static final String CAS = "cas";
	private static final String X509 = "X509";
	private static final String CONFIGDB = "configdb";

	@Value("${onesaitplatform.authentication.provider}")
	private String authProvider;
	@Value("${onesaitplatform.authentication.saml.idp_metadata}")
	private String idpMetadata;
	@Value("${onesaitplatform.authentication.saml.entity_id}")
	private String entityId;
	@Value("${onesaitplatform.authentication.saml.entity_url}")
	private String entityUrl;
	@Value("${onesaitplatform.authentication.saml.unauthorized_url:/}")
	private String unauthorizedUrl;
	@Value("${onesaitplatform.authentication.saml.jks.uri}")
	private String samlJksUri;
	@Value("${onesaitplatform.authentication.saml.jks.store_pass}")
	private String samlJksStorePass;
	@Value("${onesaitplatform.authentication.saml.jks.key_alias}")
	private String samlJksKeyAlias;
	@Value("${onesaitplatform.authentication.saml.jks.key_pass}")
	private String samlJksKeyPass;
	@Value("${onesaitplatform.authentication.saml.context.samlScheme}")
	private String samlScheme;
	@Value("${onesaitplatform.authentication.saml.context.samlServerName}")
	private String samlServerName;
	@Value("${onesaitplatform.authentication.saml.context.samlIncludePort}")
	private boolean samlIncludePort;
	@Value("${onesaitplatform.authentication.X509.enabled:false}")
	private boolean x509Enabled;
	@Value("${server.port}")
	private int samlServerPort;
	@Value("${server.servlet.contextPath}")
	private String samlContextPath;

	@Value("${onesaitplatform.authentication.oauth.enabled:false}")
	private boolean oauthLogin;

	@Value("${csrf.enable}")
	private boolean csrfOn;

	@Bean
	public FilterRegistrationBean corsFilterOauth(@Value("${onesaitplatform.secure.cors:*}") String allowedURLs) {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(Arrays.asList(allowedURLs.split(",")));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return bean;
	}

	@Autowired(required = false)
	private SAMLUserDetailsService samlUserDetailsService;
	@Autowired
	private IntegrationResourcesService integrationResourcesService;

	@Autowired
	private AccessDeniedHandler accessDeniedHandler;

	@Autowired
	private AuthenticationProvider authenticationProvider;

	@Autowired
	private ConfigDBDetailsService detailsService;

	@Autowired
	private LogoutSuccessHandler logoutSuccessHandler;
	@Autowired(required = false)
	private SingleSignOutFilter singleSignOutFilter;
	@Autowired(required = false)
	@Qualifier("casLogoutFilter")
	private LogoutFilter logoutFilter;

	@Autowired
	private Securityhandler successHandler;

	@Autowired
	private SecurityFailureHandler failureHandler;

	@Autowired
	@Lazy
	private AuthenticationEntryPoint authenticationEntryPoint;

	@Autowired
	private InvalidSessionStrategy invalidSessionStrategy;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		// Build the request matcher for CSFR

		final RequestMatcher csrfRequestMatcher = new RequestMatcher() {

			private final List<RegexRequestMatcher> requestMatchers = Arrays.asList(
					new RegexRequestMatcher("^/api-ops.*", null), new RegexRequestMatcher("^/notebook-ops.*", null),
					new RegexRequestMatcher("^/management.*", null), new RegexRequestMatcher("^/deployment.*", null),
					new RegexRequestMatcher("^/dashboardapi.*", null), new RegexRequestMatcher("^/model.*", null),
					new RegexRequestMatcher("^/api.*", null), new RegexRequestMatcher("^/layer.*", null),
					new RegexRequestMatcher("^/binary-repository.*", null),
					new RegexRequestMatcher("^/dataflow.*", null), new RegexRequestMatcher("^/notebooks.*", null),
					new RegexRequestMatcher("^/dashboards.*", null), new RegexRequestMatcher("^/viewers.*", null),
					new RegexRequestMatcher("^/virtualdatasources.*", null),
					new RegexRequestMatcher("^/querytool.*", null), new RegexRequestMatcher("^/datasources.*", null),
					new RegexRequestMatcher("^/gadgets.*", null), new RegexRequestMatcher("^/saml.*", null),
					new RegexRequestMatcher("^/bpm" + ".*", null), new RegexRequestMatcher("^/oauth" + ".*", null),
					new RegexRequestMatcher("^/users/register", null),
					new RegexRequestMatcher("^/users/reset-password", null),
					new RegexRequestMatcher("^/actuator.*", null), new RegexRequestMatcher("^/opendata.*", null),
					new RegexRequestMatcher("^/modelsmanager.*", null), new RegexRequestMatcher("^/process.*", null),
					new RegexRequestMatcher("^/microservices.*", null),
					new RegexRequestMatcher("^/mapsproject.*", null));

			// When using CsrfProtectionMatcher we need to explicitly declare allowed
			// methods
			private final Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

			@Override
			public boolean matches(HttpServletRequest request) {
				if (csrfOn) {
					final boolean matchesMethod = allowedMethods.matcher(request.getMethod()).matches();
					final boolean matchesURL = requestMatchers.stream().anyMatch(r -> r.matches(request));
					return !(matchesURL || matchesMethod);
				} else {
					return false;
				}

			}
		};

		final Integer maxSessionsPerUser = integrationResourcesService.getGlobalConfiguration().getEnv()
				.getControlpanel() != null
				&& integrationResourcesService.getGlobalConfiguration().getEnv().getControlpanel()
						.get("maxSessionsPerUser") != null
								? (Integer) integrationResourcesService.getGlobalConfiguration().getEnv()
										.getControlpanel().get("maxSessionsPerUser")
								: 10;

		http.csrf().requireCsrfProtectionMatcher(csrfRequestMatcher);

		http.headers().frameOptions().disable();
		http.authorizeRequests().antMatchers("/", "/home", "/favicon.ico", "/blocked", "/loginerror").permitAll()
				.antMatchers("/api/applications", "/api/applications/").permitAll().antMatchers("/opendata/register")
				.permitAll().antMatchers("/users/register", "/oauth/authorize", "/oauth/token", "/oauth/check_token")
				.permitAll().antMatchers(HttpMethod.POST, "/users/reset-password").permitAll()
				.antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**")
				.permitAll().antMatchers(HttpMethod.PUT, "/users/update/**/**").permitAll()
				.antMatchers(HttpMethod.GET, "/users/update/**/**").permitAll()
				.antMatchers("/health/", "/info", "/metrics", "/trace", "/logfile").permitAll()
				.antMatchers("/nodered/auth/**/**").permitAll()
				.antMatchers("/actuator/**", "/api/**", "/dashboards/view/**", "/dashboards/model/**",
						"/dashboards/editfulliframe/**", "/dashboards/viewiframe/**", "/viewers/view/**",
						"/viewers/viewiframe/**", "/gadgets/**", "/viewers/**", "/datasources/**", "/v3/api-docs/",
						"/v3/api-docs/**", "/swagger-resources/", "/swagger-resources/**",
						"/users/validateNewUserFromLogin/**", "/users/showGeneratedCredentials/**",
						"/users/createNewUserFromLogin", "/users/validateResetPassword/**",
						"/users/resetPasswordFromLogin", "/swagger-ui.html", "/layer/**", "/notebooks/app/**", "/403",
						"/gadgettemplates/getGadgetTemplateByIdentification/**", "/modelsmanager/api/**",
						"/datamodelsjsonld/**")
				.permitAll().antMatchers("/actuator/**").hasAnyRole("OPERATIONS", "ADMINISTRATOR");

		// This line deactivates login page when using SAML or other Auth Provider
		// if (!authProvider.equalsIgnoreCase(CONFIGDB))
		// http.authorizeRequests().antMatchers(LOGIN_STR).denyAll();
		http.x509().subjectPrincipalRegex("CN=(.*?)(?:,|$)").userDetailsService(detailsService);
		http.formLogin().successHandler(successHandler).failureHandler(failureHandler).permitAll();
		http.authorizeRequests().regexMatchers("^/login/cas.*", "^/cas.*", "^/login*", "^/saml*").permitAll()
				.antMatchers("/oauth/").permitAll().antMatchers("/api-ops", "/api-ops/**").permitAll()
				.antMatchers("/management", "/management/**").permitAll()
				.antMatchers("/notebook-ops", "/notebook-ops/**").permitAll().antMatchers(HttpMethod.GET, "/files/list")
				.authenticated().antMatchers(HttpMethod.GET, "/files/**").permitAll()
				.antMatchers(HttpMethod.POST, "/binary-repository", "/binary-repository/**").authenticated()
				.antMatchers("/binary-repository", "/binary-repository/**").permitAll().antMatchers("/admin")
				.hasAnyRole("ROLE_ADMINISTRATOR").antMatchers("/admin/**").hasAnyRole("ROLE_ADMINISTRATOR").anyRequest()
				.authenticated();
		http.logout().logoutSuccessHandler(logoutSuccessHandler).permitAll().and().sessionManagement()
				.invalidSessionUrl("/").maximumSessions(maxSessionsPerUser).expiredUrl("/")
				.maxSessionsPreventsLogin(false).sessionRegistry(sessionRegistry()).and().sessionFixation().none().and()
				.exceptionHandling().accessDeniedHandler(accessDeniedHandler)
				.authenticationEntryPoint(authenticationEntryPoint);

		if (authProvider.equalsIgnoreCase(CAS)) {
			http.addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class).addFilterBefore(logoutFilter,
					LogoutFilter.class);
		}
		if (authProvider.equalsIgnoreCase(SAML)) {
			http.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class).addFilterAfter(samlFilter(),
					BasicAuthenticationFilter.class);

		}
		http.addFilterBefore(new BearerTokenFilter(), AnonymousAuthenticationFilter.class)
				.addFilterBefore(new XOpAPIKeyFilter(), AnonymousAuthenticationFilter.class)
				.addFilterBefore(new MicrosoftTeamsTokenFilter(), AnonymousAuthenticationFilter.class);
		if (x509Enabled) {
			http.addFilterBefore(new X509CertFilter(), AnonymousAuthenticationFilter.class);
		}
		if (oauthLogin) {
			http.addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
		}

		http.sessionManagement().invalidSessionStrategy(invalidSessionStrategy);
		http.httpBasic();

	}

	@Autowired(required = false)
	private OAuth2ClientContext oauth2ClientContext;
	@Autowired(required = false)
	private UserInfoTokenServices userInfoTokenServices;
	@Autowired(required = false)
	@Qualifier("oauthClient")
	private AuthorizationCodeResourceDetails clientCodeDetails;

	private Filter ssoFilter() {
		final OAuth2ClientAuthenticationProcessingFilter oauthFilter = new OAuth2ClientAuthenticationProcessingFilter(
				"/login");
		final OAuth2RestTemplate oauthTemplate = new OAuth2RestTemplate(clientCodeDetails, oauth2ClientContext);
		oauthFilter.setRestTemplate(oauthTemplate);
		oauthFilter.setTokenServices(userInfoTokenServices);
		oauthFilter.setApplicationEventPublisher(BeanUtil.getContext());
		oauthFilter.setAuthenticationFailureHandler(new SimpleFailiureHandler("/login"));
		oauthFilter.setAuthenticationSuccessHandler(successHandler);
		return oauthFilter;
	}

	@ConditionalOnProperty(value = "onesaitplatform.authentication.oauth.enabled", havingValue = "true")
	@Bean
	public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(
			OAuth2ClientContextFilter filter) {
		final FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(filter);
		registration.setOrder(-100);
		return registration;
	}

	@Bean
	@Primary
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public AuthenticationProvider samlAuthenticationProvider() {
		final SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;

	}

	// cannot be processed by SessionResgistryImpl without this bean
	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
	}

	@Bean
	public FilterRegistrationBean sameOriginConfigurationFilter(
			@Value("${onesaitplatform.secure.x-frame-options:true}") boolean xFrameOptions) {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				if (xFrameOptions) {
					response.setHeader("X-Frame-Options", "SAMEORIGIN");
				}
				filterChain.doFilter(request, response);
			}

			@Override
			protected boolean shouldNotFilter(HttpServletRequest request) {
				final String path = request.getServletPath();

				return path.startsWith("/dashboards/env/") || path.startsWith("/dashboards/model/")
						|| path.startsWith("/dashboards/editfulliframe/") || path.startsWith("/dashboards/viewiframe/")
						|| path.startsWith("/dashboards/view/") || path.startsWith("/static/")
						|| path.startsWith("/gadgets/getGadgetMeasuresByGadgetId")
						|| path.startsWith("/gadgets/updateiframe/") || path.startsWith("/gadgets/createiframe/")
						|| path.startsWith("/gadgets/getGadgetConfigById/")
						|| path.startsWith("/datasources/getDatasourceById/") || path.startsWith("/viewers/view/")
						|| path.startsWith("/viewers/viewiframe/") || path.startsWith("/datamodelsjsonld/");

			}
		});
		registration.addUrlPatterns("/*");
		registration.setName("xFrameOptionsFilter");
		registration.setOrder(Ordered.LOWEST_PRECEDENCE);
		return registration;
	}

	@Bean
	public NoOpPasswordEncoder noopPasswordEncoder() {
		return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public FilterRegistrationBean xssPreventFilter() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

		registrationBean.setFilter(new XSSFilter());
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	public ServletContextInitializer servletContextInitializer(
			@Value("${onesaitplatform.secure.cookie}") boolean secure) {
		return servletContext -> {
			servletContext.getSessionCookieConfig().setSecure(secure);
			servletContext.getSessionCookieConfig().setHttpOnly(true);
		};
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider);

	}

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return new ProviderManager(Arrays.asList(authenticationProvider));
	}

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "cas")
	public CasAuthenticationFilter casAuthenticationFilter(ServiceProperties sP) throws Exception {
		final CasAuthenticationFilter filter = new CasAuthenticationFilter();
		filter.setServiceProperties(sP);
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationSuccessHandler(successHandler);
		return filter;
	}

	@Bean
	@ConditionalOnMissingBean(value = AuthenticationEntryPoint.class)
	public AuthenticationEntryPoint customAuthenticationEntryPoint() {
		return new CustomAuthenticationEntryPoint(LOGIN_STR);
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public ExtendedMetadataDelegate ssoExtendedMetadataProvider() throws MetadataProviderException {
		final Timer backgroundTaskTimer = new Timer(true);
		final String idpSSOCircleMetadataURL = idpMetadata;
		final HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(backgroundTaskTimer, httpClient(),
				idpSSOCircleMetadataURL);
		httpMetadataProvider.setParserPool(parserPool());
		final ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider,
				extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(false);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		backgroundTaskTimer.purge();
		return extendedMetadataDelegate;
	}

	@Bean(initMethod = "initialize")
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public StaticBasicParserPool parserPool() {
		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public ParserPoolHolder parserPoolHolder() {
		return new ParserPoolHolder();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public HttpClient httpClient() {
		final MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		return new HttpClient(multiThreadedHttpConnectionManager);

	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public ExtendedMetadata extendedMetadata() {
		final ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(false);
		extendedMetadata.setSignMetadata(false);
		return extendedMetadata;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public MetadataGeneratorFilter metadataGeneratorFilter() throws GenericOPException {
		return new MetadataGeneratorFilter(metadataGenerator());
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public MetadataGenerator metadataGenerator() throws GenericOPException {
		final MetadataGenerator metadataGenerator = new MetadataGenerator();
		metadataGenerator.setEntityId(entityId);
		metadataGenerator.setEntityBaseURL(entityUrl);
		metadataGenerator.setExtendedMetadata(extendedMetadata());
		metadataGenerator.setIncludeDiscoveryExtension(false);
		metadataGenerator.setKeyManager(keyManager());
		return metadataGenerator;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public KeyManager keyManager() throws GenericOPException {
		FileInputStream fis = null;
		try {
			final KeyStore ks = KeyStore.getInstance("JKS");
			final String storePass = samlJksStorePass;
			File file = new File(samlJksUri);
			if (!file.exists()) {
				file = new File(getClass().getClassLoader().getResource(samlJksUri).getFile());
			}
			fis = new FileInputStream(file);
			ks.load(fis, storePass.toCharArray());
			final Map<String, String> passwords = new HashMap<>();
			passwords.put(samlJksKeyAlias, samlJksKeyPass);
			final String defaultKey = samlJksKeyAlias;
			return new JKSKeyManager(ks, passwords, defaultKey);
		} catch (final Exception e) {
			log.error("Error loading SAML jks", e);
			throw new GenericOPException(e);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (final Exception e2) {
				log.error("" + e2);
			}
		}
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	@Primary
	public SAMLEntryPoint samlEntryPoint() {
		final SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
		return samlEntryPoint;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public WebSSOProfileOptions defaultWebSSOProfileOptions() {
		final WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setIncludeScoping(false);
		return webSSOProfileOptions;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public MetadataDisplayFilter metadataDisplayFilter() {
		return new MetadataDisplayFilter();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		return new WebSSOProfileConsumerImpl();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public WebSSOProfile webSSOprofile() {
		return new WebSSOProfileImpl();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLDefaultLogger samlLogger() {
		return new SAMLDefaultLogger();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public CachingMetadataManager metadata() throws MetadataProviderException {
		final List<MetadataProvider> providers = new ArrayList<>();
		providers.add(ssoExtendedMetadataProvider());
		return new CachingMetadataManager(providers);
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLContextProviderImpl contextProvider() {
		final SAMLContextProviderLB context = new SAMLContextProviderLB();
		context.setStorageFactory(new EmptyStorageFactory());
		context.setScheme(samlScheme);
		context.setServerName(samlServerName);
		context.setIncludeServerPortInRequestURL(samlIncludePort);
		context.setServerPort(samlServerPort);
		context.setContextPath(samlContextPath);
		return context;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public static SAMLBootstrap sAMLBootstrap() {
		return new SAMLBootstrap();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLProcessorImpl processor() {
		final Collection<SAMLBinding> bindings = new ArrayList<>();
		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		return new SAMLProcessorImpl(bindings);
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public HTTPPostBinding httpPostBinding() {
		return new HTTPPostBinding(parserPool(), velocityEngine());
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
		return new HTTPRedirectDeflateBinding(parserPool());
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public VelocityEngine velocityEngine() {
		return VelocityFactory.getEngine();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public FilterChainProxy samlFilter() {
		final List<SecurityFilterChain> chains = new ArrayList<>();
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
				metadataDisplayFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
				samlWebSSOProcessingFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
				samlLogoutProcessingFilter()));

		return new FilterChainProxy(chains);
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
		return new SAMLLogoutProcessingFilter(logoutSuccessHandler, logoutHandler());
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SecurityContextLogoutHandler logoutHandler() {
		final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.setInvalidateHttpSession(true);
		logoutHandler.setClearAuthentication(true);
		return logoutHandler;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public FilterRegistrationBean logoutSamlFilter() {
		final FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new OncePerRequestFilter() {

			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {
				response.sendRedirect("/controlpanel/saml/logout");
			}

		});
		registration.addUrlPatterns("/logout");
		registration.setName("logoutSamlFilter");
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;

	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SingleLogoutProfile logoutprofile() {
		return new SingleLogoutProfileImpl();
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLLogoutFilter samlLogoutFilter() {
		return new SAMLLogoutFilter(logoutSuccessHandler, new LogoutHandler[] { logoutHandler() },
				new LogoutHandler[] { logoutHandler() });
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		final SimpleUrlAuthenticationFailureHandler authHandler = new SimpleUrlAuthenticationFailureHandler();
		authHandler.setDefaultFailureUrl(unauthorizedUrl);
		return authHandler;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
		final SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl("/main");
		return successRedirectHandler;
	}

	@Bean
	@ConditionalOnProperty(value = "onesaitplatform.authentication.provider", havingValue = "saml")
	public SAMLProcessingFilter samlWebSSOProcessingFilter() {
		final SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
		samlWebSSOProcessingFilter
				.setAuthenticationManager(new ProviderManager(Arrays.asList(samlAuthenticationProvider())));
		samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successHandler);
		samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return samlWebSSOProcessingFilter;
	}

}
