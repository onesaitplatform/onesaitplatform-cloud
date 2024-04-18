/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.systemconfig.init.graylog.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InputConfigurationDTO {
	@Getter
	@Setter
	@JsonProperty("bind_address")
	String bindAddress;
	
	@Getter
	@Setter
	@JsonProperty("decompress_size_limit")
	long decompressSizeLimit;
	
	@Getter
	@Setter
	@JsonProperty("max_message_size")
	long maxMessageSize;
	
	@Getter
	@Setter
	@JsonProperty("number_worker_threads")
	int numberWorkerThreads;
	
	@Getter
	@Setter
	@JsonProperty("override_source")
	String overrideSource;
	
	@Getter
	@Setter
	int port;
	
	@Getter
	@Setter
	@JsonProperty("recv_buffer_size")
	long recvBufferSize;

	@Getter
	@Setter
	@JsonProperty("tcp_keepalive")
	boolean tcpKeepalive;

	@Getter
	@Setter
	@JsonProperty("tls_cert_file")
	String tlsCertFile;

	@Getter
	@Setter
	@JsonProperty("tls_client_auth")
	String tlsClientAuth;
	
	@Getter
	@Setter
	@JsonProperty("tls_client_auth_cert_file")
	String tlsClientAuthCertFile;

	@Getter
	@Setter
	@JsonProperty("tls_enable")
	boolean tlsEnable;

	@Getter
	@Setter
	@JsonProperty("tls_key_file")
	String tlsKeyFile;

	@Getter
	@Setter
	@JsonProperty("tls_key_password")
	String tlsKeyPassword;

	@Getter
	@Setter
	@JsonProperty("use_null_delimiter")
	boolean useNullDelimiter;
}
