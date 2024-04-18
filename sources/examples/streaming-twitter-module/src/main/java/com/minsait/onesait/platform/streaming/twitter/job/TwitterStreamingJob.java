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
package com.minsait.onesait.platform.streaming.twitter.job;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.streaming.twitter.listener.TwitterStreamListener;
import com.minsait.onesait.platform.streaming.twitter.service.TwitterStreamService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TwitterStreamingJob {

	@Autowired
	TwitterStreamService twitterStreamService;
	@Autowired
	TwitterStreamListener twitterStreamListener;

	public void execute(JobExecutionContext context) {

		String id = context.getJobDetail().getJobDataMap().getString("id");

		if (!twitterStreamService.isSubscribe(id)) {
			String topics = context.getJobDetail().getJobDataMap().getString("topics");
			twitterStreamListener.setId(id);
			twitterStreamListener.setUser(context.getJobDetail().getJobDataMap().getString("userId"));
			twitterStreamListener.setOntology(context.getJobDetail().getJobDataMap().getString("ontology"));
			twitterStreamListener.setClientPlatform(context.getJobDetail().getJobDataMap().getString("clientPlatform"));
			twitterStreamListener.setToken(context.getJobDetail().getJobDataMap().getString("token"));
			twitterStreamListener.setKeywords(this.getKeywordsForListener(topics));
			twitterStreamListener.setGeolocation(context.getJobDetail().getJobDataMap().getBoolean("geolocation"));
			twitterStreamListener.setTimeout(context.getJobDetail().getJobDataMap().getInt("timeout"));
			twitterStreamListener
					.setConfigurationId(context.getJobDetail().getJobDataMap().getString("configurationId"));

			try {
				twitterStreamService.subscribe(twitterStreamListener);
			} catch (Exception e) {
				log.debug("Could not suscribe listener");
			}
		}

	}

	private List<String> getKeywordsForListener(String topics) {
		try {
			topics = new String(topics.getBytes("iso-8859-1"), "utf8");
		} catch (UnsupportedEncodingException e) {
			log.debug("Problem decodifying keywords");
		}
		List<String> arrayKeywords = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(topics, ",");
		while (st.hasMoreTokens()) {
			arrayKeywords.add(st.nextToken().trim());
		}
		return arrayKeywords;
	}

}
