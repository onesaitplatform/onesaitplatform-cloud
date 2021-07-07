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
package com.minsait.onesait.platform.router.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.router.audit.aop.Auditable;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SubscriptionModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceService;
import com.minsait.onesait.platform.router.service.app.service.RouterDigitalTwinService;
import com.minsait.onesait.platform.router.service.app.service.RouterService;
import com.minsait.onesait.platform.router.service.app.service.RouterSubscriptionService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@EnableAutoConfiguration
@CrossOrigin(origins = "*")
@RequestMapping("router")
@Slf4j
public class RouterControllerImpl
implements RouterService, RouterSubscriptionService, AdviceService, RouterDigitalTwinService {

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterService routerService;

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterSubscriptionService routerSuscriptionService;

	@Autowired
	@Qualifier("routerDigitalTwinServiceImpl")
	private RouterDigitalTwinService routerDigitalTwinService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Autowired
	private EventProducer eventProducer;

	@Override
	@PostMapping(value = "/insert")
	@ApiOperation(value = "insert")
	public OperationResultModel insert(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Insert:");
		try {
			return routerService.insert(model);
		} catch (final Exception e) {
			log.error("Error in insert", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Insert operation in {}", dEnd - dStart);
		}

	}

	@Override
	@PutMapping(value = "/update")
	@ApiOperation(value = "update")
	public OperationResultModel update(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Update:");
		try {
			return routerService.update(model);
		} catch (final Exception e) {
			log.error("Error in update", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Update operation in: {} ", dEnd - dStart);
		}
	}

	@Override
	@DeleteMapping(value = "/delete")
	@ApiOperation(value = "delete")
	public OperationResultModel delete(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Delete:");
		try {
			return routerService.delete(model);
		} catch (final Exception e) {
			log.error("Error in delete", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Delete operation in: {}", dEnd - dStart);
		}
	}

	@Override
	@PostMapping(value = "/query")
	@ApiOperation(value = "query")
	public OperationResultModel query(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Query: {} Ontology: {} Type {}",model.getOperationModel().getBody(), model.getOperationModel().getOntologyName(),model.getOperationModel().getQueryType() );
		try {
			return routerService.query(model);
		} catch (final Exception e) {
			log.error("Error in query", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Query operation in: {} ", dEnd - dStart);
		}
	}

	@Override
	@PostMapping(value = "/subscribe")
	@ApiOperation(value = "subscribe")
	public OperationResultModel subscribe(@RequestBody SubscriptionModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Subscribe:");
		try {
			return routerSuscriptionService.subscribe(model);
		} catch (final Exception e) {
			log.error("Error in subscribe", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Subscribe operation in: {} ", dEnd - dStart);
		}
	}

	@Override
	@PostMapping(value = "/unsubscribe")
	@ApiOperation(value = "unsubscribe")
	public OperationResultModel unsubscribe(@RequestBody SubscriptionModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("unSuscribe:");
		try {
			return routerSuscriptionService.unsubscribe(model);
		} catch (final Exception e) {
			log.error("Error in unSuscribe", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Unsuscribe operation in: {} ", dEnd - dStart);
		}
	}

	@PostMapping(value = "/advice")
	@ApiOperation(value = "advice")
	@Override
	public OperationResultModel advicePostProcessing(@RequestBody NotificationCompositeModel input) {
		final long dStart = System.currentTimeMillis();
		log.debug("advicePostProcessing:");
		try {
			final OperationResultModel output = new OperationResultModel();
			output.setErrorCode("NOUS");
			output.setMessage("ALL IS OK");
			output.setOperation("ADVICE");
			output.setResult("OK");
			return output;
		} catch (final Exception e) {
			log.error("Error in advicePostProcessing", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Advice operation in: {} ", dEnd - dStart);
		}
	}

	@PostMapping(value = "/token")
	@ApiOperation(value = "token")
	public String tokenPostProcessing(@RequestBody String input) {
		final long dStart = System.currentTimeMillis();
		log.debug("tokenPostProcessing:");
		final String result = jwtService.extractToken(input);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed Token operation in: {} ", dEnd - dStart);
		return result;
	}

	@PostMapping(value = "/event")
	@ApiOperation(value = "event")
	@Auditable
	public String eventProcessing(@RequestBody String input) {
		final long dStart = System.currentTimeMillis();
		log.debug("eventProcessing:");
		final OPAuditEvent event = new OPAuditEvent();
		event.setMessage(input);
		eventProducer.publish(event);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed Event operation in: {} ", dEnd - dStart);
		return input;
	}

	@PostMapping(value = "/insertEvent")
	@ApiOperation(value = "insertEvent")
	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		final OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertEvent operation in: {} ", dEnd - dStart);

		return result;
	}

	@PostMapping(value = "/insertLog")
	@ApiOperation(value = "insertLog")
	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		final OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertLog operation in: {}", dEnd - dStart);

		return result;
	}

	@PostMapping(value = "/updateShadow")
	@ApiOperation(value = "updateShadow")
	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		final OperationResultModel result = routerDigitalTwinService.updateShadow(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed UpdateShadow operation in: {}", dEnd - dStart);

		return result;
	}

	@Override
	@PostMapping(value = "/insertAction")
	@ApiOperation(value = "insertAction")
	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		final OperationResultModel result = routerDigitalTwinService.insertAction(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertAction operation in: {}", dEnd - dStart);

		return result;
	}

	@Override
	@PostMapping(value = "/startTransaction")
	@ApiOperation(value = "startTransaction")
	public OperationResultModel startTransaction(@RequestBody TransactionModel model) {
		final long dStart = System.currentTimeMillis();
		log.info("Start Transaction:");
		try {
			final OperationResultModel result = routerService.startTransaction(model);
			return result;
		} catch (final Exception e) {
			log.error("Error in Start Transaction", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Start Transaction operation in: " + (dEnd - dStart));
		}
	}

	@Override
	@PostMapping(value = "/commitTransaction")
	@ApiOperation(value = "commitTransaction")
	public OperationResultModel commitTransaction(@RequestBody TransactionModel model) {
		final long dStart = System.currentTimeMillis();
		log.info("Commit Transaction:");
		try {
			final OperationResultModel result = routerService.commitTransaction(model);
			return result;
		} catch (final Exception e) {
			log.error("Error in Commit Transaction", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Commmit Transaction operation in: " + (dEnd - dStart));
		}
	}

	@Override
	@PostMapping(value = "/rollbackTransaction")
	@ApiOperation(value = "rollbackTransaction")
	public OperationResultModel rollbackTransaction(@RequestBody TransactionModel model) {
		final long dStart = System.currentTimeMillis();
		log.info("Rollback Transaction:");
		try {
			final OperationResultModel result = routerService.rollbackTransaction(model);
			return result;
		} catch (final Exception e) {
			log.error("Error in Rollback Transaction", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Rollback Transaction operation in: " + (dEnd - dStart));
		}
	}

	@Override
	@PostMapping(value = "/notifyModules")
	@ApiOperation(value = "notifyModules")
	public OperationResultModel notifyModules(@RequestBody NotificationModel model) {
		return routerService.notifyModules(model);
	}

}
