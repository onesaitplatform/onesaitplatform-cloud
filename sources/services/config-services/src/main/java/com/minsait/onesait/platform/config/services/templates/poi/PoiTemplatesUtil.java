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
package com.minsait.onesait.platform.config.services.templates.poi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.deepoove.poi.XWPFTemplate;
import com.minsait.onesait.platform.config.dto.report.ReportFormType;
import com.minsait.onesait.platform.config.dto.report.ReportParameter;
import fr.opensagres.xdocreport.converter.ConverterRegistry;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;

import fr.opensagres.xdocreport.converter.IConverter;

import fr.opensagres.xdocreport.converter.Options;

import fr.opensagres.xdocreport.converter.XDocConverterException;

import fr.opensagres.xdocreport.core.document.DocumentKind;

@Lazy
@Service
public class PoiTemplatesUtil {

	private final String PATH_TEMPLATE = "/tmp/";
	

	public List<String> extractFromDocx(InputStream is) throws OpenXML4JException, IOException {

		XWPFDocument document = new XWPFDocument(is);
		List<IBodyElement> documentPart = document.getBodyElements();
		List<String> parameters = new ArrayList<String>();

		for (IBodyElement bodyElement : documentPart) {

			if (bodyElement instanceof XWPFParagraph) {
				XWPFParagraph paragraph = (XWPFParagraph) bodyElement;

				if (paragraph.getText().length() > 1) {
					List<ReportParameter> paragraphParameterList = new ArrayList<>();
					paragraphParameterList = getElementsFromDocument(paragraph.getText());
					for (ReportParameter paragrapthElement : paragraphParameterList) {
						parameters.add(paragrapthElement.getName());
					}

				}

			} else if (bodyElement instanceof XWPFTable) {
				XWPFTable table = (XWPFTable) bodyElement;

				if (table.getText().length() > 1) {
					List<ReportParameter> tableParameterList = new ArrayList<>();
					tableParameterList = getElementsFromDocument(table.getText());
					for (ReportParameter tableElement : tableParameterList) {
						parameters.add(tableElement.getName());
					}
				}
			}
		}
		document.close();
		return parameters;

	}

	public List<Map<String, Object>> generateJSONObject(List<String> elements, ReportFormType formType) {

		String arrayName = "";

		List<Map<String, Object>> parametersJson = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < elements.size(); i++) {

			String element = elements.get(i);
			if (element.startsWith("{{?")) {
				arrayName = element.replace("{{?", "");
				arrayName = arrayName.replace("}}", "");
				Map<String, Object> elementObject = new HashMap<String, Object>();
				elementObject.put("name", arrayName);
				elementObject.put("value", "");

				List<String> listElements = new ArrayList<String>();
				for (int i2; i < elements.size(); i++) {
					String elementToCompare = elements.get(i);
					if (element.replace("?", "/").equals(elementToCompare)) {
						listElements.remove(0);
						if (ReportFormType.FIELDFORM == formType) {
							elementObject.put("childs", generateJSONObject(listElements, formType));
						} else {
							List<Map<String, Object>> valueArray = new ArrayList<Map<String, Object>>();
							Map<String, Object> props = new HashMap<String, Object>();
							props.put("props", generateJSONObject(listElements, formType));
							valueArray.add(props);
							elementObject.put("value", valueArray);
						}
						break;
					} else {
						listElements.add(elements.get(i));
					}
				}
				parametersJson.add(elementObject);

			} else if (element.startsWith("{{")) {

				String nameString = element.replace("{{", "");
				nameString = nameString.replace("}}", "");

				Map<String, Object> elementObject = new HashMap<String, Object>();
				elementObject.put("name", nameString);
				elementObject.put("value", "");

				parametersJson.add(elementObject);
			}
		}
		return parametersJson;
	}

	private List<ReportParameter> getElementsFromDocument(String element) {

		List<ReportParameter> tableList = new ArrayList<>();

		String[] parts = element.split("}}");
		for (String part : parts) {
			if (part.contains("{{?")) {
				ReportParameter reportParameter = new ReportParameter();
				String[] parts2 = part.split(Pattern.quote("{{?"));
				String elementPart = parts2[1];
				elementPart = "{{?" + elementPart + "}}";
				reportParameter.setName(elementPart);
				tableList.add(reportParameter);

			} else if (part.contains("/{{")) {

				ReportParameter reportParameter = new ReportParameter();
				String[] parts2 = part.split(Pattern.quote("{{/"));
				String elementPart = parts2[1];
				elementPart = "{{/" + elementPart + "}}";
				reportParameter.setName(elementPart);
				tableList.add(reportParameter);

			} else if (part.contains("{{")) {
				ReportParameter reportParameter = new ReportParameter();
				String[] parts2 = part.split(Pattern.quote("{{"));
				String elementPart = parts2[1];
				elementPart = "{{" + elementPart + "}}";
				reportParameter.setName(elementPart);
				tableList.add(reportParameter);
			}
		}
		return tableList;
	}

	public ReportFormType formType(List<String> fieldList) {

		boolean listOnList = false;
		for (String field : fieldList) {
			if (field.startsWith("{{?")) {
				if (listOnList == true) {
					break;
				}
				listOnList = true;
			} else if (field.startsWith("{{/")) {
				listOnList = false;
			}
		}

		if (listOnList == true) {
			return ReportFormType.valueOf("TEXTAREA");
		} else {
			return ReportFormType.valueOf("FIELDFORM");
		}

	}

	private Map<String, Object> generateJsonForReport(JSONArray strData) {

		Map<String, Object> datas = new HashMap<String, Object>();
		for (int i = 0; i < strData.length(); i++) {
			JSONObject item = strData.getJSONObject(i);
			try {
				JSONArray value = item.getJSONArray("value");
				List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
				for (int j = 0; j < value.length(); j++) {
					JSONArray ja = value.getJSONObject(j).getJSONArray("props");
					mapList.add(generateJsonForReport(ja));
				}
				datas.put(item.getString("name"), mapList);
			} catch (JSONException e) {
				datas.put(item.getString("name"), item.get("value"));
			}

		}
		return datas;
	}

	public String generateReport(String strData, byte[] file) throws IOException {

		InputStream report_template = new ByteArrayInputStream(file);

		JSONArray data = new JSONArray();
		if (strData.startsWith("[")) {
			data = new JSONArray(strData);

		} else {

			JSONObject obj = new JSONObject(strData);
			data = obj.getJSONArray("jsonParameters");
		}

		Map<String, Object> datas = generateJsonForReport(data);

		XWPFTemplate template = XWPFTemplate.compile(report_template);
		UUID uuid = UUID.randomUUID();
		String wordPath = PATH_TEMPLATE + uuid + ".docx";
		template.render(datas).writeToFile(wordPath);
		return wordPath;

	}

	public String convertToPdf(String wordPath) throws XDocConverterException, IOException {

		File inputWord = new File(wordPath);
		String pdfFilePath = wordPath;
		pdfFilePath = pdfFilePath.replace("docx", "pdf");
		File outputFile = new File(pdfFilePath);
		InputStream docxInputStream = new FileInputStream(inputWord);
		OutputStream outputStream = new FileOutputStream(outputFile);
		Options options = Options.getFrom(DocumentKind.DOCX).to(ConverterTypeTo.PDF);
		IConverter converter = ConverterRegistry.getRegistry().getConverter(options);
		converter.convert(docxInputStream, outputStream, options);
		outputStream.close();
		docxInputStream.close();
		return pdfFilePath;

	}
}
