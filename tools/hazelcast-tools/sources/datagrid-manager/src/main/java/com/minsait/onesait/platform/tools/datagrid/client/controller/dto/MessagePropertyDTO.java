package com.minsait.onesait.platform.tools.datagrid.client.controller.dto;

import lombok.Data;

@Data
public class MessagePropertyDTO {
	
	private String message;
	
	public MessagePropertyDTO(String message) {
		this.message=message;
	}
	

}
