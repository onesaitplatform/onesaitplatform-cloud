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
package com.minsait.onesait.platform.streaming.twitter.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Stream;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.libraries.social.twitter.TwitterServiceFactory;
import com.minsait.onesait.platform.libraries.social.twitter.TwitterServiceSpringSocialImpl;
import com.minsait.onesait.platform.streaming.twitter.listener.TwitterStreamListener;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TwitterStreamService {

	@Autowired
	private ConfigurationService configurationService;

	private Map<String, TwitterStreamListener> listenersMap = new HashMap<String, TwitterStreamListener>();
	private Map<String, Stream> streamMap = new HashMap<String, Stream>();

	private TwitterServiceSpringSocialImpl getTwitterConfiguration(String configurationId) {

		// TODO get default config throw exception
		Configuration configuration = this.configurationService.getConfiguration(configurationId);
		TwitterConfiguration twitterConfiguration = this.configurationService
				.getTwitterConfiguration(configuration.getEnvironment(), configuration.getSuffix());
		return TwitterServiceFactory.getSpringSocialImpl(twitterConfiguration.getConsumerKey(),
				twitterConfiguration.getConsumerSecret(), twitterConfiguration.getAccessToken(),
				twitterConfiguration.getAccessTokenSecret());

	}

	public Stream subscribe(TwitterStreamListener twitterStreamListener) throws Exception {

		String listenerId = twitterStreamListener.getId();

		if (listenersMap.containsKey(listenerId))
			throw new Exception("Listener already exists");

		String keywords = "";
		for (String keyword : twitterStreamListener.getKeywords()) {
			keywords = keywords + keyword + ",";
		}
		if (keywords.equals(""))
			throw new Exception("No keywords found for this Listener");

		Stream stream = this.getTwitterConfiguration(twitterStreamListener.getConfigurationId())
				.createFilterStreaming(keywords, twitterStreamListener);
		twitterStreamListener.setTwitterStream(stream);

		log.info("Suscribed stream: " + stream.hashCode());
		listenersMap.put(listenerId, twitterStreamListener);
		streamMap.put(listenerId, stream);
		log.debug("Listener registered with id " + listenerId + ", keywords: " + keywords);

		return stream;
	}

	public void unsubscribe(String listenerId) throws Exception {
		TwitterStreamListener listener = listenersMap.get(listenerId);

		if (listener != null) {
			listener.closeStream();
			listenersMap.remove(listenerId);
			streamMap.remove(listenerId);

		} else
			throw new Exception("Error listener not found");
	}

	public boolean isSubscribe(String id) {
		if (listenersMap.containsKey(id)) {
			if (listenersMap.get(id).getTwitterStream() == null) {
				listenersMap.remove(id);
				streamMap.remove(id);
				return false;
			} else
				return true;
		} else
			return false;

	}

}
