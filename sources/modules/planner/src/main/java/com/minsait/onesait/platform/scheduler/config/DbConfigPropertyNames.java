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
package com.minsait.onesait.platform.scheduler.config;

public class DbConfigPropertyNames {

	public static final String JPA_PROPERTY = "jpa";
	public static final String DATASOURCE_PROPERTY = "datasource.hikari";
	public static final String SCHEDULER_PARENT_PROPERTY = "quartz";
	public static final String SCHEDULER_JPA_PROPERTY = SCHEDULER_PARENT_PROPERTY + "." + JPA_PROPERTY;
	public static final String SCHEDULER_DATASOURCE_PROPERTY = SCHEDULER_PARENT_PROPERTY + "." + DATASOURCE_PROPERTY;
	public static final String SCHEDULER_DATASOURCE_NAME = "quartzDatasource";
	public static final String SCHEDULER_ENTITY_MANAGER_FACTORY_NAME = "quartzEntityManagerFactory";
	public static final String SCHEDULER_TRANSACTION_MANAGER_NAME = "quartzTransactionManager";
	public static final String SCHEDULER_BASE_PACKAGE = "com.minsait.onesait.platform.scheduler";

	private DbConfigPropertyNames() {
	}

}
