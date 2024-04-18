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
package com.minsait.onesait.platform.business.services.bundles;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.business.services.versioning.VersioningBusinessService;
import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.commons.git.GitOperations;
import com.minsait.onesait.platform.commons.git.GitServiceManager;
import com.minsait.onesait.platform.commons.git.GitlabConfiguration;
import com.minsait.onesait.platform.config.components.BundleConfiguration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.config.versioning.BundleMetaInf;
import com.minsait.onesait.platform.config.versioning.RestoreReport;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BundleBusinessServiceImpl implements BundleBusinessService {

	@Autowired
	private GitServiceManager gitManager;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private GitOperations gitOperations;
	@Autowired
	private VersioningBusinessService versioningBusinessService;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final Map<String, Long> LAST_PULL_MAP = new HashMap<>();

	private static final Long PULL_TIMER = 300000L;

	private static final String DIR = "/tmp/bundles";
	private static final String TMP_DIR = "/tmp";

	@Override
	public List<BundleDTO> getBundles() {
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		final List<BundleDTO> bundles = new ArrayList<>();
		final Long start = System.currentTimeMillis();
		log.info("Get bundles init");
		if (config != null) {
			config.getGitConnections().forEach(gc -> {
				cloneOrPullRepo(DIR, gc);
				final File parent = new File(DIR + File.separator + getBundleRepoId(gc.getProjectURL()));
				final File[] directories = parent.listFiles();
				for (final File f : directories) {
					if (f.isDirectory() && !f.isHidden()) {
						final String readme = new String(
								getFileBytes(f.getAbsolutePath() + File.separator + "/README.md"));
						final String image = Base64.getEncoder()
								.encodeToString(getFileBytes(f.getAbsolutePath() + File.separator + "/bundle.png"));
						final String metainf = new String(
								getFileBytes(f.getAbsolutePath() + File.separator + "/bundle.metainf"));
						final BundleDTO b = new BundleDTO();
						b.setGitBranch(gc.getBranch());
						b.setProjectURL(gc.getProjectURL());
						if (readme != null) {
							b.setReadme(readme);
						}
						b.setRootFolder(f.getName());
						b.setImageBase64(image);
						final Map<String, Integer> resources = new HashMap<>();
						List.of(f.listFiles()).stream().filter(
								fx -> fx.isDirectory() && !fx.isHidden() && !fx.getName().equals("bundle_extras"))
								.forEach(fc -> {
									resources.put(fc.getName(), fc.listFiles().length);
								});
						b.setResourcesCount(resources);
						try {
							if (StringUtils.hasText(metainf)) {
								final BundleMetaInf bMetaInf = MAPPER.readValue(metainf, BundleMetaInf.class);
								b.setTitle(bMetaInf.getTitle());
								b.setCreatedAt(bMetaInf.getCreatedAt());
								b.setVersion(bMetaInf.getVersion());
								b.setShortdescription(bMetaInf.getShortdescription());
							}
						} catch (final Exception e) {
							log.error("Could not read Metainf", e);
						}
						b.setId(getBundleId(gc.getProjectURL(), gc.getBranch(), f.getName()));
						bundles.add(b);
					}
				}
			});
		} else {
			log.warn("No configuration found for bundles");
		}

		log.info("Get bundles end, time : {}", System.currentTimeMillis() - start);
		return bundles;
	}

	@Override
	public BundleDTO getBundle(String bundleId) {
		final BundleIdHolder bundleIdHolder = getBundleIdHolder(bundleId);
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		BundleDTO bundle = null;
		if (config != null) {
			final GitlabConfiguration conf = config.getGitConnections().stream()
					.filter(gc -> gc.getProjectURL().equals(bundleIdHolder.getProjectURL())
							&& gc.getBranch().equals(bundleIdHolder.getGitBranch()))
					.findFirst().orElse(null);

			if (conf != null) {
				cloneOrPullRepo(DIR, conf);
				final File f = new File(DIR + File.separator + getBundleRepoId(bundleIdHolder.getProjectURL())
						+ File.separator + bundleIdHolder.getRootFolder());
				final String readme = new String(getFileBytes(f.getAbsolutePath() + File.separator + "/README.md"));
				final String image = Base64.getEncoder()
						.encodeToString(getFileBytes(f.getAbsolutePath() + File.separator + "/bundle.png"));
				final String metainf = new String(
						getFileBytes(f.getAbsolutePath() + File.separator + "/bundle.metainf"));

				final File extraFilesDir = new File(f.getAbsolutePath() + File.separator + "/bundle_extras");

				final File[] extraFilesDirContent = extraFilesDir.listFiles();

				Set<String> extraFiles = new HashSet<String>();

				if (extraFilesDirContent != null) {
					extraFiles = Stream.of(extraFilesDirContent).filter(file -> !file.isDirectory()).map(File::getName)
							.collect(Collectors.toSet());
				}

				bundle = new BundleDTO();
				bundle.setGitBranch(bundleIdHolder.getGitBranch());
				bundle.setProjectURL(bundleIdHolder.getProjectURL());
				if (readme != null) {
					bundle.setReadme(readme);
				}
				bundle.setRootFolder(bundleIdHolder.getRootFolder());
				bundle.setImageBase64(image);
				bundle.setExtraFiles(extraFiles);

				processMetaInf(bundle, metainf);

				final Map<String, List<String>> resourcesDetail = new HashMap<>();
				final Map<String, Integer> resources = new HashMap<>();

				List.of(f.listFiles()).stream()
						.filter(fx -> fx.isDirectory() && !fx.isHidden() && !fx.getName().equals("bundle_extras"))
						.forEach(fc -> {
							resources.put(fc.getName(), fc.listFiles().length);
							resourcesDetail.put(fc.getName(),
									Stream.of(fc.listFiles()).map(File::getName).collect(Collectors.toList()));
						});
				bundle.setResourcesCount(resources);
				bundle.setPlatformResources(resourcesDetail);
				bundle.setId(bundleIdHolder.getId());
			} else {
				log.warn("No bundle found using configuration");
			}
		} else {
			log.warn("No configurations found for bundles");
		}
		return bundle;
	}

	@Override
	public File downloadBundle(String bundleId) throws GenericOPException {
		final BundleIdHolder bundleIdHolder = getBundleIdHolder(bundleId);
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		if (config != null) {
			final GitlabConfiguration conf = config.getGitConnections().stream()
					.filter(gc -> gc.getProjectURL().equals(bundleIdHolder.getProjectURL())
							&& gc.getBranch().equals(bundleIdHolder.getGitBranch()))
					.findFirst().orElse(null);

			if (conf != null) {
				try {
					cloneOrPullRepo(DIR, conf);
					final File output = new File(TMP_DIR + File.separator + bundleIdHolder.getRootFolder() + ".zip");
					if (output.exists()) {
						output.delete();
					}
					VersioningUtils
							.zipFolder(new File(DIR + File.separator + getBundleRepoId(bundleIdHolder.getProjectURL())
									+ File.separator + bundleIdHolder.getRootFolder()), output);
					return output;
				} catch (final Exception e) {
					log.error("Error ziping files", e);
					throw new GenericOPException("Error ziping files");
				}
			} else {
				log.warn("No bundle found using configuration");
				throw new GenericOPException("No bundle found using configuration");
			}
		} else {
			log.warn("No configurations found for bundles");
			throw new GenericOPException("No configurations found for bundles");
		}

	}

	@Override
	public Integer countBundles() {
		return getBundles().size();
	}

	@Override
	public File downloadFile(String bundleId, String path) throws GenericOPException {
		final BundleIdHolder bundleIdHolder = getBundleIdHolder(bundleId);
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		if (config != null) {
			final GitlabConfiguration conf = config.getGitConnections().stream()
					.filter(gc -> gc.getProjectURL().equals(bundleIdHolder.getProjectURL())
							&& gc.getBranch().equals(bundleIdHolder.getGitBranch()))
					.findFirst().orElse(null);

			if (conf != null) {
				try {
					cloneOrPullRepo(DIR, conf);
					return new File(DIR + File.separator + getBundleRepoId(bundleIdHolder.getProjectURL())
							+ File.separator + bundleIdHolder.getRootFolder() + File.separator + path);
				} catch (final Exception e) {
					log.error("Error ziping files", e);
					throw new GenericOPException("Error getting file " + path);
				}
			} else {
				log.warn("No bundle found using configuration");
				throw new GenericOPException("No bundle found using configuration");
			}
		} else {
			log.warn("No configurations found for bundles");
			throw new GenericOPException("No configurations found for bundles");
		}
	}

	@Override
	public void installBundle(String bundleId, RestoreReport report) throws GenericOPException {
		final BundleIdHolder bundleIdHolder = getBundleIdHolder(bundleId);
		final BundleConfiguration config = configurationService.getBundleConfiguration();
		if (config != null) {
			final GitlabConfiguration conf = config.getGitConnections().stream()
					.filter(gc -> gc.getProjectURL().equals(bundleIdHolder.getProjectURL())
							&& gc.getBranch().equals(bundleIdHolder.getGitBranch()))
					.findFirst().orElse(null);

			if (conf != null) {
				try {
					cloneOrPullRepo(DIR, conf);
					versioningBusinessService.restoreBundle(report, conf, bundleIdHolder.getRootFolder());
				} catch (final Exception e) {
					log.error("Error ziping files", e);
					throw new GenericOPException("Error getting bundle " + bundleIdHolder.getProjectURL());
				}
			} else {
				log.warn("No bundle found using configuration");
				throw new GenericOPException("No bundle found using configuration");
			}
		} else {
			log.warn("No configurations found for bundles");
			throw new GenericOPException("No configurations found for bundles");
		}

	}

	private void processMetaInf(BundleDTO bundle, String metainf) {
		try {
			if (metainf != null) {
				final BundleMetaInf bMetaInf = MAPPER.readValue(metainf, BundleMetaInf.class);
				bundle.setTitle(bMetaInf.getTitle());
				bundle.setShortdescription(bMetaInf.getShortdescription());
				bundle.setVersion(bMetaInf.getVersion());
				bundle.setCreatedAt(bMetaInf.getCreatedAt());
			}
		} catch (final Exception e) {
			log.error("Could not read Metainf", e);
		}
	}

	private void cloneOrPullRepo(String dir, GitlabConfiguration conf) {
		if (!new File(dir).exists()) {
			new File(dir).mkdirs();
		}
		final String repoId = getBundleRepoId(conf.getProjectURL());
		if (!new File(dir + File.separator + repoId).exists()) {
			gitOperations.cloneRepository(dir, conf.getProjectURL(), conf.getUser(), conf.getPrivateToken(),
					conf.getBranch(), true, repoId);
		} else {
			boolean shouldPull = false;
			if (LAST_PULL_MAP.containsKey(repoId)
					&& (LAST_PULL_MAP.get(repoId) + PULL_TIMER < System.currentTimeMillis())) {
				shouldPull = true;
			} else if (!LAST_PULL_MAP.containsKey(repoId)) {
				shouldPull = true;
			}
			if (shouldPull) {
				gitOperations.pullWithNoPrompt(dir + File.separator + repoId);
				LAST_PULL_MAP.put(repoId, System.currentTimeMillis());
			}
		}

	}

	private String getBundleRepoId(String projectURL) {
		try {
			return convertToHex(MessageDigest.getInstance("MD5").digest(projectURL.getBytes()));
		} catch (final NoSuchAlgorithmException e) {
			log.error("Could not get hash from projectURL, returning base64", e);
			return Base64.getEncoder().encodeToString(projectURL.getBytes());
		}

	}

	private String convertToHex(final byte[] messageDigest) {
		final BigInteger bigint = new BigInteger(1, messageDigest);
		String hexText = bigint.toString(16);
		while (hexText.length() < 32) {
			hexText = "0".concat(hexText);
		}
		return hexText;
	}

	private byte[] getFileBytes(String path) {
		if (new File(path).exists()) {
			try {
				return Files.readAllBytes(Paths.get(path));
			} catch (final Exception e) {
				log.warn("Could not load file {}", path);
			}
		}

		return new byte[0];

	}

	private String getBundleId(String projectURL, String gitBranch, String rootDir) {
		return Base64.getEncoder().encodeToString((projectURL + "+++" + gitBranch + "+++" + rootDir).getBytes());
	}

	private BundleIdHolder getBundleIdHolder(String bundleId) {
		final String[] args = new String(Base64.getDecoder().decode(bundleId.getBytes()), StandardCharsets.UTF_8)
				.split("\\+\\+\\+");
		return BundleIdHolder.builder().id(bundleId).projectURL(args[0]).gitBranch(args[1]).rootFolder(args[2]).build();

	}
}
