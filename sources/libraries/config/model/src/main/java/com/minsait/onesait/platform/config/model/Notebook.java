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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Type;

import com.minsait.onesait.platform.config.model.base.OPResource;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.config.versioning.VersioningUtils;
import com.minsait.onesait.platform.multitenant.util.BeanUtil;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notebook", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFICATION" }))
public class Notebook extends OPResource implements Versionable<Notebook> {

	private static final long serialVersionUID = 1L;

	@Column(name = "IDZEP", length = 100, nullable = false)
	@Getter
	@Setter
	private String idzep;

	@Column(name = "PUBLIC", nullable = false)
	@Type(type = "org.hibernate.type.BooleanType")
	@ColumnDefault("false")
	@NotNull
	@Getter
	@Setter
	private boolean isPublic;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getIdentification().hashCode();
		result = prime * result + getUser().hashCode();
		result = prime * result + idzep.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Notebook other = (Notebook) obj;
		if (getIdentification() == null) {
			if (other.getIdentification() != null) {
				return false;
			}
		} else if (!getIdentification().equals(other.getIdentification())) {
			return false;
		}
		if (getUser() == null) {
			if (other.getUser() != null) {
				return false;
			}
		} else if (!getUser().equals(other.getUser())) {

			return false;
		}
		if (idzep == null) {
			if (other.idzep != null) {
				return false;
			}
		} else if (!idzep.equals(other.idzep)) {

			return false;
		}
		return true;
	}

	@Override
	public String fileName() {
		return getIdentification() + File.separator + getIdentification() + ".yaml";
	}

	@Override
	public String serialize() throws IOException {
		final String v = Versionable.super.serialize();

		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("NOTEBOOKS_DATA", "/usr/local/notebooks/");
		try {
			if (new File(contentsPath + getIdzep()).exists()) {
				if (!new File(versionablePath).exists()) {
					new File(versionablePath).mkdirs();
				}
				VersioningUtils.zipFolder(new File(contentsPath + getIdzep()),
						new File(versionablePath + File.separator + getIdentification() + ".zip"));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	@Override
	public Notebook deserialize(String content) throws IOException {
		final Notebook n = Versionable.super.deserialize(content);
		setIdentification(n.getIdentification());
		final String versionablePath = pathToVersionable(false);
		final String contentsPath = BeanUtil.getEnv().getProperty("NOTEBOOKS_DATA", "/usr/local/notebooks/");
		final File zip = new File(versionablePath + File.separator + n.getIdentification() + ".zip");
		if (zip.exists()) {
			try {
				final File target = new File(contentsPath + n.getIdzep());
				if (target.exists()) {
					FileUtils.deleteDirectory(target);
				}
				target.mkdirs();
				VersioningUtils.unzipFolder(zip, target);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return n;
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
