/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package org.apache.zeppelin.onesaitplatform.converter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class TableConverter {
	
	private static String fieldSeparator = "\t";
	private static char rowSeparator = '\n';
	
	 private static final Logger log = LoggerFactory.getLogger(TableConverter.class);
	
	public static List<String> fromListStr(List<String> l) {
		List<String> tableView = new LinkedList<String>(); 
		log.info("Add Header");
		appendHeaderTable(tableView);
		log.info("Add Values");
		appendJsonHeaderValuesTable(tableView,l);
		log.info("Done table" + tableView.toString());
	    return tableView;
	}
	
	
    
    private static List<String> appendHeaderTable(List<String> l) {
    	l.add("%table");
    	return l;
    }
    
    
    
    private static List<String> appendJsonHeaderValuesTable(List<String> tableView, List<String> jsonStrInstances) {
    	List<String> jsonPlannedFields = getStringJsonKeys(jsonStrInstances.get(0));
    	//Headers
    	StringBuilder strb = new StringBuilder();
    	for(String field: jsonPlannedFields) {
			strb.append(field);
			strb.append(fieldSeparator);
    	}
		int size = strb.length();
		strb.setCharAt(size-1,rowSeparator);
		tableView.add(strb.toString());
		
		//Values
    	for(String instance: jsonStrInstances) {
    		strb = new StringBuilder();
    		
    		for(String field: jsonPlannedFields) {
    			String value = getStringJsonValue(instance, field);
    			strb.append(value);
    			strb.append(fieldSeparator);
        	}
    		size = strb.length();
    		strb.setCharAt(size-1,rowSeparator);
    		tableView.add(strb.toString());
    	}
    	
    	return tableView;
    }
    
    private static List<String> getStringJsonKeys(String json) {
    	JsonObject object =  parseDataObj(json);
    	List<String> l = new LinkedList<String>();
    	for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
    	    l.add(entry.getKey());
    	}
    	return l;
    }
    
    private static String getStringJsonValue(String json, String key) {
    	JsonObject object =  parseDataObj(json);
    	return object.get(key).toString();
    }
    
    
    private static JsonObject parseDataObj(String json){
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonTree = jsonParser.parse(json).getAsJsonObject();
        return jsonTree;
    }
}
