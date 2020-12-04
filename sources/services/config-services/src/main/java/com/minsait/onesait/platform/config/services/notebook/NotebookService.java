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
package com.minsait.onesait.platform.config.services.notebook;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.minsait.onesait.platform.config.dto.NotebookForList;
import com.minsait.onesait.platform.config.model.Notebook;
import com.minsait.onesait.platform.config.model.NotebookUserAccess;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.notebook.dto.NotebookOspInfoDTO;

public interface NotebookService {

	public Notebook saveDBNotebook(String name, String idzep, User user);

	public Notebook createEmptyNotebook(String name, String userId);

	public Notebook importNotebook(String name, String data, String userId);

	public Notebook importNotebook(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations);

	public Notebook importNotebookFromJupyter(String name, String data, String userId);

	public Notebook importNotebookFromJupyter(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations);

	public Notebook cloneNotebook(String name, String idzep, String userId);

	public HttpHeaders exportHeaders(String notebookNameFile);

	public JSONObject exportNotebook(String id, String ususerIder);

	public void removeNotebook(String id, String userId);

	public String loginOrGetWSToken();

	public String loginOrGetWSTokenAdmin();

	public String loginOrGetWSTokenByBearer(String user, String bearertoken);

	public ResponseEntity<String> sendHttp(HttpServletRequest requestServlet, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> sendHttp(String url, HttpMethod httpMethod, String body, HttpHeaders headers)
			throws URISyntaxException, IOException;

	public Notebook getNotebook(String notebookId);

	public Notebook getNotebook(String identification, String userId);

	public Notebook getNotebookByZepId(String notebookZepId, String userId);

	public List<Notebook> getNotebooks(String userId);

	public boolean hasUserPermissionForNotebook(String zeppelinId, String userId);

	public ResponseEntity<String> runParagraph(String zeppelinId, String paragraphId, String bodyParams)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> runAllParagraphs(String zeppelinId) throws URISyntaxException, IOException;

	public ResponseEntity<String> getParagraphResult(String zeppelinId, String paragraphId)
			throws URISyntaxException, IOException;

	ResponseEntity<String> getAllParagraphStatus(String zeppelinId) throws URISyntaxException, IOException;

	public String cloneNotebookOnlyZeppelin(String nameClone, String notebookZepId, String userId);

	public boolean hasUserPermissionInNotebook(Notebook nt, String userId);

	List<NotebookUserAccess> getUserAccess(String notebookId);

	NotebookUserAccess createUserAccess(String notebookId, String userId, String accessType);

	List<String> createUserAccess(String notebookId, List<String> userIds, List<String> accessTypes);

	void deleteUserAccess(String notebookUserAccessId);

	void deleteUserAccess(NotebookUserAccess notebookUserAcc);

	List<String> deleteUserAccess(String notebookId, List<String> userIds, List<String> accessTypes);

	void changePublic(Notebook notebookId);

	public NotebookOspInfoDTO getOspInfoFromNotebook(String notebookJson);

	public NotebookOspInfoDTO getOspInfoFromDB(String notebookId);

	public void renameNotebook(String name, String idzep, String userId);

	public String notebookNameByIdZep(String idzep, String userId);

	public List<NotebookForList> getNotebooksAndByProjects(String userId);

	public boolean hasUserPermissionCreateNotebook(String userId);

	boolean isUserOwnerOfNotebook(User user, Notebook notebook);

	boolean isUserOwnerOfNotebook(String userId, Notebook notebook);

	public void removeNotebookByIdZep(String idZep, String user);

	public void removeNotebookOnlyZeppelin(String idZep, String user);

	public ResponseEntity<String> restartInterpreter(String interpreterName, String notebookId, User user)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> restartInterpreter(String interpreterName, String body)
			throws URISyntaxException, IOException;

	public ResponseEntity<String> restartAllInterpretersNotebook(String notebookId, String body, User user)
			throws URISyntaxException, IOException;

	String getParagraphOutputMessage(String zeppelinId, String paragraphId) throws URISyntaxException, IOException;

	public Map<String, String> getNotebookInterpreters(String notebookId) throws URISyntaxException, IOException;

	Notebook importNotebookData(String name, String data, String userId, boolean overwrite,
			boolean importAuthorizations);

}
