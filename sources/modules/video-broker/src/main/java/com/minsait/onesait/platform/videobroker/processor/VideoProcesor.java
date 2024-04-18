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
/**
 *
 */
package com.minsait.onesait.platform.videobroker.processor;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.commons.exception.GenericOPException;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope("prototype")
@Slf4j
public class VideoProcesor implements Runnable {

	@Autowired
	private BlockingQueue<VideoProcessorResults> resultsQueue;
	// Camera config params
	@Getter
	@Setter
	private VideoProcessorParameters params;

	// Receive video
	// private ScheduledExecutorService timer; // a timer for acquiring the video
	// stream
	@Getter
	@Setter
	private VideoCapture capturer; // the OpenCV object that realizes the video capture
	@Getter
	@Setter
	private boolean cameraActive = false; // a flag to change the button behavior

	// Analyze video
	@Getter
	@Setter
	private Mat lastFrame; // last frame procesed
	@Getter
	@Setter
	private BufferedImage lastBufferedImage; // last image converted from current frame procesed

	// Processors
	@Getter
	@Setter

	private String processOption = null;
	@Getter

	@Setter
	private FrameProcessor processor;

	// Store results
	@Getter
	@Setter
	private int sentinelResults = 0;

	@Getter
	@Setter
	private boolean interrupted = false;

	/**
	 * @throws GenericOPException
	 *
	 */

	public void process() throws GenericOPException {

		while (!isInterrupted()) {

			try {
				Thread.sleep(getParams().getTimerStep());

				final Mat frame = grabFrame();
				if (frame.size().area() != 0.0) {
					final VideoProcessorResults processorResult = getParams().getFrameProcessor().process(frame);
					processorResult.setIpCamera(params.getUrl());
					processorResult.setCaptureName(params.getCaptureName());
					processorResult.setOntology(params.getOntology());

					if (processorResult.isThereResults() || getSentinelResults() == 50) {
						setSentinelResults(0);
						final boolean offer = resultsQueue.offer(processorResult);
						if (!offer)
							throw new GenericOPException("Could not offer frame");
					} else {
						addToSentinelResults(1);
					}

				}

			} catch (final InterruptedException e) {
				log.warn("Interrupted Thread shutting down: {}", e);
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public void run() {

		log.info("Started new video processor");
		capturer = new VideoCapture();
		if (!cameraActive) {
			// start the video capture
			capturer.open(params.getUrl());
			// is the video stream available?
			if (capturer.isOpened()) {
				cameraActive = true;

				try {
					process();
				} catch (final GenericOPException e) {
					log.error("Stopped processing", e);
				}

			} else {
				log.error("Error trying to set camera connecton: {}", getParams().getUrl());
			}
		} else {
			cameraActive = false;

		}
		log.info("Stoped camera connection: {}", getParams().getUrl());
	}

	private Mat grabFrame() {
		// init everything
		final Mat frame = new Mat();

		// check if the capture is open
		if (capturer.isOpened()) {
			try {
				// read the current frame
				capturer.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {

					lastFrame = frame;

					final BufferedImage image = VideoUtils.matToBufferedImage(frame);
					lastBufferedImage = image;

				}
			} catch (final Exception e) {

				log.error("Error during the frame reading: {}", e.getMessage());

			}
		}

		return frame;
	}

	public void addToSentinelResults(int num) {
		sentinelResults = sentinelResults + num;
	}

}
