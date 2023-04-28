package com.minsait.onesait.payments.tools.streamsets.hazelcast.processor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.minsait.onesait.payments.tools.streamsets.hazelcast.connection.HazelcastConnection;
import com.minsait.onesait.payments.tools.streamsets.hazelcast.error.Errors;
import com.minsait.onesait.payments.tools.streamsets.hazelcast.params.IHazelcastConfigurableComponent;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.OnRecordErrorException;
import com.streamsets.pipeline.api.base.SingleLaneRecordProcessor;

public abstract class HazelcastProcessor extends SingleLaneRecordProcessor implements IHazelcastConfigurableComponent {
	
	private final static String CACHE_NAME_PROPERTY = "/CACHENAME";
	private final static String KEY_PROPERTY = "/KEY";
	private final static String VALUE_PROPERTY = "/VALUE";

	private HazelcastInstance hazelcastInstance;
	
	private static final Logger LOG = LoggerFactory.getLogger(HazelcastProcessor.class);

	/** {@inheritDoc} */
	@Override
	protected List<ConfigIssue> init() {
		// Validate configuration values and open any required resources.
		List<ConfigIssue> issues = super.init();
		
		if (isKubernetesCluster()) {
			LOG.info("Create Hazelcast instance for Kubernetes SPI");
			this.hazelcastInstance = HazelcastConnection.createHazelcastInstanceByKubernetesSPI(
					getHazelcastClientKubernetesNamespace(), getHazelcastClientKubernetesService(),
					getHazelcastInvocationTimeoutSeconds(), getHazelcastGroupName(),
					getHazelcastConnectionAttemptPeriod(), getHazelcastConnectionAttemptLimit(),
					getHazelcastConnectionTimeout());
		} else {
			LOG.info("Create Hazelcast instance for TCP/IP SPI");
			this.hazelcastInstance = HazelcastConnection.createHazelcastInstanceByTcpIpSPI(getHazelcastServers(),
					getHazelcastInvocationTimeoutSeconds(), getHazelcastGroupName(),
					getHazelcastConnectionAttemptPeriod(), getHazelcastConnectionAttemptLimit(),
					getHazelcastConnectionTimeout());
		}
		
		// If issues is not empty, the UI will inform the user of each configuration
		// issue in the list.
		return issues;
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		if(this.hazelcastInstance!=null) {
			this.hazelcastInstance.shutdown();
		}
		super.destroy();
	}

	/** {@inheritDoc} */
	@Override
	protected void process(Record record, SingleLaneBatchMaker batchMaker) throws StageException {
		if (!record.has(CACHE_NAME_PROPERTY) || !record.has(KEY_PROPERTY)) {
			throw new OnRecordErrorException(Errors.ERROR_NOT_ALL_PARAMETERS_LOOKUP, CACHE_NAME_PROPERTY, KEY_PROPERTY);
		}
		
		String cacheName = record.get(CACHE_NAME_PROPERTY).getValueAsString();
		String key = record.get(KEY_PROPERTY).getValueAsString();
		
		IMap<String, Object> cacheMap = hazelcastInstance.getMap(cacheName);

		Object storedObject=cacheMap.get(key);
		
		if(null!=storedObject) {
			Field valueField=this.createResultField(storedObject);
			record.set(VALUE_PROPERTY, valueField);
		}else {
			LOG.info("No value found for key {}", key);
		}
		
		batchMaker.addRecord(record);
	}
	
	private Field createResultField(Object storedObject) throws OnRecordErrorException {
		Field valueField;
		
		if(storedObject instanceof BigDecimal) {
			valueField=Field.create((BigDecimal)storedObject);
		}else if(storedObject instanceof Boolean) {
			valueField=Field.create((Boolean)storedObject);
		}else if(storedObject instanceof Double) {
			valueField=Field.create((Double)storedObject);
		}else if(storedObject instanceof Float) {
			valueField=Field.create((Float)storedObject);
		}else if(storedObject instanceof Integer) {
			valueField=Field.create((Integer)storedObject);
		}else if(storedObject instanceof Long) {
			valueField=Field.create((Long)storedObject);
		}else if(storedObject instanceof String) {
			valueField=Field.create((String)storedObject);
		}else if(storedObject instanceof Date) {
			valueField=Field.createDate((Date)storedObject);
		}else if(storedObject instanceof byte[]) {
			valueField=Field.create((byte[])storedObject);
		}else {
			throw new OnRecordErrorException(Errors.ERROR_TYPE_NOT_SUPPORTED, storedObject.getClass().getCanonicalName());
		}
		return valueField;
	}

}