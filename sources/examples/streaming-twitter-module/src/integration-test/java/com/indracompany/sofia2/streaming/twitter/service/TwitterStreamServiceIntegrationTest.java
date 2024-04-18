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
package com.indracompany.sofia2.streaming.twitter.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.components.TwitterConfiguration;
import com.minsait.onesait.platform.config.model.Configuration;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;
import com.minsait.onesait.platform.streaming.twitter.StreamingTwitterApplication;
import com.minsait.onesait.platform.streaming.twitter.listener.TwitterStreamListener;
import com.minsait.onesait.platform.streaming.twitter.service.TwitterStreamService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StreamingTwitterApplication.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = StreamingTwitterApplication.class)
@Category(IntegrationTest.class)
@Slf4j
@Ignore
public class TwitterStreamServiceIntegrationTest {

	@Autowired
	TwitterStreamService twitterStreamService;

	@MockBean
	ConfigurationService configurationService;

	private TwitterConfiguration twitterConfiguration;

	@Autowired
	private TwitterStreamListener twitterStreamListener;
	private final String accessToken = "74682827-D6cX2uurqpxy6yWlg6wioRl49f9Rtt2pEXUu6YNUy";
	private final String accessTokenSecret = "Cmd9XOX9N8xMRvlYUz3Wg49ZCGFnanMJvJPI9QMfTXix2";
	private final String consumerKey = "PWgCyepuon5U8X9HqfUtNpntq";
	private final String consumerSecret = "zo6rbSh6J470t7CCz4ZtXhHEFhpt36TMPKYolJgIiLOpEW9oc4";

	@Before
	public void setUp() {
		this.twitterConfiguration = new TwitterConfiguration();
		this.twitterConfiguration.setAccessToken(accessToken);
		this.twitterConfiguration.setAccessTokenSecret(accessTokenSecret);
		this.twitterConfiguration.setConsumerKey(consumerKey);
		this.twitterConfiguration.setConsumerSecret(consumerSecret);

		final List<String> keywords = new ArrayList<>();
		keywords.add("Helsinki");
		keywords.add("Borbones");
		keywords.add("Rajoy");

		// twitterStreamListener = Mockito.spy(new TwitterStreamListener());
		twitterStreamListener.setId(UUID.randomUUID().toString());
		twitterStreamListener.setOntology("TwitterOntology");
		twitterStreamListener.setClientPlatform("clientPlatform");
		twitterStreamListener.setToken(UUID.randomUUID().toString());
		twitterStreamListener.setKeywords(keywords);
		twitterStreamListener.setGeolocation(false);
		twitterStreamListener.setTimeout(60000);
		twitterStreamListener.setConfigurationId(UUID.randomUUID().toString());
	}

	@Test
	public void test_1_subscribe() throws Exception {
		when(configurationService.getConfiguration(any())).thenReturn(new Configuration());
		when(configurationService.getTwitterConfiguration(any(), any())).thenReturn(twitterConfiguration);

		Assert.assertNotNull(this.twitterStreamService.subscribe(twitterStreamListener));

	}

	@Ignore
	@Test
	public void test_2_isSubscribe() throws Exception {
		when(configurationService.getConfiguration(any())).thenReturn(new Configuration());
		when(configurationService.getTwitterConfiguration(any(), any())).thenReturn(twitterConfiguration);

		// this.twitterStreamService.subscribe(twitterStreamListener);
		Assert.assertTrue(this.twitterStreamService.isSubscribe(twitterStreamListener.getId()));

	}

	@Test
	public void test_3_unsubscribe() throws Exception {
		when(configurationService.getConfiguration(any())).thenReturn(new Configuration());
		when(configurationService.getTwitterConfiguration(any(), any())).thenReturn(twitterConfiguration);

		if (this.twitterStreamService.isSubscribe(twitterStreamListener.getId())) {
			this.twitterStreamService.unsubscribe(twitterStreamListener.getId());
		}

		Assert.assertTrue(!this.twitterStreamService.isSubscribe(twitterStreamListener.getId()));

	}

	@Test
	public void test_4_onTweet() throws Exception {
		when(configurationService.getConfiguration(any())).thenReturn(new Configuration());
		when(configurationService.getTwitterConfiguration(any(), any())).thenReturn(twitterConfiguration);
		// doNothing().when(twitterStreamListener).getSibSessionKey();
		// if(this.twitterStreamService.isSubscribe(twitterStreamListener.getId()))
		final Stream stream = this.twitterStreamService.subscribe(twitterStreamListener);

		// doNothing().when(twitterStreamListener).insertInstance(any());

		Thread.sleep(10000);

		final Tweet lastTweet = twitterStreamListener.getLastTweet();
		log.info("Last tweet by user:" + lastTweet.getFromUser() + ", text: " + lastTweet.getText());
		Assert.assertTrue(lastTweet.getText() != null && !lastTweet.getText().equals(""));
		try {
			stream.close();
		} catch (final Exception e) {
		}
	}
}
