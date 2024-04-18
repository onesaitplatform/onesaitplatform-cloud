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
package com.minsait.onesait.platform.controlpanel.services.microservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONObject;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.InclusionLevel;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SourceType;
import org.jsonschema2pojo.rules.RuleFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.git.GitOperationsImpl;
import com.minsait.onesait.platform.commons.git.GitSyncException;
import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.MicroserviceTemplate;
import com.minsait.onesait.platform.config.model.Token;
import com.minsait.onesait.platform.config.repository.ClientPlatformOntologyRepository;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.exceptions.MicroserviceException;
import com.minsait.onesait.platform.config.services.notebook.NotebookService;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.config.services.token.TokenService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;
import com.minsait.onesait.platform.git.GitlabConfiguration;
import com.minsait.onesait.platform.git.GitlabException;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;
import com.sun.codemodel.JCodeModel;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MicroserviceTemplateUtil {

	@Autowired
	private GitOperations gitOperations;
	@Autowired
	private OntologyService ontologyService;
	@Autowired
	private ClientPlatformService clientPlatformService;
	@Autowired
	private ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	private TokenService tokenService;
	@Autowired
	private IntegrationResourcesService resourcesService;
	@Autowired
	private UserService userService;
	@Autowired
	private AppWebUtils utils;
	@Autowired
	private NotebookService notebookService;

	@Value("${onesaitplatform.gitlab.scaffolding.directory:/tmp/scaffolding}")
	private String directoryScaffolding;

	// To test on windows system
	// private String directoryScaffoldingTEST = "c:\\Users\\evalinani\\test";

	private static final String GIT_REPO_URL_NODE = "http_url_to_repo";
	private static final String GITHUB_REPO_URL_NODE = "html_url";
	private static final String GIT_NAME_NODE = "name";
	private static final String DEFAULT_BRANCH_PUSH = "master";
	private static final String INITIAL_COMMIT = "Initial commit";
	private static final String MODEL_PACKAGE = "com.minsait.onesait.microservice.model";
	private static final String REPOSITORY_PACKAGE = "com.minsait.onesait.microservice.repository";
	private static final String REST_SERVICE_PACKAGE = "com.minsait.onesait.microservice.rest";
	private static final String CONFIG_PACKAGE = "com.minsait.onesait.microservice.config";
	private static final String SOURCES_PATH = "/sources/src/main/java";
	private static final String MAIN_JAVA = "src/main/java";
	private static final String MAIN_RESOURCES = "src/main/java";
	private static final String RESOURCES_PATH = "/sources/src/main/resources";
	private static final String DOCKER_PATH = "/sources/docker";
	private static final String NOTEBOOK_PATH = "/zeppelin-spark/notebook/notebook/";
	private static final String NOTEBOOK_FILE = "note.json";
	private Template genericRestServiceTemplate;
	private Template genericRepositoryTemplate;
	private Template dockerfileTemplate;
	private Configuration cfg;

	@PostConstruct
	public void init() {
		cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		try {
			final TemplateLoader templateLoader = new ClassTemplateLoader(getClass(),
					"/static/microservices/templates");

			cfg.setTemplateLoader(templateLoader);

			genericRepositoryTemplate = cfg.getTemplate("/iot-client/GenericRepository.ftl");
			genericRestServiceTemplate = cfg.getTemplate("/iot-client/GenericRestService.ftl");

		} catch (final IOException e) {
			log.error("Error configuring the template loader.", e);
		}
	}

	public void generateScaffolding(JsonNode projectInfo, GitlabConfiguration gitlabConfig, String templateType,
			String ontology, String notebook, String contextPath, int port) throws GitlabException {
		try {

//			if (templateType != null && templateType.equals(TemplateType.IOT_CLIENT_ARCHETYPE.toString())) {
//				generatePOJOs(ontology, directoryScaffolding);
//				processTemplatesIoT(ontology, projectInfo.path(GIT_NAME_NODE).asText(), contextPath, port);
//			} else if (templateType != null && templateType.equals(TemplateType.ML_MODEL_ARCHETYPE.toString())) {
//				processTemplatesML(projectInfo.path(GIT_NAME_NODE).asText());
//			} else if (templateType != null && templateType.equals(TemplateType.NOTEBOOK_ARCHETYPE.toString())) {
//				processTemplatesNb(notebook, projectInfo.path(GIT_NAME_NODE).asText());
//			}
			completeScaffolding(projectInfo, gitlabConfig);
		} catch (final Exception e) {
			log.error("Something went wrong while generating scaffolding ", e);
			throw new GitlabException(e.getMessage());
		}

	}

	public void cloneAndPush(JsonNode projectInfo, GitlabConfiguration mainConfig, GitlabConfiguration cloneConfig,
			boolean checkProjectStructure, String sourcesPath, String dockerPath) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
		gitOperations.cloneRepository(directoryScaffolding,
				com.minsait.onesait.platform.commons.git.GitlabConfiguration.builder().email(cloneConfig.getEmail())
						.password(cloneConfig.getPassword()).privateToken(cloneConfig.getPrivateToken())
						.site(cloneConfig.getSite()).user(cloneConfig.getUser()).build());
		if (checkProjectStructure) {
			checkProjectStructure(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
		}
		try {
			gitOperations.push(projectInfo.get(GIT_REPO_URL_NODE).asText(), mainConfig.getUser(),
					mainConfig.getPassword() == null ? mainConfig.getPrivateToken() : mainConfig.getPassword(),
					DEFAULT_BRANCH_PUSH, directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER, true);
		} catch (final GitSyncException e) {
			// NO-OP doesnt apply here
		}
		gitOperations.deleteDirectory(directoryScaffolding);

	}

	public void cloneAndPushWithTemplate(JsonNode projectInfo, GitlabConfiguration mainConfig,
			MicroserviceTemplate mstemplate, boolean checkProjectStructure) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
		// gitOperations.createDirectoryTEST(directoryScaffoldingTEST);

		gitOperations.cloneRepository(directoryScaffolding, mstemplate.getGitRepository(), mstemplate.getGitUser(),
				mstemplate.getGitPassword(), mstemplate.getGitBranch());
		// gitOperations.cloneRepository(directoryScaffoldingTEST,
		// mstemplate.getGitRepository(), mstemplate.getGitUser(),
		// mstemplate.getGitPassword(), mstemplate.getGitBranch());
		String dockerPath = mstemplate.getDockerRelativePath();
		String sourcesPath = mstemplate.getRelativePath();

//		If you need to test in windows, sourcesPath an dockerPath = ""
		if (checkProjectStructure) {
			if (sourcesPath.equals(".")) {
				sourcesPath = "./";
			}
			if (dockerPath.equals("")) {
				dockerPath = "./";
			}
			checkProjectStructure(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
		}
//		To test in windows, user directoryScaffoldingTEST
		try {
			copyClonedToPushDirectory(mainConfig, projectInfo.get(GIT_REPO_URL_NODE).asText(), sourcesPath, dockerPath);

		} catch (final Exception e) {
			// NO-OP doesnt apply here
		}

//		To test in windows, use function with TEST
//		gitOperations.deleteDirectoryTEST(directoryScaffoldingTEST);
		gitOperations.deleteDirectory(directoryScaffolding);

	}

	private void copyClonedToPushDirectory(GitlabConfiguration mainConfig, String projectURL, String sourcesPath,
			String dockerPath) throws Exception {
		final String randomDir = directoryScaffolding + File.separator + UUID.randomUUID().toString().substring(0, 4);
		Files.createDirectories(new File(randomDir).toPath());
		org.apache.commons.io.FileUtils.copyDirectory(new File(
				directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath),
				new File(randomDir));
		if (!new File(randomDir + File.separator + "docker").exists()) {
			new File(randomDir + File.separator + "docker").mkdir();
		}
		org.apache.commons.io.FileUtils.copyDirectory(new File(
				directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath),
				new File(randomDir + File.separator + "docker"));
		gitOperations.configureGitAndInit(mainConfig.getUser(), mainConfig.getEmail(), randomDir);
		gitOperations.addOrigin(projectURL, randomDir, false);
		gitOperations.addAll(randomDir);
		gitOperations.commit("First Commit", randomDir);
		gitOperations.createBranch("master", randomDir);
		gitOperations.push(projectURL, mainConfig.getUser(),
				mainConfig.getPassword() == null ? mainConfig.getPrivateToken() : mainConfig.getPassword(),
				DEFAULT_BRANCH_PUSH, randomDir, false);
	}

	public void cloneProcessMLAndPush(JsonNode projectInfo, GitlabConfiguration mainConfig,
			MicroserviceTemplate mstemplate, boolean checkProjectStructure) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
//		gitOperations.createDirectoryTEST(directoryScaffoldingTEST);

		gitOperations.cloneRepository(directoryScaffolding, mstemplate.getGitRepository(), mstemplate.getGitUser(),
				mstemplate.getGitPassword(), mstemplate.getGitBranch());
//		gitOperations.cloneRepository(directoryScaffoldingTEST, mstemplate.getGitRepository(), mstemplate.getGitUser(), mstemplate.getGitPassword(), mstemplate.getGitBranch());
		try {
			FileUtils.deleteDirectory(new File(
					directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + ".git"));
		} catch (final IOException e1) {
			// NO-OP
		}

		final String dockerPath = mstemplate.getDockerRelativePath();
		String sourcesPath = mstemplate.getRelativePath();
		try {
			processTemplatesMLTemplate(projectInfo.path(GIT_NAME_NODE).asText(), dockerPath);
		} catch (final Exception e) {
			log.error("Something went wrong while generating scaffolding ", e);
			throw new GitlabException(e.getMessage());
		}

		if (checkProjectStructure) {
			if (sourcesPath.equals(".")) {
				sourcesPath = "";
			}
			checkProjectStructure(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
//			checkProjectStructureTEST(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
//					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
		}
		// To test in windows, change directoryScalffolding to directoryScaffoldingTEST
		// in completeScaffoldingTemplate function
//		gitOperations.deleteDirectoryTEST(directoryScaffoldingTEST);
		completeScaffoldingTemplate(projectInfo, mainConfig);
	}

	public void cloneProcessNBAndPush(JsonNode projectInfo, GitlabConfiguration mainConfig,
			MicroserviceTemplate mstemplate, boolean checkProjectStructure, String notebook) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
		gitOperations.cloneRepository(directoryScaffolding, mstemplate.getGitRepository(), mstemplate.getGitUser(),
				mstemplate.getGitPassword(), mstemplate.getGitBranch());
		try {
			FileUtils.deleteDirectory(new File(
					directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + ".git"));
		} catch (final IOException e1) {
			// NO-OP
		}
		final String dockerPath = mstemplate.getDockerRelativePath();
		String sourcesPath = mstemplate.getRelativePath();
		try {
			processTemplatesNbTemplate(notebook, projectInfo.path(GIT_NAME_NODE).asText(), dockerPath);
		} catch (final Exception e) {
			log.error("Something went wrong while generating scaffolding ", e);
			throw new GitlabException(e.getMessage());
		}

		if (checkProjectStructure) {
			if (sourcesPath.equals(".")) {
				sourcesPath = "";
			}
			checkProjectStructure(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
		}

		completeScaffoldingTemplate(projectInfo, mainConfig);
	}

	public void cloneProcessIOTAndPush(JsonNode projectInfo, GitlabConfiguration mainConfig,
			MicroserviceTemplate mstemplate, boolean checkProjectStructure, int port, String contextPath,
			String ontology) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
//		gitOperations.createDirectoryTEST(directoryScaffoldingTEST);

		gitOperations.cloneRepository(directoryScaffolding, mstemplate.getGitRepository(), mstemplate.getGitUser(),
				mstemplate.getGitPassword(), mstemplate.getGitBranch());
//		gitOperations.cloneRepository(directoryScaffoldingTEST, mstemplate.getGitRepository(), mstemplate.getGitUser(), mstemplate.getGitPassword(), mstemplate.getGitBranch());
		try {
			FileUtils.deleteDirectory(new File(
					directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + ".git"));
		} catch (final IOException e1) {
			// NO-OP
		}
		final String dockerPath = mstemplate.getDockerRelativePath();
		String sourcesPath = mstemplate.getRelativePath();
		try {
			generatePOJOs(ontology, directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER);
			processTemplatesIOTTemplate(ontology, projectInfo.path(GIT_NAME_NODE).asText(), contextPath, port,
					dockerPath, sourcesPath);
		} catch (final Exception e) {
			log.error("Something went wrong while generating scaffolding ", e);
			throw new GitlabException(e.getMessage());
		}

		if (checkProjectStructure) {
			if (sourcesPath.equals(".")) {
				sourcesPath = "";
			}
			checkProjectStructure(GitOperationsImpl.CLONED_FOLDER + File.separator + sourcesPath,
					GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath);
		}
//		To test in windows, change directoryScalffolding to directoryScaffoldingTEST
//		in completeScaffoldingTemplate function
//		gitOperations.deleteDirectoryTEST(directoryScaffoldingTEST);
		completeScaffoldingTemplate(projectInfo, mainConfig);
	}

	public void createAndExtractFiles(String path2Resource, boolean checkProjectStructure, String sourcesPath,
			String dockerPath) throws GitlabException {
		log.info("INIT scafolding project generation");
		gitOperations.createDirectory(directoryScaffolding);
		log.info("Directory created");

		gitOperations.unzipScaffolding(directoryScaffolding, path2Resource);
		log.info("Scafolding project unzipped");

		if (checkProjectStructure) {
			checkProjectStructure(sourcesPath, dockerPath);
		}
	}

	private void checkProjectStructure(String sourcesPath, String dockerPath) throws GitlabException {
		String reason = "";
		if (!new File(directoryScaffolding + File.separator + sourcesPath).exists()) {
			reason += " path " + sourcesPath + " not found on template";
		}
		if (!new File(directoryScaffolding + File.separator + dockerPath).exists()) {
			reason += " path " + dockerPath + " not found on template";
		}
		if (!new File(directoryScaffolding + File.separator + sourcesPath + File.separator + "pom.xml").exists()) {
			reason += " Parent pom.xml not found at " + sourcesPath + "pom.xml";
		}
		if (!"".equals(reason)) {
			gitOperations.deleteDirectory(directoryScaffolding);
			throw new MicroserviceException("Microservice template does not meet requirements, reason: " + reason);

		}
	}

//	private void checkProjectStructureTEST(String sourcesPath, String dockerPath) throws GitlabException {
//		String reason = "";
//		if (!new File(directoryScaffoldingTEST + File.separator + sourcesPath).exists()) {
//			reason += " path " + sourcesPath + " not found on template";
//		}
//		if (!new File(directoryScaffoldingTEST + File.separator + dockerPath).exists()) {
//			reason += " path " + dockerPath + " not found on template";
//		}
//		if (!new File(directoryScaffoldingTEST + File.separator + sourcesPath + File.separator + "pom.xml").exists()) {
//			reason += " Parent pom.xml not found at " + sourcesPath + "pom.xml";
//		}
//		if (!"".equals(reason)) {
//			gitOperations.deleteDirectoryTEST(directoryScaffoldingTEST);
//			throw new MicroserviceException("Microservice template does not meet requirements, reason: " + reason);
//
//		}
//	}

	private void processTemplatesML(String name) throws IOException {
		dockerfileTemplate = cfg.getTemplate("/ml-model/Dockerfile.ftl");
		Writer writer = null;
		final Map<String, Object> map = new HashMap<>();
		map.put("NAME", StringUtils.isEmpty(name) ? "microservice" : name);
		map.put("DOMAIN", resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE));
		try {
			writer = new FileWriter(new File(directoryScaffolding + DOCKER_PATH + File.separator + "Dockerfile"));
			dockerfileTemplate.process(map, writer);
			writer.flush();
		} catch (final Exception e) {
			log.error("Error while processing templates", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					log.error("Could not close writer", e);
				}
			}
		}

	}

	private void processTemplatesMLTemplate(String name, String dockerPath) throws IOException {
		dockerfileTemplate = cfg.getTemplate("/ml-model/Dockerfile.ftl");
		Writer writer = null;
		final Map<String, Object> map = new HashMap<>();
		map.put("NAME", StringUtils.isEmpty(name) ? "microservice" : name);
		map.put("DOMAIN", resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE));
		try {
			writer = new FileWriter(new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER
					+ File.separator + dockerPath + File.separator + "Dockerfile"));
//			writer = new FileWriter(new File(directoryScaffoldingTEST  + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath + File.separator + "Dockerfile"));
			dockerfileTemplate.process(map, writer);
			writer.flush();
		} catch (final Exception e) {
			log.error("Error while processing templates", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					log.error("Could not close writer", e);
				}
			}
		}

	}

	private void processTemplatesNb(String notebookId, String name) throws IOException, URISyntaxException {
		final JSONObject jsonObj = notebookService.exportNotebook(notebookId, utils.getUserId());
		jsonObj.put("id", "0IDSTATIC"); // replace id for static one (always the same)
		final Map<String, String> interpreters = notebookService
				.getNotebookInterpreters(notebookService.getNotebook(notebookId).getIdzep());
		if (interpreters.containsKey("python")) {
			final Path from = Paths.get(directoryScaffolding + "/zeppelin-spark/notebook/Dockerfile.python");
			final Path to = Paths.get(directoryScaffolding + "/zeppelin-spark/notebook/Dockerfile");
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
		}
		final File notebookFolder = new File(directoryScaffolding + NOTEBOOK_PATH);
		notebookFolder.mkdir();
		try (FileWriter file = new FileWriter(directoryScaffolding + NOTEBOOK_PATH + NOTEBOOK_FILE)) {
			file.write(jsonObj.toString());
			log.info("Successfully exported Notebook {} to file {}", notebookId, NOTEBOOK_PATH + NOTEBOOK_FILE);
		}
	}

	private void processTemplatesNbTemplate(String notebookId, String name, String dockerPath)
			throws IOException, URISyntaxException {
		final JSONObject jsonObj = notebookService.exportNotebook(notebookId, utils.getUserId());
		jsonObj.put("id", "0IDSTATIC"); // replace id for static one (always the same)
		final Map<String, String> interpreters = notebookService
				.getNotebookInterpreters(notebookService.getNotebook(notebookId).getIdzep());
		if (interpreters.containsKey("python")) {
			final Path from = Paths.get(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER
					+ File.separator + dockerPath + File.separator + "Dockerfile.python");
			final Path to = Paths.get(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER
					+ File.separator + dockerPath + File.separator + "Dockerfile");
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
		}
		final File notebookFolder = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER
				+ File.separator + dockerPath + "/notebook/");
		notebookFolder.mkdir();
		try (FileWriter file = new FileWriter(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER
				+ File.separator + dockerPath + "/notebook/" + NOTEBOOK_FILE)) {
			file.write(jsonObj.toString());
			log.info("Successfully exported Notebook {} to file {}", notebookId, File.separator
					+ GitOperationsImpl.CLONED_FOLDER + File.separator + dockerPath + "/notebook/" + NOTEBOOK_FILE);
		}
	}

	private void processTemplatesIoT(String ontology, String name, String contextPath, int port) throws IOException {
		final Template swaggerConfigTemplate = cfg.getTemplate("/iot-client/SwaggerConfig.ftl");
		dockerfileTemplate = cfg.getTemplate("/iot-client/Dockerfile.ftl");
		final Template applicationYmlTemplate = cfg.getTemplate("/iot-client/application.ftl");
		final String ontologyCap = ontology.substring(0, 1).toUpperCase() + ontology.substring(1);
		final ClientPlatform client = getDeviceForOntologyAndUser(ontology);
		final Token token = tokenService.getToken(client);
		Writer writer = null;
		final Map<String, Object> map = new HashMap<>();
		map.put("WRAPPER_CLASS", ontologyCap + "Wrapper");
		map.put("ONTOLOGY", ontology);
		map.put("ONTOLOGY_CAP", ontologyCap);
		map.put("DOMAIN", resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE));
		map.put("DEVICE_TOKEN", token.getTokenName());
		map.put("DEVICE_TEMPLATE", client.getIdentification());
		map.put("NAME", StringUtils.isEmpty(name) ? "microservice" : name);
		map.put("CONTEXT_PATH", contextPath);
		map.put("PORT", String.valueOf(port));
		try {
			writer = new FileWriter(new File(directoryScaffolding + SOURCES_PATH + File.separator
					+ REPOSITORY_PACKAGE.replace(".", "/") + File.separator + ontologyCap.concat("Repository.java")));
			genericRepositoryTemplate.process(map, writer);
			writer.flush();
			writer = new FileWriter(new File(directoryScaffolding + SOURCES_PATH + File.separator
					+ REST_SERVICE_PACKAGE.replace(".", "/") + File.separator + ontologyCap.concat("Service.java")));
			genericRestServiceTemplate.process(map, writer);
			writer.flush();
			writer = new FileWriter(new File(directoryScaffolding + SOURCES_PATH + File.separator
					+ CONFIG_PACKAGE.replace(".", "/") + File.separator + "SwaggerConfig.java"));
			swaggerConfigTemplate.process(map, writer);
			writer.flush();
			writer = new FileWriter(new File(directoryScaffolding + DOCKER_PATH + File.separator + "Dockerfile"));
			dockerfileTemplate.process(map, writer);
			writer.flush();
			writer = new FileWriter(
					new File(directoryScaffolding + RESOURCES_PATH + File.separator + "application.yml"));
			applicationYmlTemplate.process(map, writer);
			writer.flush();

		} catch (final Exception e) {
			log.error("Error while processing templates", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					log.error("Could not close writer", e);
				}
			}
		}

	}

	private void processTemplatesIOTTemplate(String ontology, String name, String contextPath, int port,
			String dockerPath, String sourcesPath) throws IOException {
		final Template swaggerConfigTemplate = cfg.getTemplate("/iot-client/SwaggerConfig.ftl");
		dockerfileTemplate = cfg.getTemplate("/iot-client/Dockerfile.ftl");
		final Template applicationYmlTemplate = cfg.getTemplate("/iot-client/application.ftl");
		final String ontologyCap = ontology.substring(0, 1).toUpperCase() + ontology.substring(1);
		final ClientPlatform client = getDeviceForOntologyAndUser(ontology);
		final Token token = tokenService.getToken(client);
		Writer writer = null;
		final Map<String, Object> map = new HashMap<>();
		map.put("WRAPPER_CLASS", ontologyCap + "Wrapper");
		map.put("ONTOLOGY", ontology);
		map.put("ONTOLOGY_CAP", ontologyCap);
		map.put("DOMAIN", resourcesService.getUrl(Module.DOMAIN, ServiceUrl.BASE));
		map.put("DEVICE_TOKEN", token.getTokenName());
		map.put("DEVICE_TEMPLATE", client.getIdentification());
		map.put("NAME", StringUtils.isEmpty(name) ? "microservice" : name);
		map.put("CONTEXT_PATH", contextPath);
		map.put("PORT", String.valueOf(port));

		try {
			File f = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator
					+ sourcesPath + File.separator + MAIN_JAVA + File.separator + REPOSITORY_PACKAGE.replace(".", "/"));
			f.mkdirs();
			writer = new FileWriter(
					new File(f.getAbsolutePath() + File.separator + ontologyCap.concat("Repository.java")));
			genericRepositoryTemplate.process(map, writer);
			writer.flush();
			f = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator
					+ sourcesPath + File.separator + MAIN_JAVA + File.separator
					+ REST_SERVICE_PACKAGE.replace(".", "/"));
			f.mkdirs();
			writer = new FileWriter(
					new File(f.getAbsolutePath() + File.separator + ontologyCap.concat("Service.java")));
			genericRestServiceTemplate.process(map, writer);
			writer.flush();
			f = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator
					+ sourcesPath + File.separator + MAIN_JAVA + File.separator + CONFIG_PACKAGE.replace(".", "/"));
			f.mkdirs();
			writer = new FileWriter(new File(f.getAbsolutePath() + File.separator + "SwaggerConfig.java"));
			swaggerConfigTemplate.process(map, writer);
			writer.flush();
			f = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator
					+ dockerPath);
			f.mkdirs();
			writer = new FileWriter(new File(f.getAbsolutePath() + File.separator + "Dockerfile"));
			dockerfileTemplate.process(map, writer);
			writer.flush();
			f = new File(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER + File.separator
					+ sourcesPath + File.separator + MAIN_RESOURCES);
			f.mkdirs();
			writer = new FileWriter(new File(f.getAbsolutePath() + File.separator + "application.yml"));
			applicationYmlTemplate.process(map, writer);
			writer.flush();

		} catch (final Exception e) {
			log.error("Error while processing templates", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException e) {
					log.error("Could not close writer", e);
				}
			}
		}

	}

	public void completeScaffolding(JsonNode projectInfo, GitlabConfiguration gitlabConfig) throws GitlabException {

		gitOperations.configureGitlabAndInit(gitlabConfig.getUser(), gitlabConfig.getEmail(), directoryScaffolding);
		log.info("Gitlab project configured");
		final String sshUrl = projectInfo.path(GIT_REPO_URL_NODE).isMissingNode()
				? projectInfo.path(GITHUB_REPO_URL_NODE).asText()
				: projectInfo.path(GIT_REPO_URL_NODE).asText();
		gitOperations.addOrigin(sshUrl, directoryScaffolding, false);
		log.info("Origin added");

		gitOperations.addAll(directoryScaffolding);
		log.info("Add all");

		gitOperations.commit(INITIAL_COMMIT, directoryScaffolding);
		log.info(INITIAL_COMMIT);

		try {
			gitOperations.push(sshUrl, gitlabConfig.getUser(),
					gitlabConfig.getPassword() == null ? gitlabConfig.getPrivateToken() : gitlabConfig.getPassword(),
					DEFAULT_BRANCH_PUSH, directoryScaffolding, false);
		} catch (final GitSyncException e) {
			// NO-OP doesnt apply here
		}
		log.info("Pushed to: " + sshUrl);

		gitOperations.deleteDirectory(directoryScaffolding);
		log.info("Deleting temp directory {}", directoryScaffolding);
		log.info("END scafolding project generation");

	}

	public void completeScaffoldingTemplate(JsonNode projectInfo, GitlabConfiguration gitlabConfig)
			throws GitlabException {

		gitOperations.configureGitlabAndInit(gitlabConfig.getUser(), gitlabConfig.getEmail(),
				directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER);
		log.info("Gitlab project configured");

		gitOperations.addAll(directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER);
		log.info("Add all");

		gitOperations.commit(INITIAL_COMMIT, directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER);
		log.info(INITIAL_COMMIT);

		try {
			gitOperations.push(projectInfo.get(GIT_REPO_URL_NODE).asText(), gitlabConfig.getUser(),
					gitlabConfig.getPassword() == null ? gitlabConfig.getPrivateToken() : gitlabConfig.getPassword(),
					DEFAULT_BRANCH_PUSH, directoryScaffolding + File.separator + GitOperationsImpl.CLONED_FOLDER, true);
		} catch (final GitSyncException e) {
			// NO-OP doesnt apply here
		}
		log.info("Pushed to: " + projectInfo.get(GIT_REPO_URL_NODE).asText());

		gitOperations.deleteDirectory(directoryScaffolding);
		log.info("Deleting temp directory {}", directoryScaffolding);
		log.info("END scafolding project generation");

	}

	private ClientPlatform getDeviceForOntologyAndUser(String identification) {
		final String userId = utils.getUserId();
		ClientPlatform client = clientPlatformOntologyRepository
				.findByOntology(ontologyService.getOntologyByIdentification(identification)).stream()
				.filter(r -> r.getClientPlatform().getUser().getUserId().equals(userId)).map(r -> r.getClientPlatform())
				.findFirst().orElse(null);
		if (client == null) {
			client = new ClientPlatform();
			client.setIdentification(identification.concat("DeviceMicroservice"));
			client.setUser(userService.getUser(userId));
			clientPlatformService.createClientAndToken(
					Arrays.asList(ontologyService.getOntologyByIdentification(identification)), client);
		}
		return client;

	}

	private void generatePOJOs(String ontology, String directory) {

		final JCodeModel codeModel = new JCodeModel();
		final String ontologyCap = ontology.substring(0, 1).toUpperCase() + ontology.substring(1);
		final String source = ontologyService.getOntologyByIdentification(ontology).getJsonSchema();
		final String wrapperClassName = ontologyCap + "Wrapper";
		final File outputPojoDirectory = new File(directory + SOURCES_PATH);
		outputPojoDirectory.mkdirs();
		final String packageName = MODEL_PACKAGE;
		final GenerationConfig config = new DefaultGenerationConfig() {
			@Override
			public boolean isGenerateBuilders() {
				return true;
			}

			@Override
			public SourceType getSourceType() {
				return SourceType.JSONSCHEMA;
			}

			@Override
			public InclusionLevel getInclusionLevel() {
				return InclusionLevel.NON_NULL;
			}

			@Override
			public boolean isIncludeToString() {
				return false;
			}

			@Override
			public boolean isIncludeHashcodeAndEquals() {
				return false;
			}

		};
		final SchemaMapper mapper = new SchemaMapper(
				new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());

		try {
			mapper.generate(codeModel, wrapperClassName, packageName, source);
			codeModel.build(outputPojoDirectory);
		} catch (final IOException e) {
			log.error("Could not complete microservice model domain");
		}
	}

}
