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
/**
 *
 */
package com.minsait.onesait.platform.videobroker.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ebustos
 *
 */
@Slf4j
public class PeopleDetector extends FrameProcessor {

	private double minthreshold = 1;

	private final HOGDescriptor hog = new HOGDescriptor();
	private final MatOfFloat descriptors = HOGDescriptor.getDefaultPeopleDetector();

	private int numNoDetectionsContinuos = 0;
	private static final ProcessType processingType = ProcessType.PEOPLE;

	/**
	 *
	 */
	public PeopleDetector() {
		super();
		hog.setSVMDetector(descriptors);
		setProcessingType(processingType);
		log.info("Started {} processor", getProcessingType().toString());
	}

	public PeopleDetector(double minthreshold) {
		super();
		this.minthreshold = minthreshold;
		hog.setSVMDetector(descriptors);
		setProcessingType(processingType);
		log.info("Started {} processor", getProcessingType().toString());
	}

	@Override
	public VideoProcessorResults process(Mat frame) {

		final ArrayList<Double> subWeightList = new ArrayList<>();
		final ArrayList<Rect> subRectList = new ArrayList<>();

		final MatOfRect foundLocations = new MatOfRect();
		final MatOfDouble foundWeights = new MatOfDouble();
		final Size winStride = new Size(8, 8);
		final Size padding = new Size(32, 32);
		final Point rectPoint1 = new Point();
		final Point rectPoint2 = new Point();
		final Point fontPoint = new Point();
		final Scalar rectColor = new Scalar(0, 255, 0);
		final Scalar fontColor = new Scalar(255, 255, 255);

		hog.detectMultiScale(frame, foundLocations, foundWeights, 0.0, winStride, padding, 1.05, 2.0, false);

		if (foundLocations.rows() > 0) {

			final List<Double> weightList = foundWeights.toList();
			final List<Rect> rectList = foundLocations.toList();
			int index = 0;

			// -------------------------------------------------------------
			// Quiero separar la parte de filtrado por threshold y la parte de incluir los
			// rectangulos en el frame
			// De tal forma que los pueda incluir en otra parte desde varios sitios y varios
			// colores para cada objeto
			// FILTRO: def fitroThreshold(list1, list2) -> return subList1, subList2
			// INSERT: def insertTargetInFrame(frame, prefix = "person", color(Scalar)) ->
			// frame tagged

			for (final Rect rect : rectList) {

				final double weight = weightList.get(index);

				if (weight >= minthreshold) {

					rectPoint1.x = rect.x;
					rectPoint1.y = rect.y;
					rectPoint2.x = (double) rect.x + rect.width;
					rectPoint2.y = (double) rect.y + rect.height;
					// Draw rectangle around fond object
					Imgproc.rectangle(frame, rectPoint1, rectPoint2, rectColor, 2);
					fontPoint.x = rect.x;
					// illustration
					fontPoint.y = (double) rect.y - 4;
					// Print weight
					// illustration
					Imgproc.putText(frame, String.format("P: %1.2f", weightList.get(index)), fontPoint,
							Core.FONT_HERSHEY_PLAIN, 1.5, fontColor, 2, Core.LINE_AA, false);

					subWeightList.add(weight);
					subRectList.add(rect);
				}

				index++;
			}
		}

		final DetectionResults dr = new DetectionResults(frame, subWeightList, subRectList);
		dr.setMinThreshold(minthreshold);
		dr.setProcessingType(getProcessingType().toString());
		dr.generateResult();

		setLastProcessedFrame(frame);
		setLastProcessedResults(dr);

		return dr;
	}

	public int getNumNoDetectionsContinuos() {
		return numNoDetectionsContinuos;
	}

	public void setNumNoDetectionsContinuos(int numNoDetectionsContinuos) {
		this.numNoDetectionsContinuos = numNoDetectionsContinuos;
	}

	@Override
	public ProcessType getProcessType() {
		return ProcessType.PEOPLE;
	}

}
