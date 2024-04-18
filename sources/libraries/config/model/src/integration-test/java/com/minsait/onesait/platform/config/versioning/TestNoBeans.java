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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.minsait.onesait.platform.config.model.interfaces.Versionable;

@Ignore
public class TestNoBeans {

	private static final String CLASS_PREFIX = "com.minsait.onesait.platform.config.model.";

	@Test
	public void testSubdirs() {
		final String dir = VersioningIOService.DIR + "onesaitplatform";
		printFolderNames(dir);
	}

	private void printFolderNames(String directory) {

		// Reading the folder and getting Stream.
		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

			// Filtering the paths by a folder and adding into a list.
			final List<Path> folderNamesList = walk.filter(Files::isDirectory)
					.filter(s -> !s.toString().equals(directory))
					// .map(x -> x.getFileName().toString())
					.collect(Collectors.toList());

			folderNamesList.forEach(this::printFileNames);

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void printFileNames(Path path) {
		final String className = CLASS_PREFIX + path.getFileName().toString();
		final String directory = path.toString();

		// Reading the folder and getting Stream.
		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {

			// Filtering the paths by a regualr file and adding into a list.
			final List<String> fileNamesList = walk.filter(Files::isRegularFile).map(x -> x.toString())
					.collect(Collectors.toList());

			// printing the file nams
			fileNamesList.forEach(f -> {
				try {
					final Versionable<?> o = (Versionable<?>) Class.forName(className).newInstance();
					final String serializedEntity = readContentFromFile(f);
					final Versionable<?> deserializedEntity = (Versionable<?>) o.deserialize(serializedEntity);
					System.out.println("versionable of type " + deserializedEntity.getClass().getSimpleName() + " with id " + deserializedEntity.getId());
				} catch (final Exception e) {
					// TODO: handle exception
				}
			});

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private String readContentFromFile(String directory) throws IOException {
		return FileUtils.readFileToString(new File(directory), StandardCharsets.UTF_8);
	}
}
