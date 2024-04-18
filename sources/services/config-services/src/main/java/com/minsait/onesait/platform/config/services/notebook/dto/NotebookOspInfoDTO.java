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
package com.minsait.onesait.platform.config.services.notebook.dto;

import org.jline.utils.Log;
import org.springframework.expression.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.Setter;

public class NotebookOspInfoDTO {
	
	public static final String OSP_INFO = "ospInfo";
	private static final String OWNER_STR = "owner";
	private static final String AUTHS = "authorizations";
	
	private static final String MSG_INVALID_FORMAT = "Invalid input format";
	private static final String MSG_NOT_POSSIBLE_CONVERT2JSON = "Not possible to convert to json with values %s - %s";

	@Getter
	@Setter
	private String owner;
	
	@Getter
	@Setter
	private JsonArray authorizations;
	
	public static NotebookOspInfoDTO fromJson(JsonObject notebookJson) {
		NotebookOspInfoDTO ospInfo = null;
		if (notebookJson.has(OSP_INFO)) {
			JsonObject jsonInfo = notebookJson.get(OSP_INFO).getAsJsonObject();
			if (jsonInfo.has(OWNER_STR) && jsonInfo.has(AUTHS)) {
				ospInfo = new NotebookOspInfoDTO();
				ospInfo.owner = jsonInfo.get(OWNER_STR).getAsString();
				ospInfo.authorizations = jsonInfo.get(AUTHS).getAsJsonArray();
			} else {
				throw new ParseException(0, MSG_INVALID_FORMAT);
			}
		} else {
			throw new ParseException(0, MSG_INVALID_FORMAT);
		}
		
		return ospInfo;
	}
	
	
	public static NotebookOspInfoDTO fromJson(String notebookJsonString) {
		NotebookOspInfoDTO ospInfo = null;
		JsonParser parser = new JsonParser();
		JsonElement jsonTree = parser.parse(notebookJsonString);
		if (jsonTree.isJsonObject()) {
			JsonObject notebookJson = jsonTree.getAsJsonObject();
			ospInfo = fromJson(notebookJson);
		}
		
		return ospInfo;
	}
	
	public JsonObject toJson() {
		JsonObject jsonObject = null;
		try {
			jsonObject = new JsonObject();
			jsonObject.addProperty(OWNER_STR, getOwner());
			for (JsonElement auth: getAuthorizations()) {
				jsonObject.add(AUTHS, auth.getAsJsonObject());
			}
			jsonObject.add(AUTHS, getAuthorizations().getAsJsonArray());
		} catch (Exception e) {
			Log.warn(String.format(MSG_NOT_POSSIBLE_CONVERT2JSON, getOwner(), getAuthorizations().toString()));
		}
		return jsonObject;
	}
	

}
