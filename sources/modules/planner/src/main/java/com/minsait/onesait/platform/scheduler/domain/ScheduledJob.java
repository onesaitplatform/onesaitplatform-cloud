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
package com.minsait.onesait.platform.scheduler.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "SCHEDULED_JOB")
public class ScheduledJob {

	@Id
	@GeneratedValue
	@Column(name = "ID")
	@Getter @Setter private Long id;

	@Column(name = "USER_ID", length = 50,nullable = false)
	@NotNull
	@Getter @Setter private String userId;

	@Column(name = "JOB_NAME", length = 256, unique = true,nullable = false)
	@NotNull
	@Getter @Setter private String jobName;

	@Column(name = "JOB_GROUP", length = 256,nullable = false)
	@NotNull
	@Getter @Setter private String groupName;

	@Column(name = "SCHEDULER_ID", length = 256,nullable = false)
	@NotNull
	@Getter @Setter private String schedulerId;

	@Column(name = "SINGLETON",nullable = false)
	@NotNull
	@Getter @Setter private boolean singleton;

	public ScheduledJob() { }

	public ScheduledJob(String userId, String jobName, String groupName, String schedulerId, boolean singleton) {
		super();
		this.userId = userId;
		this.jobName = jobName;
		this.groupName = groupName;
		this.schedulerId = schedulerId;
		this.singleton = singleton;
	}



}
