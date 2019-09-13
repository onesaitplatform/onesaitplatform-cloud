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
package com.minsait.onesait.platform.persistence.mongodb.tools;

import com.minsait.onesait.platform.persistence.util.commands.CommandExecutionException;

/**
 * A component that interacts with the MongoTools stack (mongoexport,
 * mongoimport, mongoshell) using the Java 1.6 process API.
 * 
 * @see MongoDbConnectorQueryTests to view some usage examples
 */
public abstract class MongoDbToolsAdapter {

	/**
	 * Runs a MongoDB script on the given database
	 * 
	 * @param database
	 * @param path
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoDbScript(String database, String path, String args) throws CommandExecutionException;

	/**
	 * Runs a mongoshell command on the given database
	 * 
	 * @param database
	 * @param args
	 * @return
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoshellCommand(String database, String args) throws CommandExecutionException;

	/**
	 * Runs a mongoexport command on the given database
	 * 
	 * @param collection
	 * @param query
	 * @param outputFile
	 * @return
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoExportCommand(String database, String collection, String query, String outputFile)
			throws CommandExecutionException;

	/**
	 * Runs a mongoexport command on the given database
	 * 
	 * @param database
	 * @param collection
	 * @param query
	 * @param outputFile
	 * @param flagLimit
	 * @param limit
	 * @return
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoExportCommand(String database, String collection, String query, String outputFile,
			Boolean flagLimit, Integer limit) throws CommandExecutionException;

	/**
	 * Runs a mongoimport command on the given database
	 * 
	 * @param database
	 * @param collection
	 * @param inputFile
	 * @return
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoImportCommand(String database, String collection, String inputFile)
			throws CommandExecutionException;

	/**
	 * Runs a mongoimport command on the given database
	 * 
	 * @param database
	 * @param collection
	 * @param inputFile
	 * @return
	 * @throws CommandExecutionException
	 */
	public abstract String runMongoImportCommand(String database, String collection, String inputFile, String accion)
			throws CommandExecutionException;

	public static String getInnerArgDelimiter() {
		if (System.getProperty("os.name").contains("Windows")) {
			return "'";
		} else {
			return "\"";
		}
	}

	public static String getShellArgDelimiter() {
		if (System.getProperty("os.name").contains("Windows")) {
			return "\"";
		} else {
			return "";
		}
	}

}
