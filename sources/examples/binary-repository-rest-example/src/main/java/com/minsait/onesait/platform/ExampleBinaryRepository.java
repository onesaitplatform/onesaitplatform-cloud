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
package com.minsait.onesait.platform;

import java.io.File;
import java.io.IOException;

import org.overviewproject.mime_types.GetBytesException;

import com.minsait.onesait.platform.binaryrepository.BinaryDataFile;
import com.minsait.onesait.platform.binaryrepository.BinaryRepositoryClient;
import com.minsait.onesait.platform.client.exception.BinaryRepositoryException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleBinaryRepository {

	private final static String USERNAME = "developer";
	private final static String PASSWORD = "changeIt!";
	private final static String SERVER = "http://localhost:18000/controlpanel";
	private final static String PATH_TO_FILE = "/tmp/DNI.pdf";

	public static void main(String[] args) throws BinaryRepositoryException, IOException, GetBytesException {

		// Create binary repository RESTFull client
		final BinaryRepositoryClient client = new BinaryRepositoryClient(USERNAME, PASSWORD, SERVER);

		// Add binary file to platform
		final String newFileId = client.addBinaryFile(new File(PATH_TO_FILE), "");
		log.info("New file ID is {}", newFileId);

		// Retrieve binary file from platform
		final BinaryDataFile bfile = client.getBinaryFile(newFileId);
		log.info("Retrieved file with name \"{}\"", bfile.getFileName());

		// Update binary file
		final String metadata = "{\"private\" : true}";
		client.updateBinaryFile(newFileId, new File(PATH_TO_FILE), metadata);
		log.info("Updated binary file {}", newFileId);

		// delete the binary file
		client.removeBinaryFile(newFileId);
		log.info("Deleted binary file {}", newFileId);

	}
}
