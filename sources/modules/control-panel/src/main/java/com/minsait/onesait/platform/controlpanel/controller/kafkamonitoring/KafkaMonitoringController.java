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
package com.minsait.onesait.platform.controlpanel.controller.kafkamonitoring;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.minsait.onesait.platform.commons.kafka.burrow.BurrowService;
import com.minsait.onesait.platform.commons.kafka.burrow.response.consumer.BurrowGroupStatusResponse;
import com.minsait.onesait.platform.commons.kafka.monitoring.KafkaMonitoringTopicInfo;
import com.minsait.onesait.platform.controlpanel.utils.AppWebUtils;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/kafka/monitoring")
@Slf4j
public class KafkaMonitoringController {

	@Autowired
	private BurrowService burrowService;
	@Autowired
	private AppWebUtils utils;
	@Value("${onesaitplatform.kafka.burrow.monitoring.cluster.name:local}")
	private String clusterName;

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/list", produces = "text/html")
	public String list(Model model, HttpServletRequest request,
			@RequestParam(required = false, name = "identification") String identification,
			@RequestParam(required = false, name = "description") String description) {
		List<BurrowGroupStatusResponse> clientGroupStatus = null;
		try {
			clientGroupStatus = burrowService.getAllClientGroupStatus(clusterName);
		} catch (Exception e) {
			log.error("Unable to retrieve Consumers status.Cluster={}, Cause = {}, Message = {}", clusterName,
					e.getCause(), e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.monitor.not.running", "Kafka Monitor is temporarily unreachable."));
			model.addAttribute("messageAlertType", "WARNING");
		}
		model.addAttribute("groups", clientGroupStatus);

		return "kafkamonitoring/list";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/show/{id}")
	public String show(Model model, @PathVariable("id") String id, RedirectAttributes redirect) {
		List<KafkaMonitoringTopicInfo> topicsInfo = null;
		try {
			topicsInfo = burrowService.getConsumerTopicInfo(clusterName, id);
		} catch (Exception e) {
			log.error("Unable to retrieve Consumers status.Cluster={}, Cause = {}, Message = {}", clusterName,
					e.getCause(), e.getMessage());
			model.addAttribute("message",
					utils.getMessage("kafka.monitor.not.running", "Kafka Monitor is temporarily unreachable."));
			model.addAttribute("messageAlertType", "WARNING");
		}

		model.addAttribute("consumer", id);
		model.addAttribute("partitionsDetail", topicsInfo);
		return "kafkamonitoring/show";
	}

	@PreAuthorize("@securityService.hasAnyRole('ROLE_ADMINISTRATOR')")
	@GetMapping(value = "/detail/{consumer}/{topic}/{partition}")
	public String detail(Model model, @PathVariable("consumer") String consumer, @PathVariable("topic") String topic,
			@PathVariable("partition") int partition, RedirectAttributes redirect) {
		List<KafkaMonitoringTopicInfo> partitionDetail = null;
		try {
			partitionDetail = burrowService.getConsumerTopicPartitionEvolutionWindow(clusterName, consumer, topic,
					partition);
		} catch (Exception e) {
			log.error(
					"Unable to retrieve partitions detail.Cluster={}, Consumer={}, Topic={}, Partition={}, Cause = {}, Message = {}",
					clusterName, consumer, topic, partition, e.getCause(), e.getMessage());
		}

		model.addAttribute("consumer", consumer);
		model.addAttribute("topic", topic);
		model.addAttribute("partition", partition);
		model.addAttribute("partitionsDetail", partitionDetail);
		return "kafkamonitoring/detail";
	}

}
