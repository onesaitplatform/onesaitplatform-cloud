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
package com.minsait.onesait.platform.config.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "WEB_PROJECT", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class WebProject extends OPResource implements Versionable<WebProject> {

	private static final long serialVersionUID = 1L;

	@Column(name = "DESCRIPTION", length = 250, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String description;

	@Column(name = "MAIN_FILE", length = 100, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String mainFile;

	@Override
	public String fileName() {
		return getIdentification() + File.separator + getIdentification() + ".yaml";
	}

	@Override
	public String serialize() throws IOException {
		final String v = Versionable.super.serialize();
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("onesaitplatform.webproject.rootfolder.path",
				"/usr/local/webprojects/");
		try {
			if (new File(contentsPath + getIdentification()).exists()) {
				if (!new File(versionablePath).exists()) {
					new File(versionablePath).mkdirs();
				}
				VersioningUtils.zipFolder(new File(contentsPath + getIdentification()),
						new File(versionablePath + File.separator + getIdentification() + ".zip"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	@Override
	public WebProject deserialize(String content) throws IOException {
		final WebProject wp = Versionable.super.deserialize(content);
		setIdentification(wp.getIdentification());
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("onesaitplatform.webproject.rootfolder.path",
				"/usr/local/webprojects/");
		final File zip = new File(versionablePath + File.separator + wp.getIdentification() + ".zip");
		if (zip.exists()) {
			try {
				final File target = new File(contentsPath + wp.getIdentification());
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				target.mkdirs();
				VersioningUtils.unzipFolder(zip, target);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return wp;
	}

	@Override
	public String pathToVersionable(boolean toYamlFile) {
		final String path = Versionable.super.pathToVersionable(toYamlFile);
		if (toYamlFile) {
			return path;
		} else {
			return path + File.separator + getIdentification();
		}
	}

	@Override
	public List<String> zipFileNames() {
		final ArrayList<String> list = new ArrayList<>();
		list.add(pathToVersionable(false) + File.separator + getIdentification() + ".zip");
		return list;
	}

	@Override
	public void setOwnerUserId(String userId) {
		final User u = new User();
		u.setUserId(userId);
		setUser(u);
	}

}
