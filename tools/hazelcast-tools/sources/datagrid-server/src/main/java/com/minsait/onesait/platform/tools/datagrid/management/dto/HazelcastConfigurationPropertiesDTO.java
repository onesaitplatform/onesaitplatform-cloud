package com.minsait.onesait.platform.tools.datagrid.management.dto;

import java.util.List;

import lombok.Data;

@Data
public class HazelcastConfigurationPropertiesDTO {
	
	public enum ConnectionType {
		TCP,
		KUBERNETES
	}

	private String datagridInstanceIdentifier;
	
	private ConnectionType connectionType;

	private String hazelcastGroup;
	
	private List<String> members;
	
	private String hazelcastClientKubernetesNamespace;
	
	private String hazelcastClientKubernetesService;
}
