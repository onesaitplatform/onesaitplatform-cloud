/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.libraries.social.twitter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.social.twitter.api.DirectMessage;
import org.springframework.social.twitter.api.SearchParameters;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.TwitterProfile;

import lombok.Getter;

public class TwitterServiceSpringSocialImpl {

	@Getter private Twitter twitter;
	
	protected TwitterServiceSpringSocialImpl(Twitter twitter) {
		this.twitter=twitter;
	}
	public TwitterProfile getProfile() {
		return this.twitter.userOperations().getUserProfile();		
	}
	public TwitterProfile getProfile(String name) {
		return this.twitter.userOperations().getUserProfile(name);		
	}
	public void updateStatus(String status) {
		twitter.timelineOperations().updateStatus(status);
	}
	public List<Tweet> getUserTimeLine() {
		return this.twitter.timelineOperations().getUserTimeline();		
	}
	public List<Tweet> getUserTimeLine(String user) {
		return this.twitter.timelineOperations().getUserTimeline(user);		
	}
	public void follow(String user) {
		twitter.friendOperations().follow(user);		
	}
	public void unfollow(String user) {
		twitter.friendOperations().unfollow(user);		
	}
	public List<TwitterProfile> getFollowers() {
		return this.twitter.friendOperations().getFollowers();	
	}
	public List<TwitterProfile> getFollowers(String user) {
		return this.twitter.friendOperations().getFollowers(user);	
	}
	public SearchResults search(String what) {
		return twitter.searchOperations().search(what);	
	}
	public SearchResults searchPage(String what,int page) {
		return twitter.searchOperations().search(what,page);	
	}
	public SearchResults search(String what,String lang) {
		SearchParameters params = new SearchParameters(what);
		params.lang(lang);
		return twitter.searchOperations().search(params);	
	}
	public SearchResults search(SearchParameters params) {
		return twitter.searchOperations().search(params);	
	}
	public void directMessage (String to, String what) {
		twitter.directMessageOperations().sendDirectMessage(to,what);
	}
	public List<DirectMessage> getDirectMessages() {
		return twitter.directMessageOperations().getDirectMessagesReceived();	
	}
	public Stream createFilterStreaming(String keywords,StreamListener listener) {
		List<StreamListener> listeners = new ArrayList<>();
		listeners.add(listener);
		return twitter.streamingOperations().filter(keywords, listeners);
		//return twitter.streamingOperations().sample(listeners);
	}
	
}
