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
package com.minsait.onesait.platform.persistence.mongodb.gridfs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

import lombok.extern.slf4j.Slf4j;

@Component
@Lazy
@Slf4j
public class GridFSConnectorImpl implements GridFSConnector {

	@Autowired
	private MongoDbTemplate mongoDbConnector;

	private void uploadGridFsFile(String database, ObjectId fileId, InputStream stream, String metadata) {
		try {
			log.info("Uploading file to GridFS. Database = {}, metadata = {}.", database, metadata);
			GridFSBucket bucket = mongoDbConnector.configureGridFSBucket(database);
			GridFSUploadOptions uploadOptions = new GridFSUploadOptions().metadata(parseMetadata(metadata));
			bucket.uploadFromStream(new BsonObjectId(fileId), fileId.toHexString(), stream, uploadOptions);
		} catch (Throwable e) {
			log.error("Unable to upload file to GridFS. Database = {}, metadata = {}, cause = {}, errorMessage = {}.",
					database, metadata, e.getCause(), e.getMessage());
			throw new PersistenceException("Unable to upload file to GridFS", e);
		}
	}

	@Override
	public ObjectId uploadGridFsFile(String database, InputStream stream, String metadata) {
		ObjectId fileId = new ObjectId();
		uploadGridFsFile(database, fileId, stream, metadata);
		return fileId;
	}

	@Override
	public void updateGridFsFile(String database, ObjectId fileId, InputStream stream, String metadata) {
		removeGridFsFile(database, fileId);
		uploadGridFsFile(database, fileId, stream, metadata);
	}

	@Override
	public void removeGridFsFile(String database, ObjectId fileId) {
		try {
			log.info("Deleting file in GridFS. Database = {}, fileId = {}.", database, fileId);
			GridFSBucket bucket = mongoDbConnector.configureGridFSBucket(database);
			bucket.delete(fileId);
		} catch (Throwable e) {
			log.error("Unable to delete file in GridFS. Database = {}, fileId = {}, cause = {}, errorMessage = {}.",
					database, fileId, e.getCause(), e.getMessage());
			throw new PersistenceException("Unable to upload file to GridFS", e);
		}
	}

	@Override
	public GridFSDownloadStream readGridFsFile(String database, ObjectId fileId) {
		try {
			log.info("Reading GridFS file. Database = {}, fileId = {}.", database, fileId);
			GridFSBucket bucket = mongoDbConnector.configureGridFSBucket(database);
			return bucket.openDownloadStream(fileId);
		} catch (Throwable e) {
			log.error("Unable to read GridFS file. Database = {}, fileId = {}, cause = {}, errorMessage = {}.",
					database, fileId, e.getCause(), e.getMessage());
			throw new PersistenceException("Unable to read GridFS file", e);
		}
	}

	private Document parseMetadata(String metadata) {
		if (metadata == null || metadata.isEmpty())
			return new Document();
		try {
			Map<String, Object> parsedMetadata = new ObjectMapper().readValue(metadata,
					new TypeReference<Map<String, Object>>() {
					});
			return new Document(parsedMetadata);
		} catch (IOException e) {
			log.error("Error parsing metadata", e);
			return new Document();
		}
	}
}
