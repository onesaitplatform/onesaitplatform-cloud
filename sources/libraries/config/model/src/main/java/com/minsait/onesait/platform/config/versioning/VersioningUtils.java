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
package com.minsait.onesait.platform.config.versioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersioningUtils {

	public static final String README_MD = "README.md";
	public static final String BUNDLE_IMG = "bundle.png";
	public static final String BUNDLE_METAINF = "bundle.metainf";
	public static final String BUNDLE_EXTRAS_DIR = "bundle_extras";

	public static <T extends Object> Yaml versioningYaml(Class<T> clazz) {
		final DumperOptions opts = new DumperOptions();
		opts.setDefaultFlowStyle(FlowStyle.BLOCK);
		opts.setIndent(4);
		opts.setPrettyFlow(true);
		opts.setSplitLines(true);
		opts.setAllowUnicode(true);
		final HTMLRepresenter rep = new HTMLRepresenter();
		rep.addClassTag(clazz, Tag.MAP);
		return new Yaml(new HTMLConstructor(), rep, opts);
	}

	public static void zipFolder(File srcFolder, File destZipFile) throws Exception {
		try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
				ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

			addFolderToZip(srcFolder, srcFolder, zip);
		}
	}

	private static void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

		if (srcFile.isDirectory()) {
			addFolderToZip(rootPath, srcFile, zip);
		} else {
			final byte[] buf = new byte[1024];
			int len;
			try (FileInputStream in = new FileInputStream(srcFile)) {
				String name = srcFile.getPath();
				name = name.replace(rootPath.getPath(), "");
				zip.putNextEntry(new ZipEntry(name));
				while ((len = in.read(buf)) > 0) {
					zip.write(buf, 0, len);
				}
			} catch (final Exception e) {
				log.error("Could not add file {}, message:{}", srcFile.toString(), e.getMessage());
			}
		}
	}

	private static void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
		for (final File fileName : srcFolder.listFiles()) {
			addFileToZip(rootPath, fileName, zip);
		}
	}

	public static void unzipFolder(File sourceFile, File targetFile) throws IOException {
		final Path source = sourceFile.toPath();
		final Path target = targetFile.toPath();
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				boolean isDirectory = false;
				if (zipEntry.getName().endsWith(File.separator)) {
					isDirectory = true;
				}
				final Path newPath = zipSlipProtect(zipEntry, target);
				if (isDirectory) {
					Files.createDirectories(newPath);
				} else {
					if (newPath.getParent() != null) {
						if (Files.notExists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
						}
					}
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
	}

	public static void createReadme(String readmeContent, String directory) {
		if (readmeContent == null) {
			readmeContent = "";
		}
		try {
			FileUtils.writeStringToFile(new File(directory + File.separator + README_MD), readmeContent,
					StandardCharsets.UTF_8);
		} catch (final IOException e) {
			log.error("Could not write readme.md", e);
		}
	}

	public static void extraResourcesToBundle(String directory, String readmeContent,
			List<MultipartFile> extraResources, MultipartFile image, BundleMetaInf metaInf) {
		createMetainf(directory, metaInf);
		createReadme(readmeContent, directory);
		if (extraResources != null && !extraResources.isEmpty()) {
			new File(directory + File.separator + BUNDLE_EXTRAS_DIR).mkdirs();
			extraResources.stream().filter(f -> f.getSize() > 0).forEach(mf -> {
				final String fileDir = directory + File.separator + BUNDLE_EXTRAS_DIR + File.separator
						+ mf.getOriginalFilename();
				try {
					FileUtils.writeByteArrayToFile(new File(fileDir), mf.getBytes(), true);
				} catch (final IOException e) {
					log.error("Could not write file {}", fileDir, e);
				}
			});
		}
		if (image != null && image.getSize() > 0) {
			final String fileDir = directory + File.separator + BUNDLE_IMG;
			try {
				FileUtils.writeByteArrayToFile(new File(fileDir), image.getBytes(), true);
			} catch (final IOException e) {
				log.error("Could not write file {}", fileDir, e);
			}
		}
	}

	private static void createMetainf(String directory, BundleMetaInf metaInf) {
		try {
			FileUtils.writeStringToFile(new File(directory + File.separator + BUNDLE_METAINF),
					new ObjectMapper().writeValueAsString(metaInf), StandardCharsets.UTF_8);
		} catch (final IOException e) {
			log.error("Could not write readme.md", e);
		}

	}

	public static void unzipFolder(InputStream sourceFile, File targetFile) throws IOException {
		final Path target = targetFile.toPath();
		try (ZipInputStream zis = new ZipInputStream(sourceFile)) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				boolean isDirectory = false;
				if (zipEntry.getName().endsWith(File.separator)) {
					isDirectory = true;
				}
				final Path newPath = zipSlipProtect(zipEntry, target);
				if (isDirectory) {
					Files.createDirectories(newPath);
				} else {
					if (newPath.getParent() != null) {
						if (Files.notExists(newPath.getParent())) {
							Files.createDirectories(newPath.getParent());
						}
					}
					Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
		}
	}

	private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {
		String route = targetDir.toString() + zipEntry.getName();
		if (!targetDir.toString().endsWith(File.separator) && !zipEntry.getName().startsWith(File.separator)) {
			route = targetDir.toString() + File.separator + zipEntry.getName();
		}
		return new File(route).toPath();
	}

}
