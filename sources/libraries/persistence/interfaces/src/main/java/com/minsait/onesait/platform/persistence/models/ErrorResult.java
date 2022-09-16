/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2021 SPAIN
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
package com.minsait.onesait.platform.persistence.models;

import lombok.Getter;

public class ErrorResult  {
	
	public ErrorResult(String message) {
		this.error = ErrorType.ERROR.text;
		this.errorType = ErrorType.ERROR;
		this.originalMessage = message;
		this.errorCode = getErrorCode(this.errorType);
		this.persistenceType = PersistenceType.PERSISTENCE;
		this.persistence = PersistenceType.PERSISTENCE.text;
	}
	
	public ErrorResult(ErrorType errorType, String message) {
		this.error = errorType.text;
		this.errorType = errorType;
		this.originalMessage = message;
		this.errorCode = getErrorCode(errorType);
		this.persistenceType = PersistenceType.PERSISTENCE;
		this.persistence = PersistenceType.PERSISTENCE.text;
	}
	
	public ErrorResult(PersistenceType persistanceType, String message) {
		this.error = ErrorType.ERROR.text;
		this.errorType = ErrorType.ERROR;
		this.originalMessage = message;
		this.errorCode = getErrorCode(this.errorType);
		this.persistenceType = persistanceType;
		this.persistence = persistanceType.text;
	}
	
	public ErrorResult(ErrorType errorType, PersistenceType persistanceType, String message) {
		this.error = errorType.text;
		this.errorType = errorType;
		this.originalMessage = message;
		this.errorCode = getErrorCode(errorType);
		this.persistenceType = persistanceType;
		this.persistence = persistanceType.text;
	}

	public enum ErrorType {
		ERROR("General error"), 
		DUPLICATED("Duplicated key"), 
		SYNTAX("Syntax error");
		
		private final String text;

		ErrorType(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	public enum PersistenceType {
		PERSISTENCE("Persistence"), 
		VIRTUAL("Virtual"), 
		MYSQL("MySQL"), 
		MONGO("MongoDB"), 
		ORACLE("Oracle"), 
		SQLSERVER("SQLServer"), 
		ELASTIC("Elasticsearch"), 
		KUDU("Kudu"), 
		IMPALA("Impala"),
		API("Impala"), 
		HIVE("Hive"),
		TIMESCALE("TimescaleDB"),
		PRESTO("Presto");
		
		private final String text;

		PersistenceType(final String text) {
	        this.text = text;
	    }

	    @Override
	    public String toString() {
	        return text;
	    }
	}
	
	@Getter
	private String originalMessage;
	
	@Getter
	private transient ErrorType errorType;
	
	@Getter
	private transient PersistenceType persistenceType;
	
	@SuppressWarnings("unused")
	private String persistence;

	@SuppressWarnings("unused")
	private String error;

	@Getter
	private int errorCode;
	
	private int getErrorCode(ErrorType type) {
		switch (type) {
			case SYNTAX:
				return 200;
			case DUPLICATED:
				return 300;
			case ERROR:	
			default:
				return 100;
		}
	}
	
}
