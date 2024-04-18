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
package com.minsait.onesait.platform.config.repository;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.BinaryFile;
import com.minsait.onesait.platform.config.model.BinaryFileAccess;
import com.minsait.onesait.platform.config.model.BinaryFileAccess.Type;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)

public class BinaryFileRepositoryIntegrationTest {

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private BinaryFileRepository binaryRepo;
	@Autowired
	private BinaryFileAccessRepository binaryAccessRepo;

	private BinaryFile file;
	private BinaryFileAccess access;

	@Before
	public void setUp() {
		file = new BinaryFile();
		file.setId("1");
		file.setIdentification("example.pdf");
		file.setMime("application/pdf");
		file.setFileName("example.pdf");
		file.setPublic(false);
		file.setUser(userRepo.findByUserId("developer"));

		file.setFileExtension("pdf");

		// file.getFileAccesses().add(access);
		file = binaryRepo.save(file);

		access = new BinaryFileAccess();
		access.setAccessType(Type.WRITE);
		access.setBinaryFile(file);
		access.setUser(userRepo.findByUserId("user"));
		access = binaryAccessRepo.save(access);

	}

	@Test
	@Transactional
	public void getFile_ByAllowedUser() {
		Assert.assertTrue(binaryRepo.findByUser(userRepo.findByUserId("developer")).size() > 0);
	}

	@After
	public void tearOff() {
		binaryAccessRepo.delete(access);
		binaryRepo.delete(file);
	}
}
