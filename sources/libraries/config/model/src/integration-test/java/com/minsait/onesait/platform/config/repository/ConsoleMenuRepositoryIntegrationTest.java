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

import java.util.List;

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
import com.minsait.onesait.platform.config.model.ConsoleMenu;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Category(IntegrationTest.class)
public class ConsoleMenuRepositoryIntegrationTest {

	@Autowired
	ConsoleMenuRepository repository;

	@Before
	public void setUp() {

		List<ConsoleMenu> menus = this.repository.findAll();

		if (menus.isEmpty()) {
			log.info("No menu elements found...adding");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("menu_category_ontologias_label");
			menu.setJson(
					"{'menu':'Sofia4Cities','rol':'ROL_ADMINISTRADOR','noSession':'/console/login','navigation':[{'title':{'EN':'Ontologies','ES':'Ontologías'},'icon':'flaticon-network','url':'','submenu':[{'title':{'EN':'Create Ontology','ES':'Crear Ontología'},'icon':'','url':'/controlpanel/ontologies/create'},{'title':{'EN':'Ontologies Status','ES':'Estado de Cargas'},'icon':'','url':'/controlpanel/ontologies/list'},{'title':{'EN':'My Ontologies','ES':'Mis Ontologías'},'icon':'','url':'/controlpanel/ontologies/list'},{'title':{'EN':'Real-Time Instance Generator','ES':'Simulador Tiempo-Real Ontologías'},'icon':'','url':'/controlpanel/generadorinstancias/list'},{'title':{'EN':'Ontologies Authorization','ES':'Autorizaciones de Ontología'},'icon':'','url':'/controlpanel/ontologies/authorize/list'}]},{'title':{'EN':'Sofia2 THINKPs','ES':'THINKPs Sofia2'},'icon':'flaticon-share','url':'','submenu':[{'title':{'EN':'My THINKPs','ES':'Mis THINKPs'},'icon':'','url':'/kps/list'},{'title':{'EN':'THINKPs Status','ES':'Estado de THINKPs'},'icon':'','url':'/gestiondispositivos/list'},{'title':{'EN':'THINKPs Container','ES':'Contenedor de THINKPs'},'icon':'','url':'/contenedorkps/list'}]},{'title':{'EN':'Visualization','ES':'Visualización'},'icon':'flaticon-dashboard','url':'','submenu':[{'title':{'EN':'My Gadgets','ES':'Mis Gadgets'},'icon':'','url':'/gadget/list'},{'title':{'EN':'My Dashboards','ES':'Mis Dashboards'},'icon':'','url':'/dashboard/listgroup'}]},{'title':{'EN':'Tools','ES':'Herramientas'},'icon':'flaticon-open-box','url':'','submenu':[{'title':{'EN':'RTDB and HDB Console','ES':'Consola DBTR y BDH'},'icon':'','url':'/databases/show'}]},{'title':{'EN':'Administration','ES':'Administración'},'icon':'flaticon-cogwheel-2','url':'','submenu':[{'title':{'EN':'Ontology Templates','ES':'Plantillas de Ontologías'},'icon':'','url':'/plantillas/list'},{'title':{'EN':'Users','ES':'Usuarios'},'icon':'','url':'/usuarioses/list'},{'title':{'EN':'Connections','ES':'Conexiones'},'icon':'','url':'/gestionconexiones/show'}]},{'title':{'EN':'Social Media','ES':'Social Media'},'icon':'la la-globe','url':'','submenu':[{'title':{'EN':'Social Media','ES':'Social Media'},'icon':'','url':'/socialMedia/socialMedia'},{'title':{'EN':'Access Settings','ES':'Configuración de Acceso'},'icon':'','url':'/configuracionRRSS/list'},{'title':{'EN':'Schedule Twitter Streaming Search','ES':'Programación Streaming Twitter'},'icon':'','url':'/usuariosTwitter/scheduledSearch'}]},{'title':{'EN':'BOTs','ES':'BOTs'},'icon':'la la-cubes','url':'','submenu':[{'title':{'EN':'My Bots','ES':'Mis Bots'},'icon':'','url':'/bots/list'},{'title':{'EN':'My Knowledge Bases','ES':'Bots Scripts'},'icon':'','url':'/bots/scripts/list'}]}]}");
			;
			this.repository.save(menu);
		}
	}

	@Test
	@Transactional
	public void given_SomeConsoleMenusExist_When_ItIsSearchedById_Then_TheCorrectObjectIsReturned() {
		ConsoleMenu menu = this.repository.findAll().get(0);
		Assert.assertTrue(this.repository.findById(menu.getId()) != null);
	}

}
