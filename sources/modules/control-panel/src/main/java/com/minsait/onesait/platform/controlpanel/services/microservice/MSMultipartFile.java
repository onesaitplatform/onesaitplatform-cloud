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
package com.minsait.onesait.platform.controlpanel.services.microservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class MSMultipartFile implements MultipartFile {

	private final byte[] fileContent;

	private String fileName;

	private File file;

	private String destPath = System.getProperty("java.io.tmpdir");

	private FileOutputStream fileOutputStream;

	public MSMultipartFile(byte[] fileData, String name) {
		this.fileContent = fileData;
		this.fileName = name;
		file = new File(destPath + fileName);
	}

	@Override
	public String getName() {
		return fileName;
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		fileOutputStream = new FileOutputStream(dest);
		fileOutputStream.write(fileContent);
	}

	public void clearOutStreams() throws IOException {
		if (null != fileOutputStream) {
			fileOutputStream.flush();
			fileOutputStream.close();
			file.deleteOnExit();
		}
	}

	@Override
	public byte[] getBytes() throws IOException {
		return fileContent;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(fileContent);
	}

	@Override
	public String getOriginalFilename() {
		return fileName;
	}

	@Override
	public String getContentType() {
		return "application/octet-stream";
	}

	@Override
	public boolean isEmpty() {
		return file.exists();
	}

	@Override
	public long getSize() {
		return file.length();
	}

}
