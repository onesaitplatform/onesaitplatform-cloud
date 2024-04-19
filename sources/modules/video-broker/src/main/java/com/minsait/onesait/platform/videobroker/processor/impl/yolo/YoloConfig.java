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
package com.minsait.onesait.platform.videobroker.processor.impl.yolo;

import org.opencv.dnn.Dnn;

import lombok.Getter;

public class YoloConfig {

	private YoloConfig() {
		throw new IllegalArgumentException("Config class");
	}

	@Getter
	private static final String WEIGHTS = "processor/yolo/yolov3.weights";
	@Getter
	private static final String CONFIG = "processor/yolo/yolov3.cfg";
	@Getter
	private static final String NAME = "processor/yolo/coco.names";
	@Getter
	private static final int BACKEND = Dnn.DNN_BACKEND_OPENCV;
	@Getter
	private static final int TARGET = Dnn.DNN_TARGET_CPU;
	@Getter
	private static final String TEST_IMAGE = "./src/main/resources/processor/yolo/street13.jpg";
	@Getter
	private static final double SCORE_THRESHOLD = 0.4;
	@Getter
	private static final double NMS_THRESHOLD = 0.2;

}
