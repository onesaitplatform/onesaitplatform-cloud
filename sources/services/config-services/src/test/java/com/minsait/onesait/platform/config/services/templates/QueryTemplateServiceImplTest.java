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
package com.minsait.onesait.platform.config.services.templates;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryTemplateServiceImplTest {

	@InjectMocks
	private QueryTemplateServiceImpl service;

	@Test
	public void given_AQueryTemplateAndASetOfVariables_When_TheVariablesAreReplaced_Then_AValidQueryTemplateIsGenerated() {

		String query = "var pipeline = [];\n" + "\n" + "pipeline.push(\n" + "    {\n" + "        $match: {\n"
				+ "          \"Test1.temperature.timestamp\": {\"$gte\" : ISODate(\"@var_from\"), \"$lt\" : ISODate(\"@var_to\") }\n"
				+ "        }\n" + "    }\n" + ");\n" + "db.getCollection(\"AssetTestArray2\").aggregate(pipeline);\n"
				+ "return \"db.AssetTestArray2.aggregate(\" + JSON.stringify(pipeline) + \")\";\n" + "";

		String newQuery = "var pipeline = [];\n" + "\n" + "pipeline.push(\n" + "    {\n" + "        $match: {\n"
				+ "          \"Test1.temperature.timestamp\": {\"$gte\" : ISODate(\"2017-01-01T00:00:00.000Z\"), \"$lt\" : ISODate(\"2017-01-02T00:00:00.000Z\") }\n"
				+ "        }\n" + "    }\n" + ");\n" + "db.getCollection(\"AssetTestArray2\").aggregate(pipeline);\n"
				+ "return \"db.AssetTestArray2.aggregate(\" + JSON.stringify(pipeline) + \")\";\n" + "";

		// It is important to take into account that the SQL parser use the @ in
		// variables to
		// identify them, but that character is not used as part of the name.
		Map<String, VariableData> variables = new HashMap<>();
		variables.put("var_from", new VariableData("var-from", VariableData.Type.STRING, "2017-01-01T00:00:00.000Z"));
		variables.put("var_to", new VariableData("var-to", VariableData.Type.STRING, "2017-01-02T00:00:00.000Z"));
		String resultQuery = service.replaceVariables(query, variables);
		assertTrue("The result query should have all the variables replaced", resultQuery.equals(newQuery));
	}

	@Test
	public void given_AQueryTemplate_When_TheQueryTemplateIsProcessed_Then_TheJavascriptCodeIsInterpreted()
			throws NoSuchMethodException, ScriptException {

		String query = "var query = \"db.AssetTestArray2.find({})\";\n" + "\n" + "return query;";

		String expectedQuery = "db.AssetTestArray2.find({})";
		String templateName = "something";
		String returnedQuery = service.processQuery(query, templateName);
		assertTrue("The query should be interpreted by javascript correctly", expectedQuery.equals(returnedQuery));

	}

	@Test
	public void given_AQueryTemplate_When_ItIsProcessed_Then_TheNewQueryWithVariableValuesIsGenerated()
			throws NoSuchMethodException, ScriptException {
		String query = "var pipeline = [];\n" + "\n" + "var detailedFrom = \"@var_from\";\n"
				+ "var from = detailedFrom.substring(0,detailedFrom.indexOf(\"T\"));\n" + "\n"
				+ "var detailedTo = \"@var_to\";\n" + "var to = detailedTo.substring(0,detailedTo.indexOf(\"T\"));\n"
				+ "\n" + "pipeline.push(\n" + "    {\n" + "        $match: {\n"
				+ "            \"Test1.temperature.timestamp\": {\"$gte\" : {\"$date\": from}, \"$lte\" : {\"$date\": to} }\n"
				+ "        }\n" + "    }\n" + ");\n" + "\n" + "pipeline.push(\n" + "    {\n" + "        $project: {\n"
				+ "          value: \"$Test1.temperature.values.values\",\n"
				+ "          doctimestamp: \"$Test1.temperature.timestamp\"\n" + "        }\n" + "    }\n" + ");\n"
				+ "\n" + "\n" + "pipeline.push(\n" + "    {\n"
				+ "        $unwind: { path: \"$value\", includeArrayIndex: \"h\" }\n" + "    }\n" + ");\n" + "\n"
				+ "if (\"@var_scala\" === \"m\" || \"@var_scala\" === \"s\"){\n" + "    pipeline.push(\n"
				+ "        {\n" + "            $unwind: { path: \"$value.values\", includeArrayIndex: \"m\" }\n"
				+ "        }\n" + "    );\n" + "}\n" + "\n" + "if (\"@var_scala\" === \"s\"){\n"
				+ "    pipeline.push(\n" + "        {\n"
				+ "            $unwind: { path: \"$value.values.values\", includeArrayIndex: \"s\" }\n" + "        }\n"
				+ "    );\n" + "}\n" + "\n" + "var hour = \"$h\";\n" + "var minute = 0;\n" + "var second = 0;\n"
				+ "var millisecond = 0;\n" + "\n" + "var value;\n" + "if (\"@var_scala\" === \"h\"){\n"
				+ "    value = { $divide: [ \"$value.sum\", \"$value.count\" ] };\n"
				+ "} else if (\"@var_scala\" === \"m\") {\n"
				+ "    value = { $divide: [ \"$value.values.sum\", \"$value.values.count\" ] };\n"
				+ "    minute = \"$m\";\n" + "} else if (\"@var_scala\" === \"s\") {\n"
				+ "    value = \"$value.values.values\";\n" + "    minute = \"$m\";\n" + "    second = \"$s\";\n"
				+ "}\n" + " \n" + "var project = {\n" + "    value: value,\n" + "    timestamp: {\n"
				+ "        $dateFromParts:{\n" + "            \"year\": {$year: \"$doctimestamp\"},\n"
				+ "            \"month\": {$month: \"$doctimestamp\"},\n"
				+ "            \"day\": {$dayOfMonth: \"$doctimestamp\"},\n" + "            \"hour\": hour,\n"
				+ "            \"minute\": minute,\n" + "            \"second\": second,\n"
				+ "            \"millisecond\": millisecond\n" + "        }\n" + "    }\n" + "}\n" + "\n"
				+ "pipeline.push(\n" + "    {\n" + "        $project: project\n" + "    }\n" + ");\n" + "\n"
				+ "pipeline.push(\n" + "    {\n" + "        $match: {\n"
				+ "            \"timestamp\": {\"$gte\" : {\"$date\": detailedFrom}, \"$lte\" : {\"$date\": detailedTo} }\n"
				+ "        }\n" + "    }\n" + ");\n" + "\n"
				+ "return \"db.AssetSignals.aggregate(\" + JSON.stringify(pipeline) + \")\";";

		Map<String, VariableData> variables = new HashMap<>();
		variables.put("var_from", new VariableData("var-from", VariableData.Type.STRING, "2017-01-01T03:00:00.000Z"));
		variables.put("var_to", new VariableData("var-to", VariableData.Type.STRING, "2017-01-02T16:00:00.000Z"));
		variables.put("var_scala", new VariableData("var_scala", VariableData.Type.STRING, "s"));
		String replacedQuery = service.replaceVariables(query, variables);

		String templateName = "something";
		String returnedQuery = service.processQuery(replacedQuery, templateName);

		System.out.println(returnedQuery);

	}
}
