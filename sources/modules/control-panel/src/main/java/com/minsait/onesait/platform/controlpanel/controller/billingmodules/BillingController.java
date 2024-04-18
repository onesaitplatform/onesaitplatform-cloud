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
package com.minsait.onesait.platform.controlpanel.controller.billingmodules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.controlpanel.helper.billing.BillingHelper;
import com.minsait.onesait.platform.controlpanel.helper.billing.ModuleStatus;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/billingmodules")
@Slf4j
public class BillingController {

	@Autowired
	private BillingHelper billingHelper;
	
	@Autowired 
	private HttpSession httpSession;
	
	private static final String APP_ID = "appId";

	@Value("${onesaitplatform.binary-repository.tmp.file.path:/tmp/files/}")
	private String tmpDir;

	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request) {
		//CLEANING APP_ID FROM SESSION
		httpSession.removeAttribute(APP_ID);
		
		List<ModuleStatus> modules = billingHelper.getModuleStatus();
		model.addAttribute("modulesStatus", modules);
		model.addAttribute("numModulesActive",
				modules.stream().filter(m -> m.isStatus()).collect(Collectors.toList()).size());
		return "billingmodules/list";

	}

	@GetMapping(value = "downloadCSV")
	public ResponseEntity<InputStreamResource> download(Model model) {

		ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

		try {
			ObjectMapper mapper = new ObjectMapper();
			List<ModuleStatus> modules = billingHelper.getModuleStatus();
			JsonNode jsonTree = mapper.readTree(mapper.writeValueAsString(modules));
			List<String[]> csvData = new ArrayList<>();

			JsonNode firstObject = jsonTree.elements().next();
			List<String> headers = new ArrayList<>();
			firstObject.fieldNames().forEachRemaining(fieldName -> {
				headers.add(fieldName);
			});
			csvData.add(headers.toArray(new String[0]));
			Iterator<JsonNode> iterator = jsonTree.elements();
			while (iterator.hasNext()) {
				JsonNode obj = iterator.next();
				List<String> data = new ArrayList<>();
				obj.fields().forEachRemaining(field -> {
					JsonNode node = field.getValue();
					if (node.isObject())
						try {
							data.add(new ObjectMapper().writeValueAsString(field.getValue()));
						} catch (JsonProcessingException e) {
							log.error("Error parsing query result to CSV format to export", e);
						}
					else
						data.add(field.getValue().asText());
				});
				csvData.add(data.toArray(new String[0]));
			}

			final File file = createFile(tmpDir + File.separator + UUID.randomUUID());
			FileWriter outputfile = new FileWriter(file.getAbsolutePath() + File.separator + "ModuleStatus.csv");
			CSVWriter writer = new CSVWriter(outputfile, ';', CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
			writer.writeAll(csvData);
			writer.close();
			File finalFile = new File(file.getAbsolutePath() + File.separator + "ModuleStatus.csv");
			final HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			respHeaders.setContentDispositionFormData("attachment", finalFile.getName());
			respHeaders.setContentLength(finalFile.length());
			final InputStreamResource isr = new InputStreamResource(new FileInputStream(finalFile));
			deleteDirectory(finalFile);
			return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);

		} catch (IOException e) {
			log.error("Error parsing query result to CSV format to export", e);
			return null;
		}

	}

	private boolean deleteDirectory(File directoryToBeDeleted) {
		final File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (final File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private File createFile(String path) {

		log.info("New file is going to be generate on: " + path);

		final File file = new File(path);

		if (!file.exists()) {
			final Boolean success = file.mkdirs();
			if (!success) {
				log.error("Creating values file for deploy OP falied.");
				return null;
			}
		} else {
			log.error("Creating values file for deploy OP falied, the temporary directory don't exist: "
					+ file.getAbsolutePath());
			return null;
		}

		return file;

	}

}
