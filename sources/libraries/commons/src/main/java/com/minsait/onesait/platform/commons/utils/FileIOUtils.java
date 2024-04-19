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
package com.minsait.onesait.platform.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericRuntimeOPException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileIOUtils {

	private static final String TMP_DIR = "/tmp";

	public File createDirs(String path) {
		final File f = new File(path);
		if (f.exists()) {
			try {
				FileUtils.cleanDirectory(f);
			} catch (final IOException e) {
				log.error("Could not clean directory");
			}
		}
		f.mkdirs();
		return f;
	}

	public void unzipToPath(String pathToZipResource, String unzipped) throws Exception {
		try {
			final ClassPathResource zipFileCPR = new ClassPathResource(pathToZipResource);
			final File zipFile = new File(pathToZipResource);
			final File targetFile = new File(unzipped + File.separator + "zipfile.zip");

			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			if (zipFile.exists()) {
				log.debug("Zip File is file system resource");
				FileUtils.copyFile(zipFile, targetFile);
			} else {
				log.debug("Zip File is file class path resource");
				FileUtils.copyInputStreamToFile(zipFileCPR.getInputStream(), targetFile);
			}

			unzipFile(unzipped, targetFile);
		} catch (final Exception e) {
			log.error("Error while unzipping");
			throw e;
		}
	}

	private void zipFiles(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isDirectory()) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(new ZipEntry(fileName));
				zipOut.closeEntry();
			} else {
				zipOut.putNextEntry(new ZipEntry(fileName + "/"));
				zipOut.closeEntry();
			}
			final File[] children = fileToZip.listFiles();
			for (final File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}
		final FileInputStream fis = new FileInputStream(fileToZip);
		final ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		final byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	public File zipFiles(String unzipped, String zipPath) throws IOException {
		// if (new File(zipPath).exists()) {
		// new File(zipPath).delete();
		// }
		// final FileOutputStream fos = new FileOutputStream(zipPath);
		// final ZipOutputStream zipOut = new ZipOutputStream(fos);
		//
		// zipFiles(new File(unzipped), new File(unzipped).getName(), zipOut);
		//
		// return new File(zipPath);
		final File target = new File(zipPath);
		if (target.exists()) {
			target.delete();
		}
		if (!target.createNewFile()) {
			throw new IOException("could not create file at " + zipPath);
		}

		final FileOutputStream zipByte = new FileOutputStream(target);
		final ZipOutputStream zipOut = new ZipOutputStream(zipByte);
		final File source = new File(unzipped);
		try {
			if (source.isDirectory()) {
				final File[] fileList = source.listFiles();
				for (final File file : fileList) {

					zipFile(file, file.getName(), zipOut);
				}
			}
			zipOut.close();
			zipByte.close();
		} catch (final IOException e) {
			log.error("Error zipping files", e);
			throw e;
		}
		return target;
	}

	private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		log.debug("zipping file  {}", fileToZip.getAbsolutePath());
		try {
			if (fileToZip.isDirectory()) {
				if (fileName.endsWith("/")) {
					zipOut.putNextEntry(new ZipEntry(fileName));
					zipOut.closeEntry();
				} else {
					zipOut.putNextEntry(new ZipEntry(fileName + "/"));
					zipOut.closeEntry();
				}
				final File[] fileList = fileToZip.listFiles();
				for (final File file : fileList) {
					zipFile(file, fileName + "/" + file.getName(), zipOut);
				}
				return;
			}
			copyFilesToZip(fileToZip, fileName, zipOut);

		} catch (final IOException e) {
			log.error("error zipping folder", e);
			throw e;
		}
	}

	private void copyFilesToZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

		try (FileInputStream fis = new FileInputStream(fileToZip)) {
			final ZipEntry zipEntry = new ZipEntry(fileName);
			zipOut.putNextEntry(zipEntry);

			IOUtils.copy(fis, zipOut);
		} catch (final IOException e) {
			log.error("Error copying files to zip", e);
			throw e;
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

			Files.delete(targetFile.toPath());
			log.debug("Removed scaffolding");

		} catch (final Exception e) {
			log.error("Could not unzip scaffolding project: ", e);
			throw new GenericRuntimeOPException(e);
		}
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

}
