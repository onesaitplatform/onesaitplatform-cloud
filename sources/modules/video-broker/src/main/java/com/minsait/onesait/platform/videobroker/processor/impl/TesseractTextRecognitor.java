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

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.Mat;

import com.minsait.onesait.platform.videobroker.processor.common.VideoUtils;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Slf4j
public class TesseractTextRecognitor {

	public static String getImgText(Mat mat) {
		final BufferedImage img = VideoUtils.matToBufferedImage(mat);
		return getImgText(img);
	}

	public static String cleanedSimbolString(String inputString) {
		return cleanedSimbolString(inputString, null);
	}

	public static String cleanedSimbolString(String inputString, String validCharacters) {
		StringBuilder outputString = new StringBuilder();

		final String validCharacters_default = " ABCDEFGHIJKLMNOPQRSTUWVXYZ01234567890";
		final Pattern matriculaPattern = Pattern.compile("^.*(\\d{4}[A-Z]{3}).*$");

		if (validCharacters == null || validCharacters.equals("")) {
			validCharacters = validCharacters_default;
		}

		inputString = inputString.replaceAll(" ", "");

		for (int i = 0; i < inputString.length(); i++) {
			final String ch = inputString.substring(i, i + 1);
			if (validCharacters.contains(ch)) {
				outputString.append(ch);
			}

		}

		final Matcher matcher = matriculaPattern.matcher(outputString.toString());
		if (!matcher.matches()) {
			outputString = new StringBuilder();
		} else {
			outputString = new StringBuilder(matcher.group(1));
		}

		return outputString.toString();
	}

	public static String getImgText(BufferedImage img) {

		final ITesseract instance = new Tesseract();
		instance.setLanguage("eng");
		try {
			final String imgText = instance.doOCR(img);
			return cleanedSimbolString(imgText);
		} catch (final TesseractException e) {
			log.error(e.getMessage());
		}
		return "Error while ocr image";
	}

	public static void main(String[] args) {

		final String st = "12345\n67890";
		log.info("Input String: " + st);
		final String st_formated = TesseractTextRecognitor.cleanedSimbolString(st);
		log.info("Output String: " + st_formated);
	}
}
