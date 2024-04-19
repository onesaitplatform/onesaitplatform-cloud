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
/**
 *
 */
package com.minsait.onesait.platform.videobroker.processor;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import com.minsait.onesait.platform.videobroker.enums.ProcessType;
import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ebustos
 *
 */
@Slf4j
public abstract class FrameProcessor {

	@Getter
	@Setter
	private Mat lastProcessedFrame;
	@Getter
	@Setter
	private VideoProcessorResults lastProcessedResults;
	@Getter
	@Setter
	private ProcessType processingType;

	public abstract VideoProcessorResults process(Mat frame);

	public static Mat getSubMatFromRectanbe(Mat matOriginal, Rect rectange) {

		return new Mat(matOriginal, rectange);

	}

	public static void displayImage(Mat img, String title) {
		final BufferedImage imgImage = VideoUtils.matToBufferedImage(img);
		displayImage(imgImage, title);
	}

	public static void displayImage(Image img, String title) {

		final ImageIcon icon = new ImageIcon(img);
		final JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setLayout(new FlowLayout());
		frame.setSize(img.getWidth(null) + 50, img.getHeight(null) + 50);
		final JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void saveImage(Mat mat, String filename) {

		Imgcodecs.imwrite(filename, mat);
		log.info("Saved image {}", filename);
	}

	public abstract ProcessType getProcessType();

}
