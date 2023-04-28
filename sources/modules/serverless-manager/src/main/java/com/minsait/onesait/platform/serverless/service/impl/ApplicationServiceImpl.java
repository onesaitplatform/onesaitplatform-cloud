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
package com.minsait.onesait.platform.serverless.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minsait.onesait.platform.serverless.dto.ApplicationCreate;
import com.minsait.onesait.platform.serverless.dto.ApplicationInfo;
import com.minsait.onesait.platform.serverless.dto.ApplicationUpdate;
import com.minsait.onesait.platform.serverless.dto.FunctionCreate;
import com.minsait.onesait.platform.serverless.dto.FunctionInfo;
import com.minsait.onesait.platform.serverless.dto.FunctionUpdate;
import com.minsait.onesait.platform.serverless.dto.fn.FnApplication;
import com.minsait.onesait.platform.serverless.dto.fn.FnFunction;
import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;
import com.minsait.onesait.platform.serverless.exception.ApplicationException;
import com.minsait.onesait.platform.serverless.exception.ApplicationException.Code;
import com.minsait.onesait.platform.serverless.model.Application;
import com.minsait.onesait.platform.serverless.model.Function;
import com.minsait.onesait.platform.serverless.repository.ApplicationRepository;
import com.minsait.onesait.platform.serverless.service.ApplicationService;
import com.minsait.onesait.platform.serverless.service.FnService;
import com.minsait.onesait.platform.serverless.service.GitServiceManager;
import com.minsait.onesait.platform.serverless.service.UserService;
import com.minsait.onesait.platform.serverless.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {
	private static final String NOT_ENOUGH_RIGHTS = "Not enough rights";
	private static final String APP_DOES_NOT_EXIST = "App does not exist";
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private FnService fnService;
	@Autowired
	private GitServiceManager gitServiceManager;

	@Value("${onesaitplatform.serverless.url}")
	private String baseURL;
	public static final String FN_DEFAULT_URL = "http://fnproject:8080";
	private static final String APP_SUFFIX = "-serverless";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public ApplicationInfo create(ApplicationCreate app) {
		final boolean createdApp = fnService.create(app.getName());
		if (!createdApp) {
			throw new ApplicationException("Application could not be created", Code.INTERNAL_ERROR);
		}
		final String appId = fnService.getAppId(app.getName());
		final Application application = new Application();
		application.setAppId(appId);
		application.setName(app.getName());
		application.setUser(userService.getCurrentUser());
		application.setGitInfo(app.getGitInfo());
		if (app.getGitInfo() != null && app.isCreateGit()) {
			createGit(application);
		}
		return new ApplicationInfo(applicationRepository.save(application));
	}

	@Override
	public ApplicationInfo update(ApplicationUpdate appUpdate, String appName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		if (appUpdate.getGitInfo() != null) {
			app.setGitInfo(appUpdate.getGitInfo());
			if (app.getGitInfo().getProjectUrl() == null) {
				createGit(app);
			}
		}
		if (appUpdate.getName() != null) {
			app.setName(appUpdate.getName());
		}
		fnService.update(appUpdate, app.getAppId());
		return mergeApp(app);
	}

	private void createGit(Application app) {
		try {
			final String projectUrl = gitServiceManager.createGitProject(app.getName() + APP_SUFFIX,
					new GitlabConfiguration(app.getGitInfo()));
			app.getGitInfo().setProjectUrl(projectUrl);
		} catch (final Exception e) {
			log.error("Could not create git project, rolling back fn application", e);
			fnService.delete(app.getName());
			throw new ApplicationException("Invalid git configuration or permissions", Code.INTERNAL_ERROR);
		}
	}

	@Override
	public void delete(String appName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		// FOR NOW WE DON'T REMOVE PROJECT
		/*
		 * if (app.getGitInfo() != null) { try { gitServiceManager.deleteProject(new
		 * GitlabConfiguration(app.getGitInfo())); } catch (final GitException e) {
		 * throw new ApplicationException(e.getMessage(), Code.INTERNAL_ERROR); } }
		 */

		fnService.delete(appName);
		applicationRepository.delete(app);

	}

	@Override
	public List<ApplicationInfo> list() {
		final List<Application> apps = applicationRepository.findAll();
		return mergeApps(apps);
	}

	@Override
	public List<ApplicationInfo> list(String username) {
		final List<Application> apps = applicationRepository.findByUser(userService.getUser(username));
		return mergeApps(apps);
	}

	@Override
	public FunctionInfo create(FunctionCreate function, String appName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function fn = new Function();
		fn.setApplication(app);
		fn.setName(function.getName());
		fn.setPathToYaml(function.getPathToYaml().startsWith("/") ? function.getPathToYaml().substring(1)
				: function.getPathToYaml());
		app.getFunctions().add(fn);
		applicationRepository.save(app);
		return new FunctionInfo(fn);
	}

	@Override
	public ApplicationInfo find(String appName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		return mergeApp(app);
	}

	private List<ApplicationInfo> mergeApps(List<Application> apps) {
		return apps.stream().map(this::mergeApp).collect(Collectors.toList());
	}

	private ApplicationInfo mergeApp(Application application) {
		final ApplicationInfo app = new ApplicationInfo(application);
		final FnApplication fnApp = fnService.getApp(application.getAppId());
		app.setEnvironment(fnApp.getConfig());
		app.getFunctions().addAll(mergeFunctions(application.getFunctions()));
		return app;
	}

	private List<FunctionInfo> mergeFunctions(Set<Function> functions) {
		return functions.stream().map(this::mergeFunction).collect(Collectors.toList());
	}

	private FunctionInfo mergeFunction(Function function) {
		final FunctionInfo fn = new FunctionInfo(function);
		if (function.getFnId() != null) {
			final FnFunction fnFunction = fnService.getFunction(function.getFnId());
			fn.setEnvironment(fnFunction.getConfig());
			fn.setMemory(fnFunction.getMemory());
			fn.setImage(fnFunction.getImage());
			if (fnFunction.getAnnotations().get(FnFunction.INVOKE_ANNOTATION) != null) {
				fn.getInvokeEndpoints().add(((String) fnFunction.getAnnotations().get(FnFunction.INVOKE_ANNOTATION))
						.replace(FN_DEFAULT_URL, baseURL));
			}
			if (fnFunction.getTriggers() != null) {
				fnFunction.getTriggers().stream().forEach(t -> {
					if (t.getAnnotations().get(FnFunction.INVOKE_HTTP_ANNOTATION) != null) {
						fn.getInvokeEndpoints().add(((String) t.getAnnotations().get(FnFunction.INVOKE_HTTP_ANNOTATION))
								.replace(FN_DEFAULT_URL, baseURL));
					}
				});
			}
		}
		return fn;
	}

	@Override
	public FunctionInfo deploy(String appName, String fnName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		if (app.getGitInfo() == null || app.getGitInfo().getProjectUrl() == null) {
			throw new ApplicationException("GIT data not configured", Code.BAD_REQUEST);
		}
		final Function function = optFunction.get();
		final String directory = gitServiceManager.cloneRepo(app.getName() + APP_SUFFIX,
				new GitlabConfiguration(app.getGitInfo()));
		final FnFunction fnFunction = fnService.deploy(app, function, directory);
		if (fnFunction != null) {
			app.getFunctions().removeIf(f -> f.getName().equals(fnName));
			function.setFnName(fnFunction.getName());
			function.setFnId(fnFunction.getId());
			app.getFunctions().add(function);
			applicationRepository.save(app);
			gitServiceManager.addAllAndPush(app.getName() + APP_SUFFIX, new GitlabConfiguration(app.getGitInfo()));
			return mergeFunction(function);
		}

		throw new ApplicationException("Something went wrong deploying the function", Code.INTERNAL_ERROR);

	}

	@Override
	public FunctionInfo updateFunction(String appName, String fnName, FunctionUpdate functionUpdate) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		if (functionUpdate.getPathToYaml() != null) {
			function.setPathToYaml(functionUpdate.getPathToYaml());
		}
		if (functionUpdate.getName() != null) {
			function.setName(functionUpdate.getName());
		}
		applicationRepository.save(app);
		return mergeFunction(function);
	}

	@Override
	public FunctionInfo getFunction(String appName, String fnName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		return mergeFunction(function);
	}

	@Override
	public void deleteFunction(String appName, String fnName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		fnService.deleteFunction(function.getFnId());
		app.getFunctions().remove(function);
		applicationRepository.save(app);

	}

	@Override
	public void updateFunctionsVersion(String appName, String fnName, String version) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		final FnFunction fnFunction = fnService.getFunction(function.getFnId());
		fnFunction.setImage(version);
		log.debug("Updating function's version, fn: {}, version: {}", fnName, version);
		fnService.updateFunction(fnFunction);

	}

	@Override
	public ObjectNode getFunctionsEnvironment(String appName, String fnName) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		final FnFunction fnFunction = fnService.getFunction(function.getFnId());
		if (fnFunction.getConfig() == null) {
			return MAPPER.createObjectNode();
		} else {
			return MAPPER.convertValue(fnFunction.getConfig(), ObjectNode.class);
		}
	}

	@Override
	public void updateFunctionsEnvironmnet(String appName, String fnName, ObjectNode config) {
		final Application app = applicationRepository.findByName(appName);
		if (app == null) {
			throw new ApplicationException(APP_DOES_NOT_EXIST, Code.NOT_FOUND);
		}
		final Optional<Function> optFunction = app.getFunctions().stream().filter(f -> f.getName().equals(fnName))
				.findFirst();
		if (!optFunction.isPresent()) {
			throw new ApplicationException("Function does not exist", Code.NOT_FOUND);
		}
		if (!app.getUser().getUserId().equals(SecurityUtils.getCurrentUser()) && !SecurityUtils.isAdmin()) {
			throw new ApplicationException(NOT_ENOUGH_RIGHTS, Code.FORBIDDEN);
		}
		final Function function = optFunction.get();
		final FnFunction fnFunction = fnService.getFunction(function.getFnId());
		final Map<String, Object> newConfig = MAPPER.convertValue(config, new TypeReference<Map<String, Object>>() {
		});
		final List<String> varsToRemove = new ArrayList<>();
		if (fnFunction.getConfig() != null) {
			fnFunction.getConfig().entrySet().forEach(e -> {
				if (newConfig.get(e.getKey()) == null) {
					varsToRemove.add(e.getKey());
				}
			});
		}
		fnFunction.setConfig(newConfig);
		fnService.updateFunction(fnFunction);
		varsToRemove.forEach(v -> fnService.removeVar(appName, fnFunction.getName(), v));

	}

}
