package com.minsait.onesait.platform.tools.datagrid.management;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.minsait.onesait.platform.tools.datagrid.exception.InvalidSPIException;
import com.minsait.onesait.platform.tools.datagrid.management.dto.HazelcastConfigurationPropertiesDTO;
import com.minsait.onesait.platform.tools.datagrid.management.dto.HazelcastConfigurationPropertiesDTO.ConnectionType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@Component
@ConditionalOnProperty(name = "onesaitplatform.management.registry")
public class DatagridRegistryManager {

	@Value("${onesaitplatform.management.registry}")
	private String registryUrl;

	@Value("${onesaitplatform.datagrid.identifier}")
	private String datagridInstanceIdentifier;

	@Value("${onesaitplatform.datagrid.spi}")
	private String spi;

	@Value("${hazelcast.network.members}")
	private List<String> members;

	@Value("${hazelcast.group.name}")
	private String hazelcastGroupName;

	@Value("${kubernetes.namespace:}")
	private String kubernetesNamespace;

	@Value("${kubernetes.servicename:}")
	private String kubernetesServiceName;

	public void register() {

		HazelcastConfigurationPropertiesDTO config = new HazelcastConfigurationPropertiesDTO();

		config.setDatagridInstanceIdentifier(datagridInstanceIdentifier);

		if (spi.equalsIgnoreCase("tcp")) {
			config.setConnectionType(ConnectionType.TCP);
		} else if (spi.equalsIgnoreCase("kubernetes")) {
			config.setConnectionType(ConnectionType.KUBERNETES);
		} else {
			throw new InvalidSPIException(
					"The value of the property \"onesaitplatform.datagrid.spi\" must be tcp|kubernetes");
		}

		config.setMembers(members);
		config.setHazelcastGroup(hazelcastGroupName);
		config.setHazelcastClientKubernetesNamespace(kubernetesNamespace);
		config.setHazelcastClientKubernetesService(kubernetesServiceName);

		HttpEntity<HazelcastConfigurationPropertiesDTO> entity = new HttpEntity<HazelcastConfigurationPropertiesDTO>(
				config, this.getBaseHeaders());

		try {
			new RestTemplate().postForEntity(registryUrl, entity, String.class);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode() != HttpStatus.CONFLICT) {
				log.debug("Unable to register with datagrid manager");
			}
		} catch (Exception e) {
			log.debug("Unable to register with datagrid manager");
		}

	}

	@Scheduled(fixedRate = 30000)
	public void periodicRegistry() {
		try {
			this.register();
		} catch (Exception e) {
			log.warn("Error in peridic registry", e);
		}
	}

	private HttpHeaders getBaseHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Connection", "close");

		return headers;
	}

}
