/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.controlpanel.services.datarefiner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import gmbh.dtap.refine.client.JsonOperation;
import gmbh.dtap.refine.client.Operation;
import gmbh.dtap.refine.client.RefineClient;
import gmbh.dtap.refine.client.RefineClients;
import gmbh.dtap.refine.client.UploadFormat;
import gmbh.dtap.refine.client.command.ApplyOperationsCommand;
import gmbh.dtap.refine.client.command.ApplyOperationsResponse;
import gmbh.dtap.refine.client.command.CreateProjectCommand;
import gmbh.dtap.refine.client.command.CreateProjectResponse;
import gmbh.dtap.refine.client.command.DeleteProjectCommand;
import gmbh.dtap.refine.client.command.DeleteProjectResponse;
import gmbh.dtap.refine.client.command.ExportRowsCommand;
import gmbh.dtap.refine.client.command.ExportRowsResponse;
import gmbh.dtap.refine.client.command.GetCsrfTokenCommand;
import gmbh.dtap.refine.client.command.GetCsrfTokenResponse;
import gmbh.dtap.refine.client.command.RefineCommands;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataRefinerServiceImpl implements DataRefinerService {

	@Autowired
	private IntegrationResourcesService resourcesService;

	private File convert(MultipartFile file) {
		File convFile = new File(file.getOriginalFilename());
		try {
			convFile.createNewFile();

			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convFile;
	}

	private String getDataCleanerUrl() {
		return resourcesService.getUrl(Module.DATACLEANERUI, ServiceUrl.EMBEDDED);
	}

	private String getOptionsForExcelFiles(File file) {
		JSONObject result = new JSONObject();
		try {
			Workbook wb = null;
			try {
				wb = FileMagic.valueOf(file) == FileMagic.OOXML ? new XSSFWorkbook(file)
						: new HSSFWorkbook(new POIFSFileSystem(file));

				int sheetCount = wb.getNumberOfSheets();

				JSONArray sheets = new JSONArray();

				for (int i = 0; i < sheetCount; i++) {
					Sheet sheet = wb.getSheetAt(i);
					int rows = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;

					JSONObject aSheet = new JSONObject();
					aSheet.put("name", file.getName() + "#" + sheet.getSheetName());
					aSheet.put("fileNameAndSheetIndex", file.getName() + "#" + i);
					aSheet.put("rows", rows);
					if (rows > 1) {
						aSheet.put("selected", true);
					} else {
						aSheet.put("selected", false);
					}
					sheets.put(aSheet);
				}
				result.put("sheets", sheets);
			} catch (InvalidFormatException e) {
				e.printStackTrace();
			} finally {
				if (wb != null) {
					wb.close();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	@Override
	public ResponseEntity<ByteArrayResource> makeProcessData(String operation, String exportType, String engine,
			String importOptions, MultipartFile file, String authorization, String typeAuthorization) {
		try (RefineClient client = RefineClients.create(getDataCleanerUrl())) {

			GetCsrfTokenCommand tokenCommand = RefineCommands.getCsrfToken().build();
			GetCsrfTokenResponse tokenResponse = tokenCommand.execute(client);

			// file json
			String fileName = file.getOriginalFilename().toLowerCase();
			CreateProjectCommand createProject;
			String projectname = new Date().getTime() + fileName;

			File fileConverted = convert(file);

			if (fileName.contains(".json")) {

				if (importOptions == null) {
					createProject = RefineCommands.createProject().name(projectname).file(fileConverted)
							.token(tokenResponse.getToken()).format(UploadFormat.JSON).authorization(authorization)
							.typeAuthorization(typeAuthorization)
							.options(() -> "{ \"encoding\": \"UTF-8\", \"recordPath\": [\"_\", \"_\"]}").build();
				} else {
					createProject = RefineCommands.createProject().name(projectname).file(fileConverted)
							.authorization(authorization).typeAuthorization(typeAuthorization)
							.token(tokenResponse.getToken()).format(UploadFormat.JSON).options(() -> importOptions)
							.build();
				}

			} else if (fileName.contains(".xlsx") || fileName.contains(".xls")) {
				if (importOptions == null) {
					String option = getOptionsForExcelFiles(fileConverted);

					createProject = RefineCommands.createProject().name(projectname).file(fileConverted)
							.authorization(authorization).typeAuthorization(typeAuthorization)
							.token(tokenResponse.getToken()).format(UploadFormat.EXCEL).options(() -> option).build();
				} else {
					createProject = RefineCommands.createProject().name(projectname).file(fileConverted)
							.authorization(authorization).typeAuthorization(typeAuthorization)
							.token(tokenResponse.getToken()).format(UploadFormat.EXCEL).options(() -> importOptions)
							.build();
				}

			} else {
				// file csv
				createProject = RefineCommands.createProject().name(projectname).file(fileConverted)
						.authorization(authorization).typeAuthorization(typeAuthorization)
						.token(tokenResponse.getToken()).build();
			}

			CreateProjectResponse createProjectResponse = createProject.execute(client);

			if (operation != null && operation.trim().length() > 0) {
				Operation oper = JsonOperation.from(operation);
				ApplyOperationsCommand applyOperationsCommand = RefineCommands.applyOperations()
						.project(createProjectResponse.getProjectId()).operations(oper).token(tokenResponse.getToken())
						.build();
				ApplyOperationsResponse applyOperationsResponse = applyOperationsCommand.execute(client);

			}
			// initialize engine parameter
			if (engine == null || engine.trim().length() == 0) {
				engine = "{\"facets\":[],\"mode\":\"row-based\"}";
			}
			ExportRowsCommand exportRowsCommand = RefineCommands.exportRows().engine(engine).format(exportType)
					.project(createProjectResponse.getProjectId()).token(tokenResponse.getToken()).build();
			ExportRowsResponse exportRowsResponse = exportRowsCommand.execute(client);

			DeleteProjectCommand deleteProjectCommand = RefineCommands.deleteProject()
					.project(createProjectResponse.getProjectId()).token(tokenResponse.getToken()).build();
			DeleteProjectResponse deleteProjectResponse = deleteProjectCommand.execute(client);

			final ByteArrayResource resource = new ByteArrayResource(
					FileUtils.readFileToByteArray(exportRowsResponse.getFile()));

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=".concat(exportRowsResponse.getFile().getName()))
					.contentLength(resource.contentLength())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (

		IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (Exception e1) {
			e1.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

}
