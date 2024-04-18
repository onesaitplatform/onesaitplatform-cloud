/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.collection.IQueue;
import com.minsait.onesait.platform.commons.model.MultiDocumentOperationResult;
import com.minsait.onesait.platform.persistence.services.BasicOpsPersistenceServiceFacade;
import com.minsait.onesait.platform.router.service.app.model.NotificationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationModel;
import com.minsait.onesait.platform.router.service.app.model.OperationResultModel;
import com.minsait.onesait.platform.router.service.app.model.TransactionModel;
import com.minsait.onesait.platform.router.service.processor.RouterFlowManagerService;
import com.minsait.onesait.platform.router.transaction.exception.TransactionException;
import com.minsait.onesait.platform.router.transaction.operation.Transaction;
import com.minsait.onesait.platform.router.transaction.operation.TransactionalOperation;
import com.minsait.onesait.platform.router.transaction.operation.TransactionalOperation.TransactionalOperationType;

import lombok.extern.slf4j.Slf4j;

//TBD Si la BD subyacente a una ontologia proporciona soporte nativo a transacciones, habria que utlizar el soporte nativo
//MongoDB 4 las soporta
//Todas las relacionales la soportan
@Component
@Slf4j
public class OpenPlatformTransactionManagerImpl implements OpenPlatformTransactionManager {

	public static final String NOT_VALID_TRANSACTION_CODE = "NOT_VALID_TRANSACTION";
	public static final String PERMISSION_DENIED = "NOT_VALID_SESSIONKEY_FOR_TRANSACTION";
	public static final String NOT_VALID_TRANSACTION_MESSAGE = "Transaction is not currenty in progress";
	public static final String NOT_VALID_SESSIONKEY_FOR_TRANSACTION_MESSAGE = "Sessionkey is not valid for this transaction";
	public static final String ONTOLOGIES_NOT_AVAILABLE_MESSAGE = "Ontologies are locked, please try again.";
	public static final String ONTOLOGIES_NOT_AVAILABLE_CODE = "ONTOLOGIES_NOT_AVAILABLE";
	public static final String ONTOLOGIES_INSTANCE_ERROR_CODE = "ONTOLOGIES_INSTANCE_NOT_VALID";
	public static final String ONTOLOGIES_INSTANCE_ERROR_MESSAGE = "Ontology instance not valid for schema: ";
	public static final String GENERIC_OPERATION_ERROR_CODE = " OPERATION_ERROR";
	public static final String GENERIC_OPERATION_ERROR_MESSAGE = " Operation error";

	@Value("${sofia2.transaction.check.orphan.timeout.seconds:1000}")
	private Integer oprhanTimeout;

	@Autowired
	@Qualifier("globalCache")
	private HazelcastInstance hazelcastInstance;

	@Autowired
	@Qualifier("transactionalOperations")
	private IMap<String, Transaction> transactionalOperationsMap;

	@Autowired
	@Qualifier("disconectedClientsQueue")
	private IQueue<String> disconectedClientsQueue;

	@Autowired
	RouterFlowManagerService routerFlowManagerService;

	@Autowired
	private BasicOpsPersistenceServiceFacade basicOpsService;

	@Autowired
	private OntologyLockChecker ontologyLockChecker;

	@PostConstruct
	public void init() {
		ExecutorService thread = Executors.newSingleThreadExecutor();
		log.info("PostConstruct transaction.");
		thread.execute(() -> {
			while (true) {
				try {
					final String hostName = disconectedClientsQueue.take();
					log.info("Event receive to router. Client disconnected: {}", hostName);
					if (hostName != null) {
						this.rollbackOrphanTransaction();
					}
				} catch (final Exception e1) {
					log.error("Interrupted Disconnected Clients Queue listening", e1);
				}
			}
		});
	}

	@Override
	public OperationResultModel startTransaction(TransactionModel model) {

		OperationResultModel result = new OperationResultModel();

		try {
			String sessionkey = model.getClientSession();
			log.info("Strat Transaction with sessionKey: {}", sessionkey);

			String transactionId = null;
			do {
				transactionId = UUID.randomUUID().toString();
				if (transactionalOperationsMap.containsKey(transactionId)) {
					transactionId = null;
				}
			} while (transactionId == null);

			Transaction transaction = new Transaction(sessionkey);
			transactionalOperationsMap.put(transactionId, transaction);

			result.setStatus(true);
			result.setResult(transactionId);

			log.info("Transaction created with transactionId: {}", transactionId);

		} catch (Exception e) {
			log.error("Error starting transaction", e);
			result.setStatus(false);
		}

		return result;
	}

	@Override
	public OperationResultModel commitTransaction(TransactionModel model) {
		String transactionId = model.getTransactionId();
		String sessionkey = model.getClientSession();

		log.info("Commit Transaction with transactionId: {}", transactionId);

		OperationResultModel result = new OperationResultModel();

		try {
			this.validateTransactionExistsAndSessionKey(transactionId, sessionkey);
		} catch (TransactionException e) {
			log.error("Commit Transaction not valid with transactionId: {} . {}", transactionId, e);

			result.setStatus(false);
			result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
			result.setMessage(e.getMessage());

			return result;
		}
		List<String> ontologiesLocked = new ArrayList<>();
		try {

			log.info("Lock Transaction with transactionId: {}", transactionId);

			// Lock the transaction while it is running
			transactionalOperationsMap.lock(transactionId);

			if (transactionalOperationsMap.containsKey(transactionId)) {
				Transaction transaction = transactionalOperationsMap.get(transactionId);

				List<TransactionalOperation> lTransactionOps = transaction.getOperations();
				Collections.sort(lTransactionOps);

				boolean rollback = false;
				int currentOperation = 0;

				if (model.isLockTransaction()) {
					// Block access to the ontologies in a distributed way so that it
					// cannot be accessed from the broker until the transaction is finished
					log.info("Ontologies will be locked");
					ontologiesLocked = ontologyLockChecker.lockOntologies(lTransactionOps, transactionId);
					if (ontologiesLocked == null) {
						// Ontologies cannot be locked --> The operation is rejected.
						result.setStatus(false);
						result.setErrorCode(ONTOLOGIES_NOT_AVAILABLE_CODE);
						result.setMessage(ONTOLOGIES_NOT_AVAILABLE_MESSAGE);
						return result;
					}
				} else {
					// Check if ontologies are locked for another transaction
					Boolean isLocked = ontologyLockChecker.isOntologiesLocked(lTransactionOps);
					if (isLocked) {
						// Ontologies cannot be locked --> The operation is rejected.
						result.setStatus(false);
						result.setErrorCode(ONTOLOGIES_NOT_AVAILABLE_CODE);
						result.setMessage(ONTOLOGIES_NOT_AVAILABLE_MESSAGE);
						return result;
					}
				}
				for (int i = 0; i < lTransactionOps.size() && !rollback; i++) {
					log.info("The transaction operations are carried out");
					TransactionalOperation transactionalOperation = lTransactionOps.get(i);

					switch (transactionalOperation.getType()) {
					case INSERT:
						rollback = !processOperationTxInsert(transactionalOperation);
						if(rollback) {
							result.setStatus(false);
							result.setErrorCode(ONTOLOGIES_INSTANCE_ERROR_CODE);
							result.setMessage(ONTOLOGIES_INSTANCE_ERROR_MESSAGE + transactionalOperation.getNotificationModel().getOperationModel().getBody());
						}
						break;
					case UPDATE:
					case DELETE:
						rollback = !processOperationTxUpdateDelete(transactionalOperation,
								transactionalOperation.getType());
						if(rollback) {
							result.setStatus(false);
							result.setErrorCode(transactionalOperation.getType() + GENERIC_OPERATION_ERROR_CODE);
							if (transactionalOperation.getNotificationModel().getOperationModel().getObjectId() != null &&
								transactionalOperation.getNotificationModel().getOperationModel().getObjectId().length() > 0) {
									result.setMessage(transactionalOperation.getType() + GENERIC_OPERATION_ERROR_MESSAGE + " ID: " + transactionalOperation.getNotificationModel().getOperationModel().getObjectId());
							} else {
									result.setMessage(transactionalOperation.getType() + GENERIC_OPERATION_ERROR_MESSAGE + " Statement: " + transactionalOperation.getNotificationModel().getOperationModel().getBody());
							}
						}
						break;
					}
					currentOperation = i;
					transaction.setNextOperation(currentOperation + 1);
					transactionalOperationsMap.put(transactionId, transaction);

				}

				if (rollback) {
					log.info("Transaction {} failed, rollback will be done.", transactionId);
					
					for (int i = currentOperation - 1; i >= 0; i--) {
						TransactionalOperation transactionalOperation = lTransactionOps.get(i);
					
						switch (transactionalOperation.getType()) {
						case INSERT:
							if(transactionalOperation.getAffectedIds() != null) {
								
								rollback = !processCompensationTxInsert(transactionalOperation);
							
							} else {
								result.setErrorCode(GENERIC_OPERATION_ERROR_CODE);
								result.setMessage(ONTOLOGIES_INSTANCE_ERROR_MESSAGE + transactionalOperation.getNotificationModel().getOperationModel().getBody());
								
							}
							
							break;
							
							
						case UPDATE:
						case DELETE:
							
							if(transactionalOperation.getAffectedIds() != null) {
								
										rollback = !processCompensationTxUpdateDelete(transactionalOperation,transactionalOperation.getType());
							
							} else {
								result.setErrorCode(GENERIC_OPERATION_ERROR_CODE);
								result.setMessage(ONTOLOGIES_INSTANCE_ERROR_MESSAGE + transactionalOperation.getNotificationModel().getOperationModel().getBody());
								
							}
							break;
						}
					} 
							
					
					
					transaction.setNextOperation(0);
					transactionalOperationsMap.put(transactionId, transaction);
					result.setStatus(false);
				} else {
					log.info("Transaction {} finish successfully", transactionId);
					result.setStatus(true);
					result.setResult("ok");
				}

				if (model.isLockTransaction()) {
					// Unlock access to the ontologies in a distributed way so that it
					// cannot be accessed from the broker until the transaction is finished
					log.info("Ontologies will be unlocked");
					ontologyLockChecker.unlockOntologies(ontologiesLocked);
				}

			} else {
				log.error("Transaction {} doesn't exist", transactionId);
				result.setStatus(false);
				result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
				result.setMessage(NOT_VALID_TRANSACTION_MESSAGE);
				return result;
			}

		} finally {// Desbloquea la transaccion

			// Borra las operaciones de la transacción
			transactionalOperationsMap.remove(transactionId);

			transactionalOperationsMap.unlock(transactionId);

			if (model.isLockTransaction()) {
				// Unlock access to the ontologies in a distributed way so that it
				// cannot be accessed from the broker until the transaction is finished
				log.info("Ontologies will be unlocked");
				ontologyLockChecker.unlockOntologies(ontologiesLocked);
			}
		}

		return result;
	}

	@Override
	public OperationResultModel rollbackTransaction(TransactionModel model) {
		String transactionId = model.getTransactionId();
		String sessionkey = model.getClientSession();

		OperationResultModel result = new OperationResultModel();

		try {
			this.validateTransactionExistsAndSessionKey(transactionId, sessionkey);
		} catch (TransactionException e) {
			result.setStatus(false);
			result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
			result.setMessage(e.getMessage());

			return result;
		}

		try {
			transactionalOperationsMap.lock(transactionId);

			// Ask again after adquiring the lock, because it could be waiting for a commit
			if (transactionalOperationsMap.containsKey(transactionId)) {
				// Borra las operaciones de la transacción
				transactionalOperationsMap.remove(transactionId);

				result.setStatus(true);
				result.setResult("ok");
				return result;

			} else {
				result.setStatus(false);
				result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
				result.setMessage(NOT_VALID_TRANSACTION_MESSAGE);
				return result;
			}
		} finally {
			transactionalOperationsMap.unlock(transactionId);

		}
	}

	@Override
	public OperationResultModel insert(NotificationModel modelNotification) {
		return this.processOperation(modelNotification, TransactionalOperationType.INSERT);
	}

	@Override
	public OperationResultModel update(NotificationModel modelNotification) {
		return this.processOperation(modelNotification, TransactionalOperationType.UPDATE);
	}

	@Override
	public OperationResultModel delete(NotificationModel modelNotification) {
		return this.processOperation(modelNotification, TransactionalOperationType.DELETE);
	}

	@Override
	public void rollbackOrphanTransaction() {
		log.debug("continueOrphanTransaction.");
		for (Map.Entry<String, Transaction> entry : transactionalOperationsMap.entrySet()) {

			int nextOperation = entry.getValue().getNextOperation();
			String transactionId = entry.getKey();
			if (nextOperation > 0) {
				// We wait to see if transaction progresses or not. If it does not advance we
				// identify it as an orphan transaction and assume it
				try {
					log.info("Waitting to check if the transaction is orphan.");
					Thread.sleep(oprhanTimeout);
				} catch (InterruptedException e) {
					log.error("Error watting. {}", e);
				}
				int nextOperationAux = transactionalOperationsMap.get(transactionId).getNextOperation();
				int currentOperation = 0;
				if (nextOperation == nextOperationAux) {
					log.info("The transaction is orphan.");
					// The transaction is orphan
					// Lock the transaction while it is running
					transactionalOperationsMap.lock(transactionId);
					currentOperation = nextOperation;
					Transaction transaction = entry.getValue();
					List<TransactionalOperation> lTransactionOps = transaction.getOperations();

					for (int i = currentOperation - 1; i >= 0; i--) {

						TransactionalOperation transactionalOperation = lTransactionOps.get(i);
						switch (transactionalOperation.getType()) {
						case INSERT:
							processCompensationTxInsert(transactionalOperation);
							break;
						case UPDATE:
						case DELETE:
							processCompensationTxUpdateDelete(transactionalOperation, transactionalOperation.getType());
							break;
						}

						transaction.setNextOperation(currentOperation - 1);
						transactionalOperationsMap.put(transactionId, transaction);
					}

					// Borra las operaciones de la transacción
					transactionalOperationsMap.remove(transactionId);

					transactionalOperationsMap.unlock(transactionId);
				}
			}

		}
	}

	private OperationResultModel processOperation(NotificationModel modelNotification,
			TransactionalOperationType opType) {

		String transactionId = modelNotification.getOperationModel().getTransactionId();

		OperationResultModel result = new OperationResultModel();

		String sessionkey = modelNotification.getOperationModel().getClientSession();

		try {
			this.validateTransactionExistsAndSessionKey(transactionId, sessionkey);
		} catch (TransactionException e) {
			result.setStatus(false);
			result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
			result.setMessage(e.getMessage());

			return result;
		}

		try {
			transactionalOperationsMap.lock(transactionId);

			if (transactionalOperationsMap.containsKey(transactionId)) {// Transaction is in progress
				TransactionalOperation txOperation = TransactionalOperation.newInstance().sessionkey(sessionkey)
						.timestamp(System.currentTimeMillis()).type(opType).notificationModel(modelNotification);

				Transaction transaction = transactionalOperationsMap.get(transactionId);
				transaction.addOperation(txOperation);
				transactionalOperationsMap.replace(transactionId, transaction);

				result.setStatus(true);
				result.setResult(String.valueOf(transaction.getOperations().size()));// Sequence
				// number
				return result;

			} else {// Transaction is not in progress
				result.setStatus(false);
				result.setErrorCode(NOT_VALID_TRANSACTION_CODE);
				result.setMessage(NOT_VALID_SESSIONKEY_FOR_TRANSACTION_MESSAGE);

				return result;
			}
		} finally {
			transactionalOperationsMap.unlock(transactionId);
		}

	}

	// TODO cambiar la sessionkey por el client y clientId
	private void validateTransactionExistsAndSessionKey(String transactionId, String sessionkey)
			throws TransactionException {
		Transaction transaction = transactionalOperationsMap.get(transactionId);

		if (null != transaction && transaction.getStartSessionkey().equals(sessionkey)) {
			return;
		} else if (null == transaction) {
			throw new TransactionException(NOT_VALID_TRANSACTION_MESSAGE);
		} else {
			throw new TransactionException(NOT_VALID_SESSIONKEY_FOR_TRANSACTION_MESSAGE);
		}

	}

	private boolean processOperationTxInsert(TransactionalOperation transactionalOperation) {
		try {
			log.info("Proccess Operation Transaction Type {}.", transactionalOperation.getType());
			NotificationModel notificationModel = transactionalOperation.getNotificationModel();
			notificationModel.getOperationModel().setIncludeIds(true);

			// Dispara flujos de notificacion (kafka, flowengine...)
			final OperationResultModel result = routerFlowManagerService
					.startBrokerFlow(transactionalOperation.getNotificationModel());

			if (!result.getResult().equals("ERROR")) {
				List<String> affectedIds = new ArrayList<>();
				String repositoryResponse = result.getResult();

				final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult
						.fromString(repositoryResponse);
				final long totalInserted = multidocument.getCount();
				if (totalInserted == 1) {
					affectedIds.add(multidocument.getIds().get(0));
				} else if (totalInserted > 1) {
					affectedIds.addAll(multidocument.getIds());
				}
				transactionalOperation.setAffectedIds(affectedIds);

				return true;

			} else {
				log.error("Error proccessing operation transaction type {}", transactionalOperation.getType());
				return false;
			}

		} catch (Exception e) {
			log.error("Error proccessing operation transaction type {}. Error: {}", transactionalOperation.getType(),
					e);
			return false;
		}
	}

	/**
	 * Delete elements previously inserted by Id
	 * 
	 * @param transactionalOperation
	 * @return
	 * @throws Exception
	 */
	private boolean processCompensationTxInsert(TransactionalOperation transactionalOperation) {

		log.info("Proccess compensation Transaction Type {}.", transactionalOperation.getType());

		final List<String> affectedIds = transactionalOperation.getAffectedIds();
		final OperationModel opModel = transactionalOperation.getNotificationModel().getOperationModel();
		final String ontologyName = opModel.getOntologyName();
		boolean compensation = true;

		for (String compensationId : affectedIds) {
			try {
				// No dispara flujos de notificación (Es devolver a la BD al estado anterior
				basicOpsService.deleteNativeById(ontologyName, compensationId);

			} catch (Exception e) {
				log.error("Error processing compensation {}", e);
				compensation = false;
			}
		}

		return compensation;
	}

	private boolean processOperationTxUpdateDelete(TransactionalOperation transactionalOperation,
			TransactionalOperation.TransactionalOperationType type) {
		try {

			log.info("Proccess operation Transaction Type {}.", transactionalOperation.getType());

			final OperationModel opModel = transactionalOperation.getNotificationModel().getOperationModel();

			final String ontologyName = opModel.getOntologyName();
			final String objectId = opModel.getObjectId();
			final String statement = opModel.getBody();

			List<String> lCompensation = new ArrayList<>();

			if (objectId != null && objectId.length() > 0) {
				String originalData = null;
				if (type == TransactionalOperationType.UPDATE) {
					originalData = basicOpsService
							.queryUpdateTransactionCompensationNativeByObjectIdAndBodyData(ontologyName, objectId);
				} else if (type == TransactionalOperationType.DELETE) {
					originalData = basicOpsService.queryDeleteTransactionCompensationNativeById(ontologyName, objectId);
				}

				if (null != originalData) {
					lCompensation.add(originalData);
				}

			} else {
				if (type == TransactionalOperationType.UPDATE) {
					lCompensation = basicOpsService.queryUpdateTransactionCompensationNative(ontologyName, statement);
				} else if (type == TransactionalOperationType.DELETE) {
					lCompensation = basicOpsService.queryDeleteTransactionCompensationNative(ontologyName, statement);
				}
			}
			transactionalOperation.setCompensation(lCompensation);

			try {
				opModel.setIncludeIds(true);

				// Dispara flujos de notificación
				final OperationResultModel result = routerFlowManagerService
						.startBrokerFlow(transactionalOperation.getNotificationModel());

				if (!result.getResult().equals("ERROR")) {
					List<String> affectedIds = new ArrayList<String>();
					String repositoryResponse = result.getResult();

					final MultiDocumentOperationResult multidocument = MultiDocumentOperationResult.fromString(repositoryResponse);
					final long totalInserted = multidocument.getCount();
					if (totalInserted == 1) {
						if(multidocument.getIds() != null) {
							affectedIds.add(multidocument.getIds().get(0));
						}
					} else if (totalInserted > 1) {
						if(multidocument.getIds() != null) {
							affectedIds.addAll(multidocument.getIds());
						}
					}
					transactionalOperation.setAffectedIds(affectedIds);
					return true;

				} else {
					log.error("Error launching flows for transaction type: {} ", type);
					return false;
				}

			} catch (Exception e) {
				log.error("Error launching flows for transaction: {} ", e);
				return false;
			}

		} catch (Exception e) {
			log.error("Error launching flows for transaction: {} ", e);
			return false;
		}
	}

	private boolean processCompensationTxUpdateDelete(TransactionalOperation transactionalOperation,
			TransactionalOperation.TransactionalOperationType type) {
		final List<String> affectedIds = transactionalOperation.getAffectedIds();
		final List<String> restorationInstances = transactionalOperation.getCompensation();
		final OperationModel opModel = transactionalOperation.getNotificationModel().getOperationModel();
		final String ontologyName = opModel.getOntologyName();

		log.info("Proccess compensation Transaction Type {}.", transactionalOperation.getType());

		boolean compensation = true;

		if (type == TransactionalOperationType.UPDATE) {
			for (String affectedId : affectedIds) {
				try {
					basicOpsService.deleteNativeById(ontologyName, affectedId);
				} catch (Exception e) {
					log.error("Error processing compensation", e);
					compensation = false;
				}
			}
		}

		for (String restoreInstance : restorationInstances) {
			try {// No sirve un insert porque hay que mantener los Ids
				basicOpsService.insert(ontologyName, restoreInstance);
			} catch (Exception e) {
				log.error("Error processing compensation", e);
			}
		}

		return compensation;

	}
}
