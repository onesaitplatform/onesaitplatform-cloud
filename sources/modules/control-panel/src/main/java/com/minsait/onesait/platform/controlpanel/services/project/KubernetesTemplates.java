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
package com.minsait.onesait.platform.controlpanel.services.project;

public interface KubernetesTemplates {

	public static final String DEPLOY_TEMPLATE="apiVersion: apps/v1\n"
			+ "kind: Deployment\n"
			+ "metadata:\n"
			+ "  namespace: {{{NAMESPACE}}}\n"
			+ "  name: {{{MICROSERVICE}}}\n"
			+ "  labels:\n"
			+ "    app: {{{MICROSERVICE}}}\n"
			+ "    version: v1\n"
			+ "spec:\n"
			+ "  replicas: 1\n"
			+ "  progressDeadlineSeconds: 120\n"
			+ "  selector:\n"
			+ "    matchLabels:\n"
			+ "      app: {{{MICROSERVICE}}}\n"
			+ "  strategy:\n"
			+ "    rollingUpdate:\n"
			+ "      maxSurge: 25%\n"
			+ "      maxUnavailable: 25%\n"
			+ "    type: RollingUpdate\n"
			+ "  template:\n"
			+ "    metadata:\n"
			+ "      labels:\n"
			+ "        app: {{{MICROSERVICE}}}\n"
			+ "    spec:\n"
			+ "      nodeName: {{{NODE_NAME}}}\n"
			+ "      containers:\n"
			+ "      - name: {{{MICROSERVICE}}}\n"
			+ "        image: {{{DOCKER_IMAGE}}}\n"
			+ "        imagePullPolicy: Always\n"
			+ "        ports:\n"
			+ "        - containerPort: {{{PORT}}}\n"
			+ "        env:\n"
			+ "           - name: ONESAIT_SERVER_NAME\n"
			+ "             value: \"{{ONESAIT_SERVER_NAME}}\"\n"
			+ "           - name: SERVER_NAME\n"
			+ "             value: \"{{SERVER_NAME}}\"\n"
			+ "           - name: CONTEXT_PATH\n"
			+ "             value: \"{{CONTEXT_PATH}}\"\n"
			+ "           - name: PORT\n"
			+ "             value: \"{{PORT}}\"\n"
			+ "";
	public static final String SERVICE_TEMPLATE="apiVersion: v1\n"
			+ "kind: Service\n"
			+ "metadata:\n"
			+ "  name: {{{MICROSERVICE}}}\n"
			+ "spec:\n"
			+ "  selector:\n"
			+ "    app: {{{MICROSERVICE}}}\n"
			+ "  ports:\n"
			+ "    - protocol: TCP\n"
			+ "      port: {{{PORT}}}\n"
			+ "      targetPort: {{{PORT}}}\n"
			+ "";
}
