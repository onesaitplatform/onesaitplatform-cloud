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
package com.minsait.onesait.platform.config.services.dataflow;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.minsait.onesait.platform.config.model.Pipeline;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class StreamsetsApiWrapper {

    //using classes as constants instead enums to be used in annotations
	public final class OrderField {
	    private OrderField() {      
	    }
	    
	    public static final String NAME = "NAME";
	    public static final String TITLE = "TITLE";
	    public static final String LAST_MODIFIED = "LAST_MODIFIED";
	    public static final String CREATED = "CREATED";
	    public static final String STATUS = "STATUS";
	    
	    public static final String ALL_OPTIONS = NAME + ","
	            + TITLE + ","
	            + LAST_MODIFIED + ","
	            + CREATED + ","
	            + STATUS;
	}
	
	public final class Order {
	    private Order() {	        
	    }
	    
	    public static final String ASC = "ASC";
	    public static final String DESC = "DESC";
	    
	    public static final String ALL_OPTIONS = ASC + "," 
	            + DESC;
	}
	  
	
	public final class SystemLabel {
	    private SystemLabel() {	        
	    }
	    
	    public static final String ALL_PIPELINES = "system:allPipelines"; 
	    public static final String RUNNING_PIPELINES = "system:runningPipelines";
	    public static final String NON_RUNNING_PIPELINES = "system:nonRunningPipelines"; 
	    public static final String INVALID_PIPES = "system:invalidPipelines"; 
	    public static final String ERROR_PIPELINES = "system:errorPipelines";
	    public static final String SHARE_WITH_ME_PIPELINES = "system:sharedWithMePipelines";
	    
	    public static final String ALL_OPTIONS = ALL_PIPELINES + "," 
	            + RUNNING_PIPELINES + "," 
                + NON_RUNNING_PIPELINES + "," 
                + INVALID_PIPES + "," 
	            + ERROR_PIPELINES + "," 
	            + SHARE_WITH_ME_PIPELINES;
	}

	private static final String CONTEXT = "/rest";
	private static final String VERSION = "/v1";
	private static final String PIPELINEID_STR = "pipelineId";
	private static final String XREQUESTEDBY_STR = "X-Requested-By";
	private static final String FILTER_TEXT = "filterText";
	private static final String LABEL = "label";
	private static final String OFFSET = "offset";
	private static final String LEN = "len";
	private static final String ORDER_BY = "orderBy";
	private static final String ORDER = "order";
	private static final String INCLUDE_STATUS = "includeStatus";
	
	
	private StreamsetsApiWrapper() {

    }


	public static ResponseEntity<String> pipelineCreate(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														String pipelineName, Pipeline.PipelineType type) {
		// /rest/v1/pipeline/<pipelineName>

		// configure headers
		headers.set(XREQUESTEDBY_STR, "Onesait Platform"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		final StringBuilder urlBuilder = new StringBuilder();
		final String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/pipeline/{pipelineId}")
				.toString();

		// construct uri with url parameters
		final Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineName);

		final URI uri = UriComponentsBuilder
				.fromUriString(url)
				.queryParam("rev", "0")
				.queryParam("autoGeneratePipelineId", "true")
				.queryParam("pipelineType", type.toString())
				.buildAndExpand(uriParams)
				.toUri();

		return rt.exchange(uri, HttpMethod.PUT, headersEntity, String.class);
	}

	public static ResponseEntity<String>  pipelineRemove(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl, String streamsetsId) {
		// /rest/v1/pipeline

		// configure headers
		headers.set(XREQUESTEDBY_STR, "Onesait Platform");
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);


		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/pipeline/{pipelineId}").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, streamsetsId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.DELETE, headersEntity, String.class);
	}



	public static ResponseEntity<String> restartInstance(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl) {
		// /rest/v1/system/restart

		// configure headers
		headers.set(XREQUESTEDBY_STR, "Onesait Platform");
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/system/restart")
				.toString();

		return rt.exchange(url, HttpMethod.POST, headersEntity, String.class);
	}


	public static String pipelineHistory(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
										 String pipelineId, boolean fromBeginning) {
		// /rest/v1/pipeline/<pipelineId>/history
		// /rest/v1/pipeline/OPCUANODESINFOc266de0a-d81c-4032-b7f7-3592c3440ca4/history?rev=0&fromBeginning=false

		// configure headers
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/history").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").queryParam("fromBeginning", fromBeginning)
				.build().toUri();

		HttpEntity<String> response = rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);

		return response.getBody();
	}

	public static ResponseEntity<String> pipelineStart(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
													   String pipelineId, String parameters) {
		// /rest/v1/pipeline/<pipelineId>/start
		// /rest/v1/pipeline/OPCUAdatab87df379-a083-4e25-846d-3d4b4859a28d/start?rev=0

		// configure headers
		headers.set(XREQUESTEDBY_STR, "sdc"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> headersEntity = new HttpEntity<>(parameters, headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/start").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.POST, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelineStop(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
													  String pipelineId) {
		// /rest/v1/pipeline/<pipelineId>/stop
		// /rest/v1/pipeline/OPCUAREADSIGNALSac1aec00-91d4-4563-b948-59f1fba8d4c5/stop?rev=0

		// configure headers
		headers.set(XREQUESTEDBY_STR, "sdc"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/stop").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.POST, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelineStatus(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														String pipelineId) {
		// /rest/v1/pipeline/<pipelineId>/status
		// /rest/v1/pipeline/Exampleabfb6db8-8b00-4185-a40f-0c1695d912a2/status?rev=0

		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/status").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelinesStatus(RestTemplate rt, HttpHeaders headers,
			String dataflowServiceUrl) {
		// /rest/v1/pipelines/status

		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION).append("/pipelines/status")
				.toString();

		// construct uri with url
		URI uri = UriComponentsBuilder.fromUriString(url).build().toUri();

		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelineExport(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														String pipelineId) {
		// /rest/v1/pipeline/<pipelineId>/export
		// /rest/v1/pipeline/Algo7b511a1f-ad22-4afc-bf71-ca9b58d51040/export?rev=0&attachment=false&includeLibraryDefinitions=true

		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/export").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").queryParam("attachment", false)
				.queryParam("includeLibraryDefinitions", true).build().toUri();

		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelineImport(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														String pipelineId, boolean override,
														boolean autoGeneratePipelineId, String config) {
		// /rest/v1/pipeline/<pipelineId>/start
		// /rest/v1/pipeline/OPCUAdatab87df379-a083-4e25-846d-3d4b4859a28d/start?rev=0

		// configure headers
		headers.set(XREQUESTEDBY_STR, "sdc"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		HttpEntity<?> headersEntity = new HttpEntity<>(config, headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder.append(dataflowServiceUrl).append(CONTEXT).append(VERSION)
				.append("/pipeline/{pipelineId}/import").toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		// add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").queryParam("overwrite", override)
				.queryParam("autoGeneratePipelineId", autoGeneratePipelineId).queryParam("draft", false).build()
				.toUri();

		return rt.exchange(uri, HttpMethod.POST, headersEntity, String.class);
	}
	
	
	public static ResponseEntity<String> pipelines(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
												   String filterText, String label, int offset, int len, String orderBy,
												   String order, boolean includeStatus){
	    // /rest/v1/pipelines?filterText=<filter>&label=<label>&offset=<offset>&len=<longitud>&orderBy=<orderField>&order=<orderType>&includeStatus=true
	    // /rest/v1/pipelines?filterText=kfk&label=system%3AallPipelines&offset=0&len=50&orderBy=NAME&order=ASC&includeStatus=true
	    
	    //configure headers
	    HttpEntity<?> headersEntity = new HttpEntity<>(headers);
	    
	    // construct url
        StringBuilder urlBuilder = new StringBuilder();
        String url = urlBuilder
                .append(dataflowServiceUrl)
                .append(CONTEXT)
                .append(VERSION)
                .append("/pipelines")
                .toString();
        
        // construct uri with url parameters
        Map<String, String> uriParams = new HashMap<>();
        URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();
        
        //add query parameters
        uri = UriComponentsBuilder.fromUri(uri)
                .queryParam(FILTER_TEXT, filterText)
                .queryParam(LABEL, label)
                .queryParam(OFFSET, Integer.toString(offset))
                .queryParam(LEN, Integer.toString(len))
                .queryParam(ORDER_BY, orderBy)
                .queryParam(ORDER, order)
                .queryParam(INCLUDE_STATUS, Boolean.toString(includeStatus))
                .build().toUri();
        
        return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}
	
	public static ResponseEntity<String> pipelineMetrics(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														  String pipelineId){
		// /rest/v1/pipelines?filterText=<filter>&label=<label>&offset=<offset>&len=<longitud>&orderBy=<orderField>&order=<orderType>&includeStatus=true
		// /rest/v1/pipelines?filterText=kfk&label=system%3AallPipelines&offset=0&len=50&orderBy=NAME&order=ASC&includeStatus=true

		//configure headers
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/pipeline/{pipelineId}/metrics")
				.toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		//add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}

	public static ResponseEntity<String> resetOffset(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														 String pipelineId){
		// /rest/v1/pipeline/OPCUAdatab87df379-a083-4e25-846d-3d4b4859a28d/resetOffset

		// configure headers
		headers.set(XREQUESTEDBY_STR, "sdc"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/pipeline/{pipelineId}/resetOffset")
				.toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		//add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.POST, headersEntity, String.class);
	}

	public static ResponseEntity<String> pipelineConfiguration(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
														 String pipelineId){
		//configure headers
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);

		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
				.append(dataflowServiceUrl)
				.append(CONTEXT)
				.append(VERSION)
				.append("/pipeline/{pipelineId}")
				.toString();

		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();

		//add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();

		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}

	public static ResponseEntity<String> getCommittedOffsets(RestTemplate rt, HttpHeaders headers, String dataflowServiceUrl,
			 String pipelineId){
		// /rest/v1/pipeline/OPCUAdatab87df379-a083-4e25-846d-3d4b4859a28d/committedOffsets
		
		// configure headers
		//headers.set(XREQUESTEDBY_STR, "sdc"); // this header is needed in POST, DELETE and PUT methods
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<?> headersEntity = new HttpEntity<>(headers);
		
		// construct url
		StringBuilder urlBuilder = new StringBuilder();
		String url = urlBuilder
		.append(dataflowServiceUrl)
		.append(CONTEXT)
		.append(VERSION)
		.append("/pipeline/{pipelineId}/committedOffsets")
		.toString();
		
		// construct uri with url parameters
		Map<String, String> uriParams = new HashMap<>();
		uriParams.put(PIPELINEID_STR, pipelineId);
		URI uri = UriComponentsBuilder.fromUriString(url).buildAndExpand(uriParams).toUri();
		
		//add query parameters
		uri = UriComponentsBuilder.fromUri(uri).queryParam("rev", "0").build().toUri();
		
		return rt.exchange(uri, HttpMethod.GET, headersEntity, String.class);
	}
	
}
