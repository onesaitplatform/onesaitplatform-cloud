/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.iotbroker.processor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.comms.protocol.SSAPMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyCommandMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyIndicationMessage;
import com.minsait.onesait.platform.comms.protocol.body.SSAPBodyReturnMessage;

@Component
public class GatewayNotifierDelegate implements GatewayNotifier {

	ConcurrentHashMap<String, Consumer<SSAPMessage<SSAPBodyIndicationMessage>>> subscriptions = new ConcurrentHashMap<>(
			10);
	ConcurrentHashMap<String, Function<SSAPMessage<SSAPBodyCommandMessage>, SSAPMessage<SSAPBodyReturnMessage>>> commands = new ConcurrentHashMap<>(
			10);

	private ExecutorService executor;

	@PostConstruct
	private void init() {
		executor = Executors.newFixedThreadPool(10);

	}

	@PreDestroy
	public void destroy() {
		this.executor.shutdown();
	}

	@Override
	public void addSubscriptionListener(String key, Consumer<SSAPMessage<SSAPBodyIndicationMessage>> consumer) {
		subscriptions.put(key, consumer);

	}

	@Override
	public void addCommandListener(String key,
			Function<SSAPMessage<SSAPBodyCommandMessage>, SSAPMessage<SSAPBodyReturnMessage>> command) {
		commands.put(key, command);

	}

	@Override
	public void notify(String notifierGW, SSAPMessage<SSAPBodyIndicationMessage> indication) {
		executor.submit(() -> subscriptions.entrySet().stream().filter(x -> x.getKey().equals(notifierGW)).forEach(s -> s.getValue().accept(indication)));
		//executor.submit(() -> subscriptions.values().stream().forEach(s -> s.accept(indication)));
	}

	@Override
	public void sendCommandAsync(SSAPMessage<SSAPBodyCommandMessage> command) {
		executor.submit(() -> commands.values().stream().forEach(s -> s.apply(command)));
	}
}
