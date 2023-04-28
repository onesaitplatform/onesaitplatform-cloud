package com.minsait.onesait.platform.tools.datagrid.client.controller.dto;

import java.util.List;

import com.hazelcast.core.LifecycleEvent.LifecycleState;

import lombok.Data;

@Data
public class DatagridStatusDTO {
	
	
	private LifecycleState status;
	private List<String> members;
	

}
