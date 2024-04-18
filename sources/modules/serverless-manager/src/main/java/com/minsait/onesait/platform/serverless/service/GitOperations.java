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
package com.minsait.onesait.platform.serverless.service;

import com.minsait.onesait.platform.serverless.dto.git.GitlabConfiguration;

public interface GitOperations {

	public void unzipScaffolding(String directory, String path2Resource);

	public void createReadme(String content, String path2Resource);

	public void createDirectory(String directory) ;

	public void configureGitAndInit(String user, String email, String directory) ;

	public void configureGit(String user, String email, String directory) ;

	public void addOrigin(String url, String directory, boolean fetchAfterOrigin);

	public void addAll(String directory);

	public void commit(String message, String directory);

	public void push(String sshUrl, String username, String password, String branch, String directory, boolean mirror);

	public void sparseCheckoutAddPath(String path, String directory);

	public void sparseCheckoutConfig(String directory);

	public void checkout(String branch, String directory);

	public void createBranch(String branch, String directory);

	public void deleteDirectory(String directory);

	public void cloneRepository(String directory, GitlabConfiguration remoteConfig);
}
