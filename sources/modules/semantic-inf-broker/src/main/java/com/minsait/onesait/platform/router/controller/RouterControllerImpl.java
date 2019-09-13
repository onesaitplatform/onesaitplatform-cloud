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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.minsait.onesait.platform.audit.bean.OPAuditEvent;
import com.minsait.onesait.platform.commons.audit.producer.EventProducer;
import com.minsait.onesait.platform.config.services.oauth.JWTService;
import com.minsait.onesait.platform.router.audit.aop.Auditable;
import com.minsait.onesait.platform.router.service.app.model.DigitalTwinCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationCompositeModel;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.SuscriptionModel;
import com.minsait.onesait.platform.router.service.app.service.AdviceService;
import com.minsait.onesait.platform.router.service.app.service.RouterDigitalTwinService;
import com.minsait.onesait.platform.router.service.app.service.RouterService;
import com.minsait.onesait.platform.router.service.app.service.RouterSuscriptionService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@RestController
@EnableAutoConfiguration
@CrossOrigin(origins = "*")
@RequestMapping("router")
@Slf4j
public class RouterControllerImpl
		implements RouterService, RouterSuscriptionService, AdviceService, RouterDigitalTwinService {

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterService routerService;

	@Autowired
	@Qualifier("routerServiceImpl")
	private RouterSuscriptionService routerSuscriptionService;

	@Autowired
	@Qualifier("routerDigitalTwinServiceImpl")
	private RouterDigitalTwinService routerDigitalTwinService;

	@Autowired(required = false)
	private JWTService jwtService;

	@Autowired
	private EventProducer eventProducer;

	@RequestMapping(value = "/insert", method = RequestMethod.POST)
	@ApiOperation(value = "insert")
	public OperationResultModel insert(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Insert:");
		try {
			return routerService.insert(model);
		} catch (Exception e) {
			log.error("Error in insert", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Insert operation in {}",(dEnd - dStart));
		}

	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	@ApiOperation(value = "update")
	public OperationResultModel update(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Update:");
		try {
			return routerService.update(model);
		} catch (Exception e) {
			log.error("Error in update", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Update operation in: {} ", (dEnd - dStart));
		}
	}

	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ApiOperation(value = "delete")
	public OperationResultModel delete(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Delete:");
		try {
			return routerService.delete(model);
		} catch (Exception e) {
			log.error("Error in delete", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Delete operation in: {}",(dEnd - dStart));
		}
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ApiOperation(value = "query")
	public OperationResultModel query(@RequestBody NotificationModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Query:");
		try {
			return routerService.query(model);
		} catch (Exception e) {
			log.error("Error in query", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Query operation in: {} ",(dEnd - dStart));
		}
	}

	@RequestMapping(value = "/suscribe", method = RequestMethod.POST)
	@ApiOperation(value = "subscribe")
	public OperationResultModel suscribe(@RequestBody SuscriptionModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("Suscribe:");
		try {
			return routerSuscriptionService.suscribe(model);
		} catch (Exception e) {
			log.error("Error in suscribe", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Subscribe operation in: {} ",(dEnd - dStart));
		}
	}

	@RequestMapping(value = "/unsuscribe", method = RequestMethod.POST)
	@ApiOperation(value = "unsuscribe")
	public OperationResultModel unSuscribe(@RequestBody SuscriptionModel model) {
		final long dStart = System.currentTimeMillis();
		log.debug("unSuscribe:");
		try {
			return routerSuscriptionService.unSuscribe(model);
		} catch (Exception e) {
			log.error("Error in unSuscribe", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Unsuscribe operation in: {} ",(dEnd - dStart));
		}
	}

	@RequestMapping(value = "/advice", method = RequestMethod.POST)
	@ApiOperation(value = "advice")
	@Override
	public OperationResultModel advicePostProcessing(@RequestBody NotificationCompositeModel input) {
		final long dStart = System.currentTimeMillis();
		log.debug("advicePostProcessing:");
		try {
			OperationResultModel output = new OperationResultModel();
			output.setErrorCode("NOUS");
			output.setMessage("ALL IS OK");
			output.setOperation("ADVICE");
			output.setResult("OK");
			return output;
		} catch (Exception e) {
			log.error("Error in advicePostProcessing", e);
			throw e;
		} finally {
			final long dEnd = System.currentTimeMillis();
			log.info("Processed Advice operation in: {} ",(dEnd - dStart));
		}
	}

	@RequestMapping(value = "/token", method = RequestMethod.POST)
	@ApiOperation(value = "token")
	public String tokenPostProcessing(@RequestBody String input) {
		final long dStart = System.currentTimeMillis();
		log.debug("tokenPostProcessing:");
		String result = jwtService.extractToken(input);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed Token operation in: {} ", (dEnd - dStart));
		return result;
	}

	@RequestMapping(value = "/event", method = RequestMethod.POST)
	@ApiOperation(value = "event")
	@Auditable
	public String eventProcessing(@RequestBody String input) {
		final long dStart = System.currentTimeMillis();
		log.debug("eventProcessing:");
		OPAuditEvent event = new OPAuditEvent();
		event.setMessage(input);
		eventProducer.publish(event);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed Event operation in: {} ", (dEnd - dStart));
		return input;
	}

	@RequestMapping(value = "/insertEvent", method = RequestMethod.POST)
	@ApiOperation(value = "insertEvent")
	@Override
	public OperationResultModel insertEvent(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		OperationResultModel result = routerDigitalTwinService.insertEvent(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertEvent operation in: {} ", (dEnd - dStart));

		return result;
	}

	@RequestMapping(value = "/insertLog", method = RequestMethod.POST)
	@ApiOperation(value = "insertLog")
	@Override
	public OperationResultModel insertLog(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		OperationResultModel result = routerDigitalTwinService.insertLog(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertLog operation in: {}", (dEnd - dStart));

		return result;
	}

	@RequestMapping(value = "/updateShadow", method = RequestMethod.POST)
	@ApiOperation(value = "updateShadow")
	@Override
	public OperationResultModel updateShadow(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		OperationResultModel result = routerDigitalTwinService.updateShadow(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed UpdateShadow operation in: {}", (dEnd - dStart));

		return result;
	}

	@Override
	@RequestMapping(value = "/insertAction", method = RequestMethod.POST)
	@ApiOperation(value = "insertAction")
	public OperationResultModel insertAction(DigitalTwinCompositeModel compositeModel) {
		final long dStart = System.currentTimeMillis();
		OperationResultModel result = routerDigitalTwinService.insertAction(compositeModel);

		final long dEnd = System.currentTimeMillis();
		log.info("Processed InsertAction operation in: {}",(dEnd - dStart));

		return result;
	}

}
