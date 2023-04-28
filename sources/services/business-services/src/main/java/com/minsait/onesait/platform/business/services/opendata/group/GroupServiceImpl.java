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
package com.minsait.onesait.platform.business.services.opendata.group;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.business.services.opendata.OpenDataApi;
import com.minsait.onesait.platform.config.services.opendata.dto.OpenDataGroup;
import com.minsait.onesait.platform.config.services.opendata.dto.responses.GroupListResponse;

@Service
public class GroupServiceImpl implements GroupService {
	
	@Autowired
	private OpenDataApi api;
	
	@Override
	public List<OpenDataGroup> getGroupsByUser(String userToken) {		
		GroupListResponse responseGroups = (GroupListResponse) api.getOperation("group_list?all_fields=true",
				userToken, GroupListResponse.class);
		
		if (responseGroups.getSuccess()) {
			return responseGroups.getResult();
		} else {
			return new ArrayList<>();
		}		
	}

}
