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
package com.minsait.onesait.platform.controlpanel.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.yaml.snakeyaml.Yaml;

public class YamlHttpMessageConverter<T> extends AbstractHttpMessageConverter<T> {

	public YamlHttpMessageConverter() {
		super(new MediaType("application", "yaml"));
	}

	@Override
	protected boolean supports(Class<?> clazz) {

		return true;
	}

	@Override
	protected T readInternal(Class<? extends T> clazz, HttpInputMessage inputMessage) throws IOException {
		final Yaml yaml = new Yaml();
		return yaml.loadAs(inputMessage.getBody(), clazz);
	}

	@Override
	protected void writeInternal(T t, HttpOutputMessage outputMessage) throws IOException {
		final Yaml yaml = new Yaml();
		final OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody());
		yaml.dump(t, writer);
		writer.close();

	}

}
