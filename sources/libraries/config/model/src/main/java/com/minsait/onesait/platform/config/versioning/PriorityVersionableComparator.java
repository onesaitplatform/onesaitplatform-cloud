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

import java.util.Comparator;

import com.minsait.onesait.platform.config.model.App;
import com.minsait.onesait.platform.config.model.Project;
import com.minsait.onesait.platform.config.model.interfaces.Versionable;

public class PriorityVersionableComparator implements Comparator<Versionable<?>> {

	@Override
	public int compare(Versionable<?> o1, Versionable<?> o2) {
		int result = 0;
		final int o1Index = VersioningRepositoryFacade.PRIORITY_PROCESSING.indexOf(o1.getClass().getSimpleName());
		final int o2Index = VersioningRepositoryFacade.PRIORITY_PROCESSING.indexOf(o2.getClass().getSimpleName());
		if (o1 instanceof App && o2 instanceof App) {
			// special case for apps, we have to insert first childs
			return compareApps((App) o1, (App) o2);
		}
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

	private int compareApps(App app1, App app2) {
		if (!app1.getChildApps().isEmpty() && app2.getChildApps().isEmpty()) {
			return 1;
		} else if (app1.getChildApps().isEmpty() && !app2.getChildApps().isEmpty()) {
			return -1;
		} else if (!app1.getChildApps().isEmpty() && !app2.getChildApps().isEmpty()) {
			return app1.getChildApps().size() - app2.getChildApps().size();
		} else {
			return 0;
		}

	}
}
