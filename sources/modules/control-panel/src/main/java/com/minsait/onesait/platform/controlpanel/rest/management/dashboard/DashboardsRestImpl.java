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
package com.minsait.onesait.platform.controlpanel.rest.management.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.config.model.Category;
import com.minsait.onesait.platform.config.model.CategoryRelation;
import com.minsait.onesait.platform.config.model.Dashboard;
import com.minsait.onesait.platform.config.model.Subcategory;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.CategoryRelationRepository;
import com.minsait.onesait.platform.config.repository.CategoryRepository;
import com.minsait.onesait.platform.config.repository.DashboardRepository;
import com.minsait.onesait.platform.config.repository.SubcategoryRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

@Deprecated
@RestController
@EnableAutoConfiguration
public class DashboardsRestImpl implements DashboardsRest {

	@Autowired
	CategoryRelationRepository categoryRelationRepository;

	@Autowired
	DashboardRepository dashboardRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	SubcategoryRepository subcategoryRepository;

	@Value("${onesaitplatform.dashboardengine.url.view}")
	private String url;

	@Override
	public ResponseEntity<?> getByUser(@RequestParam(name = "userId", required = true) String userId,
			@RequestParam(name = "category", required = false) String category,
			@RequestParam(name = "subcategory", required = false) String subcategory) {
		try {
			User user = userRepository.findByUserId(userId);

			if (user != null) {

				List<Dashboard> dashboards = dashboardRepository.findByUser(user);
				if (category == null && subcategory == null && !dashboards.isEmpty()) {

					List<DashboardDTO> dashboardsResult = new ArrayList<DashboardDTO>();

					for (Dashboard d : dashboards) {
						CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(d.getId());
						if (categoryRelation != null) {
							Category c = categoryRepository.findById(categoryRelation.getCategory());
							Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

							dashboardsResult.add(DashboardDTO.builder().identification(d.getIdentification())
									.user(d.getUser().getUserId()).url(url + d.getId()).category(c.getIdentification())
									.subcategory(subc.getIdentification()).createdAt(d.getCreatedAt().toString())
									.modifiedAt(d.getUpdatedAt().toString()).viewUrl(url + d.getId()).build());
						} else {
							dashboardsResult.add(DashboardDTO.builder().identification(d.getIdentification())
									.user(d.getUser().getUserId()).url(url + d.getId()).category(null).subcategory(null)
									.createdAt(d.getCreatedAt().toString()).modifiedAt(d.getUpdatedAt().toString())
									.viewUrl(url + d.getId()).build());
						}
					}
					return new ResponseEntity<>(dashboardsResult, HttpStatus.OK);
				} else if (category != null && subcategory == null) {
					List<DashboardDTO> dashboardsResult = new ArrayList<DashboardDTO>();
					for (Dashboard d : dashboards) {
						CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(d.getId());
						if (categoryRelation != null) {

							Category c = categoryRepository.findById(categoryRelation.getCategory());
							Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

							if (category.equalsIgnoreCase(c.getIdentification())) {
								dashboardsResult.add(DashboardDTO.builder().identification(d.getIdentification())
										.user(d.getUser().getUserId()).url(url + d.getId())
										.category(c.getIdentification()).subcategory(subc.getIdentification())
										.createdAt(d.getCreatedAt().toString()).modifiedAt(d.getUpdatedAt().toString())
										.viewUrl(url + d.getId()).build());
							}
						}
					}
					return new ResponseEntity<>(dashboardsResult, HttpStatus.OK);
				} else if (category != null && subcategory != null) {
					List<DashboardDTO> dashboardsResult = new ArrayList<DashboardDTO>();
					for (Dashboard d : dashboards) {
						CategoryRelation categoryRelation = categoryRelationRepository.findByTypeId(d.getId());
						if (categoryRelation != null) {

							Category c = categoryRepository.findById(categoryRelation.getCategory());
							Subcategory subc = subcategoryRepository.findById(categoryRelation.getSubcategory());

							if (category.equalsIgnoreCase(c.getIdentification())
									&& subcategory.equalsIgnoreCase(subc.getIdentification())) {
								dashboardsResult.add(DashboardDTO.builder().identification(d.getIdentification())
										.user(d.getUser().getUserId()).url(url + d.getId())
										.category(c.getIdentification()).subcategory(subc.getIdentification())
										.createdAt(d.getCreatedAt().toString()).modifiedAt(d.getUpdatedAt().toString())
										.viewUrl(url + d.getId()).build());
							}
						}
					}
					return new ResponseEntity<>(dashboardsResult, HttpStatus.OK);
				} else {
					List<DashboardDTO> dashboardsResult = new ArrayList<DashboardDTO>();
					return new ResponseEntity<>(dashboardsResult, HttpStatus.OK);
				}

			} else {
				return new ResponseEntity<>("User is not found.", HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
