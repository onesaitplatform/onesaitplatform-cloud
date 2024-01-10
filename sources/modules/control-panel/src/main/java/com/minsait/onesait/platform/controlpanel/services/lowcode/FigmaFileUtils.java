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
package com.minsait.onesait.platform.controlpanel.services.lowcode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.minsait.onesait.platform.commons.utils.FileIOUtils;
import com.minsait.onesait.platform.config.model.Api;
import com.minsait.onesait.platform.config.model.ApiOperation;
import com.minsait.onesait.platform.config.services.apimanager.ApiManagerService;
import com.minsait.onesait.platform.config.services.exceptions.OPResourceServiceException;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FigmaFileUtils {

	private static final String TMP_DIR = "/tmp";
	private static final String JS_FOLDER = "figma-template";
	private static final String PATH_TO_HOME_VUE = "/src/views/Home.vue";
	private static final String PATH_TO_MAIN_JS = "/src/js/MainService.js";
	private static final String PATH_TO_DASHBOARD_WRAPPER = "/src/components/DashboardWrapper.vue";
	private static final String OUTPUT_ZIP = "figma-template.zip";
	private static final String IMPORT_VAR = "IMPORT";
	private static final String CUSTOM_COMPONENT_VAR = "CUSTOM_COMPONENT";
	private static final String API_VAR = "API";
	private static final String API_VERSION_VAR = "API_VERSION";
	private static final String FIGMA_FILE_VAR = "FIGMA_FILE";
	private static final String FIGMA_TOKEN_VAR = "FIGMA_TOKEN";
	private static final String COMPONENTS_VAR = "COMPONENTS";
	private static final String METHODS_VAR = "METHODS";
	private static final String MAIN_METHODS_VAR = "MAIN_METHODS";
	private static final String MAIN_LOGIN_METHOD_VAR = "LOGIN_METHOD";
	private static final String USERNAME_VAR = "USERNAME_VAR";
	private static final String PASSWORD_VAR = "PASSWORD_VAR";
	private static final String OPERATION_PATH = "OPERATION_PATH_VAR";
	private static final String MAIN_PAGE_VAR = "MAIN_PAGE";
	private static final String METHOD_BINDED_VAR = "METHOD_BINDED";
	private static final String LOGIN_METHOD_BINDED_VAR = "LOGIN_METHOD_BINDED";
	private static final String METHOD_NAME_VAR = "METHOD_NAME";
	private static final String BODY_VAR = "BODY";
	private static final String METHOD_VAR = "METHOD";
	private static final String OUTPUT_VAR = "OUTPUT";
	private static final String ROUTE_CALLBACK_VAR = "ROUTE_CALLBACK";
	private static final String SERVER_NAME_VAR = "SERVER_NAME";
	private static final String PREPROCESS_BODY_VAR = "PREPROCESS_BODY";
	private static final String DASHBOARD_NAME_VAR = "DASHBOARD_NAME";
	private static final String DASHBOARD_ID_VAR = "DASHBOARD_ID";
	private static final String EDIT_MODE_VAR = "EDIT_MODE";
	private static final String OPERATION_PARAM_PATTERN = ".*(\\{[a-zA-Z]+\\})";

	@Autowired
	private FileIOUtils fileUtils;
	@Autowired
	private AppWebUtils webUtils;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private ApiManagerService apiManagerService;

	public File generateTemplate(FigmaSetUp figmaSetUp) {
		try {
			final String userId = webUtils.getUserId();
			final String pathToZip = "static/microservices/templates/figma/figma-template.zip";
			final String basePath = TMP_DIR + File.separator + userId + File.separator + JS_FOLDER;
			log.debug("Creating DIRs");
			fileUtils.createDirs(basePath);
			if (log.isDebugEnabled()) {
				log.debug("Unzipping {} to path {}", pathToZip, basePath);
			}			
			fileUtils.unzipToPath(pathToZip, basePath);
			log.debug("Compiling templates");
			compileTemplates(figmaSetUp, basePath);
			if (log.isDebugEnabled()) {
				log.debug("Zipping files to {}", basePath + File.separator + OUTPUT_ZIP);
			}			
			return fileUtils.zipFiles(basePath, TMP_DIR + File.separator + userId + File.separator + OUTPUT_ZIP);
		} catch (final Exception e) {
			log.error("Error while generating JS client", e);
			throw new OPResourceServiceException("Could not generate JS client: " + e.getMessage(), e);
		}
	}

	private void compileTemplates(FigmaSetUp figmaSetUp, String basePath) throws IOException {
		final HashMap<String, Object> scopes = new HashMap<>();
		scopes.put(FIGMA_TOKEN_VAR, figmaSetUp.getFigmaToken());
		scopes.put(FIGMA_FILE_VAR, figmaSetUp.getFigmaFile());
		scopes.put(COMPONENTS_VAR, "");
		if (figmaSetUp.isUseLogin()) {
			scopes.put(MAIN_LOGIN_METHOD_VAR, FigmaTemplateComponents.MAIN_SERVICE_JS_LOGIN);
			scopes.put(LOGIN_METHOD_BINDED_VAR, figmaSetUp.getFigmaLogin().getMethodName());
		}
		scopes.put(METHODS_VAR, compileHomeMethods(figmaSetUp));
		scopes.put(MAIN_METHODS_VAR, compileMainJSMethods(figmaSetUp));
		String serverURL = StringUtils.hasText(figmaSetUp.getOnesaitBaseURL()) ? figmaSetUp.getOnesaitBaseURL()
				: resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE);
		if (serverURL.endsWith("/")) {
			serverURL = serverURL.substring(0, serverURL.length() - 1);
		}
		scopes.put(SERVER_NAME_VAR, serverURL);
		if (figmaSetUp.isUseDashboard()) {
			scopes.put(DASHBOARD_NAME_VAR, figmaSetUp.getFigmaDashboard().getCustomComponentName());
			scopes.put(DASHBOARD_ID_VAR, figmaSetUp.getFigmaDashboard().getDashboardId());
			scopes.put(EDIT_MODE_VAR, figmaSetUp.getFigmaDashboard().isEditMode());
			scopes.put(COMPONENTS_VAR, figmaSetUp.getFigmaDashboard().getCustomComponentName());
			scopes.put(CUSTOM_COMPONENT_VAR, figmaSetUp.getFigmaDashboard().getCustomComponentName());
			scopes.put(IMPORT_VAR, renderMustacheContent(FigmaTemplateComponents.IMPORT_VUE, scopes));
			writeMustacheTemplate(basePath + PATH_TO_DASHBOARD_WRAPPER, scopes);
			final Path source = Paths.get(basePath + PATH_TO_DASHBOARD_WRAPPER);
			Files.move(source, source.resolveSibling(figmaSetUp.getFigmaDashboard().getCustomComponentName() + ".vue"));
		} else {
			Files.delete(Paths.get(basePath + PATH_TO_DASHBOARD_WRAPPER));
		}
		writeMustacheTemplate(basePath + PATH_TO_HOME_VUE, scopes);
		writeMustacheTemplate(basePath + PATH_TO_MAIN_JS, scopes);

	}

	private String compileHomeMethods(FigmaSetUp figmaSetUp) throws IOException {
		final StringBuilder result = new StringBuilder();
		if (figmaSetUp.isUseLogin()) {
			final HashMap<String, Object> scopes = new HashMap<>();
			scopes.put(MAIN_PAGE_VAR,
					figmaSetUp.getFigmaLogin().getHomePage().endsWith(".html")
							? figmaSetUp.getFigmaLogin().getHomePage()
							: figmaSetUp.getFigmaLogin().getHomePage() + ".html");
			scopes.put(USERNAME_VAR, figmaSetUp.getFigmaLogin().getUsernameVar());
			scopes.put(PASSWORD_VAR, figmaSetUp.getFigmaLogin().getPasswordVar());
			scopes.put(LOGIN_METHOD_BINDED_VAR, figmaSetUp.getFigmaLogin().getMethodName());
			result.append(renderMustacheContent(FigmaTemplateComponents.HOME_VUE_LOGIN, scopes));
			result.append(",\n");
		}
		figmaSetUp.getMappings().forEach(fgp -> {
			final HashMap<String, Object> scopes = new HashMap<>();
			scopes.put(METHOD_BINDED_VAR, fgp.getMethodBinded());
			scopes.put(API_VAR, fgp.getApiIdentification());
			scopes.put(API_VERSION_VAR, fgp.getApiVersion());
			try {
				result.append(renderMustacheContent(FigmaTemplateComponents.HOME_VUE_METHOD, scopes));
				result.append(",\n");
			} catch (final IOException e) {
				log.error("Error while adding binded method {}", fgp.getMethodBinded(), e);
			}
		});
		if (result.toString().endsWith(",\n")) {
			return result.toString().substring(0, result.length() - 4) + " \n }";
		} else {
			return result.toString();
		}

	}

	private String compileMainJSMethods(FigmaSetUp figmaSetUp) {
		final StringBuilder result = new StringBuilder();
		figmaSetUp.getMappings().forEach(fgp -> {
			final HashMap<String, Object> scopes = new HashMap<>();
			final Api api = apiManagerService.getApiByIdentificationVersionOrId(fgp.getApiId(), null);
			scopes.put(METHOD_NAME_VAR, fgp.getMethodBinded());
			scopes.put(METHOD_VAR, fgp.getOperationHTTPMethod());
			if (StringUtils.hasText(fgp.getInputVar())) {
				scopes.put(PREPROCESS_BODY_VAR, "let body=JSON.parse(JSON.stringify(viewModel." + fgp.getInputVar()
						+ "))\n delete body._id \n");
				scopes.put(BODY_VAR, "body: JSON.stringify(body),");
			}
			if (StringUtils.hasText(fgp.getOutputVar())) {
				scopes.put(OUTPUT_VAR, "viewModel." + fgp.getOutputVar() + "=r");
			}
			if (StringUtils.hasText(fgp.getCallbackRoute())) {
				String callbackRoute = fgp.getCallbackRoute();
				if (!fgp.getCallbackRoute().endsWith(".html")) {
					callbackRoute = callbackRoute + ".html";
				}
				scopes.put(ROUTE_CALLBACK_VAR, "router.push('" + callbackRoute + "')");
			}
			String path = apiManagerService.getOperations(api).stream()
					.filter(ao -> ao.getId().equals(fgp.getOperationId())).map(ApiOperation::getPath).findFirst()
					.orElse("/");
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			// TO-DO sustituir parametros bien POST/GET -> array de inputs?
			scopes.put(OPERATION_PATH, processApiOperationParams(path, fgp.getOperationParamMap()));
			try {
				result.append(renderMustacheContent(FigmaTemplateComponents.MAIN_SERVICE_JS_METHOD_TEMPLATE, scopes));
			} catch (final IOException e) {
				log.error("Error while adding binded method {}", fgp.getMethodBinded(), e);
			}

		});
		return result.toString();
	}

	private String processApiOperationParams(String path, Map<String, String> operationParamMap) {
		if (operationParamMap != null && !operationParamMap.isEmpty()) {
			final Pattern pattern = Pattern.compile(OPERATION_PARAM_PATTERN);
			final Matcher matcher = pattern.matcher(path);
			while (matcher.find()) {
				final String param = matcher.group(1);
				final String inputVar = operationParamMap.get(param) != null ? operationParamMap.get(param)
						: operationParamMap.get(param.replace("{", "").replace("}", ""));
				path = path.replace(param, "${viewModel." + inputVar + "}");
			}
		}
		return path;
	}

	private String renderMustacheContent(String templateContent, HashMap<String, Object> scopes) throws IOException {
		final MustacheFactory mf = new DefaultMustacheFactory();
		final Mustache mustache = mf.compile(new StringReader(templateContent), "template");
		final StringWriter writer = new StringWriter();
		mustache.execute(writer, scopes).flush();
		return writer.toString();
	}

	private void writeMustacheTemplate(String writePath, Map<String, Object> scopes) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Compiling template {}", writePath);
		}		
		final String content = new String(Files.readAllBytes(Paths.get(writePath)));

		try (Writer writer = new FileWriter(writePath)) {

			final MustacheFactory mf = new DefaultMustacheFactory();
			final Mustache mustache = mf.compile(new StringReader(content), "compile");
			mustache.execute(writer, scopes);
			writer.flush();
		} catch (final IOException e) {
			log.error("error at file {}", writePath);
			throw e;
		}
	}

}
