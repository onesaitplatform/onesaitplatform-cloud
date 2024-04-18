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
package com.minsait.onesait.platform.raspberry.sensehat.digitaltwin.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.digitaltwin.logic.LogicManager;
import com.minsait.onesait.platform.digitaltwin.logic.api.JavascriptAPI;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.SenseHat;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.Color;
import com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.joystick.Direction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SenseHatApi implements JavascriptAPI {

	private static SenseHatApi instance;

	private SenseHat senseHat;

	private String joystickUpListener;
	private String joystickDownListener;
	private String joystickLeftListener;
	private String joystickRightListener;
	private String joystickMiddleListener;

	private ExecutorService exService;

	@Autowired
	private LogicManager logicManager;

	@Override
	@PostConstruct
	public void init() {
		instance = this;
		this.senseHat = new SenseHat();
		this.exService = Executors.newSingleThreadExecutor();

		log.info("Wait for JoystickEvent");
		joystickEvent();
	}

	public static SenseHatApi getInstance() {
		return instance;
	}

	public Double getPressure() {
		return (double) senseHat.environmentalSensor.getPressure();
	}

	public Double getTemperature() {
		return (double) senseHat.environmentalSensor.getTemperature();
	}

	public Double getHumidity() {
		return (double) senseHat.environmentalSensor.getHumidity();
	}

	public void showTextLedMatrix(String text) {
		SenseHat senseHat = new SenseHat();
		Color e = new Color(0, 0, 0);
		Color w = new Color(150, 150, 150);

		if (text.equalsIgnoreCase("Up")) {

			Color[] pixels = new Color[] { e, e, e, w, w, e, e, e, e, e, w, w, w, w, e, e, e, w, e, w, w, e, w, e, w, e,
					e, w, w, e, e, w, e, e, e, w, w, e, e, e, e, e, e, w, w, e, e, e, e, e, e, w, w, e, e, e, e, e, e,
					w, w, e, e, e };
			senseHat.ledMatrix.setPixels(pixels);
		} else if (text.equalsIgnoreCase("Right")) {

			Color[] pixels = new Color[] { e, e, e, e, w, e, e, e, e, e, e, e, e, w, e, e, e, e, e, e, e, e, w, e, w, w,
					w, w, w, w, w, w, w, w, w, w, w, w, w, w, e, e, e, e, e, e, w, e, e, e, e, e, e, w, e, e, e, e, e,
					e, w, e, e, e };
			senseHat.ledMatrix.setPixels(pixels);
			;
		} else if (text.equalsIgnoreCase("Down")) {

			Color[] pixels = new Color[] { e, e, e, w, w, e, e, e, e, e, e, w, w, e, e, e, e, e, e, w, w, e, e, e, e, e,
					e, w, w, e, e, e, w, e, e, w, w, e, e, w, e, w, e, w, w, e, w, e, e, e, w, w, w, w, e, e, e, e, e,
					w, w, e, e, e };
			senseHat.ledMatrix.setPixels(pixels);
			;
		} else if (text.equalsIgnoreCase("Left")) {

			Color[] pixels = new Color[] { e, e, e, w, e, e, e, e, e, e, w, e, e, e, e, e, e, w, e, e, e, e, e, e, w, w,
					w, w, w, w, w, w, w, w, w, w, w, w, w, w, e, w, e, e, e, e, e, e, e, e, w, e, e, e, e, e, e, e, e,
					w, e, e, e, e };
			senseHat.ledMatrix.setPixels(pixels);
			;
		} else {
			senseHat.ledMatrix.showMessage(text);
		}
	}

	public void setJoystickUpListener(String functionListener) {
		joystickUpListener = functionListener;
	}

	public void setJoystickDownListener(String functionListener) {
		joystickDownListener = functionListener;
	}

	public void setJoystickLeftListener(String functionListener) {
		joystickLeftListener = functionListener;
	}

	public void setJoystickRightListener(String functionListener) {
		joystickRightListener = functionListener;
	}

	public void setJoystickMiddleListener(String functionListener) {
		joystickMiddleListener = functionListener;
	}

	private void joystickEvent() {

		exService.execute(new Runnable() {
			@Override
			public void run() {

				while (true) {
					log.info("JoystickEvent execution");
					com.minsait.onesait.platform.raspberry.sensehat.sensehatlibrary.api.dto.JoystickEvent event = senseHat.joystick
							.waitForEvent();

					Direction direction = event.getDirection();

					// Execute javascript logic for the event
					try {

						if (direction.equals(Direction.RIGHT)) {
							if (null != joystickRightListener) {
								logicManager.invokeFunction(joystickRightListener, direction.name());
							} else {
								log.info("No listener registered for event RIGH");
							}
						} else if (direction.equals(Direction.LEFT)) {
							if (null != joystickLeftListener) {
								logicManager.invokeFunction(joystickLeftListener, direction.name());
							} else {
								log.info("No listener registered for event LEFT");
							}
						} else if (direction.equals(Direction.UP)) {
							if (null != joystickUpListener) {
								logicManager.invokeFunction(joystickUpListener, direction.name());
							} else {
								log.info("No listener registered for event UP");
							}
						} else if (direction.equals(Direction.DOWN)) {
							if (null != joystickDownListener) {
								logicManager.invokeFunction(joystickDownListener, direction.name());
							} else {
								log.info("No listener registered for event DOWN");
							}
						} else if (direction.equals(Direction.MIDDLE)) {
							if (null != joystickMiddleListener) {
								logicManager.invokeFunction(joystickMiddleListener, direction.name());
							} else {
								log.info("No listener registered for event MIDDLE");
							}
						}

					} catch (ScriptException e1) {
						log.error("Execution logic for action", e1);
					} catch (NoSuchMethodException e2) {
						log.error("Event joystickEvent not found", e2);
					}
				}
			}
		});
	}
}
