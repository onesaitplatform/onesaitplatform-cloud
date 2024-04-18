En local lanzar opensearch (desde la carpeta con el contenido de opensearch...) con opentelemetry y jaeger para que no de errores de red.

la configuración de un microservicio sin agente es :

application properties por ejemplo:
server.port=8081
otel.config.trace-id-ratio-based: 1.0 
otel.exporter.otlp.endpoint: http://localhost:4317 
service.name: Onesait-Platform-Microservice2

y las librerias del pom.xml

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-api</artifactId>
		</dependency>
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-sdk</artifactId>
		</dependency>
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-exporter-otlp</artifactId>
		</dependency>
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-semconv</artifactId>
			<version>1.27.0-alpha</version>
		</dependency>
		 

	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.opentelemetry</groupId>
				<artifactId>opentelemetry-bom</artifactId>
				<version>1.27.0</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	
	
	
	para lanzar el agente desde eclipse en el VM arguments añadir :
	-javaagent:/home/user/Descargas/opentelemetry-javaagent.jar  -Dotel.resource.attributes=service.name=semantic-inf-broker -Dotel.exporter.otlp.endpoint:http://opensearch_collector_1:4317
	descargar del github Download the latest version: https://github.com/open-telemetry/opentelemetry-java-instrumentation 
	https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
	

