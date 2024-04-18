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
package com.minsait.onesait.platform.config.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Configurable;

import com.minsait.onesait.platform.config.model.base.OPResource;

import lombok.Getter;
import lombok.Setter;

@Configurable
@Entity
@Table(name = "spark_launcher_config", uniqueConstraints = @UniqueConstraint(name = "spark_launcher_config_ident_UQ", columnNames = {
"IDENTIFICATION" }))
public class SparkLauncherConfig extends OPResource {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "com.minsait.onesait.platform.config.model.base.AuditableEntityWithUUIDGenerator")
	@Column(name = "ID", length = 50)
	@Getter
	@Setter
	private String id;
	
	@Column(name = "IDENTIFICATION", length = 50, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String identification;

	@Column(name = "DESCRIPTION", length = 512)
	@Getter
	@Setter
	private String description;
	
	//Driver conf
	@Column(name = "DRIVER_EXTRA_CLASSPATH", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String driverExtraClassPath;
	
	@Column(name = "DRIVER_EXTRA_JAVA_OPTIONS", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String driverExtraJavaOpts;
	
	@Column(name = "DRIVER_EXTRA_LIBRARY_PATH", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String driverExtraLibPath;
	
	@Column(name = "DRIVER_MEMORY", length = 50, unique = false, nullable = false)
	@Getter
	@Setter
	private String driverMemory;
	
	//Executors conf
	
	@Column(name = "EXECUTOR_CORES", length = 50, unique = false, nullable = false)
	@Getter
	@Setter
	private String executorCores;
	
	
	@Column(name = "EXECUTOR_MEMORY", length = 50, unique = false, nullable = false)
	@Getter
	@Setter
	private String executorMemory;
	
	@Column(name = "EXECUTOR_EXTRA_CLASSPATH", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String executorExtraClassPath;
	
	@Column(name = "EXECUTOR_EXTRA_JAVA_OPTIONS", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String executorExtraJavaOpts;
	
	@Column(name = "EXECUTOR_EXTRA_LIBRARY_PATH", length = 250, unique = false, nullable = true)
	@Getter
	@Setter
	private String executorExtraLibPath;
	
	@Column(name = "TOKEN", length = 50, unique = true, nullable = false)
	@NotNull
	@Getter
	@Setter
	private String SparkConfToken;
}
