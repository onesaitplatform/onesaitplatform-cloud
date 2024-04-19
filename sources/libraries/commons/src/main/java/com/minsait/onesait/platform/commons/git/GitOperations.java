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

import java.util.List;

public interface GitOperations {

	public void unzipScaffolding(String directory, String path2Resource);

	public void createDirectory(String directory);

	public void configureGitlabAndInit(String user, String email, String directory);

	public void addOrigin(String url, String directory, boolean fetchAfterOrigin);

	public void addFile(String gitDirectory, String file);

	public void addAll(String directory);

	public void commit(String message, String directory);

	public void commit(String message, String directory, String author);

	public void push(String sshUrl, String username, String password, String branch, String directory, boolean mirror) throws GitSyncException;

	void push(String sshUrl, String username, String password, String branch, String directory, boolean mirror,
			boolean force) throws GitSyncException;

	public void sparseCheckoutAddPath(String path, String directory);

	public void sparseCheckoutConfig(String directory);

	public void checkout(String branch, String directory);

	public void checkout(String branch, String directory, boolean isNew);

	public void deleteDirectory(String directory);

	public void cloneRepository(String directory, GitlabConfiguration remoteConfig);

	public void cloneRepository(String directory, GitlabConfiguration remoteConfig, String branch);

	public void cloneRepository(String directory, String url, String user, String token, String branch);

	public void cloneRepository(String directory, String url, String user, String token, String branch,
			boolean cloneToSpecificDir);

	void createBranch(String branch, String directory);

	void createReadme(String content, String path2Resource);

	void configureGitAndInit(String user, String email, String directory);

	public void rollbackFile(String directory, String commitId, String relativeFilePath);

	public String showFileFromCommit(String directory, String commitId, String relativeFilePath);

	public void changeRemoteURL(String directory, String destinationURL);

	public void createTag(String directory, String tagName);

	public void pushTags(String directory) throws GitSyncException;

	public boolean checkTagIsValid(String tagName);

	public String getCurrentSHA(String directory);

	public List<String> getFilesChanged(String directory, String SHA1, String SHA2);

	public void pullWithNoPrompt(String directory);

}
