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
package com.minsait.onesait.platform.controlpanel.services.lowcode;

public interface FigmaTemplateComponents {

	public static final String MAIN_SERVICE_JS_METHOD_TEMPLATE = "{{{METHOD_NAME}}} (viewModel, api, version) {\n{{{PREPROCESS_BODY}}}"
			+ "    fetch(`${this.server}/api-manager/server/api/${version}/${api}{{{OPERATION_PATH_VAR}}}`,\n"
			+ "      {\n" + "        method: '{{{METHOD}}}',\n" + "        {{{BODY}}}\n" + "        headers: {\n"
			+ "          'Content-Type': 'application/json',\n" + "          'Accept': 'application/json',\n"
			+ "          'Authorization': `Bearer ${JSON.parse(localStorage.getItem('JWT')).access_token}`\n"
			+ "        }\n" + "      })\n" + "      .then(res => {\n" + "        if(res.ok)\n"
			+ "          return res.json()\n" + "        else{\n" + "          throw res;\n" + "        }\n"
			+ "      })\n" + "      .then(r => {\n" + "        {{{OUTPUT}}}\n" + "        {{{ROUTE_CALLBACK}}}  \n"
			+ "      })\n" + "      .catch()\n" + "\n" + "    }\n";

	public static final String MAIN_SERVICE_JS_LOGIN = "login (username,password,mainPage){\n"
			+ "    const BASIC_AUTH = btoa('onesaitplatform:onesaitplatform');\n"
			+ "    const body = new URLSearchParams({\n" + "        username,\n" + "        password,\n"
			+ "        grant_type: 'password',\n" + "        scope: 'openid'\n" + "      })\n"
			+ "    fetch(`${this.server}/oauth-server/oauth/token`,\n" + "      {\n" + "        method: 'post',\n"
			+ "        body: body,\n" + "        headers :{\n"
			+ "           'Content-Type' : 'application/x-www-form-urlencoded;charset=UTF-8',\n"
			+ "           'Authorization' : `Basic ${BASIC_AUTH}`\n" + "          }\n" + "      } )\n"
			+ "      .then(res => {\n" + "        if(res.ok)\n" + "          return res.json()\n" + "        else\n"
			+ "          return res.json().then(e => {throw e;})\n" + "          //res.json().then(r => )\n"
			+ "      })\n" + "      .then(r =>{\n" + "        localStorage.setItem('JWT', JSON.stringify(r))\n"
			+ "        router.push(mainPage)\n" + "      })\n" + "      .catch(e => {\n" + "        console.log(e)\n"
			+ "      })\n" + "  }";

	public static final String HOME_VUE_LOGIN = "{{{LOGIN_METHOD_BINDED}}} (e) {\n"
			+ "       MainService.login(e.viewModel.{{{USERNAME_VAR}}},e.viewModel.{{{PASSWORD_VAR}}},'{{{MAIN_PAGE}}}')\n"
			+ "       return false;\n" + "     }";
	public static final String HOME_VUE_METHOD = "{{{METHOD_BINDED}}} (e) {\n"
			+ "       MainService.{{{METHOD_BINDED}}}(e.viewModel, '{{{API}}}', 'v{{{API_VERSION}}}')\n" + "     }";

	public static final String IMPORT_VUE = "import {{{CUSTOM_COMPONENT}}} from '../components/{{{CUSTOM_COMPONENT}}}.vue'";

}
