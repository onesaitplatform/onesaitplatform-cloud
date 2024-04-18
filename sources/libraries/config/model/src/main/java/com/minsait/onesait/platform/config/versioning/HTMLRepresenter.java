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
package com.minsait.onesait.platform.config.versioning;

import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

public class HTMLRepresenter extends Representer {

	public HTMLRepresenter() {
		representers.put(HTML.class, new YamlHTMLRepresenter());
		representers.put(String.class, new YamlStringRepresenter());
	}

	private class YamlHTMLRepresenter implements Represent {
		@Override
		public Node representData(Object data) {
			final HTML html = (HTML) data;
			String value = html.getHTMLContent();
			//replace tabs for 3 spaces & invalid linebreaks with spaces
			if(value != null) {
				value = value.replace("\t", "   ").replaceAll("\\r", "").replaceAll("([ ]+\\n)", "\n").replaceAll("([ ]+\\r\\n)", "\r\n");
				defaultScalarStyle = ScalarStyle.LITERAL;
				return representScalar(new Tag("!html"), value);
			}else {
				return representScalar(new Tag("!html"), "");
			}

		}
	}
	private class YamlStringRepresenter implements Represent {
		@Override
		public Node representData(Object data) {
			final String value = (String) data;
			defaultScalarStyle = ScalarStyle.PLAIN;
			return representScalar(Tag.STR, value);
		}
	}

}
