package com.minsait.onesait.microservice.repository;

import java.util.List;

import com.minsait.onesait.microservice.model.${WRAPPER_CLASS};
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerQuery;
import com.minsait.onesait.platform.client.springboot.aspect.IoTBrokerRepository;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;

@IoTBrokerRepository("${ONTOLOGY}")
public interface ${ONTOLOGY_CAP}Repository {

	@IoTBrokerQuery(value = "SELECT r FROM ${ONTOLOGY} as r", queryType = SSAPQueryType.SQL)
	List<${WRAPPER_CLASS}> findAll();
}
