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
package com.minsait.onesait.platform.business.services.versioning;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.git.CommitWrapper;
import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.git.GitServiceManager;
import com.minsait.onesait.platform.commons.git.GitSyncException;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.config.model.DashboardConf;
import com.minsait.onesait.platform.config.model.GadgetTemplateType;
import com.minsait.onesait.platform.config.model.Role;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.base.AuditableEntity;
import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.model.interfaces.Versionable.SpecialVersionable;
import com.minsait.onesait.platform.config.services.opresource.OPResourceService;
import com.minsait.onesait.platform.config.services.user.UserService;
import com.minsait.onesait.platform.config.versioning.BundleGenerateDTO;
import com.minsait.onesait.platform.config.versioning.RestorePlatformDTO;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.RestoreReport.OperationResult;
import com.minsait.onesait.platform.config.versioning.VersionableVO;
import com.minsait.onesait.platform.config.versioning.VersioningException;
import com.minsait.onesait.platform.config.versioning.VersioningIOService;
import com.minsait.onesait.platform.config.versioning.VersioningManager;
import com.minsait.onesait.platform.config.versioning.VersioningManager.EventType;
import com.minsait.onesait.platform.config.versioning.VersioningRepositoryFacade;
import com.minsait.onesait.platform.config.versioning.VersioningTxBusinessService;
import com.minsait.onesait.platform.config.versioning.VersioningTxBusinessServiceImpl;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VersioningBusinessServiceImpl implements VersioningBusinessService {

	private static final List<String> ONLY_ADMIN_ENTITIES = Arrays.asList(DashboardConf.class.getName(),
			GadgetTemplateType.class.getName(), User.class.getName());
	private static final String GENERIC_COMMIT_OVERWRITE = "File changes from controlpanel UI by user %s";

	@Autowired
	private OPResourceService opResourceService;
	@Autowired
	private UserService userService;
	@Autowired
	private VersioningRepositoryFacade versioningRepositoryFacade;
	@Autowired
	private GitServiceManager gitManager;
	@Autowired
	private VersioningIOService versioningIOService;
	@Autowired
	private VersioningManager versioningManager;
	@Autowired
	private GitOperations gitOperations;
	@Autowired
	private VersioningTxBusinessService versioningTxBusinessService;

	@Value("${spring.application.name:no-name}")
	private String applicationName;

	private final Map<String, RestoreReport> reports = new HashMap<>();

	@PostConstruct
	public void reinitGit() {
		if (applicationName.contains("control-panel")) {
			reinitializeGitDir();
		}
	}

	@Override
	public Map<String, String> getVersionableClases() {
		final Map<String, String> map = new LinkedHashMap<>();
		final BeanDefinitionRegistry bdr = new SimpleBeanDefinitionRegistry();
		final ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(bdr);

		final TypeFilter tf = new AssignableTypeFilter(Versionable.class);
		s.addIncludeFilter(tf);
		s.setIncludeAnnotationConfig(false);
		s.scan(VersioningTxBusinessServiceImpl.SCAN_PACKAGE);
		Arrays.asList(bdr.getBeanDefinitionNames()).stream().sorted()
				.forEach(b -> map.put(bdr.getBeanDefinition(b).getBeanClassName(), b));
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Versionable<?>> getEntitiesForUser(String userId, String clazz) {
		try {
			final User user = userService.getUser(userId);
			final Versionable<?> o = (Versionable<?>) Class.forName(clazz).newInstance();
			if (o instanceof OPResource) {
				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					return opResourceService.getResourcesVersionablesByType(user, o.getClass());
				}
				return opResourceService.getResourcesVersionablesForUserAndType(user, o.getClass());
			} else {
				if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
					return versioningRepositoryFacade.getJpaRepository(o).findAll().stream()
							.map(v -> (Versionable<?>) v).collect(Collectors.toList());
				} else if (!ONLY_ADMIN_ENTITIES.contains(clazz)) {
					final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(o);
					try {
						final Method findByUser = repo.getClass().getMethod("findByUser", User.class);
						return (List<Versionable<?>>) findByUser.invoke(repo, user);
					} catch (NoSuchMethodException | SecurityException | IllegalArgumentException
							| InvocationTargetException e) {
						log.error("Reflection error while trying to execute findByUser", e);
					}
				}
			}

		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error("Error instaciating class of type {}", clazz, e);
		}

		return new ArrayList<>();
	}

	@Override
	public Versionable<?> findById(Object id, String clazz) {
		try {
			final Versionable<?> o = (Versionable<?>) Class.forName(clazz).newInstance();
			return versioningRepositoryFacade.getJpaRepository(o).findById(id).orElse(null);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error("Error instaciating class of type {}", clazz, e);
		}
		return null;
	}

	@Override
	public <T> List<CommitWrapper> getCommitsForVersionable(Versionable<T> versionable) {
		final GitlabConfiguration gitConfig = getGitConfiguration();
		final List<String> zipfiles = versionable.zipFileNames();
		final List<CommitWrapper> commits = gitManager.dispatchService(gitConfig.getSite()).getCommitsForFile(gitConfig,
				versioningIOService.relativePath(versionable), gitConfig.getBranch());
		if (zipfiles != null) {
			final List<CommitWrapper> commitsZips = zipfiles.stream()
					.map(s -> gitManager.dispatchService(gitConfig.getSite()).getCommitsForFile(gitConfig,
							s.substring(VersioningIOService.DIR.length()), gitConfig.getBranch()))
					.flatMap(c -> c.stream()).collect(Collectors.toList());
			return Stream.of(commits, commitsZips).flatMap(c -> c.stream()).collect(Collectors.toList());
		}
		return commits;
	}

	@Override
	@Transactional
	public RestoreReport restoreFile(RestoreRequestDTO restoreRequest, RestoreReport report) {
		report.setInitTime(System.currentTimeMillis());
		Versionable<?> versionable;
		try {
			versionable = (Versionable<?>) Class.forName(restoreRequest.getClazz()).newInstance();
			if (restoreRequest.isExistingVersionable()) {
				versionable = versioningRepositoryFacade.getJpaRepository(versionable)
						.findById(restoreRequest.getEntityId()).orElse(null);
			} else {
				versionable = (Versionable<?>) versionable.deserialize(gitOperations.showFileFromCommit(
						VersioningIOService.DIR, restoreRequest.getCommitId(), restoreRequest.getFileName()));
			}
			versioningManager.restoreSerialization(versionable, restoreRequest.getCommitId(),
					restoreRequest.getUserId(), restoreRequest.getCommitMessage());
			report.setOperationResult(OperationResult.SUCCESS);
		} catch (final Exception e) {
			log.error("Could not restore file {} from commit {}", restoreRequest.getFileName(),
					restoreRequest.getCommitId(), e);
			report.getErrors().add(e.getMessage());
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getInitTime() - report.getEndTime());
		return report;
	}

	@Override
	public String getFileContent(String file, String commitId) {
		return gitOperations.showFileFromCommit(VersioningIOService.DIR, commitId, file);
	}

	@Override
	public String getFileContent(Versionable<?> versionable, String commitId) {
		return gitOperations.showFileFromCommit(VersioningIOService.DIR, commitId,
				versioningIOService.relativePath(versionable));
	}

	@Override
	public boolean isActive() {
		return versioningManager.isActive();
	}

	@Override
	public GitlabConfiguration getGitConfiguration() {
		return versioningManager.getGitConfiguration();
	}

	@Override
	@Transactional
	public void createGitConfiguration(GitlabConfiguration gitConfiguration, boolean createGit) {
		if (createGit) {
			final String projectUrl = gitManager.dispatchService(gitConfiguration.getSite()).createGitlabProject(false,
					null, gitConfiguration, gitConfiguration.getProjectName());
			gitConfiguration.setProjectURL(projectUrl);
			gitConfiguration.setEmail(
					gitManager.dispatchService(gitConfiguration.getSite()).getGitlabConfigurationFromPrivateToken(
							gitConfiguration.getSite(), gitConfiguration.getPrivateToken()).getEmail());
			FileSystemUtils.deleteRecursively(new File(VersioningIOService.DIR));
			gitOperations.createDirectory(VersioningIOService.DIR);
			gitOperations.configureGitAndInit(gitConfiguration.getUser(), gitConfiguration.getEmail(),
					VersioningIOService.DIR);
			gitOperations.checkout(gitConfiguration.getBranch(), VersioningIOService.DIR, true);
		} else {
			if (!StringUtils.hasText(gitConfiguration.getSite())) {
				gitConfiguration.setSite(extractGitSiteFromRepositorty(gitConfiguration.getProjectURL()));
			}
			gitConfiguration.setEmail(
					gitManager.dispatchService(gitConfiguration.getSite()).getGitlabConfigurationFromPrivateToken(
							gitConfiguration.getSite(), gitConfiguration.getPrivateToken()).getEmail());
			// gitConfiguration.setSite(gitConfiguration.getProjectURL());
			gitConfiguration.setProjectName(
					gitConfiguration.getProjectURL().split("/")[gitConfiguration.getProjectURL().split("/").length - 1]
							.split("\\.")[0]);
			if (getGitConfiguration() != null
					&& !gitConfiguration.getProjectURL().equals(getGitConfiguration().getProjectURL())
					|| !new File(VersioningIOService.DIR).exists()) {
				FileSystemUtils.deleteRecursively(new File(VersioningIOService.DIR));
				gitOperations.createDirectory(VersioningIOService.DIR);
				gitOperations.cloneRepository(VersioningIOService.DIR, gitConfiguration.getProjectURL(),
						gitConfiguration.getUser(), gitConfiguration.getPrivateToken(), gitConfiguration.getBranch(),
						true);
				gitOperations.configureGitAndInit(gitConfiguration.getUser(), gitConfiguration.getEmail(),
						VersioningIOService.DIR);
				// // syncFilesystemToDB(null);
			} else {
				gitOperations.configureGitAndInit(gitConfiguration.getUser(), gitConfiguration.getEmail(),
						VersioningIOService.DIR);
			}
			gitOperations.checkout(gitConfiguration.getBranch(), VersioningIOService.DIR, true);
		}
		gitConfiguration.setEnable(true);
		versioningManager.saveGitConfiguration(gitConfiguration);
		if (createGit) {
			generateSnapShot(null, new RestoreReport());
		}
	}

	@Override
	public void enableFeature(boolean enable) {
		versioningManager.enableFeature(enable);
	}

	@Override
	@Async
	public void restorePlatform(RestorePlatformDTO restoreDTO, RestoreReport report) {
		report.setInitTime(System.currentTimeMillis());
		if (isActive()) {
			try {
				// TWO STEP RESTORE 1)RESTORE 2)DELETE NON PRESENT USERS
				versioningTxBusinessService.restorePlatform(restoreDTO, report, getVersionableClases(),
						getGitConfiguration());
				report.getUsersToBeRemoved().forEach(u -> versioningTxBusinessService.deleteUser(u));
				if (report.getErrors().isEmpty()) {
					report.setResultMessage("Platform Resources that are versionable were restored from "
							+ restoreDTO.getSourceProject() + " and " + restoreDTO.getRestoreType().name() + " "
							+ restoreDTO.getSourceBranchTag());
					report.setOperationResult(OperationResult.SUCCESS);
					concludeRestoration(getGitConfiguration(), restoreDTO, report);
				} else {
					report.setResultMessage("Restoration of versionable resources couldn't be completed. Errors are: "
							+ String.join(";", report.getErrors()));
					report.setOperationResult(OperationResult.FAILED);
					// Rollback git directories
					reinitializeGitDir();
				}
			} catch (final Exception e) {
				report.setResultMessage("Error restoring the platform from " + restoreDTO.getSourceProject() + " and "
						+ restoreDTO.getRestoreType().name() + " " + restoreDTO.getSourceBranchTag() + ", error: "
						+ e.getMessage()
						+ (report.getErrors().isEmpty() ? "" : ".Errors: " + String.join(";", report.getErrors())));
				log.error("Error restaurando la plataforma: ", e);
				report.setOperationResult(OperationResult.FAILED);
				// Rollback git directories
				reinitializeGitDir();
			}
		}
		report.setFinished(true);
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		log.info(
				"Finished Restoration process in background mode.Time taken: {} ms. Result state is {} and result message: {}. Num of versionables in repository: {}",
				report.getEndTime() - report.getInitTime(), report.getOperationResult().name(),
				report.getResultMessage(), report.getVersionablesInRepository());
		reports.put(report.getExecutionId(), report);
	}

	@Override
	@Async
	public void restoreBundle(RestoreReport report, GitlabConfiguration gitConfig, String folderName) {
		report.setInitTime(System.currentTimeMillis());
		final String dir = "/tmp/import/" + UUID.randomUUID().toString().substring(0, 5);
		initializeGitDir(gitConfig, dir);
		try {
			versioningTxBusinessService.restoreBundle(report, getVersionableClases(), dir, folderName,
					report.getUserId());
			if (report.getErrors().isEmpty()) {
				report.setResultMessage("Bundle " + folderName + " was restored from " + gitConfig.getProjectURL()
						+ " and branch " + gitConfig.getBranch());
				report.setOperationResult(OperationResult.SUCCESS);
			} else {
				report.setResultMessage(
						"Loading of bundle couldn't be completed. Errors are: " + String.join(";", report.getErrors()));
				report.setOperationResult(OperationResult.FAILED);
			}
		} catch (final Exception e) {
			report.setResultMessage("Error loading bundle " + folderName + " from " + gitConfig.getProjectURL()
					+ " and branch " + gitConfig.getBranch() + ", error: " + e.getMessage()
					+ (report.getErrors().isEmpty() ? "" : ".Errors: " + String.join(";", report.getErrors())));
			log.error("Error loading bundle: {}", folderName, e);
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setFinished(true);
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		log.info(
				"Loaded bundle {} in background mode.Time taken: {} ms. Result state is {} and result message: {}. Num of versionables loaded: {}",
				folderName, report.getEndTime() - report.getInitTime(), report.getOperationResult().name(),
				report.getResultMessage(), report.getVersionablesInRepository());
		reports.put(report.getExecutionId(), report);
		FileSystemUtils.deleteRecursively(new File(dir));
	}

	@Override
	@Async
	public void loadBundleZip(RestoreReport report, InputStream file, String fileName) {
		report.setInitTime(System.currentTimeMillis());
		final String folderName = fileName.split("\\.")[0];
		final String dir = "/tmp/import/" + UUID.randomUUID().toString().substring(0, 5);
		FileSystemUtils.deleteRecursively(new File(dir));
		new File(dir).mkdirs();
		try {
			VersioningUtils.unzipFolder(file, new File(dir));
			versioningTxBusinessService.restoreBundle(report, getVersionableClases(), dir, folderName,
					report.getUserId());
			if (report.getErrors().isEmpty()) {
				report.setResultMessage("Bundle " + folderName + " was restored from zip file: " + fileName);
				report.setOperationResult(OperationResult.SUCCESS);
			} else {
				report.setResultMessage(
						"Loading of bundle couldn't be completed. Errors are: " + String.join(";", report.getErrors()));
				report.setOperationResult(OperationResult.FAILED);
			}
		} catch (final Exception e) {
			report.setResultMessage(
					"Error loading bundle " + folderName + " from " + fileName + ", error: " + e.getMessage()
							+ (report.getErrors().isEmpty() ? "" : ".Errors: " + String.join(";", report.getErrors())));
			log.error("Error loading bundle: {}", folderName, e);
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setFinished(true);
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		log.info(
				"Loaded bundle {} in background mode.Time taken: {} ms. Result state is {} and result message: {}. Num of versionables loaded: {}",
				folderName, report.getEndTime() - report.getInitTime(), report.getOperationResult().name(),
				report.getResultMessage(), report.getVersionablesInRepository());
		reports.put(report.getExecutionId(), report);
		FileSystemUtils.deleteRecursively(new File(dir));
	}

	private void concludeRestoration(GitlabConfiguration gitConfig, RestorePlatformDTO restoreDTO,
			RestoreReport report) {
		try {
			if (restoreDTO.getRestoreType().equals(RestorePlatformDTO.Restore.TAG)) {
				gitOperations.checkout(gitConfig.getBranch(), VersioningIOService.DIR, true);
				gitOperations.changeRemoteURL(VersioningIOService.DIR, gitConfig.getProjectURL());
				try {
					gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
							gitConfig.getBranch(), VersioningIOService.DIR, false, true);
				} catch (final GitSyncException e) {
					versioningManager.syncOriginAndDB();
					gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
							gitConfig.getBranch(), VersioningIOService.DIR, false, true);
				}
			} else {
				gitOperations.changeRemoteURL(VersioningIOService.DIR, gitConfig.getProjectURL());
				gitOperations.addAll(VersioningIOService.DIR);
				gitOperations.commit("Platform Restore from project: " + restoreDTO.getSourceProject() + " & Branch: "
						+ restoreDTO.getSourceBranchTag(), VersioningIOService.DIR);
				try {
					gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
							gitConfig.getBranch(), VersioningIOService.DIR, false, true);
				} catch (final GitSyncException e) {
					versioningManager.syncOriginAndDB();
					gitOperations.push(gitConfig.getProjectURL(), gitConfig.getUser(), gitConfig.getPrivateToken(),
							gitConfig.getBranch(), VersioningIOService.DIR, false, true);
				}
			}
			if (CollectionUtils.isEmpty(report.getExcludeResources())) {
				versioningManager.updateLastCommitProcessed();
			}
		} catch (final Exception e) {
			log.error("Error cloncluding restoration process", e);
		}

	}

	@Override
	@Async
	public void generateSnapShot(String tagName, @NotNull RestoreReport report) {
		report.setInitTime(System.currentTimeMillis());
		versioningTxBusinessService.generateSnapShot(tagName, report, getVersionableClases(), getGitConfiguration());
		if (report.getErrors().isEmpty()) {
			if (!!StringUtils.hasText(tagName)) {
				report.setResultMessage("Created snapshot of resources and Git Tag " + tagName + " successfully.");
			} else {
				report.setResultMessage("Created snapshot of resources successfully.");
			}
			report.setOperationResult(OperationResult.SUCCESS);
		} else {
			report.setResultMessage(
					"Snapshot & Tag process couldn't be completed. Error: " + String.join(";", report.getErrors()));
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setFinished(true);
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		log.info(
				"Finished Snapshot process in background mode.Time taken: {} ms. Result state is {} and result message: {}. Num of versionables in repository: {}",
				report.getEndTime() - report.getInitTime(), report.getOperationResult().name(),
				report.getResultMessage(), report.getVersionablesInRepository());
		reports.put(report.getExecutionId(), report);
	}

	@Override
	public void createBundle(GitlabConfiguration gitConfig, @NotNull RestoreReport report, BundleGenerateDTO bundle) {
		report.setInitTime(System.currentTimeMillis());
		final String dir = "/tmp/export/" + UUID.randomUUID().toString().substring(0, 5);
		initializeGitDir(gitConfig, dir);
		versioningTxBusinessService.createBundle(report, getVersionableClases(), gitConfig, dir, bundle);
		if (report.getErrors().isEmpty()) {

			report.setResultMessage("Exported resources successfully.");

			report.setOperationResult(OperationResult.SUCCESS);
		} else {
			report.setResultMessage(
					"Export of resources couldn't be completed. Error: " + String.join(";", report.getErrors()));
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setFinished(true);
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		log.info(
				"Finished export process in background mode.Time taken: {} ms. Result state is {} and result message: {}. Num of versionables exported: {}",
				report.getEndTime() - report.getInitTime(), report.getOperationResult().name(),
				report.getResultMessage(), report.getVersionablesInRepository());
		reports.put(report.getExecutionId(), report);
		FileSystemUtils.deleteRecursively(new File(dir));
	}

	@Override
	public File createZipBundle(@NotNull RestoreReport report, BundleGenerateDTO bundle) throws Exception {
		report.setInitTime(System.currentTimeMillis());
		final String dir = "/tmp/export/" + UUID.randomUUID().toString().substring(0, 5);
		FileSystemUtils.deleteRecursively(new File(dir));
		new File(dir).mkdirs();
		versioningTxBusinessService.createZipBundle(report, getVersionableClases(), dir, bundle);
		final File output = new File("/tmp" + File.separator + bundle.getFolderName() + ".zip");
		if (output.exists()) {
			output.delete();
		}
		VersioningUtils.zipFolder(new File(dir), output);
		FileSystemUtils.deleteRecursively(new File(dir));
		return output;
	}

	private void initializeGitDir(GitlabConfiguration gitConfig, String directory) {
		FileSystemUtils.deleteRecursively(new File(directory));
		new File(directory).mkdirs();
		gitOperations.cloneRepository(directory, gitConfig.getProjectURL(), gitConfig.getUser(),
				gitConfig.getPrivateToken(), gitConfig.getBranch(), true);
		gitOperations.configureGitAndInit(gitConfig.getUser(),
				gitManager.dispatchService(gitConfig.getProjectURL())
						.getGitlabConfigurationFromPrivateToken(
								gitConfig.getProjectURL().substring(0, gitConfig.getProjectURL().indexOf(".com") + 4),
								gitConfig.getPrivateToken())
						.getEmail(),
				directory);
		gitOperations.checkout(gitConfig.getBranch(), directory);
	}

	@Override
	public void removeGitConfiguration() {
		versioningManager.removeGitConfiguration();
	}

	@Override
	public RestoreReport saveFileChangesToEntity(SaveFileToEntityDTO saveFileToEntityDTO) {
		final RestoreReport report = new RestoreReport();
		report.setInitTime(System.currentTimeMillis());
		try {
			Versionable<?> o = (Versionable<?>) Class.forName(saveFileToEntityDTO.getClazz()).newInstance();
			o = (Versionable<?>) o.deserialize(saveFileToEntityDTO.getFileContent());
			if (!!StringUtils.hasText(saveFileToEntityDTO.getCommitMsg())) {
				VersioningCommitContextHolder.setCommitMessage(saveFileToEntityDTO.getCommitMsg());
			} else {
				VersioningCommitContextHolder
						.setCommitMessage(String.format(GENERIC_COMMIT_OVERWRITE, saveFileToEntityDTO.getUserId()));
			}
			versioningRepositoryFacade.save(o);
			report.setOperationResult(OperationResult.SUCCESS);
			report.setResultMessage("Saved resource successfully with id " + o.getId());
		} catch (final Exception e) {
			log.error("Error while trying to save file changes to entity: {}", e.getMessage(), e);
			report.setResultMessage(
					"Could not save resource\nError: " + VersioningException.processErrorMessageToFront(e));
			report.setOperationResult(OperationResult.FAILED);
		}
		report.setEndTime(System.currentTimeMillis());
		report.setTimeTaken(report.getEndTime() - report.getInitTime());
		return report;
	}

	@Override
	public void reinitializeGitDir() {
		if (isActive()) {
			try {
				final GitlabConfiguration gitConfiguration = getGitConfiguration();
				FileSystemUtils.deleteRecursively(new File(VersioningIOService.DIR));
				gitOperations.createDirectory(VersioningIOService.DIR);
				gitOperations.cloneRepository(VersioningIOService.DIR, gitConfiguration.getProjectURL(),
						gitConfiguration.getUser(), gitConfiguration.getPrivateToken(), gitConfiguration.getBranch(),
						true);
				gitOperations.configureGitAndInit(gitConfiguration.getUser(), gitConfiguration.getEmail(),
						VersioningIOService.DIR);
				if (!!StringUtils.hasText(gitConfiguration.getLastCommitSHA())) {
					versioningTxBusinessService.syncOriginAndDB(gitConfiguration.getLastCommitSHA());
				}
			} catch (final Exception e) {
				log.error("Error while reinitializing Git for versioning, disabling current configuration.", e);
			}
		}
	}

	@Override
	public boolean isTagValid(String tagName) {
		return gitOperations.checkTagIsValid(tagName);
	}

	private String extractGitSiteFromRepositorty(String repository) {
		URL url;
		try {
			url = new URL(repository);
			return url.getProtocol() + "://" + url.getHost() + "/";
		} catch (final MalformedURLException e) {
			return repository;
		}
	}

	@Override
	public RestoreReport getReport(String executionId) {
		return reports.get(executionId);
	}

	@Scheduled(fixedDelay = 10000000)
	public void removeReports() {
		reports.entrySet().removeIf(e -> e.getValue().getEndTime() < System.currentTimeMillis() + 10000000L);
	}

	@Scheduled(fixedDelay = 60000)
	public void syncGitConfig() {
		if (versioningManager.isActive()) {
			VersioningCommitContextHolder.setProcessPostAllEvents(false);
			versioningManager.saveGitConfiguration(versioningManager.getGitConfiguration());
		}
	}

	@Override
	@Async
	public void syncGitAndDB() {
		if (isActive()) {
			versioningTxBusinessService.syncOriginAndDB();
		}
	}

	@Override
	public void commitSpecialVersionable(SpecialVersionable versionable, Object id, String commitMessage) {
		try {
			Versionable<?> o = (Versionable<?>) Class
					.forName(VersioningTxBusinessServiceImpl.SCAN_PACKAGE + "." + versionable.name()).newInstance();
			if (o instanceof OPResource) {
				try {
					o = (Versionable<?>) opResourceService.getResourceByIdentification((String) id);
				} catch (final Exception e) {
					log.error("Could not retrieve opresource of type {}", versionable.name(), e);
				}
			} else {
				o = versioningRepositoryFacade.getJpaRepository(o).findById(id).orElse(null);

			}
			if (o != null) {
				((AuditableEntity) o).setUpdatedAJson(System.currentTimeMillis());
				VersioningCommitContextHolder.setProcessPostAllEvents(false);
				versioningRepositoryFacade.save(o);
				versioningManager.serialize(o, SecurityContextHolder.getContext().getAuthentication().getName(),
						commitMessage, EventType.UPDATE);
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error("Error loading versionable class", e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VersionableVO> versionablesVO() {
		final long initTime = System.currentTimeMillis();
		final List<VersionableVO> versionables = new ArrayList<>();
		getVersionableClases().keySet().forEach(c -> {
			try {
				final Versionable<?> o = (Versionable<?>) Class.forName(c).newInstance();
				if (!(o instanceof OPResource)) {
					final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(o);

					final Method findVersionableViews = repo.getClass().getMethod("findVersionableViews");
					versionables.addAll((List<VersionableVO>) findVersionableViews.invoke(repo));
				}
			} catch (final Exception e) {
				log.error("Reflection error while trying to execute findVersionableViews for class {}", c, e);
			}
		});
		final long secondTime = System.currentTimeMillis();
		log.info("non op resources loaded in {} ms", secondTime - initTime);
		opResourceService.getAllResourcesVersionablesVOs().forEach(v -> versionables
				.add(new VersionableVO(v.getIdentification(), v.getId(), v.getClass().getSimpleName())));
		log.info("op resources loaded in {} ms", System.currentTimeMillis() - secondTime);
		return versionables;

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VersionableVO> versionablesVOForUser(String userId) {
		final List<VersionableVO> versionables = new ArrayList<>();
		final User user = userService.getUser(userId);
		// IF ADMIN GO OTHER METHOD
		if (user.getRole().getId().equals(Role.Type.ROLE_ADMINISTRATOR.name())) {
			return versionablesVO();
		}
		getVersionableClases().keySet().forEach(c -> {
			try {
				final Versionable<?> o = (Versionable<?>) Class.forName(c).newInstance();
				if (!(o instanceof OPResource)) {
					final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(o);

					final Method findVersionableViews = repo.getClass().getMethod("findVersionableViews");
					List<VersionableVO> vos = (List<VersionableVO>) findVersionableViews.invoke(repo);
					vos = vos.stream().filter(v -> v.getUserId() == null || userId.equals(v.getUserId()))
							.collect(Collectors.toList());
					versionables.addAll(vos);
				} else {
					// FILTER RESOURCES BY USER (NON ADMIN)
					versionables.addAll(
							opResourceService.getResourcesVersionablesForUserAndType(user, o.getClass()).stream()
									.map(v -> new VersionableVO(((OPResource) v).getIdentification(),
											(String) v.getId(), v.getClass().getSimpleName()))
									.collect(Collectors.toList()));
				}
			} catch (final Exception e) {
				log.error("Reflection error while trying to execute findVersionableViews for class {}", c, e);
			}
		});
		return versionables;

	}

	@Override
	public List<String> getVersionableSimpleClassNames() {
		return getVersionableClases().keySet().stream().map(c -> {
			try {
				final Versionable<?> o = (Versionable<?>) Class.forName(c).newInstance();
				return o.getClass().getSimpleName();
			} catch (final Exception e) {
				log.error("Error while instantiating versionable of type {}", c, e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
