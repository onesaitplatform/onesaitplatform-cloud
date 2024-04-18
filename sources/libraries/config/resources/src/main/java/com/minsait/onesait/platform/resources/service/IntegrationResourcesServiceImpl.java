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
package com.minsait.onesait.platform.resources.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.commons.ActiveProfileDetector;
import com.minsait.onesait.platform.config.components.GlobalConfiguration;
import com.minsait.onesait.platform.config.components.Urls;
import com.minsait.onesait.platform.config.services.configuration.ConfigurationService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IntegrationResourcesServiceImpl implements IntegrationResourcesService {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private ActiveProfileDetector profileDetector;

	private String profile;

	private Urls urls;
	@Getter
	private GlobalConfiguration globalConfiguration;

	public enum ServiceUrl {
		BASE, ADVICE, ROUTER, HAWTIO, SWAGGERUI, API, SWAGGERUIMANAGEMENT, SWAGGERJSON, EMBEDDED, UI, GATEWAY, MANAGEMENT
	}

	public enum Module {
		IOTBROKER("iotbroker"), SCRIPTINGENGINE("scriptingEngine"), FLOWENGINE("flowEngine"), ROUTERSTANDALONE(
				"routerStandAlone"), APIMANAGER("apiManager"), CONTROLPANEL("controlpanel"), DIGITALTWINBROKER(
						"digitalTwinBroker"), DOMAIN("domain"), MONITORINGUI(
								"monitoringUI"), RULES_ENGINE("rulesEngine");

		String moduleString;

		Module(String module) {
			moduleString = module;
		}

		String getModule() {
			return moduleString;
		}
	}

	public static final String SWAGGER_UI_SUFFIX = "swagger-ui.html";
	public static final String LOCALHOST = "localhost";

	@PostConstruct
	public void getActiveProfile() {

		profile = profileDetector.getActiveProfile();
		loadConfigurations();
	}

	@Override
	public String getUrl(Module module, ServiceUrl service) {
		try {
			switch (module) {
			case CONTROLPANEL:
				if (service.equals(ServiceUrl.BASE))
					return urls.getControlpanel().getBase();
				break;
			case IOTBROKER:
				switch (service) {
				case BASE:
					return urls.getIotbroker().getBase();
				case ADVICE:
					return urls.getIotbroker().getAdvice();
				default:
					break;
				}
				break;
			case SCRIPTINGENGINE:
				switch (service) {
				case BASE:
					return urls.getScriptingEngine().getBase();
				case ADVICE:
					return urls.getScriptingEngine().getAdvice();
				default:
					break;
				}
				break;
			case FLOWENGINE:
				switch (service) {
				case BASE:
					return urls.getFlowEngine().getBase();
				case ADVICE:
					return urls.getFlowEngine().getAdvice();
				default:
					break;
				}
				break;
			case ROUTERSTANDALONE:
				switch (service) {
				case BASE:
					return urls.getRouterStandAlone().getBase();
				case ADVICE:
					return urls.getRouterStandAlone().getAdvice();
				case MANAGEMENT:
					return urls.getRouterStandAlone().getManagement();
				case ROUTER:
					return urls.getRouterStandAlone().getRouter();
				case HAWTIO:
					return urls.getRouterStandAlone().getHawtio();
				case SWAGGERUI:
					return urls.getRouterStandAlone().getSwaggerUI();
				default:
					break;
				}
				break;
			case APIMANAGER:
				switch (service) {
				case BASE:
					return urls.getApiManager().getBase();
				case API:
					return urls.getApiManager().getApi();
				case SWAGGERUI:
					return urls.getApiManager().getSwaggerUI();
				case SWAGGERUIMANAGEMENT:
					return urls.getApiManager().getSwaggerUIManagement();
				case SWAGGERJSON:
					return urls.getApiManager().getSwaggerJson();

				default:
					break;
				}
				break;
			case DIGITALTWINBROKER:
				if (service.equals(ServiceUrl.BASE))
					return urls.getDigitalTwinBroker().getBase();

				break;
			case DOMAIN:
				if (service.equals(ServiceUrl.BASE))
					return urls.getDomain().getBase();
				break;
			case MONITORINGUI:
				switch (service) {
				case BASE:
					return urls.getMonitoringUI().getBase();
				case EMBEDDED:
					return urls.getMonitoringUI().getEmbedded();
				default:
					break;
				}
				break;
			case RULES_ENGINE:
				switch (service) {
				case ADVICE:
					return urls.getRulesEngine().getAdvice();
				case BASE:
					return urls.getRulesEngine().getBase();
				default:
					break;
				}
				break;
			default:
				break;
			}
		} catch (

		final Exception e) {
			log.error("Error : {}", e);
		}
		return "RESOURCE_URL_NOT_FOUND";
	}

	@Override
	public Map<String, String> getSwaggerUrls() {
		final Map<String, String> map = new HashMap<>();
		final String base = urls.getDomain().getBase();
		String controlpanel = base.endsWith("/") ? base.concat("controlpanel") : base.concat("/controlpanel");
		String iotbroker = base.endsWith("/") ? base.concat("iot-broker") : base.concat("/iot-broker");
		String apimanager = base.endsWith("/") ? base.concat("api-manager") : base.concat("/api-manager");
		String router = base.endsWith("/") ? base.concat("router") : base.concat("/router");
		String digitalTwinBroker = base.endsWith("/") ? base.concat("digitaltwinbroker")
				: base.concat("/digitaltwinbroker");
		if (base.contains(LOCALHOST)) {
			controlpanel = urls.getControlpanel().getBase();
			iotbroker = urls.getIotbroker().getBase();
			apimanager = urls.getApiManager().getBase();
			router = urls.getRouterStandAlone().getBase();
			digitalTwinBroker = urls.getDigitalTwinBroker().getBase();
		}
		map.put(Module.CONTROLPANEL.getModule(), controlpanel.endsWith("/") ? controlpanel.concat(SWAGGER_UI_SUFFIX)
				: controlpanel.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.IOTBROKER.getModule(), iotbroker.endsWith("/") ? iotbroker.concat(SWAGGER_UI_SUFFIX)
				: iotbroker.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.APIMANAGER.getModule(), apimanager.endsWith("/") ? apimanager.concat(SWAGGER_UI_SUFFIX)
				: apimanager.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.ROUTERSTANDALONE.getModule(),
				router.endsWith("/") ? router.concat(SWAGGER_UI_SUFFIX) : router.concat("/").concat(SWAGGER_UI_SUFFIX));
		map.put(Module.DIGITALTWINBROKER.getModule(),
				digitalTwinBroker.endsWith("/") ? digitalTwinBroker.concat(SWAGGER_UI_SUFFIX)
						: digitalTwinBroker.concat("/").concat(SWAGGER_UI_SUFFIX));

		return map;
	}

	@Override
	public void reloadConfigurations() {
		loadConfigurations();
	}

	private void loadConfigurations() {
		urls = configurationService.getEndpointsUrls(profile);
		globalConfiguration = configurationService.getGlobalConfiguration(profile);

	}
}
