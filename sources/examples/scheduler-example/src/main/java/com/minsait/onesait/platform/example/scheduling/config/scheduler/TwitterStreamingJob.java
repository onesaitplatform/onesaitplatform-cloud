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
package com.minsait.onesait.platform.example.scheduling.config.scheduler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TwitterStreamingJob {
	
	public void execute (JobExecutionContext context) {
		
		String id = context.getJobDetail().getJobDataMap().getString("id");
		String ontology = context.getJobDetail().getJobDataMap().getString("ontology");
		String kp = context.getJobDetail().getJobDataMap().getString("clientPlatform");
		String token = context.getJobDetail().getJobDataMap().getString("token");
		String topics = context.getJobDetail().getJobDataMap().getString("topics");
		boolean geolocation = context.getJobDetail().getJobDataMap().getBoolean("geolocation");
		int timeout = context.getJobDetail().getJobDataMap().getInt("timeout");
		String config=context.getJobDetail().getJobDataMap().getString("configuration");
		List<String> keywords=getKeywordsForListener(topics);
	
		
		//now suscribe
	}

	private List<String> getKeywordsForListener(String topics) {
		try {
			topics = new String(topics.getBytes("iso-8859-1"), "utf8");
		 } catch (UnsupportedEncodingException e) {
			 log.debug("Problem decodifying keywords");
		 }
		 List<String> arrayKeywords=new ArrayList<String>();
		 StringTokenizer st=new StringTokenizer(topics, ",");
	     while(st.hasMoreTokens()) {
			 arrayKeywords.add(st.nextToken().trim());
		 }
	     return arrayKeywords;
	}

}
