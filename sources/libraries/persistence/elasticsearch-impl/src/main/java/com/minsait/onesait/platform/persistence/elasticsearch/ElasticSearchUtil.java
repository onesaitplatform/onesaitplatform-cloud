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
package com.minsait.onesait.platform.persistence.elasticsearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchUtil {
    
    private ElasticSearchUtil() {
        
    }
    
    private static final String QUERY_ERROR = "Query Error:";
    
    public static String parseSearchResultFailures(SearchResponse res) {
        if (res == null) {
            return "null";
        } else if (res.getFailedShards() > 0) {
            final ShardSearchFailure[] shardFailures = res.getShardFailures();
            StringBuilder reasonBuilder = new StringBuilder();
            for (ShardSearchFailure failure : shardFailures) {
                final String reason = failure.reason();
                log.info("Status: " + failure.status()
                        + " - Search failed on shard: " + reason);
                reasonBuilder.append(reason);
            }
            return reasonBuilder.toString();
        }
        return "";
    }
    
    public static List<String> processSearchResponseToStringList(SearchResponse searchResponse) {
        if (searchResponse != null && searchResponse.status().equals( RestStatus.OK)) {
            SearchHits hits = searchResponse.getHits(); 
            SearchHit[] searchHits = hits.getHits();
            List<String> result = new ArrayList<>();
            for (SearchHit hit : searchHits) {
                result.add(hit.getSourceAsString());
            }
            
            return result;
            
        } else {
            String msg = ElasticSearchUtil.parseSearchResultFailures(searchResponse);
            log.error("Error in findQueryDataAsJson:" + msg);
            return Arrays.asList(QUERY_ERROR + msg);
        }
    }
    
    public static String processSearchResponseToJson(SearchResponse searchResponse) {
        if (searchResponse != null && searchResponse.status().equals( RestStatus.OK)) {
            return parseElastiSearchResult(searchResponse.toString(), true);
        } else {
            String msg = ElasticSearchUtil.parseSearchResultFailures(searchResponse);
            log.error("Error in findQueryDataAsJson:" + msg);
            return QUERY_ERROR + msg;
        }
    }

	public static String parseElastiSearchResult(String response, boolean addId) throws JSONException {

		JSONArray hitsArray = null;
		JSONObject hits = null;
		JSONObject aggregations = null;
		JSONObject source = null;
		JSONObject json = null;

		final JSONArray jsonArray = new JSONArray();
		
		json = new JSONObject(response);
		hits = json.getJSONObject("hits");
		hitsArray = hits.getJSONArray("hits");

		try {

			aggregations = json.optJSONObject("aggregations");
			if (aggregations != null) {
    			try {
    				return aggregations.getJSONObject("gender").getJSONArray("buckets").toString();
    			} catch (final JSONException e) {
    				return aggregations.toString();
    			}
			}
		} catch (final JSONException e) {
		    log.error("" + e);
		}
		if (hitsArray.length() > 0) {
			for (int i = 0; i < hitsArray.length(); i++) {
				final JSONObject h = hitsArray.getJSONObject(i);
				source = h.getJSONObject("_source");
				if (addId)
					source.put("_id", h.getString("_id"));
				jsonArray.put(source);
			}
		} else if (hits.length() > 0) {
			jsonArray.put(hits);
		}

		return jsonArray.toString();
	}
	

    public static Map<String, String> processUpdateStatement(String updateStmt) {
        JSONObject json = new JSONObject(updateStmt);
        JSONObject script = json.getJSONObject("script");
        JSONObject query = json.getJSONObject("query");        
        Map<String, String> stmt = new HashMap<>();
        stmt.put("source_script", script.getString("source"));
        stmt.put("query", query.toString());
        return stmt;
    }

}
