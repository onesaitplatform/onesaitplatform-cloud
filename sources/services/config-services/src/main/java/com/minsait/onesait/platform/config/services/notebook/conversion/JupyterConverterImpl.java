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
package com.minsait.onesait.platform.config.services.notebook.conversion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.profiles.pegdown.Extensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.DataHolder;
import org.apache.zeppelin.jupyter.nbformat.Cell;
import org.apache.zeppelin.jupyter.nbformat.CodeCell;
import org.apache.zeppelin.jupyter.nbformat.DisplayData;
import org.apache.zeppelin.jupyter.nbformat.Error;
import org.apache.zeppelin.jupyter.nbformat.ExecuteResult;
import org.apache.zeppelin.jupyter.nbformat.HeadingCell;
import org.apache.zeppelin.jupyter.nbformat.MarkdownCell;
import org.apache.zeppelin.jupyter.nbformat.Nbformat;
import org.apache.zeppelin.jupyter.nbformat.Output;
import org.apache.zeppelin.jupyter.nbformat.RawCell;
import org.apache.zeppelin.jupyter.nbformat.Stream;
import org.apache.zeppelin.jupyter.zformat.Note;
import org.apache.zeppelin.jupyter.zformat.Paragraph;
import org.apache.zeppelin.jupyter.zformat.Result;
import org.apache.zeppelin.jupyter.zformat.TypeData;
import org.jline.utils.Log;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class JupyterConverterImpl implements JupyterConverter { 
	
	private RuntimeTypeAdapterFactory<Cell> cellTypeFactory = RuntimeTypeAdapterFactory.of(Cell.class, "cell_type")
			.registerSubtype(MarkdownCell.class, "markdown").registerSubtype(CodeCell.class, "code")
			.registerSubtype(RawCell.class, "raw").registerSubtype(HeadingCell.class, "heading");
	
	private RuntimeTypeAdapterFactory<Output> outputTypeFactory = RuntimeTypeAdapterFactory.of(Output.class, "output_type")
	        .registerSubtype(ExecuteResult.class, "execute_result")
	        .registerSubtype(DisplayData.class, "display_data").registerSubtype(Stream.class, "stream")
	        .registerSubtype(Error.class, "error");
	
	private DataHolder parserOptions = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL);
	private Parser markdownProcessor = Parser.builder(parserOptions).build();
	private HtmlRenderer htmlRender = HtmlRenderer.builder(parserOptions).build();
	
	private Gson getGson(GsonBuilder gsonBuilder) {
	    return gsonBuilder.registerTypeAdapterFactory(cellTypeFactory)
	        .registerTypeAdapterFactory(outputTypeFactory).create();
	  }
	
	public Nbformat getNotebookbformat(String in) {
	    return getNotebookbformat(in, new GsonBuilder());
	}
	
	public Nbformat getNotebookbformat(String in, GsonBuilder gsonBuilder) {
		return getGson(gsonBuilder).fromJson(in, Nbformat.class);
	}
	
	public Note getNoteFromJupyter(String in, String codeReplaced, String markdownReplaced) {
	    return getNoteFromGson(in, new GsonBuilder(), codeReplaced, markdownReplaced);
	}
	
	public Note getNoteFromGson(String in, GsonBuilder gsonBuilder, String codeReplaced,
	      String markdownReplaced) {
	    return getNoteFromStringCode(getNotebookbformat(in, gsonBuilder), codeReplaced, markdownReplaced);
	  }
	
	public Note getNoteFromStringCode(Nbformat nbformat, String codeReplaced, String markdownReplaced) {
	    Note note = new Note();

	    String name = nbformat.getMetadata().getTitle();
	    if (null == name) {
	      name = "Note converted from Jupyter";
	    }
	    note.setName(name);
	    
	    String lineSeparator = System.lineSeparator();
	    List<Paragraph> paragraphs;
	    paragraphs = getParagraphs(nbformat, codeReplaced, markdownReplaced, lineSeparator);
	    note.setParagraphs(paragraphs);

	    return note;
	  }

	private List<Paragraph> getParagraphs(Nbformat nbformat, String codeReplaced, String markdownReplaced, String lineSeparator) {
		List<Paragraph> paragraphs = new ArrayList<>();
		Paragraph paragraph;
		
		for (Cell cell : nbformat.getCells()) {
	      paragraph = getParagraph(codeReplaced, markdownReplaced, lineSeparator, cell);

	      paragraphs.add(paragraph);
	    }
		
		return paragraphs;
	}

	private Paragraph getParagraph(String codeReplaced, String markdownReplaced, String lineSeparator, Cell cell) {
		Paragraph paragraph;
		String interpreterName;
		List<TypeData> typeDataList;
		String status = Result.SUCCESS;
		paragraph = new Paragraph();
	    typeDataList = new ArrayList<>();
	    Object cellSource = cell.getSource();
	    List<String> sourceRaws = new ArrayList<>();
	
	    if (cellSource instanceof String) {
	    	sourceRaws.add((String) cellSource);
	    } else {
	    	sourceRaws.addAll((List<String>) cellSource);
	        
	    }
	
	    List<String> source = Output.verifyEndOfLine(sourceRaws);
	    String codeText = source.stream().map(String::valueOf).collect(Collectors.joining(""));
	
		if (cell instanceof CodeCell) {
			interpreterName = codeReplaced;
		    
		    status = getParagraphOutputs(typeDataList, cell, status);
		} else if (cell instanceof MarkdownCell || cell instanceof HeadingCell) {
		    interpreterName = markdownReplaced;
		    Node document = markdownProcessor.parse(codeText);
		    String markdownContent = htmlRender.render(document); 
		    typeDataList.add(new TypeData(TypeData.HTML, markdownContent));
		    paragraph.setUpMarkdownConfig(true);
		} else {
		    interpreterName = "";
		}
	
	    paragraph.setText(interpreterName + lineSeparator + codeText);
	    paragraph.setResults(new Result(status, typeDataList));
		return paragraph;
	}

	private String getParagraphOutputs(List<TypeData> typeDataList, Cell cell, String status) {
		for (Output output : ((CodeCell) cell).getOutputs()) {
		  if (output instanceof Error) {
		    typeDataList.add(output.toZeppelinResult());
		  } else {
			  try {
		        typeDataList.add(output.toZeppelinResult());
		        if (output instanceof Stream) {
		          Stream streamOutput = (Stream) output;
		          if (streamOutput.isError()) {
		            status = Result.ERROR;
		          }
		        }
			  }
			  catch (Exception e) {
				  Log.info("Error adding output paragraph - status: " + status);
			  }
		  }
		}
		return status;
	}
	
	public String jupyterNotebookToZeppelinNotebook(String jupyterData, String name) {
		String language = getLanguageFromData(jupyterData);
		
		String interpreter = "";
		if (language != null) {
			interpreter = "%" + language;
		}
		
		Note note = getNoteFromJupyter(jupyterData, interpreter, "%md");
		note.setName(name);
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    return gson.toJson(note);
	}
	
	private String getLanguageFromData(String data) {
		String language;
		try {
			JsonParser jsonParser = new JsonParser();
			JsonElement jsonTree = jsonParser.parse(data);
			JsonObject jsonObject = jsonTree.getAsJsonObject();
			JsonElement languageObject = jsonObject.getAsJsonObject("metadata").getAsJsonObject("kernelspec").get("language");
			language = languageObject.getAsString().toLowerCase();
		}
		catch (Exception e) {
			language = null;
		}
		return language;
		
		
	}
	
	
	
}
