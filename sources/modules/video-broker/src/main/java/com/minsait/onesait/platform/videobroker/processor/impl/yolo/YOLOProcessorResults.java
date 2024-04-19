/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
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
package com.minsait.onesait.platform.videobroker.processor.impl.yolo;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.springframework.util.CollectionUtils;

import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.Getter;
import lombok.Setter;

public class YOLOProcessorResults extends VideoProcessorResults {

	@Getter
	@Setter
	private int numDetections = 0;
	@Getter
	@Setter
	private List<String> detectedNamesClasses = new ArrayList<>();
	@Getter
	@Setter
	private List<Double> detectedConfidences = new ArrayList<>();
	@Getter
	@Setter
	private List<Rect> detectedBoxes = new ArrayList<>();
	@Getter
	@Setter
	private Mat detectedImage;

	public YOLOProcessorResults() {
		super();
	}

	public YOLOProcessorResults(List<String> detectedNamesClasses, List<Double> detectedConfidences,
			List<Rect> detectedBoxes, Mat detectedImage) {
		super();
		setDetectedNamesClasses(detectedNamesClasses);
		setDetectedConfidences(detectedConfidences);
		setDetectedBoxes(detectedBoxes);
		setDetectedImage(detectedImage);
		if (!CollectionUtils.isEmpty(detectedNamesClasses)) {
			setNumDetections(detectedNamesClasses.size());
		}
		setCurrentTime();
	}

	@Override
	public void generateResult() {
		setThereResults(!CollectionUtils.isEmpty(getDetectedNamesClasses()));

		final BufferedImage buffImage = VideoUtils.matToBufferedImage(getDetectedImage());

		final byte[] bitesImage = VideoUtils.bufferedImageToByteArray(buffImage);

		final String extraInf = "classes=" + getDetectedNamesClasses() + ";" + "confidences=" + getDetectedConfidences()
				+ ";" + "boxes=" + getDetectedBoxes() + ";image=" + Base64.getEncoder().encodeToString(bitesImage);

		final String res = Integer.toString(getNumDetections());

		setResult(res);
		setCurrentTime();
		setExtraInfo(extraInf);
	}

}
