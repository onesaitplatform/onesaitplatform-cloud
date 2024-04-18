package com.minsait.onesait.payments.tools.streamsets.hazelcast.params;

import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.Label;

@GenerateResourceBundle
public enum Groups implements Label {
	//Each element in the enum is a tab in the configuration section
  HAZELCAST_SERVER_CONFIG("Hazelcast Server Config"),
  HAZELCAST_CONNECTION_CONFIG("Hazelcast Connection Config"),
  HAZELCAST_COMPONENT_CONFIGURATION("Component Config")
  ;

  private final String label;

  private Groups(String label) {
    this.label = label;
  }

  @Override
  public String getLabel() {
    return this.label;
  }
}