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
package net.sf.jasperreports.data.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import net.sf.jasperreports.data.DataFile;

/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public interface HttpDataLocation extends DataFile
{

	RequestMethod getMethod();
	
	String getUrl();
	
	void setUrl(String url);
	public void setPostParameters(List<HttpLocationParameter> postParameters);
	public void setHeaders(List<HttpLocationParameter> headers);
	
	String getUsername();
	
	String getPassword();
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("urlParameter")
	List<HttpLocationParameter> getUrlParameters();
	
	String getBody();

	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("postParameter")
	List<HttpLocationParameter> getPostParameters();
	
	@JacksonXmlElementWrapper(useWrapping = false)
	@JsonProperty("header")
	List<HttpLocationParameter> getHeaders();

}
