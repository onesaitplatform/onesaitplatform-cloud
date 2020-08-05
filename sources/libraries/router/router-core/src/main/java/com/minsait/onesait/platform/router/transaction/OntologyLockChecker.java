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
package com.minsait.onesait.platform.router.transaction;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.hazelcast.core.IMap;
import com.minsait.onesait.platform.router.transaction.operation.TransactionalOperation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OntologyLockChecker {

	@Autowired
	@Qualifier("lockedOntologies")
	private IMap<String, OntologyStatus> lockedOntologies;

	public List<String> lockOntologies(List<TransactionalOperation> lTransactionOps, String transactionId) {
		List<String> ontologiesLocked = new ArrayList<>();
		for (TransactionalOperation operation : lTransactionOps) {
			String ontology = operation.getNotificationModel().getOperationModel().getOntologyName();

			if (null != ontology) {
				Boolean isLock = false;
				OntologyStatus ontologyStatus = lockedOntologies.get(ontology);
				if (ontologyStatus == null || !ontologyStatus.getIsLocked()) {
					log.info("Ontology {} is not locked, We lock it.", ontology);
					lockedOntologies.put(ontology, new OntologyStatus(true, transactionId));
					ontologiesLocked.add(ontology);
					isLock = true;
				} else if (ontologyStatus.getIsLocked()) {
					if (ontologyStatus.getIdTransaction().equals(transactionId)) {
						log.info(
								"Ontology {} is locked for another operation of the same transactionId {}. Check next operation.",
								ontology, transactionId);
						isLock = true;
					} else {
						log.info("Ontology {} already locked for another transactionId. Waitting ... ", ontology);
						for (int i = 0; i <= 10; i++) {
							String transactionIdLockAux = lockedOntologies.get(ontology).getIdTransaction();
							if (transactionIdLockAux == null) {
								log.info("Ontology {} is not locked, We lock it.", ontology);
								lockedOntologies.put(ontology, new OntologyStatus(true, transactionId));
								ontologiesLocked.add(ontology);
								isLock = true;
							}
						}
					}

					if (!isLock) {
						log.info("Ontology {} cannot be locked. The operation is rejected.", ontology);
						return null;
					}
				}
			} else {
				log.error("Error locking ontology {}, ontology connot be null. Ontology not locked.", ontology);
				return null;
			}
		}
		return ontologiesLocked;
	}

	public void unlockOntologies(List<String> ontologiesLocked) {
		for (String ontology : ontologiesLocked) {
			lockedOntologies.put(ontology, new OntologyStatus(false, null));
			log.info("Ontology {} unlocked.", ontology);
		}
	}

	public Boolean isOntologyLocked(String ontology) {
		OntologyStatus status = lockedOntologies.get(ontology);
		if (status == null || !status.getIsLocked()) {
			return false;
		} else if (status.getIsLocked()) {
			log.info("Ontology {} locked for a transaction. Waitting ... ", ontology);
			for (int i = 0; i <= 10; i++) {
				if (!lockedOntologies.get(ontology).getIsLocked()) {
					return false;
				}
			}
		}
		return true;
	}

	public Boolean isOntologiesLocked(List<TransactionalOperation> lTransactionOps) {
		for (TransactionalOperation operation : lTransactionOps) {
			String ontology = operation.getNotificationModel().getOperationModel().getOntologyName();
			boolean ontologyIsLocked = this.isOntologyLocked(ontology);
			if (ontologyIsLocked) {
				return true;
			}
		}
		return false;
	}
}
