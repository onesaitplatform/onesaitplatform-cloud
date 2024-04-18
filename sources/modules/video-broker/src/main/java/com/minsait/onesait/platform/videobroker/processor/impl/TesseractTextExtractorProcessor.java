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
package com.minsait.onesait.platform.videobroker.processor.impl;

import org.opencv.core.Mat;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;

@Slf4j
public class TesseractTextExtractorProcessor extends FrameProcessor {

	@Getter
	@Setter
	private static ITesseract instance;
	private ProcessType processingType = ProcessType.TEXT;

	public TesseractTextExtractorProcessor() {
		super();
		setProcessingType(processingType);
		log.info("Started {} processor", getProcessingType().toString());
	}

	@Override
	public VideoProcessorResults process(Mat frame) {

		final String textExtracted = TesseractTextRecognitor.getImgText(frame);

		final TextExtractionResults dr = new TextExtractionResults(frame, textExtracted);
		dr.setProcessingType(getProcessingType().toString());
		dr.generateResult();

		setLastProcessedFrame(frame);
		setLastProcessedResults(dr);

		return dr;
	}

	@Override
	public ProcessType getProcessType() {
		return ProcessType.TEXT;
	}

}
