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
package com.minsait.onesait.platform.videobroker.processor.impl;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class MatchingDemo {
	public void run(String inFile, String templateFile, String outFile, int matchMethod) {
		log.debug("\nRunning Template Matching");

		final Mat img = Imgcodecs.imread(inFile);
		final Mat templ = Imgcodecs.imread(templateFile);

		// / Create the result matrix
		final int resultCols = img.cols() - templ.cols() + 1;
		final int resultRows = img.rows() - templ.rows() + 1;
		final Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

		// / Do the Matching and Normalize
		Imgproc.matchTemplate(img, templ, result, matchMethod);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		// / Localizing the best match with minMaxLoc
		final MinMaxLocResult mmr = Core.minMaxLoc(result);

		Point matchLoc;
		if (matchMethod == Imgproc.TM_SQDIFF || matchMethod == Imgproc.TM_SQDIFF_NORMED) {
			matchLoc = mmr.minLoc;

		} else {
			matchLoc = mmr.maxLoc;

		}

		// / Show me what you got
		Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0));

		// Save the visualized detection.
		log.debug("Writing " + outFile);
		Imgcodecs.imwrite(outFile, img);

	}

	public static void main(String[] args) {
		log.debug("Loading OpenCV...");
		OpenCvLoader.loadOpenCV();
		final String templatePlate = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/template4.jpg";
		final String testImagePath = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/matricula_001.jpg";
		final String outFile = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/outFile.jpg";
		new MatchingDemo().run(testImagePath, templatePlate, outFile, Imgproc.TM_CCOEFF);
	}
}