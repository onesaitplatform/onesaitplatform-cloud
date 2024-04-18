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
package com.minsait.onesait.platform.config.versioning;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.git.GitSyncException;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.Report;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.repository.AppUserRepository;
import com.minsait.onesait.platform.config.repository.LineageRelationsRepository;
import com.minsait.onesait.platform.config.repository.OPResourceRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.repository.UserTokenRepository;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;
import com.minsait.onesait.platform.multitenant.config.model.OAuthAccessToken;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthAccessTokenRepository;
import com.minsait.onesait.platform.multitenant.config.repository.OAuthRefreshTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VersioningTxBusinessServiceImpl implements VersioningTxBusinessService {

	@Autowired
	private VersioningManager versioningManager;
	@Autowired
	private GitOperations gitOperations;
	@Autowired
	private VersioningRepositoryFacade versioningRepositoryFacade;
	@Autowired
	private VersioningIOService versioningIOService;
	@Autowired
	private UserRepository userRepository;

	public static final String SCAN_PACKAGE = "com.minsait.onesait.platform.config.model";
	private static final String TAG_BRANCH_PREFIX = "snapshot/";

	@Override
	@Transactional(readOnly = true)
	public void generateSnapShot(String tagName, @NotNull RestoreReport report, Map<String, String> versionableClasses,
			GitlabConfiguration configuration) {
		try {
			final long start = System.currentTimeMillis();
			List<Versionable<?>> dbVersionables = getCurrentDbVersionables(versionableClasses);
			if (!report.getExcludeResources().isEmpty()) {
				log.info("Running exclusions, current size: {}", dbVersionables.size());
				// FIRST EXCLUDE BY USER
				dbVersionables.forEach(v -> v.runExclusionsByUser(report.getExcludeResources(),
						report.getExcludeResources().get("User")));
				dbVersionables = dbVersionables.stream()
						.map(v -> v.runExclusionsByUser(report.getExcludeResources(),
								report.getExcludeResources().get("User")))
						.filter(Objects::nonNull)
						.map(v -> v.runExclusions(report.getExcludeResources(),
								report.getExcludeResources().get("User")))
						.filter(Objects::nonNull).collect(Collectors.toList());
				log.info("Finished exclusions, current size: {}", dbVersionables.size());
				// TO-DO if tagname is null
				gitOperations.checkout(TAG_BRANCH_PREFIX + tagName, VersioningIOService.DIR, true);
			}
			final long temp1 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Retrieving db versionables took: {} ms", temp1 - start);
			}
			report.setVersionablesInRepository(dbVersionables.size());
			FileSystemUtils.deleteRecursively(new File(VersioningIOService.DIR + Tenant2SchemaMapper
					.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())));
			dbVersionables.forEach(versioningManager::serialize);
			final long temp2 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Serializing db versionables took: {} ms", temp2 - temp1);
			}			
			gitOperations.addAll(VersioningIOService.DIR);
			gitOperations.commit("SNAPSHOT " + new DateTime().toString(), VersioningIOService.DIR);
			try {
				gitOperations.push(configuration.getProjectURL(), configuration.getUser(),
						configuration.getPrivateToken(),
						report.getExcludeResources().isEmpty() ? configuration.getBranch()
								: TAG_BRANCH_PREFIX + tagName,
						VersioningIOService.DIR, false);
			} catch (final GitSyncException e) {
				versioningManager.syncOriginAndDB();
				gitOperations.push(configuration.getProjectURL(), configuration.getUser(),
						configuration.getPrivateToken(),
						report.getExcludeResources().isEmpty() ? configuration.getBranch()
								: TAG_BRANCH_PREFIX + tagName,
						VersioningIOService.DIR, false);
			}
			if (StringUtils.hasText(tagName)) {
				gitOperations.createTag(VersioningIOService.DIR, tagName);
				gitOperations.pushTags(VersioningIOService.DIR);
				gitOperations.checkout(configuration.getBranch(), VersioningIOService.DIR);
			}
			versioningManager.updateLastCommitProcessed();
		} catch (final Exception e) {
			log.error("Error generating snapshot", e);
			report.getErrors().add(e.getMessage());
		}

	}

	@Override
	@Transactional(readOnly = true)
	public void createBundle(@NotNull RestoreReport report, Map<String, String> versionableClasses,
			GitlabConfiguration configuration, String directory, BundleGenerateDTO bundle) {
		try {
			final long start = System.currentTimeMillis();
			List<Versionable<?>> dbVersionables = getCurrentDbVersionables(versionableClasses);
			// report.getIncludeResources() never empty

			log.info("Running inclusions, current size: {}", dbVersionables.size());
			dbVersionables = dbVersionables.stream().map(v -> v.runInclusions(report.getIncludeResources()))
					.filter(Objects::nonNull).collect(Collectors.toList());
			log.info("Finished inclusions, current size: {}", dbVersionables.size());
			gitOperations.checkout(configuration.getBranch(), directory, true);

			final long temp1 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Retrieving db versionables took: {} ms", temp1 - start);
			}			
			report.setVersionablesInRepository(dbVersionables.size());

			if (Files.exists(Paths.get(directory + "/" + bundle.getFolderName()))) {
				throw new GitSyncException("Folder " + bundle.getFolderName() + " already exists, can't overwrite");
			}
			Files.createDirectories(Paths.get(directory + "/" + bundle.getFolderName()));
			dbVersionables.forEach(v -> versioningManager.serialize(v, directory + "/" + bundle.getFolderName()));

			final long temp2 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Serializing db versionables took: {} ms", temp2 - temp1);
			}			
			final BundleMetaInf metaInf = BundleMetaInf.builder().version(bundle.getVersion())
					.shortdescription(bundle.getShortDesc()).title(bundle.getTitle())
					.createdAt(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).build();
			VersioningUtils.extraResourcesToBundle(directory + "/" + bundle.getFolderName(), bundle.getReadme(),
					bundle.getExtraResources(), bundle.getImage(), metaInf);
			gitOperations.addAll(directory);
			gitOperations.commit("Export of asset " + bundle.getFolderName(), directory);
			try {
				gitOperations.push(configuration.getProjectURL(), configuration.getUser(),
						configuration.getPrivateToken(), configuration.getBranch(), directory, false);
			} catch (final GitSyncException e) {
				// NO-OP
				log.error("Error while pushing to Git, repeat the process: {}", e.getMessage(), e);
				report.getErrors().add("Error while pushing to Git, repeat the process: " + e.getMessage());
			}
		} catch (final Exception e) {
			log.error("Error generating export of asset", e);
			report.getErrors().add(e.getMessage());
		}

	}

	@Override
	@Transactional(readOnly = true)
	public void createZipBundle(@NotNull RestoreReport report, Map<String, String> versionableClasses, String directory,
			BundleGenerateDTO bundle) {
		try {
			final long start = System.currentTimeMillis();
			List<Versionable<?>> dbVersionables = getCurrentDbVersionables(versionableClasses);
			// report.getIncludeResources() never empty

			log.info("Running inclusions, current size: {}", dbVersionables.size());
			dbVersionables = dbVersionables.stream().map(v -> v.runInclusions(report.getIncludeResources()))
					.filter(Objects::nonNull).collect(Collectors.toList());
			log.info("Finished inclusions, current size: {}", dbVersionables.size());

			final long temp1 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Retrieving db versionables took: {} ms", temp1 - start);
			}			
			report.setVersionablesInRepository(dbVersionables.size());

			if (Files.exists(Paths.get(directory + "/" + bundle.getFolderName()))) {
				throw new GitSyncException("Folder " + bundle.getFolderName() + " already exists, can't overwrite");
			}
			Files.createDirectories(Paths.get(directory + "/" + bundle.getFolderName()));
			dbVersionables.forEach(v -> versioningManager.serialize(v, directory + "/" + bundle.getFolderName()));

			final long temp2 = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("Serializing db versionables took: {} ms", temp2 - temp1);
			}			

			final BundleMetaInf metaInf = BundleMetaInf.builder().version(bundle.getVersion())
					.shortdescription(bundle.getShortDesc()).title(bundle.getTitle())
					.createdAt(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).build();

			VersioningUtils.extraResourcesToBundle(directory + "/" + bundle.getFolderName(), bundle.getReadme(),
					bundle.getExtraResources(), bundle.getImage(), metaInf);

		} catch (final Exception e) {
			log.error("Error generating export of asset", e);
			report.getErrors().add(e.getMessage());
		}

	}

	@Override
	@Transactional
	public void restorePlatform(RestorePlatformDTO restoreDTO, RestoreReport report,
			Map<String, String> versionableClasses, GitlabConfiguration gitConfig) {
		try {
			FileSystemUtils.deleteRecursively(new File(VersioningIOService.DIR));
			gitOperations.createDirectory(VersioningIOService.DIR);
			gitOperations.cloneRepository(VersioningIOService.DIR, restoreDTO.getSourceProject(),
					restoreDTO.getSourceUser(), restoreDTO.getSourceToken(), restoreDTO.getSourceBranchTag(), true);
			gitOperations.configureGitAndInit(gitConfig.getUser(), gitConfig.getEmail(), VersioningIOService.DIR);

			// PROCESO SAVE AND DELETE
			syncFilesystemToDB(report, versionableClasses);
		} catch (final Exception e) {
			log.error("Error generating snapshot", e);
			report.getErrors().add(e.getMessage());
		}

	}

	@Override
	@Transactional
	public void restoreBundle(RestoreReport report, Map<String, String> versionableClasses, String directory,
			String folderName, String userId) {
		try {
			syncDirectoryToDB(report, versionableClasses, directory + "/" + folderName, userId);
		} catch (final Exception e) {
			log.error("Error generating snapshot", e);
			report.getErrors().add(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void syncOriginAndDB() {
		final String sha1 = gitOperations.getCurrentSHA(VersioningIOService.DIR);
		syncOriginAndDB(sha1);
	}

	@Override
	@Transactional
	public void syncOriginAndDB(String originSHA) {
		final String sha1 = originSHA;
		gitOperations.pullWithNoPrompt(VersioningIOService.DIR);
		final String sha2 = gitOperations.getCurrentSHA(VersioningIOService.DIR);
		if (!sha1.equals(sha2)) {
			final List<String> changedFiles = gitOperations.getFilesChanged(VersioningIOService.DIR, sha1, sha2)
					.stream().filter(s -> !s.endsWith(".zip")).collect(Collectors.toList());
			final List<String> changedFilesToDelete = changedFiles.stream()
					.filter(f -> isVersionableToDelete(VersioningIOService.DIR + f)).collect(Collectors.toList());
			final List<String> changedFilesToRestore = changedFiles.stream()
					.filter(f -> !isVersionableToDelete(VersioningIOService.DIR + f)).collect(Collectors.toList());
			prioritizeClassList(changedFilesToDelete, true, true);
			changedFilesToDelete.forEach(f -> deleteInDB(f, sha1));
			prioritizeClassList(changedFilesToRestore, false, true);
			changedFilesToRestore.forEach(this::restoreFileToDB);
		}
	}

	/**
	 *
	 * Metodo para sistema de versionado global. Borra todos los recursos que no
	 * estan en el directorio e importa los que si estan.
	 *
	 * @param report
	 * @param versionableClasses
	 */

	private void syncFilesystemToDB(RestoreReport report, Map<String, String> versionableClasses) {
		final long start = System.currentTimeMillis();
		final List<Versionable<?>> versionables = versioningIOService.readAllVersionables();
		final long temp1 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Deserializing versionables took: {} ms", temp1 - start);
		}		
		report.setVersionablesInRepository(versionables.size());
		VersioningCommitContextHolder.setProcessPostAllEvents(false);
		processVersionablesToDelete(versionables, report, versionableClasses);
		final long temp2 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Deleting versionables took: {} ms", temp2 - temp1);
		}		
		prioritizeList(versionables, false);
		processVersionablesToRestore(versionables, report);
		final long temp3 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Restoring versionables took: {} ms", temp3 - temp2);
		}		
	}

	/**
	 * Metodo para restaurar/importar una serie de versionables a partir de un
	 * directorio especifico, para uso parcial, no se borra nada solo se importa
	 *
	 * @param report
	 * @param versionableClasses
	 * @param directory
	 */

	private void syncDirectoryToDB(RestoreReport report, Map<String, String> versionableClasses, String directory,
			String userId) {
		final long start = System.currentTimeMillis();
		final List<Versionable<?>> versionables = versioningIOService.readAllVersionables(directory);
		final long temp1 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Deserializing versionables took: {} ms", temp1 - start);
		}		
		report.setVersionablesInRepository(versionables.size());
		if (userId != null) {
			versionables.forEach(v -> v.setOwnerUserId(userId));
		}
		VersioningCommitContextHolder.setProcessPostAllEvents(true);
		prioritizeList(versionables, false);
		processVersionablesToRestore(versionables, report);
		final long temp3 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("Restoring versionables took: {} ms", temp3 - temp1);
		}		
	}

	private void processVersionablesToRestore(List<Versionable<?>> versionables, RestoreReport report) {
		versionables.forEach(v -> {
			try {
				if (v instanceof Configuration && Configuration.Type.VERSIONING.equals(((Configuration) v).getType())) {
					// DO NOT OVERWRITE VERSIONING CONFIG
					return;
				}
				// TO-DO Maybe insert bulk by filtering first by Versionable class
				versioningRepositoryFacade.save(v);
				if (v instanceof Project) {
					postProcessVersionable(versionables, v);
				}
			} catch (final Exception e) {
				log.error("Could not save versionable with id {}", v.getId(), e);
				if (report != null) {
					report.getErrors().add(e.getMessage());
				}
			}

		});
	}

	private void processVersionablesToDelete(List<Versionable<?>> versionables, RestoreReport report,
			Map<String, String> versionableClasses) {
		final List<String> listedClasses = new ArrayList<>(versionableClasses.keySet());
		prioritizeClassList(listedClasses, true, false);
		listedClasses.stream().forEach(e -> {
			final List<Versionable<?>> filteredVersionables = versionables.stream()
					.filter(v -> v.getClass().getName().equals(e)).collect(Collectors.toList());
			prioritizeList(filteredVersionables, true);
			final List<String> ids = filteredVersionables.stream().map(Versionable::getId).map(i -> (String) i)
					.collect(Collectors.toList());
			if (Configuration.class.getName().equals(e)) {
				versioningManager.getGitConfigId().ifPresent(ids::add);
			}
			if (Project.class.getName().equals(e) || Report.class.getName().equals(e)) {
				// Must delete all entities that have useraccess
				ids.clear();
			}
			if (ids.isEmpty()) {
				// arbitrary id not matching any to avoid sql exception
				ids.add("1s3v");
			}
			try {
				final Versionable<?> o = (Versionable<?>) Class.forName(e).newInstance();
				if (o instanceof User) {
					userRepository.findAll().stream().filter(u -> !ids.contains(u.getUserId()))
							.forEach(u -> report.getUsersToBeRemoved().add(u.getUserId()));
				} else {
					final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(o);
					final Method deleteByIdNotIn = repo.getClass().getMethod("deleteByIdNotIn", Collection.class);
					deleteByIdNotIn.invoke(repo, ids);
					// FLUSH in order to delete before insert when Transaction ends
					repo.flush();
				}
			} catch (final Exception e1) {
				log.error("Error while trying to delete resources of type {}", e, e1);
				if (report != null) {
					report.getErrors().add(e1.getMessage());
				}
			}
		});
	}

	private void prioritizeClassList(List<String> versionableClasses, boolean reverse, boolean isFile) {
		if (reverse) {
			Collections.sort(versionableClasses,
					Collections.reverseOrder(new PriorityVersionableClassComparator(isFile)));
		} else {
			Collections.sort(versionableClasses, new PriorityVersionableClassComparator(isFile));
		}
	}

	private void postProcessVersionable(List<Versionable<?>> versionables, Versionable<?> v) {
		final Project p = (Project) v;
		// We need to add project to each user manually because of relationship
		if (!CollectionUtils.isEmpty(p.getUsers())) {
			p.getUsers().forEach(u -> {
				final User user = userRepository.findByUserNoCache(u.getUserId());
				user.getProjects().add(p);
				userRepository.save(user);
			});
		}
		// Need to set bidirectional relationship
		// also app must be on the same Versionable Collection as its referenced in the
		// restoration process
		if (p.getApp() != null) {
			final Optional<Versionable<?>> appOpt = versionables.stream()
					.filter(va -> va instanceof App && p.getApp().getId().equals(((App) va).getId())).findFirst();
			if (appOpt.isPresent()) {
				final App app = (App) appOpt.get();
				app.setProject(p);
				versioningRepositoryFacade.save(appOpt.get());
			}
		}
	}

	private void prioritizeList(List<Versionable<?>> versionables, boolean reverse) {
		if (reverse) {
			Collections.sort(versionables, Collections.reverseOrder(new PriorityVersionableComparator()));
		} else {
			Collections.sort(versionables, new PriorityVersionableComparator());
		}
	}

	private List<Versionable<?>> getCurrentDbVersionables(Map<String, String> listedClasses) {
		return listedClasses.entrySet().stream().map(e -> {
			Versionable<?> o;
			try {
				o = (Versionable<?>) Class.forName(e.getKey()).newInstance();
				return versioningRepositoryFacade.getJpaRepository(o).findAll();
			} catch (final Exception e1) {
				log.error("Could not instantiate class of type {}", e.getKey(), e1);
			}
			return null;
		}).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
	}

	private void restoreFileToDB(String fileName) {
		final String className = getClassNameFromPath(fileName);
		try {
			log.info("Detected changes in versionable file {}, applying changes to database...", fileName);
			final Versionable<?> o = (Versionable<?>) Class.forName(SCAN_PACKAGE + "." + className).newInstance();
			versioningIOService.restoreFromFileSystem(o, VersioningIOServiceImpl.DIR + fileName);
		} catch (final Exception e) {
			log.error("Could not restore file {}", fileName, e);
		}
	}

	private void deleteInDB(String fileName, String originalSHA) {
		final String versionableContent = gitOperations.showFileFromCommit(VersioningIOServiceImpl.DIR, originalSHA,
				fileName);
		final String className = getClassNameFromPath(fileName);
		try {
			log.info("Detected deleted versionable file {}, removing from database...", fileName);

			Versionable<?> o = (Versionable<?>) Class.forName(SCAN_PACKAGE + "." + className).newInstance();
			o = (Versionable<?>) o.deserialize(versionableContent);
			VersioningCommitContextHolder.setProcessPostAllEvents(false);
			if (User.class.getSimpleName().equals(className)) {
				VersioningCommitContextHolder.setProcessPostAllEvents(false);
				deleteUser((String) o.getId());
				userRepository.flush();
			} else {
				final JpaRepository<Versionable<?>, Object> repo = versioningRepositoryFacade.getJpaRepository(o);
				repo.deleteById(o.getId());
				repo.flush();
			}
			VersioningCommitContextHolder.setProcessPostAllEvents(true);

		} catch (final Exception e) {
			log.error("Failed to delete versionable {}", fileName, e);
		}
	}

	private String getClassNameFromPath(String filePath) {
		final String pattern = "^"
				+ Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
				+ "\\/([a-zA-Z]+)\\/.*$";
		final Pattern p = Pattern.compile(pattern);
		final Matcher m = p.matcher(filePath);
		String result = null;
		if (m.matches()) {
			result = m.group(1);
		}
		return result;
	}

	private boolean isVersionableToDelete(String absolutePath) {
		return !new File(absolutePath).exists();
	}

	// FROM ENTITY DELETION SERVICE TO DELETE USERS
	@Autowired
	private UserTokenRepository userTokenRepository;
	@Autowired
	private OAuthAccessTokenRepository oauthAccessTokenRepository;
	@Autowired
	private OAuthRefreshTokenRepository oauthRefreshTokenRepository;
	@Autowired
	private OPResourceRepository resourceRepository;
	@Autowired
	private AppUserRepository appUserRepository;
	@Autowired
	private LineageRelationsRepository lineageRelationsRepository;

	@Override
	public void deleteUser(String userId) {
		try {
			lineageRelationsRepository.deleteByUser(userId);
			appUserRepository.deleteByUserId(userId);
			invalidateUserTokens(userId);
			resourceRepository.deleteByUser(userId);
			userRepository.deleteByUserId(userId);
		} catch (final Exception e) {
			throw new RuntimeException("Could not delete user, there are resources owned by " + userId);
		}

	}

	private void invalidateUserTokens(String userId) {
		if (log.isDebugEnabled()) {
			log.debug("Deleteing user token x-op-apikey for user {}", userId);
		}		
		userTokenRepository.deleteByUser(userId);
		if (log.isDebugEnabled()) {
			log.debug("Revoking Oauth2 access tokens for user{}", userId);
		}		
		final Collection<OAuthAccessToken> tokens = oauthAccessTokenRepository.findByUserName(userId);
		tokens.forEach(t -> {
			oauthRefreshTokenRepository.deleteById(t.getRefreshToken());
			oauthAccessTokenRepository.deleteById(t.getTokenId());
		});

	}

}
