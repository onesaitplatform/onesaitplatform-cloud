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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.FrameProcessor;
import com.minsait.onesait.platform.videobroker.processor.VideoProcessorResults;
import com.minsait.onesait.platform.videobroker.processor.common.OpenCvLoader;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LicensePlateRecognition extends FrameProcessor {

	private static final ProcessType processingType = ProcessType.PLATES;

	public LicensePlateRecognition() {
		super();
		setProcessingType(processingType);
	}

	public static void main(String[] args) {

		log.trace("Loading OpenCV...");
		OpenCvLoader.loadOpenCV();

		final String testImagePath = "./src/main/resources/processor/licenseplaterecognition/licenseplateimages/matricula_010.jpg";
		final Mat img = Imgcodecs.imread(testImagePath);

		final BufferedImage imgImage = VideoUtils.matToBufferedImage(img);
		displayImage(imgImage, "1 - Original image");

		final List<Rect> boundRect = detectLetters1(img);
		final Mat tagged = addContoursToMat(img, boundRect);
		final BufferedImage imgTaggedImage = VideoUtils.matToBufferedImage(tagged);
		displayImage(imgTaggedImage, "Contours image");
		log.trace("Bounding boxes result: " + boundRect);

	}

	public static Mat addContoursToMat(Mat mat, List<Rect> rectList) {

		final ArrayList<Rect> subRectList = new ArrayList<>();

		final Point rectPoint1 = new Point();
		final Point rectPoint2 = new Point();
		final Point fontPoint = new Point();
		final Scalar rectColor = new Scalar(0, 255, 0);

		for (final Rect rect : rectList) {

			rectPoint1.x = rect.x;
			rectPoint1.y = rect.y;
			rectPoint2.x = (double) rect.x + rect.width;
			rectPoint2.y = (double) rect.y + rect.height;
			// Draw rectangle around fond object
			Imgproc.rectangle(mat, rectPoint1, rectPoint2, rectColor, 8);
			fontPoint.x = rect.x;
			// illustration
			fontPoint.y = (double) rect.y - 4;
			// Print weight
			// illustration

			subRectList.add(rect);

		}

		return mat;

	}

	public static List<Rect> getPossibleContours(Mat mat, double minRatioWH, double minWith) {

		final List<Rect> boundRect = new ArrayList<>();
		final Mat hierarchy = new Mat();
		final List<MatOfPoint> contours = new ArrayList<>();

		Imgproc.findContours(mat, contours, hierarchy, 0, 1);

		for (int i = 0; i < contours.size(); i++) {

			final MatOfPoint2f mMOP2f1 = new MatOfPoint2f();
			final MatOfPoint2f mMOP2f2 = new MatOfPoint2f();

			contours.get(i).convertTo(mMOP2f1, CvType.CV_32FC2);
			Imgproc.approxPolyDP(mMOP2f1, mMOP2f2, 2, true);
			mMOP2f2.convertTo(contours.get(i), CvType.CV_32S);

			final Rect appRect = Imgproc.boundingRect(contours.get(i));
			final double width = appRect.width;
			final double height = appRect.height;
			final double ratioWithHeight = width / height;

			if (ratioWithHeight > minRatioWH && width > minWith) {
				log.trace("dimensions: " + width + ", " + height);
				boundRect.add(appRect);

			}
		}
		return boundRect;
	}

	public static void detectPlate(Mat mat) {
		final String pathXMLPlates = "./src/main/resources/processor.licenseplaterecognition/haarCascade_license_plate.xml";
		final CascadeClassifier faceDetector = new CascadeClassifier(pathXMLPlates);

		final Mat imgGray = new Mat();
		Imgproc.cvtColor(mat, imgGray, Imgproc.COLOR_RGB2GRAY);

		final Mat image = imgGray;

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		final MatOfRect faceDetections = new MatOfRect();

		faceDetector.detectMultiScale(image, faceDetections);

		log.trace(String.format("Detected %s faces", faceDetections.toArray().length));

		// Draw a bounding box around each face.
		for (final Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y),
					new Point(Double.valueOf(rect.x) + rect.width, Double.valueOf(rect.y) + rect.height),
					new Scalar(0, 255, 0));
		}

		displayImage(image, "---");

	}

	public static void findLicensePlate() {
		final String templatePlate = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/template.jpg";
		final String testImagePath = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/matricula_001.jpg";
		final String outFile = "./src/main/resources/processor.licenseplaterecognition/licenseplateimages/outFile.jpg";
		final Mat img = Imgcodecs.imread(testImagePath);
		final Mat templ = Imgcodecs.imread(templatePlate);

		displayImage(img, "find");
		displayImage(templ, "find");

		final Mat result = new Mat();
		Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);

		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
		saveImage(result, outFile);

		displayImage(result, "find");

	}

	public static List<Rect> detectLetters1(Mat imgg) {
		List<Rect> boundRect;

		final Mat imgGray = new Mat();
		final Mat imgSobel = new Mat();
		final Mat imgThreshold = new Mat();
		final Mat imgElement = new Mat();
		Mat element;
		Imgproc.cvtColor(imgg, imgGray, Imgproc.COLOR_RGB2GRAY);
		final BufferedImage imgGrayImage = VideoUtils.matToBufferedImage(imgGray);
		displayImage(imgGrayImage, "2 - Gray scale image");

		Imgproc.Sobel(imgGray, imgSobel, CvType.CV_8U, 1, 0, 1, 3, 0, Core.BORDER_DEFAULT);
		final BufferedImage imgSobelImage = VideoUtils.matToBufferedImage(imgSobel);
		displayImage(imgSobelImage, "3 - Sobel image");

		Imgproc.threshold(imgSobel, imgThreshold, 0, 255, 8);
		final BufferedImage imgSThresImage = VideoUtils.matToBufferedImage(imgThreshold);
		displayImage(imgSThresImage, "4 - Threshold image");

		displayImage(imgThreshold, "4 - morphologyEx");
		element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(75, 10));
		Imgproc.morphologyEx(imgThreshold, imgElement, Imgproc.MORPH_CLOSE, element);
		displayImage(imgElement, "4 - morphologyEx");

		boundRect = getPossibleContours(imgElement, 2, 10);

		return boundRect;

	}

	public static List<Rect> detectLetters2(Mat img) {
		List<Rect> contours;

		final Mat imgGray = new Mat();
		final Mat imgGaussBlur = new Mat();
		final Mat imgadaptThres = new Mat();
		final Mat imgContours = new Mat();
		log.trace("2 - Gray scale...");
		Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
		final BufferedImage imgGrayImage = VideoUtils.matToBufferedImage(imgGray);
		displayImage(imgGrayImage, "Gray scale image");

		log.trace("3 - GaussianBlur...");
		Imgproc.GaussianBlur(imgGray, imgGaussBlur, new Size(5, 5), 0);
		final BufferedImage imgGaussBlurImage = VideoUtils.matToBufferedImage(imgGaussBlur);
		displayImage(imgGaussBlurImage, "GaussianBlur image");

		final Mat imgSobel = new Mat();
		Imgproc.Sobel(imgGaussBlur, imgSobel, CvType.CV_8U, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
		final BufferedImage imgSobelImage = VideoUtils.matToBufferedImage(imgSobel);
		displayImage(imgSobelImage, "3 - Sobel image");

		log.trace("4 - Adaptative Threshold...");
		Imgproc.adaptiveThreshold(imgGaussBlur, imgadaptThres, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY, 15, 2);
		final BufferedImage imgimgadaptThresImage = VideoUtils.matToBufferedImage(imgGray);
		displayImage(imgimgadaptThresImage, "Adaptative Threshold");

		log.trace("5 - Contours...");
		contours = getPossibleContours(imgContours, 1, 3);

		return contours;
	}

	public static List<Rect> detectLetters3(Mat img) {
		List<Rect> contours;

		final Mat imgGaussBlur = img;
		final Mat imgadaptThres = img;
		final Mat imgContours = img;

		final Mat imgHSV = img;
		final Mat imgHue = img;
		final Mat imgSaturation = img;
		final Mat imgValue = img;
		final List<Mat> hsv = new ArrayList<>(Arrays.asList(imgHue, imgSaturation, imgValue));
		Imgproc.cvtColor(img, imgHSV, Imgproc.COLOR_BGR2HSV);
		Core.split(imgHSV, hsv);

		log.trace("2 - Gray scale...");
		img = hsv.get(2);
		final BufferedImage imgGrayImage = VideoUtils.matToBufferedImage(img);
		displayImage(imgGrayImage, "Gray scale image");

		log.trace("3 - Maximize contrast scale...");
		final Mat imgTopHat = img;
		final Mat imgBlackHat = img;

		final Mat structuringElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 5));

		Imgproc.morphologyEx(img, imgTopHat, Imgproc.MORPH_TOPHAT, structuringElement);
		Imgproc.morphologyEx(img, imgBlackHat, Imgproc.MORPH_BLACKHAT, structuringElement);

		final Mat imgGrayscalePlusTopHat = new Mat();
		final Mat imgGrayscalePlusTopHatMinusBlackHat = new Mat();
		Core.add(img, imgTopHat, imgGrayscalePlusTopHat);
		Core.subtract(imgGrayscalePlusTopHat, imgBlackHat, imgGrayscalePlusTopHatMinusBlackHat);
		final BufferedImage imgGrayscalePlusTopHatMinusBlackHatImage = VideoUtils
				.matToBufferedImage(imgGrayscalePlusTopHatMinusBlackHat);
		displayImage(imgGrayscalePlusTopHatMinusBlackHatImage, "Maximize contrast scale");

		log.trace("3 - GaussianBlur...");
		Imgproc.GaussianBlur(img, imgGaussBlur, new Size(15, 15), 0);
		final BufferedImage imgGaussBlurImage = VideoUtils.matToBufferedImage(img);
		displayImage(imgGaussBlurImage, "GaussianBlur image");

		log.trace("4 - Adaptative Threshold...");
		Imgproc.adaptiveThreshold(imgGaussBlur, imgadaptThres, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY, 15, 2);
		final BufferedImage imgimgadaptThresImage = VideoUtils.matToBufferedImage(img);
		displayImage(imgimgadaptThresImage, "Adaptative Threshold");

		log.trace("5 - Contours...");
		contours = getPossibleContours(imgContours, 1, 3);

		return contours;
	}

	@Override
	public VideoProcessorResults process(Mat frame) {
		return null;
	}

	@Override
	public ProcessType getProcessType() {
		return ProcessType.PLATES;
	}

}
