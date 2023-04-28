package com.minsait.onesait.payments.tools.streamsets.hazelcast.params;

import com.streamsets.pipeline.api.ConfigDef;

public class TargetComponentConfigBean {
	
	@ConfigDef(required = true, type = ConfigDef.Type.BOOLEAN, defaultValue = "false", label = "Delete key if value is empty", description = "Delete key if value is empty", displayPosition = 10, group = "HAZELCAST_COMPONENT_CONFIGURATION")
	public boolean deleteIfValueEmpty = false;

	public boolean isDeleteIfValueEmpty() {
		return deleteIfValueEmpty;
	}

	
}
