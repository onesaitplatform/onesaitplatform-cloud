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
package com.minsait.onesait.platform.controlpanel.controller.datamodel;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.PersistentObjectException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.minsait.onesait.platform.config.model.DataModel;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.services.datamodel.DataModelService;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

@RunWith(MockitoJUnitRunner.class)
public class DataModelControllerTest {

	@Mock
	private DataModelService dataModelService;

	@Mock
	private AppWebUtils utils;

	private MockMvc mockMvc;

	@InjectMocks
	private DataModelController dataModelController;

	@Before
	public void setup() {
		// Setup Spring test in standalone mode
		this.mockMvc = MockMvcBuilders.standaloneSetup(dataModelController).build();
	}

	@Test
	public void given_ThereAreTwoDataModels_When_TheyHaveToBeListedWithoutFilters_Then_AllOfThemAreListed()
			throws Exception {
		DataModel dm1 = new DataModel();
		DataModel dm2 = new DataModel();

		List<DataModel> allDataModels = new ArrayList<DataModel>(2);
		allDataModels.add(dm1);
		allDataModels.add(dm2);

		given(dataModelService.getAllDataModels()).willReturn(allDataModels);

		mockMvc.perform(get("/datamodels/list").param("dataModelId", "").param("name", "").param("description", ""))
				.andExpect(view().name("datamodels/list")).andExpect(model().attribute("dataModels", allDataModels));

	}

	@Test
	public void given_ThereIsOneDataModel_When_TheCorrectIdIsProvidedToShow_Then_TheDetailsOfTheDataModelIsShown()
			throws Exception {
		String userId = "administrator";
		User user = new User();
		user.setUserId(userId);

		DataModel dm = new DataModel();
		String id = "1";
		dm.setId(id);
		dm.setUser(user);

		given(dataModelService.getDataModelById(id)).willReturn(dm);
		given(utils.getUserId()).willReturn(userId);

		mockMvc.perform(get("/datamodels/show/" + id)).andExpect(view().name("datamodels/show"))
				.andExpect(model().attribute("dataModel", dm));

	}

	@Test
	public void given_ThereIsOneDataModel_When_AnInvalidIdIsProvidedToShow_Then_ItShowsView404() throws Exception {

		String id = "1";

		given(dataModelService.getDataModelById(id)).willReturn(null);

		mockMvc.perform(get("/datamodels/show/" + id)).andExpect(view().name("error/404"));
	}

	@Test
	public void given_ThereIsOneDataModel_When_ErrorInDataBase_Then_ItIsRedirectedToList() throws Exception {
		DataModel dm = new DataModel();
		String id = "1";
		dm.setId(id);

		doThrow(new PersistentObjectException("Any Database Error")).when(dataModelService).getDataModelById(id);

		mockMvc.perform(get("/datamodels/show/" + id)).andExpect(view().name("datamodels/list"));
	}

}
