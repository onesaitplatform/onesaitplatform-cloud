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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.minsait.onesait.platform.commons.git.IEResourcesContextHolder;
import com.minsait.onesait.platform.commons.git.VersioningCommitContextHolder;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VersioningIOServiceImpl implements VersioningIOService {

	@Autowired
	private VersioningRepositoryFacade versioningRepositoryFacade;

	private static final List<String> nonProcessableDirectories = List.of("bundle_extras");
	private static final List<String> nonProcessableFiles = List.of(".zip", "bundle.metainf", "README.md",
			"bundle.png");

	@Override
	public <T> void serializeToFileSystem(Versionable<T> versionable) {
		serializeToFileSystem(versionable, null);
	}

	@Override
	public <T> void serializeToFileSystem(Versionable<T> versionable, String directory) {
		try {
			log.trace("Serializing Versionable entity of class {} with id {}", versionable.getClass().getSimpleName(),
					versionable.getId());
			if (directory != null) {
				IEResourcesContextHolder.setDirectory(directory);
			}
			saveToFile(absolutePath(versionable, directory), versionable.serialize());
			if (directory != null) {
				IEResourcesContextHolder.clear();
			}
		} catch (final IOException e) {
			log.error("Could not serialize Versionable Entity of class {}", versionable.getClass().getSimpleName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void restoreFromFileSystem(Versionable<T> versionable) {
		try {
			final String serializedEntity = readContentFromFile(absolutePath(versionable));
			final Versionable<T> deserializedEntity = (Versionable<T>) versionable.deserialize(serializedEntity);
			// DO NOT FIRE ENTITY LISTENER EVENT
			VersioningCommitContextHolder.setProcessPostUpdate(false);
			VersioningCommitContextHolder.setProcessPostCreate(false);
			versioningRepositoryFacade.save(deserializedEntity);
		} catch (final Exception e) {
			throw new VersioningException("Could not restore Versionable Entity of class "
					+ versionable.getClass().getSimpleName() + " : " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void restoreFromFileSystem(Versionable<T> versionable, String absolutePath) {
		try {
			final String serializedEntity = readContentFromFile(absolutePath);
			final Versionable<T> deserializedEntity = (Versionable<T>) versionable.deserialize(serializedEntity);
			// DO NOT FIRE ENTITY LISTENER EVENT
			VersioningCommitContextHolder.setProcessPostUpdate(false);
			versioningRepositoryFacade.save(deserializedEntity);
		} catch (final Exception e) {
			throw new VersioningException("Could not restore Versionable Entity of class "
					+ versionable.getClass().getSimpleName() + " : " + e.getMessage());
		}
	}

	@Override
	public <T> void removeFromFileSystem(Versionable<T> versionable) {
		try {
			removeFile(absolutePath(versionable));
		} catch (final Exception e) {
			throw new VersioningException("Could not remove Versionable Entity of class "
					+ versionable.getClass().getSimpleName() + " from filesystem: " + e.getMessage());
		}
	}

	@Override
	public <T> String absolutePath(Versionable<T> versionable) {
		return absolutePath(versionable, null);
	}

	private <T> String absolutePath(Versionable<T> versionable, String directory) {
		if (directory == null) {
			return DIR
					+ Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema())
					+ "/" + versionable.getClass().getSimpleName() + "/" + versionable.fileName();
		} else {
			return directory + "/" + versionable.getClass().getSimpleName() + "/" + versionable.fileName();
		}
	}

	@Override
	public <T> String relativePath(Versionable<T> versionable) {
		return Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()) + "/"
				+ versionable.getClass().getSimpleName() + "/" + versionable.fileName();
	}

	@Override
	public List<Versionable<?>> readAllVersionables() {
		return readAllVersionables(null);
	}

	@Override
	public List<Versionable<?>> readAllVersionables(String directory) {
		if (directory == null) {
			return processSubdirectories(DIR
					+ Tenant2SchemaMapper.extractVerticalNameFromSchema(MultitenancyContextHolder.getVerticalSchema()),
					null);
		} else {
			IEResourcesContextHolder.setDirectory(directory);
			final List<Versionable<?>> vs = processSubdirectories(directory, null);
			IEResourcesContextHolder.clear();
			return vs;
		}

	}

	private void saveToFile(String directory, String content) throws IOException {
		if (StringUtils.hasText(content)) {
			FileUtils.writeStringToFile(new File(directory), content, StandardCharsets.UTF_8);
		} else {
			log.warn("Tried to save versionable entity with empty content");
		}
	}

	private String readContentFromFile(String directory) throws IOException {
		return FileUtils.readFileToString(new File(directory), StandardCharsets.UTF_8);
	}

	private void removeFile(String directory) throws IOException {
		final File f = new File(directory);
		if (f.exists() && !f.isDirectory()) {
			Files.delete(f.toPath());
		} else {
			log.debug("Serialized file {} does not exist", directory);
		}

	}

	private List<Versionable<?>> processSubdirectories(String directory, String clazz) {
		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {
			final List<Path> folderNamesList = walk.filter(Files::isDirectory)
					.filter(s -> !s.toString().equals(directory) && !isDirectoryExcluded(s.toString()))
					.collect(Collectors.toList());
			return folderNamesList.stream().map(p -> filesToVersionables(p, clazz)).flatMap(l -> l.stream())
					.collect(Collectors.toList());
		} catch (final IOException e) {
			log.error("Error reading directory {}", directory, e);
			return null;
		}
	}

	private List<Versionable<?>> filesToVersionables(Path path, String clazz) {
		final String className = getClassNameFromPath(path);
		final String directory = path.toString();
		List<Versionable<?>> versionables = new ArrayList<>();
		try (Stream<Path> walk = Files.walk(Paths.get(directory))) {
			final List<Path> namesList = walk.collect(Collectors.toList());
			versionables = namesList.stream().filter(lf -> !Files.isDirectory(lf)).filter(Files::isRegularFile)
					.map(x -> x.toString()).filter(s -> !isFileExcluded(s)).map(f -> {
						try {
							log.trace("Initializing file to versionable");
							final Versionable<?> o = (Versionable<?>) Class.forName(className).newInstance();
							log.trace("Initializing readContentFromFile");
							final String serializedEntity = readContentFromFile(f);
							log.trace("Initializing deserialize");
							final Versionable<?> deserializedEntity = (Versionable<?>) o.deserialize(serializedEntity);
							log.trace("versionable of type {} with id {}",
									deserializedEntity.getClass().getSimpleName(), deserializedEntity.getId());
							return deserializedEntity;
						} catch (final Exception e) {
							log.error("Could not deserialize versionable of type {}", className, e);
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toList());
		} catch (final IOException e) {
			log.error("Error reading contents of path {}", path.toString(), e);
		}
		return versionables;
	}

	private boolean isFileExcluded(String f) {
		return nonProcessableFiles.stream().anyMatch(s -> f.contains(s));
	}

	private boolean isDirectoryExcluded(String d) {
		return nonProcessableFiles.stream().anyMatch(s -> d.contains(s));
	}

	@Override
	public String getClassNameFromPath(String path) {
		return getClassNameFromPath(new File(path).toPath());
	}

	private String getClassNameFromPath(Path path) {
		final String className = CONFIG_MODEL_CLASS_PREFIX + path.getFileName().toString();
		try {
			Class.forName(className).newInstance();
		} catch (final Exception | Error e) {
			return CONFIG_MODEL_CLASS_PREFIX + path.getParent().getFileName().toString();
		}
		return className;
	}

}
