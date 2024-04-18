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
package com.minsait.onesait.platform.repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.Ontology.RtdbDatasource;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.UserRepository;
import com.minsait.onesait.platform.config.services.ontology.OntologyService;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.GeoSpatialOpsService;
import com.minsait.onesait.platform.persistence.services.GeoSpatialOpsService.GeoQueries;
import com.minsait.onesait.platform.persistence.services.ManageDBPersistenceServiceFacade;
import com.minsait.onesait.platform.persistence.services.QueryToolService;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Ignore
public class GeoSpatialRepositoryTest {

	public final static String TEST_INDEX = "newdoc";
	public final static String TEST_INDEX_PIN = "newpin";
	public final static String TEST_INDEX_MONGO = TEST_INDEX + System.currentTimeMillis();
	public final static String TEST_INDEX_MONGO_AGNOSTIC = TEST_INDEX + System.currentTimeMillis() + "agnostic";
	public final static String TEST_INDEX_MONGO_PIN = TEST_INDEX_PIN + System.currentTimeMillis();

	private static User userAdministrator = null;

	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsFacade;

	@Autowired
	private ManageDBPersistenceServiceFacade manageFacade;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private UserRepository userCDBRepository;

	@Autowired
	QueryToolService queryToolService;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private GeoSpatialOpsService geoService;

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = this.userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	private String partial_polygon_agnostic = "partial_polygon_agnostic.json";

	public static String getString(String file) throws IOException {
		File in = new ClassPathResource(file).getFile();
		return FileUtils.readFileToString(in);
	}

	@Before
	public void doBefore() throws Exception {
		log.info("up process...");

		File in = new ClassPathResource("type.json").getFile();
		String TYPE = FileUtils.readFileToString(in);

		File data = new ClassPathResource("type_data.json").getFile();
		String DATA = FileUtils.readFileToString(data);

		doBeforeALL(TYPE, DATA, TEST_INDEX, RtdbDatasource.ELASTIC_SEARCH);

		in = new ClassPathResource("type_geo_point.json").getFile();
		TYPE = FileUtils.readFileToString(in);

		data = new ClassPathResource("type_geo_point_data.json").getFile();
		DATA = FileUtils.readFileToString(data);

		doBeforeALL(TYPE, DATA, TEST_INDEX_PIN, RtdbDatasource.ELASTIC_SEARCH);

		in = new ClassPathResource("type_geo_mongo.json").getFile();
		TYPE = FileUtils.readFileToString(in);

		data = new ClassPathResource("type_geo_point_data.json").getFile();
		DATA = FileUtils.readFileToString(data);

		doBeforeALL(TYPE, DATA, TEST_INDEX_MONGO_PIN, RtdbDatasource.MONGO);

		in = new ClassPathResource("type_mongo.json").getFile();
		TYPE = FileUtils.readFileToString(in);

		data = new ClassPathResource("type_data.json").getFile();
		DATA = FileUtils.readFileToString(data);
		doBeforeALL(TYPE, DATA, TEST_INDEX_MONGO, RtdbDatasource.MONGO);

		in = new ClassPathResource("type_geo_mongo.json").getFile();
		TYPE = FileUtils.readFileToString(in);

		data = new ClassPathResource("type_geo_point_data.json").getFile();
		DATA = FileUtils.readFileToString(data);
		doBeforeALL(TYPE, DATA, TEST_INDEX_MONGO_AGNOSTIC, RtdbDatasource.ELASTIC_SEARCH);

	}

	public void doBeforeALL(String TYPE, String DATA, String ontologyName, RtdbDatasource dataSource) throws Exception {
		log.info("doBefore4 up process...");

		try {
			Ontology ontology = new Ontology();
			ontology.setJsonSchema(TYPE);
			ontology.setIdentification(ontologyName);
			ontology.setDescription(ontologyName);
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setRtdbDatasource(dataSource);
			ontology.setUser(getUserAdministrator());

			Ontology index1 = ontologyService.getOntologyByIdentification(ontologyName,
					getUserAdministrator().getUserId());
			if (index1 == null) {
				try {
					ontologyService.createOntology(ontology, null);
				} catch (Exception e) {
				}

				try {
					manageFacade.createTable4Ontology(ontologyName, TYPE, null);
				} catch (Exception e) {
				}

			}

			String idES = basicOpsFacade.insert(ontologyName, DATA);
			log.info("doBefore4 inserted object with id " + idES);

		} catch (Exception e) {
			log.info("Issue creating table4ontology " + e);
		}

	}

	@Test
	public void testGeoServiceElasticWithin() {
		try {
			Thread.sleep(10000);
			log.info(">>>>>>>>>>>>> testGeoServiceElasticWithin");
			partial_polygon_agnostic = getString(partial_polygon_agnostic);

			log.info(basicOpsFacade.findAllAsJson(TEST_INDEX));

			log.info(basicOpsFacade.findAllAsJson(TEST_INDEX, 10));

			List<String> listQ = geoService.within(TEST_INDEX, partial_polygon_agnostic);

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceElasticWithin failure. " + e);
		}
	}

	@Test
	public void testGeoServiceMongoWithin() {
		try {
			log.info(">>>>>>>>>>>>> testGeoServiceMongoWithin");
			partial_polygon_agnostic = getString(partial_polygon_agnostic);

			List<String> listQ = geoService.within(TEST_INDEX_MONGO, partial_polygon_agnostic);

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceMongoWithin failure. " + e);
		}
	}

	@Test
	public void testGeoServiceElastic() {
		try {
			Thread.sleep(10000);
			log.info(">>>>>>>>>>>>> testGeoServiceElastic");
			partial_polygon_agnostic = getString(partial_polygon_agnostic);

			log.info(basicOpsFacade.findAllAsJson(TEST_INDEX));

			List<String> listQ = geoService.intersects(TEST_INDEX, partial_polygon_agnostic);

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceElastic failure. " + e);
		}
	}

	@Test
	public void testGeoServiceMongo() {
		try {
			log.info(">>>>>>>>>>>>> testGeoServiceMongo");
			partial_polygon_agnostic = getString(partial_polygon_agnostic);

			List<String> listQ = geoService.intersects(TEST_INDEX_MONGO, partial_polygon_agnostic);

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceMongo failure. " + e);
		}
	}

	@Test
	public void testGeoServiceNear() {
		try {
			Thread.sleep(10000);
			log.info(basicOpsFacade.findAllAsJson(TEST_INDEX_PIN));
			log.info(">>>>>>>>>>>>> testGeoServiceNear");
			String TWO_HUNDRED_KILOMETERS = "" + (1000 * 200);

			List<String> listQ = geoService.near(TEST_INDEX_PIN, TWO_HUNDRED_KILOMETERS, "40", "-70");

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceNear failure. " + e);
		}
	}

	@Test
	public void testGeoServiceNearMongo() {
		try {
			log.info(">>>>>>>>>>>>> testGeoServiceNearMongo");

			String TWO_HUNDRED_KILOMETERS = "" + (1000 * 200);
			List<String> listQ = geoService.near(TEST_INDEX_MONGO_PIN, TWO_HUNDRED_KILOMETERS, "40", "-70");

			log.info("result  " + listQ);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceNearMongo failure. " + e);
		}
	}

	@Test
	public void testGeoServiceByQueryTool() {
		try {
			log.info(">>>>>>>>>>>>> testGeoServiceByQueryTool");

			String TWO_HUNDRED_KILOMETERS = "" + (1000 * 200);
			List<String> listQ = geoService.near(TEST_INDEX_MONGO_PIN, TWO_HUNDRED_KILOMETERS, "40", "-70");
			log.info("result  " + listQ);
			String query = geoService.getQuery(GeoQueries.NEAR, TEST_INDEX_MONGO_PIN, "geometry", "40", "-70",
					TWO_HUNDRED_KILOMETERS);

			log.info(">>>>>>>>>>>>> USE of querytool with the GetQuery Generator of GeoService");

			String result = queryToolService.queryNativeAsJson(getUserAdministrator().getUserId(), TEST_INDEX_MONGO_PIN,
					query);

			log.info("result QueryTool " + result);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceNearMongo failure. " + e);
		}
	}

	@Test
	public void testGeoServiceByQueryToolAgnostic() {
		try {
			log.info(">>>>>>>>>>>>> testGeoServiceByQueryToolAgnostic");

			String TWO_HUNDRED_KILOMETERS = "" + (1000 * 200);
			List<String> listQ = geoService.near(TEST_INDEX_MONGO_AGNOSTIC, TWO_HUNDRED_KILOMETERS, "40", "-70");
			log.info("result  " + listQ);
			String query = geoService.getQuery(GeoQueries.NEAR, TEST_INDEX_MONGO_AGNOSTIC, "geometry", "40", "-70",
					TWO_HUNDRED_KILOMETERS);

			log.info(">>>>>>>>>>>>> USE of querytool with the GetQuery Generator of GeoService");

			String result = queryToolService.queryNativeAsJson(getUserAdministrator().getUserId(),
					TEST_INDEX_MONGO_AGNOSTIC, query);

			log.info("result QueryTool " + result);
			Assert.assertTrue(listQ != null);
		} catch (Exception e) {
			Assert.fail("testGeoServiceNearMongo failure. " + e);
		}
	}

}
