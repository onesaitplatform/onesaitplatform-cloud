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
package com.minsait.onesait.platform.config.components;

import java.util.Comparator;

import com.minsait.onesait.platform.config.model.ProjectResourceAccess;

public class ProjectResourceAccessComparator implements Comparator<ProjectResourceAccess> {

	@Override
	public int compare(ProjectResourceAccess o1, ProjectResourceAccess o2) {
		if (o1.getResource() != null && o2.getResource() != null) {
			int i = o1.getResource().getIdentification().compareToIgnoreCase(o2.getResource().getIdentification());
			if (i == 0) {
				i = o1.getResource().getId().compareToIgnoreCase(o2.getResource().getId());
				if (i == 0 && o1.getUser() != null)
					i = o1.getUser().getUserId().compareTo(o2.getUser().getUserId());
				else if (i == 0 && o1.getAppRole() != null && o1.getAppRole().getName() !=null)
					i = o1.getAppRole().getName().compareTo(o2.getAppRole().getName());
			}
			return i;
		} else
			return o1.getId().compareTo(o2.getId());
	}

}
