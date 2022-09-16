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
package com.minsait.onesait.platform.config.versioning;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;
import com.minsait.onesait.platform.multitenant.MultitenancyContextHolder;
import com.minsait.onesait.platform.multitenant.Tenant2SchemaMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriorityVersionableClassComparator implements Comparator<String> {

	private boolean isFileName = false;

	public PriorityVersionableClassComparator(boolean isFileName) {
		this.isFileName = isFileName;
	}

	@Override
	public int compare(String s1, String s2) {
		if(isFileName) {
			s1 = getClassNameFromPath(s1);
			s2 = getClassNameFromPath(s2);
		}
		int result = 0;
		try {
			final Versionable<?> o1 = (Versionable<?>) Class.forName(s1).newInstance();
			final Versionable<?> o2 = (Versionable<?>) Class.forName(s2).newInstance();
			final int o1Index = VersioningRepositoryFacade.PRIORITY_PROCESSING.indexOf(o1.getClass().getSimpleName());
			final int o2Index = VersioningRepositoryFacade.PRIORITY_PROCESSING.indexOf(o2.getClass().getSimpleName());
			// Projects last
			if (o1 instanceof Project || o2 instanceof Project) {
				return compareProject(o1, o2);
			}
			if (o1Index < 0 && o2Index < 0) {
				result = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());

			} else if (o1Index < 0) {
				result = 1;
			} else if (o2Index < 0) {
				result = -1;
			} else {
				result = o1Index - o2Index;
			}
		} catch (final Exception e) {
			log.error("Error while comparing versionable classes, returning 0", e);
		}
		return result;
	}

	private int compareProject(Versionable<?> o1, Versionable<?> o2) {
		if (o1 instanceof Project && !(o2 instanceof Project)) {
			return 100;
		} else if (!(o1 instanceof Project) && o2 instanceof Project) {
			return -100;
		} else {
			return 0;
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
		return VersioningTxBusinessServiceImpl.SCAN_PACKAGE + "." +result;
	}

}
