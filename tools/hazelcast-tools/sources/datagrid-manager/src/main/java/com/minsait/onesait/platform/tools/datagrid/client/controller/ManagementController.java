package com.minsait.onesait.platform.tools.datagrid.client.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.minsait.onesait.platform.tools.datagrid.client.controller.dto.DatagridStatusDTO;
import com.minsait.onesait.platform.tools.datagrid.client.controller.dto.HazelcastConfigurationPropertiesDTO;
import com.minsait.onesait.platform.tools.datagrid.client.controller.dto.MessagePropertyDTO;
import com.minsait.onesait.platform.tools.datagrid.client.exception.DuplicateInstanceException;
import com.minsait.onesait.platform.tools.datagrid.client.hazelcast.HazelcastInstancesManager;


@RestController
@RequestMapping(value ="management")
public class ManagementController {
	
	@Autowired
	private HazelcastInstancesManager hzInstances;
	
	@GetMapping(path = "datagrid/{datagridIdentifier}")
	public ResponseEntity<?> getDatagrid(@PathVariable("datagridIdentifier") String datagridIdentifier) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridIdentifier);
		
		if(hzInstance==null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		List<String> lMembers = hzInstance.getCluster().getMembers().stream().map(member -> member.getAddress().getHost()+":"+member.getAddress().getPort()).collect(Collectors.toList());
		
		DatagridStatusDTO dto=new DatagridStatusDTO();
		dto.setMembers(lMembers);
		dto.setStatus(hzInstances.getHazelcastInstanceState(datagridIdentifier));
		
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
	
	
	@GetMapping(path = "datagrid")
	public ResponseEntity<?> list() {
		List<String> lInstances = hzInstances.listAllHazelcastInstances();
		return new ResponseEntity<>(lInstances, HttpStatus.OK);
	}
	
	@PostMapping(path = "datagrid", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> registerInstance(@RequestBody HazelcastConfigurationPropertiesDTO datagridConfig) {
		try {
			hzInstances.createHazelcastInstance(datagridConfig.getDatagridInstanceIdentifier(), datagridConfig);
		} catch (DuplicateInstanceException e) {
			return new ResponseEntity<>(new MessagePropertyDTO(e.getMessage()), HttpStatus.CONFLICT);
		}
		
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	private HazelcastInstance getHazelcastInstance(String datagridInstance) {
		return this.hzInstances.getHazelcastInstance(datagridInstance);
	}
	
	
}

