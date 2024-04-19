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
package com.minsait.onesait.platform.systemconfig.init;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.minsait.onesait.platform.config.model.ActionsDigitalTwinType;
import com.minsait.onesait.platform.config.model.DigitalTwinDevice;
import com.minsait.onesait.platform.config.model.DigitalTwinType;
import com.minsait.onesait.platform.config.model.EventsDigitalTwinType;
import com.minsait.onesait.platform.config.model.PropertyDigitalTwinType;
import com.minsait.onesait.platform.config.model.PropertyDigitalTwinType.Direction;
import com.minsait.onesait.platform.config.model.User;
import com.minsait.onesait.platform.config.repository.DigitalTwinDeviceRepository;
import com.minsait.onesait.platform.config.repository.DigitalTwinTypeRepository;
import com.minsait.onesait.platform.config.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InitConfigDBDigitalTwin {

	@Autowired
	DigitalTwinTypeRepository digitalTwinTypeRepository;
	@Autowired
	DigitalTwinDeviceRepository digitalTwinDeviceRepository;
	@Autowired
	UserRepository userCDBRepository;

	private User userDeveloper = null;

	private static final String LINE_SEPARATOR = "line.separator";
	private static final String DT_API_SEND_UPDATE_SHADOW = "    digitalTwinApi.sendUpdateShadow();";
	private static final String DT_API_LOG_SEND_UPDATE_SHADOW = "    digitalTwinApi.log('Send Update Shadow');";
	private static final String SENSE_HAT_API_SHOW_TEXT_LED_MATRIX = "   senseHatApi.showTextLedMatrix(event);";
	private static final String MASTER_EVENTS_DT_TYPE = "MASTER-EventsDigitalTwinType-";
	private static final String MASTER_ACTIONS_DT_TYPE = "MASTER-ActionsDigitalTwinType-";
	private static final String MASTER_PROPERTY_DT_TYPE = "MASTER-PropertyDigitalTwinType-";
	private static final String DOUBLE_STR = "double";

	private User getUserDeveloper() {
		if (userDeveloper == null)
			userDeveloper = userCDBRepository.findByUserId("developer");
		return userDeveloper;
	}

	public void initDigitalTwinDevice() {
		log.info("init_DigitalTwinDevice");
		if (digitalTwinDeviceRepository.count() == 0) {
			DigitalTwinDevice device = new DigitalTwinDevice();
			// Turbine example
			device.setId("MASTER-DigitalTwinDevice-1");
			device.setContextPath("/turbine");
			device.setDigitalKey("f0e50f5f8c754204a4ac601f29775c15");
			device.setIdentification("TurbineHelsinki");
			device.setIntrface("eth0");
			device.setIpv6(false);
			device.setLatitude("60.17688297979675");
			device.setLongitude("24.92333816559176");
			device.setPort(10000);
			device.setUrlSchema("http");
			device.setUrl("https://s4citiespro.westeurope.cloudapp.azure.com/digitaltwinbroker");
			device.setTypeId(digitalTwinTypeRepository.findByIdentification("Turbine"));
			device.setUser(getUserDeveloper());
			digitalTwinDeviceRepository.save(device);

			// Sensehat example
			device = new DigitalTwinDevice();
			device.setId("MASTER-DigitalTwinDevice-2");
			device.setContextPath("/sensehat");
			device.setDigitalKey("f0e50f5f8c754204a4ac601f29765c15");
			device.setIdentification("SensehatHelsinki");
			device.setIntrface("eth0");
			device.setIpv6(false);
			device.setLatitude("60.17688297979675");
			device.setLongitude("24.92333816559176");
			device.setPort(10000);
			device.setUrlSchema("http");
			device.setUrl("https://s4citiespro.westeurope.cloudapp.azure.com/digitaltwinbroker");
			device.setTypeId(digitalTwinTypeRepository.findByIdentification("Sensehat"));
			device.setUser(getUserDeveloper());
			digitalTwinDeviceRepository.save(device);
		}
	}

	public void initDigitalTwinType() {
		log.info("init_DigitalTwinType");
		if (digitalTwinTypeRepository.count() == 0) {
			// Turbine example
			DigitalTwinType type = new DigitalTwinType();
			type.setId("MASTER-DigitalTwinType-1");
			type.setIdentification("Turbine");
			type.setType("thing");
			type.setDescription("Wind Turbine for electricity generation");
			type.setJson(
					"{\"title\":\"Turbine\",\"links\":{\"properties\":\"thing/Turbine/properties\",\"actions\":\"thing/Turbine/actions\",\"events\":\"thing/Turbine/events\"},\"description\":\"Wind Turbine for electricity generation\",\"properties\":{\"rotorSpeed\":{\"type\":\"int\",\"units\":\"rpm\",\"direction\":\"out\",\"description\":\"Rotor speed\"},\"maxRotorSpeed\":{\"type\":\"int\",\"units\":\"rpm\",\"direction\":\"in_out\",\"description\":\"Max allowed speed for the rotor\"},\"power\":{\"type\":\"double\",\"units\":\"wat/h\",\"direction\":\"out\",\"description\":\"Current Power generated by the turbine\"},\"alternatorTemp\":{\"type\":\"double\",\"units\":\"celsius\",\"direction\":\"out\",\"description\":\"Temperature of the alternator\"},\"nacelleTemp\":{\"type\":\"double\",\"units\":\"celsius\",\"direction\":\"out\",\"description\":\"Temperature into the nacelle\"},\"windDirection\":{\"type\":\"int\",\"units\":\"degrees\",\"direction\":\"out\",\"description\":\"Wind direction\"}},\"actions\":{\"connectElectricNetwork\":{\"description\":\"Connect the turbine to the electric network to provide power\"},\"disconnectElectricNetwork\":{\"description\":\"Disconnect the turbine to the electric network to prevent problems\"},\"limitRotorSpeed\":{\"description\":\"Limits the rotor speed\"}},\"events\":{\"register\":{\"description\":\"Register the device into the plaform\"},\"updateshadow\":{\"description\":\"Updates the shadow in the plaform\"},\"ping\":{\"description\":\"Ping the platform to keepalive the device\"},\"log\":{\"description\":\"Log information in plaform\"}}}");
			type.setUser(getUserDeveloper());
			type.setLogic(
					"var digitalTwinApi = Java.type('com.minsait.onesait.platform.digitaltwin.logic.api.DigitalTwinApi').getInstance();"
							+ System.getProperty(LINE_SEPARATOR) + "function init(){"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.log('Init TurbineHelsinki shadow');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('alternatorTemp', 25.0);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('power', 50000.2);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('nacelleTemp', 25.9);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('rotorSpeed', 30);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('windDirection', 68);"
							+ System.getProperty(LINE_SEPARATOR) + "" + System.getProperty(LINE_SEPARATOR)
							+ DT_API_SEND_UPDATE_SHADOW + System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.log('Send Update Shadow for init function');"
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "function main(){" + System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.log('New loop');" + System.getProperty(LINE_SEPARATOR)
							+ "    var alternatorTemp = digitalTwinApi.getStatusValue('alternatorTemp');"
							+ System.getProperty(LINE_SEPARATOR) + "" + System.getProperty(LINE_SEPARATOR)
							+ "    alternatorTemp ++;" + System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('alternatorTemp', alternatorTemp);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('power', 50000.2);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('nacelleTemp', 25.9);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('rotorSpeed', 30);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('windDirection', 68);"
							+ System.getProperty(LINE_SEPARATOR) + "" + System.getProperty(LINE_SEPARATOR)
							+ DT_API_SEND_UPDATE_SHADOW + System.getProperty(LINE_SEPARATOR)
							+ DT_API_LOG_SEND_UPDATE_SHADOW + System.getProperty(LINE_SEPARATOR) + " "
							+ System.getProperty(LINE_SEPARATOR) + "   if(alternatorTemp>=30){"
							+ System.getProperty(LINE_SEPARATOR) + "      digitalTwinApi.sendCustomEvent('tempAlert');"
							+ System.getProperty(LINE_SEPARATOR) + "   }" + System.getProperty(LINE_SEPARATOR) + "}"
							+ System.getProperty(LINE_SEPARATOR) + "" + System.getProperty(LINE_SEPARATOR)
							+ "var onActionConnectElectricNetwork=function(data){ }"
							+ System.getProperty(LINE_SEPARATOR)
							+ "var onActionDisconnectElectricNetwork=function(data){ }"
							+ System.getProperty(LINE_SEPARATOR) + "var onActionLimitRotorSpeed=function(data){ }");

			final Set<PropertyDigitalTwinType> properties = createTurbinePropertiesDT(type);
			final Set<ActionsDigitalTwinType> actions = createTurbineActionsDT(type);
			final Set<EventsDigitalTwinType> events = createTurbineEventsDT(type);
			type.setPropertyDigitalTwinTypes(properties);
			type.setActionDigitalTwinTypes(actions);
			type.setEventDigitalTwinTypes(events);
			digitalTwinTypeRepository.save(type);

			// Sensehat example
			type = new DigitalTwinType();
			type.setId("MASTER-DigitalTwinType-2");
			type.setIdentification("Sensehat");
			type.setType("thing");
			type.setDescription("Raspberry with Sensehat");
			type.setJson(
					"{\"title\": \"Sensehat\",\"links\": {\"properties\": \"thing/Sensehat/properties\",\"actions\": \"thing/Sensehat/actions\",\"events\": \"thing/Sensehat/events\"},\"description\": \"Raspberry - Sensehat\",\"properties\": {\"temperature\": {\"type\": \"double\",\"units\": \"degrees\",\"direction\": \"out\",\"description\": \"Temperature\"},\"humidity\": {\"type\": \"double\",\"units\": \"milibars\",\"direction\": \"out\",\"description\": \"Humidity\"},\"pressure\": {\"type\": \"double\",\"units\": \"%\",\"direction\": \"out\",\"description\": \"Pressure\"}},\"actions\": {\"joystickUp\": {\"description\": \"Joysctick action up\"},\"joystickRight\": {\"description\": \"Joystick action to the right\"},\"joystickDown\": {\"description\": \"Joystick action down\"},\"joystickLeft\": {\"description\": \"Joystick action to the left\"},\"joystickMiddle\": {\"description\": \"Joystick action to the middle\"}},\"events\": {\"ping\": {\"description\": \"Ping\"},\"register\": {\"description\": \"Register\"},\"log\": {\"description\": \"Log information in platform\"},\"joystickEventRigth\": {\"description\": \"Send joystick event to the right\"},\"joystickEventLeft\": {\"description\": \"Send joystick event to the left\"},\"joystickEventUp\": {\"description\": \"Send joystick event up\"},\"joystickEventDown\": {\"description\": \"Send joystick event down\"},\"joystickEventMiddle\": {\"description\": \"Send joystick event to the middle\"},\"updateShadow\": {\"description\": \"Send joystick event to the right\"}}}");
			type.setUser(getUserDeveloper());
			type.setLogic(
					"var digitalTwinApi = Java.type('com.minsait.onesait.platform.digitaltwin.logic.api.DigitalTwinApi').getInstance();"
							+ System.getProperty(LINE_SEPARATOR)
							+ "var senseHatApi = Java.type('com.minsait.onesait.platform.raspberry.sensehat.digitaltwin.api.SenseHatApi').getInstance();"
							+ System.getProperty(LINE_SEPARATOR) + "function init(){"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   senseHatApi.setJoystickUpListener('joystickEventUp');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   senseHatApi.setJoystickDownListener('joystickEventDown')"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   senseHatApi.setJoystickLeftListener('joystickEventLeft');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   senseHatApi.setJoystickRightListener('joystickEventRight');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   senseHatApi.setJoystickMiddleListener('joystickEventMiddle');"
							+ System.getProperty(LINE_SEPARATOR) + "   digitalTwinApi.log('Init SenseHatSpain shadow');"
							+ System.getProperty(LINE_SEPARATOR) + "   var sensorPress = senseHatApi.getPressure();"
							+ System.getProperty(LINE_SEPARATOR) + "   var sensorTemp = senseHatApi.getTemperature();"
							+ System.getProperty(LINE_SEPARATOR) + "   var sensorHum = senseHatApi.getHumidity();"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.setStatusValue('pressure', sensorPress);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.setStatusValue('temperature', sensorTemp);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('humidity', sensorHum);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    var temp = digitalTwinApi.getStatusValue('temperature');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    var hum = digitalTwinApi.getStatusValue('humidity');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    var pressure = digitalTwinApi.getStatusValue('pressure');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.log('Temperature: ' + temp + ' - Humidity: ' + hum + ' - Pressure: '+ pressure);"
							+ System.getProperty(LINE_SEPARATOR) + DT_API_SEND_UPDATE_SHADOW
							+ System.getProperty(LINE_SEPARATOR) + DT_API_LOG_SEND_UPDATE_SHADOW
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "function main(){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('New main execution');" + System.getProperty(LINE_SEPARATOR)
							+ "   var sensorPress = senseHatApi.getPressure();" + System.getProperty(LINE_SEPARATOR)
							+ "   var sensorTemp = senseHatApi.getTemperature();" + System.getProperty(LINE_SEPARATOR)
							+ "   var sensorHum = senseHatApi.getHumidity();" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.setStatusValue('pressure', sensorPress);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.setStatusValue('temperature', sensorTemp);"
							+ System.getProperty(LINE_SEPARATOR)
							+ "    digitalTwinApi.setStatusValue('humidity', sensorHum);"
							+ System.getProperty(LINE_SEPARATOR) + DT_API_SEND_UPDATE_SHADOW
							+ System.getProperty(LINE_SEPARATOR) + DT_API_LOG_SEND_UPDATE_SHADOW
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var joystickEventLeft=function(event){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick event to the left');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.sendCustomEvent('joystickEventLeft');"
							+ System.getProperty(LINE_SEPARATOR) + SENSE_HAT_API_SHOW_TEXT_LED_MATRIX
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var joystickEventRight=function(event){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick event to the right');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.sendCustomEvent('joystickEventRight');"
							+ System.getProperty(LINE_SEPARATOR) + SENSE_HAT_API_SHOW_TEXT_LED_MATRIX
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var joystickEventUp=function(event){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick event up');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.sendCustomEvent('joystickEventUp');"
							+ System.getProperty(LINE_SEPARATOR) + SENSE_HAT_API_SHOW_TEXT_LED_MATRIX
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var joystickEventDown=function(event){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick event down');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.sendCustomEvent('joystickEventDown');"
							+ System.getProperty(LINE_SEPARATOR) + SENSE_HAT_API_SHOW_TEXT_LED_MATRIX
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var joystickEventMiddle=function(event){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick event to the middle');"
							+ System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.sendCustomEvent('joystickEventMiddle');"
							+ System.getProperty(LINE_SEPARATOR) + SENSE_HAT_API_SHOW_TEXT_LED_MATRIX
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var onActionJoystickRight=function(data){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick action to the right');"
							+ System.getProperty(LINE_SEPARATOR) + "   senseHatApi.showTextLedMatrix('Right');"
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "function onActionJoystickLeft(data){\r\n"
							+ "   digitalTwinApi.log('Received joystick action to the left');"
							+ System.getProperty(LINE_SEPARATOR) + "   senseHatApi.showTextLedMatrix('Left');"
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var onActionJoystickUp=function(data){ " + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick action up');"
							+ System.getProperty(LINE_SEPARATOR) + "   senseHatApi.showTextLedMatrix('Up');"
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var onActionJoystickDown=function(data){" + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick action down');"
							+ System.getProperty(LINE_SEPARATOR) + "   senseHatApi.showTextLedMatrix('Down');"
							+ System.getProperty(LINE_SEPARATOR) + "}" + System.getProperty(LINE_SEPARATOR)
							+ "var onActionJoystickMiddle=function(data){ " + System.getProperty(LINE_SEPARATOR)
							+ "   digitalTwinApi.log('Received joystick action to the middle');"
							+ System.getProperty(LINE_SEPARATOR) + "   senseHatApi.showTextLedMatrix('Middle');"
							+ System.getProperty(LINE_SEPARATOR) + "}");

			final Set<PropertyDigitalTwinType> propertiesSensehat = createSensehatPropertiesDT(type);
			final Set<ActionsDigitalTwinType> actionsSensehat = createSensehatActionsDT(type);
			final Set<EventsDigitalTwinType> eventsSensehat = createSensehatEventsDT(type);
			type.setPropertyDigitalTwinTypes(propertiesSensehat);
			type.setActionDigitalTwinTypes(actionsSensehat);
			type.setEventDigitalTwinTypes(eventsSensehat);
			digitalTwinTypeRepository.save(type);
		}
	}

	private Set<EventsDigitalTwinType> createTurbineEventsDT(DigitalTwinType type) {
		final Set<EventsDigitalTwinType> events = new HashSet<>();
		EventsDigitalTwinType event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-1");
		event.setName("ping");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.PING);
		event.setDescription("Ping the platform to keepalive the device");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-2");
		event.setName("updateshadow");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.UPDATE_SHADOW);
		event.setDescription("Updates the shadow in the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-3");
		event.setName("log");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.LOG);
		event.setDescription("Log information in plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-4");
		event.setName("register");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.REGISTER);
		event.setDescription("Register the device into the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-5");
		event.setName("tempAlert");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send an Alarm when temperature is high.");
		event.setTypeId(type);
		events.add(event);

		return events;
	}

	private Set<ActionsDigitalTwinType> createTurbineActionsDT(DigitalTwinType type) {
		final Set<ActionsDigitalTwinType> actions = new HashSet<>();
		ActionsDigitalTwinType action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-1");
		action.setName("disconnectElectricNetwork");
		action.setDescription("Disconnect the turbine to the electric network to prevent problems");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-2");
		action.setName("connectElectricNetwork");
		action.setDescription("Connect the turbine to the electric network to provide power");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-3");
		action.setName("limitRotorSpeed");
		action.setDescription("Limits the rotor speed");
		action.setTypeId(type);
		actions.add(action);

		return actions;
	}

	private Set<PropertyDigitalTwinType> createTurbinePropertiesDT(DigitalTwinType type) {
		final Set<PropertyDigitalTwinType> props = new HashSet<>();
		PropertyDigitalTwinType prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-1");
		prop.setName("alternatorTemp");
		prop.setType(DOUBLE_STR);
		prop.setUnit("celsius");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature of the alternator");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-2");
		prop.setName("power");
		prop.setType(DOUBLE_STR);
		prop.setUnit("wat/h");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Current Power generated by the turbine");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-3");
		prop.setName("nacelleTemp");
		prop.setType(DOUBLE_STR);
		prop.setUnit("celsius");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature into the nacelle");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-4");
		prop.setName("rotorSpeed");
		prop.setType("int");
		prop.setUnit("rpm");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Rotor speed");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-5");
		prop.setName("maxRotorSpeed");
		prop.setType("int");
		prop.setUnit("rpm");
		prop.setDirection(Direction.IN_OUT);
		prop.setDescription("Max allowed speed for the rotor");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-6");
		prop.setName("windDirection");
		prop.setType("int");
		prop.setUnit("degrees");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Wind direction");
		prop.setTypeId(type);
		props.add(prop);

		return props;
	}

	private Set<EventsDigitalTwinType> createSensehatEventsDT(DigitalTwinType type) {
		final Set<EventsDigitalTwinType> events = new HashSet<>();
		EventsDigitalTwinType event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-1");
		event.setName("ping");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.PING);
		event.setDescription("Ping the platform to keepalive the device");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-2");
		event.setName("updateshadow");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.UPDATE_SHADOW);
		event.setDescription("Updates the shadow in the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-3");
		event.setName("log");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.LOG);
		event.setDescription("Log information in plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-4");
		event.setName("register");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.REGISTER);
		event.setDescription("Register the device into the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-5");
		event.setName("joystickEventMiddle");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send joystick event to the middle");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-6");
		event.setName("joystickEventRight");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send joystick event to the right");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-7");
		event.setName("joystickEventLeft");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send joystick event to the left");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-8");
		event.setName("joystickEventUp");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send joystick event up");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setId(MASTER_EVENTS_DT_TYPE + type.getIdentification() + "-9");
		event.setName("joystickEventDown");
		event.setStatus(true);
		event.setType(EventsDigitalTwinType.Type.OTHER);
		event.setDescription("Send joystick event down");
		event.setTypeId(type);
		events.add(event);

		return events;
	}

	private Set<ActionsDigitalTwinType> createSensehatActionsDT(DigitalTwinType type) {
		final Set<ActionsDigitalTwinType> actions = new HashSet<>();
		ActionsDigitalTwinType action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-1");
		action.setName("joystickUp");
		action.setDescription("Joystick action up");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-2");
		action.setName("joystickDown");
		action.setDescription("Joystick action down");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-3");
		action.setName("joystickLeft");
		action.setDescription("Joystick action to the left");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-4");
		action.setName("joystickRight");
		action.setDescription("Joystick action to the right");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setId(MASTER_ACTIONS_DT_TYPE + type.getIdentification() + "-5");
		action.setName("joystickMiddle");
		action.setDescription("Joystick action to the middle");
		action.setTypeId(type);
		actions.add(action);

		return actions;
	}

	private Set<PropertyDigitalTwinType> createSensehatPropertiesDT(DigitalTwinType type) {
		final Set<PropertyDigitalTwinType> props = new HashSet<>();
		PropertyDigitalTwinType prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-1");
		prop.setName("temperature");
		prop.setType(DOUBLE_STR);
		prop.setUnit("degrees");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-2");
		prop.setName("humidity");
		prop.setType(DOUBLE_STR);
		prop.setUnit("%");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Humidity");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setId(MASTER_PROPERTY_DT_TYPE + type.getIdentification() + "-3");
		prop.setName("pressure");
		prop.setType(DOUBLE_STR);
		prop.setUnit("milibars");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Pressure");
		prop.setTypeId(type);
		props.add(prop);

		return props;
	}

}
