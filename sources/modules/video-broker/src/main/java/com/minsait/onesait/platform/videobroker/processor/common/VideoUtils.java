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
package com.minsait.onesait.platform.videobroker.processor.common;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Mat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoUtils {

	// Mat() to BufferedImage
	public static BufferedImage matToBufferedImage(Mat frame) {

		int type = 0;

		if (frame.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (frame.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}

		final BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
		final WritableRaster raster = image.getRaster();
		final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();

		final byte[] data = dataBuffer.getData();
		frame.get(0, 0, data);

		return image;
	}

	public static byte[] bufferedImageToByteArray(BufferedImage image) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] imageInByte = null;
		try {
			ImageIO.write(image, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (final IOException e) {
			log.error(e.getMessage());
		}
		return imageInByte;
	}

	public static void saveImage(BufferedImage img) {
		try {
			final File outputfile = new File("/tmp/images/" + UUID.randomUUID().toString() + ".jpg");
			ImageIO.write(img, "jpg", outputfile);
		} catch (final Exception e) {
			log.error(e.getMessage());
		}
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

	// Grayscale filter (not tested)
	public BufferedImage grayscale(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				final Color c = new Color(img.getRGB(j, i));

				final int red = (int) (c.getRed() * 0.299);
				final int green = (int) (c.getGreen() * 0.587);
				final int blue = (int) (c.getBlue() * 0.114);

				final Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);

				img.setRGB(j, i, newColor.getRGB());
			}
		}

		return img;
	}

	// Mat to double[][][]
	public static double[][][] matToDouble3D(Mat mat) {

		mat.put(0, 1, 2);
		log.trace(mat.dump());

		final int width = mat.width();
		final int height = mat.height();
		final int depth = mat.channels();
		log.trace("width: " + width + ", height: " + height + "; depth: " + depth);

		final double[][][] result3D = new double[width][height][depth];

		for (int i = 0; i < height; i++) {

			for (int j = 0; j < width; j++) {

				for (int z = 0; z < depth; z++) {
					final double[] number = mat.get(i, j);
					result3D[i][j][z] = number[z];
				}
			}
		}

		return result3D;

	}

}
