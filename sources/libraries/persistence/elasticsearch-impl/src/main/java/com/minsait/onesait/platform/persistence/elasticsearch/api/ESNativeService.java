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
package com.minsait.onesait.platform.persistence.elasticsearch.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;
import com.minsait.onesait.platform.commons.exception.GenericOPException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ESNativeService {

	private TransportClient client;

	@Value("${onesaitplatform.database.elasticsearch.url:elasticdb}")
	private String host;

	@Value("${onesaitplatform.database.elasticsearch.port:9300}")
	private String port;

	@Value("${onesaitplatform.database.elasticsearch.cluster.name:sofia2_s4c}")
	private String clusterName;

	private PreBuiltTransportClient preBuiltTransportClient;

	@PostConstruct
	void initializeIt() {

		try {
			System.setProperty("es.set.netty.runtime.available.processors", "false");
			Settings settings = Settings.builder().put("client.transport.ignore_cluster_name", true)
					.put("client.transport.sniff", false).put("cluster.name", clusterName).build();
			preBuiltTransportClient = new PreBuiltTransportClient(settings);
			client = preBuiltTransportClient.addTransportAddress(getTransportAddress());
			log.info(String.format("Settings %s ", client.settings().toString()));
		} catch (Exception e) {
			log.info(String.format("Cannot Instantiate ElasticSearch Feature due to : %s ", e.getMessage()));
			log.error(String.format("Cannot Instantiate ElasticSearch Feature due to : %s ", e.getMessage()));

		}

	}

	private TransportAddress getTransportAddress() throws UnknownHostException {
		log.info(String.format("Connection details: host: %s. port:%s.", host, port));
		return new TransportAddress(InetAddress.getByName(host), Integer.parseInt(port));
	}

	public TransportClient getClient() {
		if (client == null) {
			log.error("CLIENT IS NULL");
			return null;
		}

		else
			return client;
	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public boolean loadBulkFromArray(String index, String type, List<String> docs) throws GenericOPException {
		log.info(String.format("Ingest content: %s Size of Files into elasticsearch %s %s", docs.size(), index, type));
		BulkRequestBuilder bulkRequest = getClient().prepareBulk();
		docs.forEach(doc -> bulkRequest.add(getClient().prepareIndex(index, type).setSource(doc)));

		if (bulkRequest.get().hasFailures()) {
			throw new GenericOPException(
					String.format("Failed during bulk load of files %s.", bulkRequest.get().buildFailureMessage()));
		}
		return bulkRequest.get().hasFailures();
	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public BulkResponse loadBulkFromFile(String index, File jsonPath) throws GenericOPException, IOException {
		log.info(String.format("Loading file %s into elasticsearch cluster", jsonPath));

		BulkRequestBuilder bulkBuilder = getClient().prepareBulk();
		byte[] buffer = ByteStreams.toByteArray(new FileInputStream(jsonPath));

		try {
			bulkBuilder.add(buffer, 0, buffer.length, index, null, XContentType.JSON);
		} catch (Exception e) {
			throw new GenericOPException(e);
		}

		BulkResponse response = bulkBuilder.get();

		if (response.hasFailures()) {
			throw new GenericOPException(String.format("Failed during bulk load of file %s. failure message: %s",
					jsonPath, response.buildFailureMessage()));
		}
		return response;
	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public BulkResponse loadBulkFromFileResource(String index, String jsonPath) throws GenericOPException, IOException {
		log.info(String.format("Loading file %s into elasticsearch cluster", jsonPath));

		BulkRequestBuilder bulkBuilder = getClient().prepareBulk();
		byte[] buffer = ByteStreams.toByteArray(new FileInputStream(jsonPath));

		try {
			bulkBuilder.add(buffer, 0, buffer.length, index, null, XContentType.JSON);
		} catch (Exception e) {
			throw new GenericOPException(e);
		}

		BulkResponse response = bulkBuilder.get();

		if (response.hasFailures()) {
			throw new GenericOPException(String.format("Failed during bulk load of file %s. failure message: %s",
					jsonPath, response.buildFailureMessage()));
		}
		return response;
	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public BulkResponse loadBulkFromJson(String index, String content) throws GenericOPException {

		BulkRequestBuilder bulkBuilder = getClient().prepareBulk();
		byte[] buffer = content.getBytes();
		try {
			bulkBuilder.add(buffer, 0, buffer.length, index, null, XContentType.JSON);
		} catch (Exception e) {
			throw new GenericOPException(e);
		}
		BulkResponse response = bulkBuilder.get();

		if (response.hasFailures()) {
			throw new GenericOPException(
					String.format("Failed during bulk load failure message: %s", response.buildFailureMessage()));
		}
		return response;
	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public SearchResponse updateByQuery(String index, String type, String jsonScript)
			throws InterruptedException, ExecutionException {

		log.info("updateByQuery ");
		SearchResponse response = null;

		UpdateByQueryRequestBuilder ubqrb = UpdateByQueryAction.INSTANCE.newRequestBuilder(getClient());

		Script script = new Script(jsonScript);

		response = ubqrb.source(index).script(script).source().setTypes(type).execute().get();

		log.info("updateByQuery response " + response);
		return response;

	}

	/**
	 * 
	 * @deprecated (Use ESInsertService instead)
	 */
	@Deprecated
	public SearchResponse updateByQueryAndFilter(String index, String type, String jsonScript, String jsonFilter)
			throws InterruptedException, ExecutionException {

		log.info("updateByQuery ");
		SearchResponse response = null;

		UpdateByQueryRequestBuilder ubqrb = UpdateByQueryAction.INSTANCE.newRequestBuilder(getClient());

		Script script = new Script(jsonScript);

		WrapperQueryBuilder build = QueryBuilders.wrapperQuery(jsonFilter);
		response = ubqrb.source(index).script(script).filter(build).source().setTypes(type).execute().get();

		log.info("updateByQuery response " + response);
		return response;

	}

}
