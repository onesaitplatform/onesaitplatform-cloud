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
package com.minsait.onesait.platform.persistence.mongodb.gridfs;

import java.io.InputStream;

import javax.persistence.PersistenceException;

import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.GridFSDownloadStream;

public interface GridFSConnector {
	/**
	 * Uploads a file to the GridFS filesystem.
	 * 
	 * @param database
	 * @param stream
	 * @param metadata
	 * @return The unique identifier of the file
	 */
	public ObjectId uploadGridFsFile(String database, InputStream stream, String metadata);

	/**
	 * Updates a file stored in the GridFS filesystem.
	 * 
	 * @param database
	 * @param fileId
	 * @param stream
	 * @param metadata
	 * @throws PersistenceException
	 */
	public void updateGridFsFile(String database, ObjectId fileId, InputStream stream, String metadata);

	/**
	 * Deletes a file stored in the GridFS filesystem.
	 * 
	 * @param database
	 * @param fileId
	 * @throws PersistenceException
	 */
	public void removeGridFsFile(String database, ObjectId fileId);

	/**
	 * Reads a file stored in the GridFS filesystem.
	 * 
	 * @param database
	 * @param fileId
	 * @return
	 * @throws PersistenceException
	 */
	public GridFSDownloadStream readGridFsFile(String database, ObjectId fileId);
}
