/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.config.services.migration;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.config.model.Ontology;

@RunWith(SpringRunner.class)
@SpringBootTest
@Category(IntegrationTest.class)
public class MigrationUtilsTest {
	
	@Test
	@Transactional
	public void given_OneObjectWithJPAIdAnnotation_When_ItsIdIsRequested_Then_TheIdValueIsReturned() throws IllegalArgumentException, IllegalAccessException {
		Ontology ontology = new Ontology();
		ontology.setId("id");
		
		Serializable id = MigrationUtils.getId(ontology);
		assertTrue("The id should be returned", "id".equals(id));
	}
	
	@Test
	@Transactional
	public void given_OneObjectWithoutJPAIdAnnotation_When_ItsIdIsRequested_Then_NullIsReturned() throws IllegalArgumentException, IllegalAccessException {
		Object object = new Object();
		Serializable id = MigrationUtils.getId(object);
		assertTrue("The id should be null", id == null);
	}
	
	@Test
	@Transactional
	public void given_OneObjectWithParent_When_ItIsRequestedAllItsField_Then_AllFieldAreReturned() {
		
		Map<String, Field> allFields = MigrationUtils.getAllFields(Ontology.class);
		
		Field idField = allFields.get("id");
		assertTrue("Ontology class should have id field due to inheritance", idField != null);
	}
	
	@Test
	@Transactional
	public void given_OneJPAEntityObject_When_ItsIdTypeIsRequested_Then_TheIdTypeIsReturned() {		
		Class<?> idType = MigrationUtils.getIdType(Ontology.class);
		assertTrue("Ontology id should be of type String", String.class.equals(idType));
	}
	
	@Test
	@Transactional
	public void given_OneNonJPAEntityObject_When_IdTypeIsRequested_Then_NullIsReturned() {
		Class<?> idType = MigrationUtils.getIdType(Object.class);
		assertTrue("The id type of a non JPA Object should be null", idType == null);
	}
}
