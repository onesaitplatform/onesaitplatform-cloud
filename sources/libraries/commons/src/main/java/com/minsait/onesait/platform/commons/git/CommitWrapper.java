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
package com.minsait.onesait.platform.commons.git;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommitWrapper {

	@JsonProperty("sha")
	@JsonAlias("id")
	private String commitId;
	@JsonProperty("message")
	private String message;
	@JsonProperty("author_name")
	private String authorName;
	@JsonProperty("author_email")
	private String authorEmail;
	@JsonProperty("committer_email")
	private String commiterEmail;
	@JsonProperty("committer_name")
	private String committerName;
	private Commit commit = new Commit();

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Commit {
		private String message;
		private GitPerson author = new GitPerson();
		private GitPerson commiter = new GitPerson();

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		@JsonInclude(JsonInclude.Include.NON_NULL)
		public static class GitPerson {
			private String name;
			private String email;
		}

	}

	public void setMessage(String message) {
		this.message = message;
		commit.message = message;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
		commit.author.email = authorEmail;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
		commit.author.name = authorName;
	}

	public void setCommiterEmail(String commiterEmail) {
		this.commiterEmail = commiterEmail;
		commit.commiter.email = commiterEmail;
	}

	public void setCommitterName(String committerName) {
		this.committerName = committerName;
		commit.commiter.name = committerName;
	}
}
