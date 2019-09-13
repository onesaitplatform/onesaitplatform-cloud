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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.OperationType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.QueryType;
import com.minsait.onesait.platform.router.service.app.model.OperationModel.Source;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.service.RouterServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

	public static void main(String args[]) throws KeyManagementException, NoSuchAlgorithmException {
		RouterServiceImpl impl = new RouterServiceImpl();
		impl.setRouterStandaloneURL("http://localhost:20000/router/router/");

		NotificationModel input = new NotificationModel();
		String query = "select * from Restaurants";
		OperationModel model = OperationModel
				.builder("Restaurants", OperationType.GET, "administrator", Source.INTERNAL_ROUTER)
				.queryType(QueryType.SQL).body(query).build();

		input.setOperationModel(model);

		OperationResultModel result = impl.execute(input);

		log.error(result.toString());

	}

}