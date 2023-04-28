package com.minsait.onesait.platform.tools.datagrid.client.hazelcast;

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.LifecycleEvent.LifecycleState;

public class HazelcastInstanceLifecycleListener implements LifecycleListener {
	private LifecycleState state=LifecycleState.CLIENT_CONNECTED;
	
	@Override
	public void stateChanged(LifecycleEvent event) {
		state=event.getState();		
	}
	
	public LifecycleState getState() {
		return state;
	}
}
