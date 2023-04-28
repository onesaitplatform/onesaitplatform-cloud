package com.minsait.onesait.payments.tools.streamsets.hazelcast.error;

import com.streamsets.pipeline.api.ErrorCode;
import com.streamsets.pipeline.api.GenerateResourceBundle;

@GenerateResourceBundle
public enum Errors implements ErrorCode {

	ERROR_GENERIC("Error: {}"),
	ERROR_NOT_ALL_PARAMETERS_DESTINY("The received record does not contain all required parameters: {}, {}, {}"),
	ERROR_NOT_ALL_PARAMETERS_LOOKUP("The received record does not contain all required parameters: {}, {}"),
	ERROR_TYPE_NOT_SUPPORTED("The requested object from Hazelcast is in an invalid format for Streamsets: {}");
	private final String msg;

	Errors(String msg) {
		this.msg = msg;
	}

	/** {@inheritDoc} */
	@Override
	public String getCode() {
		return name();
	}

	/** {@inheritDoc} */
	@Override
	public String getMessage() {
		return msg;
	}
}
