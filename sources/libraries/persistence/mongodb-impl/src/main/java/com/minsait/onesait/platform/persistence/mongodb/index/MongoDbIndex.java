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
package com.minsait.onesait.platform.persistence.mongodb.index;

import java.util.Map;
import java.util.Objects;

import org.bson.Document;

import com.minsait.onesait.platform.persistence.mongodb.UtilMongoDB;
import com.mongodb.client.model.IndexOptions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A class that represents a MongoDB index
 */
@ToString
public class MongoDbIndex {

	@Getter
	@Setter
	private int version;
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String namespace;
	@Getter
	@Setter
	private Map<String, ?> key;
	@Getter
	@Setter
	private IndexOptions indexOptions;

	public MongoDbIndex() {
	}

	public MongoDbIndex(Map<String, Integer> key, IndexOptions indexOptions) {
		this.key = key;
		this.indexOptions = indexOptions;
	}

	public MongoDbIndex(Map<String, Integer> key) {
		this(key, null);
	}

	public MongoDbIndex(String name) {
		this.name = name;
	}

	public static MongoDbIndex fromIndexDocument(Document index_asDocument) {
		MongoDbIndex index = new MongoDbIndex();
		index.setName(index_asDocument.getString("name"));
		index.setKey(new UtilMongoDB().toJavaMap(index_asDocument.get("key", Document.class), Integer.class));
		index.setVersion(index_asDocument.getInteger("v", 1));
		index.setNamespace(index_asDocument.getString("ns"));
		IndexOptions indexOptions = new IndexOptions();
		indexOptions.name(index_asDocument.getString("name"));
		if (index_asDocument.getBoolean("unique") != null)
			indexOptions.unique(index_asDocument.getBoolean("unique"));
		if (index_asDocument.getBoolean("sparse") != null)
			indexOptions.sparse(index_asDocument.getBoolean("sparse"));
		if (index_asDocument.getBoolean("background") != null)
			indexOptions.background(index_asDocument.getBoolean("background"));
		index.setIndexOptions(indexOptions);
		return index;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof MongoDbIndex))
			return false;
		MongoDbIndex that = (MongoDbIndex) other;
		return Objects.equals(this.name, that.name) && Objects.equals(this.key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.key);
	}

}
