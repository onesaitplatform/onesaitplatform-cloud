/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.rtdbmaintainer.job;

import static org.mockito.Mockito.when;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.minsait.onesait.platform.commons.testing.IntegrationTest;
import com.minsait.onesait.platform.persistence.mongodb.template.MongoDbTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(IntegrationTest.class)
@Ignore
public class RtdbMaintainerJobTest {

	@Autowired
	RtdbMaintainerJob job;
	@Autowired
	private MongoDbTemplate mongoDbConnector;
	@Mock
	JobExecutionContext jobContext;
	@Mock
	JobDetail jobDetail;
	@Mock
	JobDataMap jobData;

	@Value("${onesaitplatform.database.mongodb.export.path:#{null}}")
	private String mongoExport;
	@Value("${onesaitplatform.database.mongodb.database:#{onesaitplatform_rtdb}}")
	private static final String MONGO_DATABASE = "onesaitplatform_rtdb";
	private static final String ONTOLOGY = "HelsinkiPopulation";

	@Before
	public void insertOntologies() {
		final String data = "{\"year\":1993,\"population\":7000,\"population_women\":4000,\"population_men\":3000,\"contextData\":{\"timestampMillis\":100}}";
		if (!mongoDbConnector.collectionExists(MONGO_DATABASE, ONTOLOGY))
			mongoDbConnector.createCollection(MONGO_DATABASE, ONTOLOGY);
		mongoDbConnector.insert(MONGO_DATABASE, ONTOLOGY, data);
		init_mocks();

	}

	public void init_mocks() {
		MockitoAnnotations.initMocks(this);
		when(jobContext.getJobDetail()).thenReturn(jobDetail);
		when(jobDetail.getJobDataMap()).thenReturn(jobData);
		when(jobData.getLong("timeout")).thenReturn((long) 40000);
		when(jobData.get("timeUnit")).thenReturn(TimeUnit.MINUTES);
		// when(this.auditProcessor.getEvent(Matchers.any(),
		// Matchers.any())).thenReturn(RtdbMaintainerAuditEvent.builder()
		// .id(UUID.randomUUID().toString()).timeStamp((new Date()).getTime())
		// .formatedTimeStamp(CalendarUtil.builder().build().convert(new
		// Date())).message("test message audit")
		// .user("administrator").module(Module.RTDBMAINTAINER).type(EventType.BATCH)
		// .operationType(OperationType.EXPORT.name()).resultOperation(ResultOperationType.SUCCESS).build());
	}

	@Test
	public void test_FileIsWritten_AndDataDeleted_Mongo() throws InterruptedException {
		job.execute(jobContext);
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-dd-MM-hh-mm");
		final String exportPath = mongoExport + ONTOLOGY + format.format(new Date()) + ".json";
		final File fileExport = new File(exportPath);
		Thread.sleep(5000);
		Assert.assertTrue(fileExport.exists());
		Assert.assertTrue(fileExport.length() > 0);
		final String query = "{\"contextData.timestampMillis\":{\"$eq\":100}}";
		Assert.assertTrue(mongoDbConnector.remove(MONGO_DATABASE, ONTOLOGY, query, false).getCount() == 0);

	}

}
