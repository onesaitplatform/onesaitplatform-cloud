/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
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
package com.minsait.onesait.platform.cache.listener;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterMembershipListener implements MembershipListener {

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		 log.info("Added: " + membershipEvent);
		 log.info("Info Added: " + membershipEvent.getMember().getUuid());
		 log.info("Info Added HOST: " + membershipEvent.getMember().getAddress().getHost()+":"+membershipEvent.getMember().getAddress().getPort());

	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		 log.info("Removed: " + membershipEvent);
		 log.info("Info Removed: " + membershipEvent.getMember().getUuid());
		 log.info("Info Removed HOST: " + membershipEvent.getMember().getAddress().getHost()+":"+membershipEvent.getMember().getAddress().getPort());


	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		 log.info("Member attribute changed: " + memberAttributeEvent);

	}

}
