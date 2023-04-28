package com.minsait.onesait.platform.tools.datagrid.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.minsait.onesait.platform.tools.datagrid.client.controller.dto.MapEntryDTO;
import com.minsait.onesait.platform.tools.datagrid.client.controller.dto.MessagePropertyDTO;
import com.minsait.onesait.platform.tools.datagrid.client.hazelcast.HazelcastInstancesManager;

@RestController
@RequestMapping(value ="cache")
public class CacheController {
	
	@Autowired
	private HazelcastInstancesManager hzInstances;
	
	@GetMapping(path = "entry/{cacheName}/{key}")
	public ResponseEntity<?> getValue(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName, @PathVariable("key") String key) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		if(hzInstance.getMap(cacheName)==null) {
			return new ResponseEntity<>("Cache name does not exist", HttpStatus.CONFLICT);
		}
		
		if(hzInstance.getMap(cacheName).get(key)==null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Cache key "+key+" does not exist in cache "+cacheName), HttpStatus.NOT_FOUND);
		}
		
		Object result = hzInstance.getMap(cacheName).get(key);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@PostMapping(path = "putEntry/{cacheName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> putEntry(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName, @RequestBody MapEntryDTO entry) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		String key=entry.getKey();
		Object value=entry.getValue();
		
		IMap<String, Object> cache=hzInstance.getMap(cacheName);
		
		if(cache==null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Cache key "+key+" does not exist in cache "+cacheName), HttpStatus.NOT_FOUND);
		}
		
		Object result=cache.put(key, value);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	
	@PostMapping(path = "setEntry/{cacheName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> setEntry(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName, @RequestBody MapEntryDTO entry) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		String key=entry.getKey();
		Object value=entry.getValue();
		
		IMap<String, Object> cache=hzInstance.getMap(cacheName);
		
		if(cache==null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Cache key "+key+" does not exist in cache "+cacheName), HttpStatus.NOT_FOUND);
		}
		
		cache.set(key, value);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@DeleteMapping(path = "entry/{cacheName}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteEntry(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName, @PathVariable("key") String key) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		if(hzInstance.getMap(cacheName)==null) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		hzInstance.getMap(cacheName).remove(key);
		
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	
	
	@DeleteMapping(path = "/{cacheName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteAll(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		if(hzInstance.getMap(cacheName)==null) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		
		hzInstance.getMap(cacheName).clear();
		
		return new ResponseEntity<>(HttpStatus.OK);
		
	}
	
	
	@GetMapping(path = "names")
	public ResponseEntity<?> getExistingCacheNames(@RequestHeader("datagrid-instance") String datagridInstance) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		List<String> lMapNames=new ArrayList<String>();
		hzInstance.getDistributedObjects().stream().filter(obj -> obj instanceof IMap).map(cache -> cache.getName()).forEach(lMapNames::add);
		
		return new ResponseEntity<>(lMapNames, HttpStatus.OK);
	}
	
	@GetMapping(path = "/{cacheName}")
	public ResponseEntity<?> getAllValues(@RequestHeader("datagrid-instance") String datagridInstance, @PathVariable("cacheName") String cacheName) {
		HazelcastInstance hzInstance = getHazelcastInstance(datagridInstance);
		if(hzInstance == null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Datagrid does not exist"), HttpStatus.CONFLICT);
		}
		
		if(hzInstance.getMap(cacheName)==null) {
			return new ResponseEntity<>(new MessagePropertyDTO("Cache name does not exist"), HttpStatus.CONFLICT);
		}
		
		Map<String, Object> mResult=new HashMap<String, Object>();
		for(Map.Entry<Object, Object> entry:hzInstance.getMap(cacheName).entrySet()) {
			mResult.put(entry.getKey().toString(), entry.getValue());
		}
		
		return new ResponseEntity<>(mResult, HttpStatus.OK);
	}
	
	
	private HazelcastInstance getHazelcastInstance(String datagridInstance) {
		return this.hzInstances.getHazelcastInstance(datagridInstance);
	}
	

}
