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
package com.minsait.onesait.platform.iotbroker.plugable.interfaces.gateway;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.minsait.onesait.platform.iotbroker.processor.MessageProcessor;

public abstract class Gateway {
	@Autowired
	MessageProcessor messageProcessor;
	
	public abstract void startGateway(boolean clearState);
	public abstract void listen(MessageProcessor processor);
	public abstract void stopGateway();
	
	@PostConstruct
	public void init() {
		this.startGateway(true);
		this.listen(messageProcessor);	
		
	}
	
	@PreDestroy
	public void destroy() {
		this.stopGateway();
		
	}
//	SSAPMessage<SSAPBodyReturnMessage> process(SSAPMessage<?> mensaje);

}
