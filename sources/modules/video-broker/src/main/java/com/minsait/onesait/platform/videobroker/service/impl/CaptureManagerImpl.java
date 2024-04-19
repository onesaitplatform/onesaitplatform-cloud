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
package com.minsait.onesait.platform.videobroker.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.minsait.onesait.platform.config.model.ClientPlatform;
import com.minsait.onesait.platform.config.model.ClientPlatformOntology;
import com.minsait.onesait.platform.config.model.Ontology;
import com.minsait.onesait.platform.config.model.VideoCapture;
import com.minsait.onesait.platform.config.model.VideoCapture.Processor;
import com.minsait.onesait.platform.config.model.VideoCapture.State;
import com.minsait.onesait.platform.config.services.client.ClientPlatformService;
import com.minsait.onesait.platform.config.services.videobroker.VideoBrokerService;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcesor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorParameters;
import com.minsait.onesait.platform.videobroker.processor.impl.LicensePlateRecognition;
import com.minsait.onesait.platform.videobroker.processor.impl.PeopleDetector;
import com.minsait.onesait.platform.videobroker.processor.impl.TesseractTextExtractorProcessor;
import com.minsait.onesait.platform.videobroker.processor.impl.groovy.GroovyProcessorExecutor;
import com.minsait.onesait.platform.videobroker.processor.impl.yolo.YOLOProcessor;
import com.minsait.onesait.platform.videobroker.service.CaptureManager;

import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@Slf4j
public class CaptureManagerImpl implements CaptureManager {

	@Autowired
	private TaskExecutor threadPoolTaskExecutor;

	@Autowired
	private VideoBrokerService videoBrokerService;

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${onesaitplatform.kafka.user}")
	private String deviceTemplate;

	@Autowired
	private ClientPlatformService clientPlatformService;

	private final Map<String, Runnable> executingThreads = new HashMap<>();

	@Override
	public void manageThread(String videoCaptureId) {
		final VideoCapture videoCapture = videoBrokerService.get(videoCaptureId);

		if (videoCapture != null) {
			if (videoCapture.getState().equals(State.STOP))
				stopThread(videoCaptureId);
			else {
				startThread(videoCapture);
			}
		} else {
			stopThread(videoCaptureId);
		}

	}

	@Override
	public void startThread(VideoCapture videoCapture) {
		if (!executingThreads.containsKey(videoCapture.getId())) {
			if (!isOntologyAuthorized(videoCapture.getOntology()))
				associateOntologyWithDevice(videoCapture.getOntology());
			final VideoProcessorParameters parameters = VideoProcessorParameters.builder()
					.ontology(videoCapture.getOntology().getIdentification()).url(videoCapture.getUrl())
					.frameProcessor(getProcessorType(videoCapture.getProcessor()))
					.timerStep(videoCapture.getSamplingInterval()).captureName(videoCapture.getIdentification())
					.build();
			final VideoProcesor videoProcessor = applicationContext.getBean(VideoProcesor.class);
			videoProcessor.setParams(parameters);
			threadPoolTaskExecutor.execute(videoProcessor);
			executingThreads.put(videoCapture.getId(), videoProcessor);
			log.info("Started new video capture");
		}
	}

	@Override
	public void stopThread(String videoCaptureId) {
		final VideoProcesor thread = (VideoProcesor) executingThreads.get(videoCaptureId);
		if (thread != null) {
			thread.setInterrupted(true);
			executingThreads.remove(videoCaptureId);
		}

	}

	@Scheduled(fixedDelay = 300000L)
	public void synchronize() {
		log.debug("Synchronizing with database");
		executingThreads.entrySet()
				.removeIf(e -> e.getValue() == null || ((VideoProcesor) e.getValue()).isInterrupted());
		videoBrokerService.getAll().forEach(vc -> manageThread(vc.getId()));
	}

	private FrameProcessor getProcessorType(Processor processor) {
		switch (processor) {
		case PEOPLE:
			return new PeopleDetector();
		case TEXT:
			return new TesseractTextExtractorProcessor();
		case PLATES:
			return new LicensePlateRecognition();
		case STATS:
			return new GroovyProcessorExecutor("stats");
		case YOLO:
			return new YOLOProcessor();
		default:
			break;
		}
		return null;
	}

	private boolean isOntologyAuthorized(Ontology ontology) {
		final ClientPlatform cp = clientPlatformService.getByIdentification(deviceTemplate);
		return cp.getClientPlatformOntologies().stream().map(ClientPlatformOntology::getOntology)
				.collect(Collectors.toSet()).contains(ontology);
	}

	private void associateOntologyWithDevice(Ontology ontology) {
		final ClientPlatform cp = clientPlatformService.getByIdentification(deviceTemplate);
		clientPlatformService.createOntologyRelation(ontology, cp);
	}

}
