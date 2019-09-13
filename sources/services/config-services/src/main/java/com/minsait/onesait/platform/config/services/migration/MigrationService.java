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
package com.minsait.onesait.platform.config.services.migration;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.minsait.onesait.platform.config.model.MigrationData;
import com.minsait.onesait.platform.config.model.User;

import de.galan.verjson.core.IOReadException;
import de.galan.verjson.core.NamespaceMismatchException;
import de.galan.verjson.core.VersionNotSupportedException;
import de.galan.verjson.step.ProcessStepException;

public interface MigrationService {

	public ExportResult exportData(MigrationConfiguration config) throws IllegalAccessException;

	public String getJsonFromData(DataFromDB data) throws JsonProcessingException ;

	public DataFromDB getDataFromJson(String json) throws VersionNotSupportedException, NamespaceMismatchException, ProcessStepException, IOReadException;

	public LoadEntityResult loadData(MigrationConfiguration config, DataFromDB readData) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException;

	public void persistData(List<Object> entities, MigrationErrors errors) throws NoSuchFieldException, IllegalAccessException;
	
	public MigrationConfiguration configImportAll(DataFromDB data);
	
	public ExportResult exportAll() throws IllegalAccessException;
	public MigrationErrors importAll(DataFromDB data) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException;
	public MigrationErrors importData(MigrationConfiguration config, DataFromDB data)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException;
	
	MigrationError persistEntity(Object entity, Serializable id);
	
	public SchemaFromDB exportSchema();
	public String getJsonFromSchema(SchemaFromDB schema) throws JsonProcessingException;

	public String compareSchemas(String currentSchemaJson, String otherSchemaJson) throws IOException;
	
	public void storeMigrationData(User user, String name, String description, String fileName, byte[] file);
	public MigrationData findMigrationData(User user); 

}
