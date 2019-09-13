<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>digitaltwin</groupId>
	<artifactId>${ProjectName}</artifactId>
	<version>0.0.1</version>
	<packaging>jar</packaging>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.5.10.RELEASE</version>
	</parent>
	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>com.minsait.onesait.platform</groupId>
			<artifactId>onesaitplatform-digital-twin-library</artifactId>
			<version>0.0.5</version>
		</dependency>
		<dependency>
		    <groupId>io.springfox</groupId>
		    <artifactId>springfox-swagger2</artifactId>
		    <version>2.8.0</version>
		</dependency>
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.8.0</version>
		</dependency>
		<#if sensehat == true>
		<dependency>
			<groupId>com.minsait.onesait.platform</groupId>
			<artifactId>sensehat-python-library</artifactId>
			<version>0.0.1</version>
		</dependency>
		</#if>
	</dependencies>
	<repositories>
		<repository>
			<id>public</id>
			<url>http://nexus.onesaitplatform.com/nexus/content/groups/public/</url>
		</repository>
		<repository>
			<id>onesait platform releases</id>
			<url>http://nexus.onesaitplatform.com/nexus/content/repositories/releases/</url>
		</repository>
	</repositories>
	
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
			
			<plugin>
			    <groupId>org.apache.maven.plugins</groupId>
			    <artifactId>maven-compiler-plugin</artifactId>
			    <configuration>
					<source>1.8</source>
					<target>1.8</target>
					<encoding>UTF-8</encoding>
			    </configuration>
			</plugin>	
			
		</plugins>
	</build>
</project>