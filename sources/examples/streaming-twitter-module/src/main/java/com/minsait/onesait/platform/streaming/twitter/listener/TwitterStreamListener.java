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
package com.minsait.onesait.platform.streaming.twitter.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.TwitterListening;
import com.minsait.onesait.platform.config.services.twitter.TwitterListeningService;
import com.minsait.onesait.platform.streaming.twitter.persistence.PeristenceService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component("prototype")
@EnableScheduling
@Slf4j
public class TwitterStreamListener implements StreamListener {

	@Autowired
	private PeristenceService peristenceService;
	@Autowired
	private TwitterListeningService twitterListeningService;

	@Getter
	@Setter
	private List<String> keywords;
	@Getter
	@Setter
	private String user;
	@Getter
	@Setter
	private boolean geolocation;
	@Getter
	@Setter
	private int timeout;
	@Getter
	@Setter
	private String ontology;
	@Getter
	@Setter
	private String clientPlatform;
	@Getter
	@Setter
	private String token;
	@Getter
	@Setter
	private String id;
	@Getter
	@Setter
	private String configurationId;

	@Getter
	@Setter
	private String sessionKey;

	@Getter
	@Setter
	private ExecutorService executor;
	@Getter
	@Setter
	private Runnable tweetInsert;
	private static final int THREADS = 10;
	@Getter
	private LinkedBlockingQueue<Tweet> tweetsQueue;
	// Just for debug
	@Getter
	private Tweet lastTweet;
	@Getter
	@Setter
	private Stream twitterStream;

	private static final int TIME_SCHEDULING = 60000;
	private static final int QUEUE_LENGTH = 25;
	private static final String A_PATTERN = "<a href=\"(.*)\" rel=\"(.*)\">(.*)</a>";
	private static final String MONGO_DATE_FORMAT = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String ONTOLOGY_INSTANCE_PATTERN = "{'Tweet': {" + " 'timestamp': {'$date':'%s'},"
	// + " 'geometry': {'type': 'Point', 'coordinates': [ %s, %s ]},"
			+ " 'tweet_id': '%s'," + " 'tweet_createdat': {'$date':'%s'}," + " 'tweet_source': '%s',"
			+ " 'tweet_text': '%s'," + " 'tweet_user_id': '%s'," + " 'tweet_user_screenname': '%s',"
			+ " 'tweet_user_name': '%s'," + " 'tweet_user_profileimage': '%s'," + " 'tweet_favoritecount': '%s',"
			+ " 'tweet_retweetcount': '%s'" + "}}";

	public TwitterStreamListener() {

		tweetsQueue = new LinkedBlockingQueue<Tweet>();
		executor = Executors.newFixedThreadPool(THREADS);
		tweetInsert = defineMonitoringRunnable();
		for (int i = 0; i < THREADS; i++) {
			executor.execute(tweetInsert);
		}
	}

	@Override
	public void onTweet(Tweet tweet) {

		try {
			log.info("New Tweet, Stream: " + this.twitterStream.hashCode());
			this.lastTweet = tweet;

			/* Review each thread, if it is shutted down, then reactivate */
			if (executor instanceof ThreadPoolExecutor && ((ThreadPoolExecutor) executor).getActiveCount() < THREADS) {
				for (int i = 0; i < (THREADS - ((ThreadPoolExecutor) executor).getActiveCount()); i++) {
					executor.execute(tweetInsert);
				}
			}

			/* Add to queue */
			if (tweetsQueue.size() < QUEUE_LENGTH) {
				tweetsQueue.add(tweet);
			}

		} catch (Exception e) {
			log.debug("Error on status: " + e.getMessage());
		}

	}

	@Override
	public void onDelete(StreamDeleteEvent deleteEvent) {
		// TODO
	}

	@Override
	public void onLimit(int numberOfLimitedTweets) {
		// TODO
	}

	@Override
	public void onWarning(StreamWarningEvent warningEvent) {
		// TODO Auto-generated method stub

	}

	public void insertInstance(String instance) {
		try {
			instance = instance.replaceAll("'", "\"");
			this.peristenceService.insertOntologyInstance(instance, this.getOntology(), user, this.clientPlatform,
					this.clientPlatform + ":twitterStream");
		} catch (Exception e) {
			log.debug("Error inserting tweet : " + this.getOntology() + ". Cause: " + e.getMessage(), e);
		}

	}

	class ListenerThread implements Runnable {

		public ListenerThread() {

		}

		@Override
		public void run() {
			while (true) {
				if (tweetsQueue.size() > 0) {

					Tweet tweet = tweetsQueue.poll();
					if (tweet != null) {

						// insertTweet
						// String foundLongitude = "0.0";
						// String foundLatitude = "0.0";
						/* Ontology instance generation */
						SimpleDateFormat sdf_mongo = new SimpleDateFormat(MONGO_DATE_FORMAT);
						Pattern pattern = Pattern.compile(A_PATTERN);
						Matcher matcher = pattern.matcher(tweet.getSource());
						String processed_source = "";
						if (matcher.find()) {
							processed_source = matcher.group(1);
						}
						String instance = String.format(ONTOLOGY_INSTANCE_PATTERN,
								sdf_mongo.format(new Date()).toString(),
								// (tweet.getExtraData()== null ? foundLongitude
								// :
								// Double.toString(tweet.getGeoLocation().getLongitude())),
								// (tweet.getGeoLocation() == null ?
								// foundLatitude :
								// Double.toString(tweet.getGeoLocation().getLatitude())),
								Long.toString(tweet.getId()), sdf_mongo.format(tweet.getCreatedAt()).toString(),
								processed_source,
								tweet.getText().replace("%", "%25").replace("\\", "").replace("'", "%27")
										.replace("\"", "%22").replace("\r", " ").replace("\n", " "),
								Long.toString(tweet.getUser().getId()),
								tweet.getUser().getScreenName().replace("%", "%25").replace("\\", "")
										.replace("'", "%27").replace("\"", "%22"),
								tweet.getUser().getName().replace("%", "%25").replace("\\", "").replace("'", "%27")
										.replace("\"", "%22"),
								tweet.getUser().getProfileImageUrl(), Integer.toString(tweet.getFavoriteCount()),
								Integer.toString(tweet.getRetweetCount()));
						/* Se inserta el tweet */
						insertInstance(instance);

					}
				}
				try {
					Thread.sleep(150);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private Runnable defineMonitoringRunnable() {
		return new ListenerThread();
	}

	public void closeStream() {
		try {
			this.twitterStream.close();
		} catch (Exception e) {
			log.info("Error while killing threaded stream, non critic.");
		} finally {
			this.twitterStream = null;
		}

	}

	@Scheduled(fixedDelay = TIME_SCHEDULING)
	private void stopStreamIfListeningIsDeleted() {
		if (this.twitterStream != null) {
			TwitterListening twitterListening = this.twitterListeningService.getListenById(this.id);
			// Check if currently scheduled twitter listening was deleted from control panel
			if (twitterListening == null) {
				// If twitter listening was unscheduled then kill stream thread

				log.info("Detected deleted listening, closing stream...");
				this.closeStream();

			} else {
				if (twitterListening.getDateTo().getTime() < System.currentTimeMillis()
						|| twitterListening.getDateFrom().getTime() > System.currentTimeMillis()) {
					log.info("This listening should have already ended, closing stream...");
					this.closeStream();
				}
			}

		}

	}

}
