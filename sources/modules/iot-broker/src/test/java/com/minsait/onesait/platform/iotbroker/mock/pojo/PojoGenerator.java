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
package com.minsait.onesait.platform.iotbroker.mock.pojo;

import java.util.UUID;

import com.github.javafaker.Faker;
import com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway.GatewayInfo;
import com.minsait.onesait.platform.multitenant.config.model.IoTSession;

public class PojoGenerator {
	public static Person generatePerson() {
		final Person person = new Person();
		person.setName(Faker.instance().name().firstName());
		person.setSurname(Faker.instance().name().lastName());
		person.setTelephone(Faker.instance().phoneNumber().cellPhone());

		return person;
	}

	public static IoTSession generateSession() {
		final IoTSession session = new IoTSession();
		session.setUserID("valid_user_id");
		session.setClientPlatform(Faker.instance().chuckNorris().fact());
		session.setDevice(Faker.instance().chuckNorris().fact());
		session.setSessionKey(UUID.randomUUID().toString());
		return session;
	}

	public static GatewayInfo generateGatewayInfo() {
		final GatewayInfo info = new GatewayInfo();
		info.setName("testGateway");
		info.setProtocol("testProtocol");

		return info;
	}

}
