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
package com.minsait.onesait.platform.serverless.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;
import com.minsait.onesait.platform.serverless.exception.GitException;
import com.minsait.onesait.platform.serverless.service.GitOperations;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GitOperationsImpl implements GitOperations {
	private static final String CONFIG_STR = "config";
	private static final String COULD_NOT_PUSH_TO_ORIGIN_MSSG = "Could not push to origin {}";
	private static final String COULD_NOT_PUSH_TO_ORIGIN = "Could not push to origin";
	public static final String CLONED_FOLDER = "cloned";

	@PostConstruct
	public void loadFileUtilsClass() {

	}

	@Override
	public void createDirectory(String directory) {
		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				final File file = new File(directory);
				file.mkdir();
			} else {
				final ProcessBuilder pb = new ProcessBuilder("mkdir", directory);
				pb.redirectErrorStream(true);
				executeAndReadOutput(pb.start());
			}

		} catch (final Exception e) {
			log.error("Could not create directory {}", e.getMessage());
			throw new GitException("Could not create directory", e);
		}

	}

	@Override
	public void configureGitAndInit(String user, String email, String directory) {
		try {

			ProcessBuilder pb = new ProcessBuilder("git", "init");
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			pb = new ProcessBuilder("git", CONFIG_STR, "user.name", user);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			if (email != null) {
				pb = new ProcessBuilder("git", CONFIG_STR, "user.email", email);
				pb.directory(new File(directory));
				executeAndReadOutput(pb.start());
			}

			pb = new ProcessBuilder("git", CONFIG_STR, "http.sslVerify", "false");
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());

		} catch (final Exception e) {
			log.error("Could not config git {}", e.getMessage());
			throw new GitException("Could not config git", e);
		}

	}

	@Override
	public void configureGit(String user, String email, String directory) {
		try {

			ProcessBuilder pb = new ProcessBuilder("git", CONFIG_STR, "user.name", user);
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
			if (email != null) {
				pb = new ProcessBuilder("git", CONFIG_STR, "user.email", email);
				pb.directory(new File(directory));
				executeAndReadOutput(pb.start());
			}

			pb = new ProcessBuilder("git", CONFIG_STR, "http.sslVerify", "false");
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());

		} catch (final Exception e) {
			log.error("Could not config git {}", e.getMessage());
			throw new GitException("Could not config git", e);
		}

	}

	@Override
	public void addOrigin(String url, String directory, boolean fetchAfterOrigin) {

		try {
			final ProcessBuilder pb;
			if (!fetchAfterOrigin) {
				pb = new ProcessBuilder("git", "remote", "add", "origin", url);
			} else {
				pb = new ProcessBuilder("git", "remote", "add", "-f", "origin", url);
			}
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("Could not add git origin {}", e.getMessage());
			throw new GitException("Could not add git origin", e);
		}

	}

	@Override
	public void addAll(String directory) {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", "add", ".");
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("Could not add file for commit {}", e.getMessage());
			throw new GitException("Could not add file for commit", e);
		}
	}

	@Override
	public void commit(String message, String directory) {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", message);
			pb.redirectErrorStream(true);
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error("Could not commit {}", e.getMessage());
			throw new GitException("Could not commit", e);
		}

	}

	@Override
	public void push(String sshUrl, String username, String password, String branch, String directory, boolean mirror)
			throws GitException {
		try {
			String url = null;
			if (sshUrl.toLowerCase().startsWith("http://")) {
				url = sshUrl.replace("http://", "http://" + username + ":" + password + "@");
			} else {
				url = sshUrl.replace("https://", "https://" + username + ":" + password + "@");
			}
			ProcessBuilder pb;
			if (mirror) {
				pb = new ProcessBuilder("git", "push", "--mirror", "-u", url);
			} else {
				pb = new ProcessBuilder("git", "push", "-u", url, branch);
			}
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	private String executeAndReadOutput(Process p) throws IOException, InterruptedException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final StringBuilder builder = new StringBuilder();
		String line = null;
		p.waitFor();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append(System.getProperty("line.separator"));
		}
		final String result = builder.toString();
		log.debug(result);
		return result;
	}

	private void copyResourcesToScaffolding(ClassPathResource cpr, File sourceFile, File targetFile,
			String path2Resource) {
		try {
			if (!targetFile.exists()) {
				final boolean newFile = targetFile.createNewFile();
				if (log.isDebugEnabled()) {
					log.debug("createNewFile:{}", newFile);
				}				
			}
			if (sourceFile.exists()) {
				FileUtils.copyFile(sourceFile, targetFile);
			} else {
				FileUtils.copyInputStreamToFile(cpr.getInputStream(), targetFile);
			}
		} catch (final Exception e) {
			log.error("Could not copy {} into {}", path2Resource, targetFile.getAbsolutePath());
			throw new RuntimeException(e);
		}
	}

	private void unzipFile(String directory, File targetFile) {
		try (final ZipFile zipFile = new ZipFile(targetFile)) {

			final Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				final ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				final String name = zipEntry.getName();

				// Do we need to create a directory ?
				final File file = new File(directory + File.separator + name);
				if (name.endsWith("/")) {
					file.mkdirs();
					continue;
				}

				final File parent = file.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}
				writeFile(zipFile, zipEntry, file);

			}

			zipFile.close(); // necessary to free stream and delete file
			Files.delete(targetFile.toPath());
			log.debug("Removed scaffolding");

		} catch (final Exception e) {
			log.error("Could not unzip scaffolding project: ", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unzipScaffolding(String directory, String path2Resource) {

		final ClassPathResource cpr = new ClassPathResource(path2Resource);
		final File sourceFile = new File(path2Resource);
		final File targetFile = new File(directory + File.separator + "zipfile.zip");

		copyResourcesToScaffolding(cpr, sourceFile, targetFile, path2Resource);

		unzipFile(directory, targetFile);

	}

	private void writeFile(ZipFile zipFile, ZipEntry zipEntry, File file) throws IOException {
		// Extract the file
		final InputStream is = zipFile.getInputStream(zipEntry);
		try (final FileOutputStream fos = new FileOutputStream(file)) {
			final byte[] bytes = new byte[1024];
			int length;
			while ((length = is.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			is.close();
		} catch (final Exception e) {
			log.error("Could not open FileOutputStream: ", e);
		}
	}

	@Override
	public void sparseCheckoutAddPath(String path, String directory) {
		try {
			final ProcessBuilder pb = new ProcessBuilder("echo", "'" + path + "' >> .git/info/sparse-checkout");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void sparseCheckoutConfig(String directory) {
		try {
			final ProcessBuilder pb = new ProcessBuilder("git", CONFIG_STR, "core.sparseCheckout", "true");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void checkout(String branch, String directory) {
		try {

			final ProcessBuilder pb = new ProcessBuilder("git", "checkout", branch);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void createBranch(String branch, String directory) {
		try {

			final ProcessBuilder pb = new ProcessBuilder("git", "branch", "-M", branch);
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void deleteDirectory(String directory) {
		try {
			if (SystemUtils.IS_OS_WINDOWS) {
				FileSystemUtils.deleteRecursively(new File(directory));

			} else {
				final ProcessBuilder pb = new ProcessBuilder("rm", "-r", directory);
				pb.redirectErrorStream(true);
				executeAndReadOutput(pb.start());
			}

		} catch (final Exception e) {
			log.error("Could not delete directory {}", e.getMessage());
			throw new GitException("Could not delete directory", e);
		}

	}

	@Override
	public void cloneRepository(String directory, GitlabConfiguration remoteConfig) {
		try {
			final String user = remoteConfig.getUser();
			final String token = remoteConfig.getPrivateToken();
			String url = remoteConfig.getProjectURL();
			if (!StringUtils.isEmpty(user) && !StringUtils.isEmpty(token)) {
				url = url.replaceAll("://", "://" + user + ":" + token + "@");
			}
			final ProcessBuilder pb = new ProcessBuilder("git", "clone", "--depth=1", "--branch",
					remoteConfig.getBranch(), url, ".");
			pb.redirectErrorStream(true);
			log.info(pb.command().toString());
			pb.directory(new File(directory));
			executeAndReadOutput(pb.start());
		} catch (final Exception e) {
			log.error(COULD_NOT_PUSH_TO_ORIGIN_MSSG, e.getMessage());
			throw new GitException(COULD_NOT_PUSH_TO_ORIGIN, e);
		}

	}

	@Override
	public void createReadme(String content, String path2Resource) {
		try {
			FileUtils.writeStringToFile(
					new File(path2Resource.endsWith("/") ? path2Resource + "README.MD" : path2Resource + "/README.MD"),
					content);
		} catch (final IOException e) {
			log.error("Could not create README file");
		}
	}

}
