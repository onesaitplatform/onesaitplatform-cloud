package com.minsait.onesait.platform.tools.datagrid.client.controller.dto;

import com.minsait.onesait.platform.tools.datagrid.client.hazelcast.HazelcastConfigurationProperties;

import lombok.Data;

@Data
public class HazelcastConfigurationPropertiesDTO extends HazelcastConfigurationProperties {

	private String datagridInstanceIdentifier;
}
