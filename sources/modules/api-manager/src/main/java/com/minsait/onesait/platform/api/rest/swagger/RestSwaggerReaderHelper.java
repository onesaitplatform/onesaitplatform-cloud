/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2023 SPAIN
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
package com.minsait.onesait.platform.api.rest.swagger;

import java.util.List;

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

public class RestSwaggerReaderHelper {

	private static final String STRING_TYPE = "string";

	public static Parameter populateParameter(Swagger swagger, String name, String description, boolean required,
			String parameterType, String dataType, String arrayType, List<String> value) {
		Parameter parameter = RestSwaggerReaderHelper.getParameterType(parameterType);

		if (parameter != null) {
			parameter.setName(name);
			parameter.setDescription(description);
			parameter.setRequired(required);

			if (parameter instanceof SerializableParameter) {
				RestSwaggerReaderHelper.populateSerializableParameter(value, parameter, dataType, arrayType);
			}

			// set schema on body parameter
			if (parameter instanceof BodyParameter) {
				final BodyParameter bp = (BodyParameter) parameter;

				RestSwaggerReaderHelper.processBodyParameter(dataType, swagger, bp);
			}

		}
		return parameter;
	}

	private static void populateSerializableParameter(List<String> value, Parameter parameter, String dataType,
			String arrayType) {
		final SerializableParameter serializableParameter = (SerializableParameter) parameter;

		if (value != null && (!value.isEmpty())) {
			((SerializableParameter) parameter).setEnumValue(value);
		}

		if (dataType != null) {
			serializableParameter.setType(dataType);

			if (dataType.equalsIgnoreCase("array")) {
				RestSwaggerReaderHelper.setArrayType(arrayType, serializableParameter);
			}
			if (isStringType(dataType)) {
				serializableParameter.setType(STRING_TYPE);
				serializableParameter.setFormat(dataType);
			}

		}

	}

	private static void processBodyParameter(String dataType, Swagger swagger, BodyParameter bp) {
		if (dataType != null) {
			if (dataType.endsWith("[]")) {
				String typeName = dataType;
				typeName = typeName.substring(0, typeName.length() - 2);
				final Property prop = modelTypeAsProperty(typeName, swagger);
				final ArrayModel arrayModel = new ArrayModel();
				arrayModel.setItems(prop);
				bp.setSchema(arrayModel);
			} else {
				final String ref = modelTypeAsRef(dataType, swagger);
				if (ref != null) {
					bp.setSchema(new RefModel(ref));
				} else {

					final StringModel arrayModel = new StringModel();
					arrayModel.setType(STRING_TYPE);
					bp.setSchema(arrayModel);
				}
			}
		}
	}

	private static Parameter getParameterType(String parameterType) {
		Parameter parameter = null;
		if (parameterType.equalsIgnoreCase("body")) {
			parameter = new BodyParameter();
		} else if (parameterType.equalsIgnoreCase("formData")) {
			parameter = new FormParameter();
		} else if (parameterType.equalsIgnoreCase("header")) {
			parameter = new HeaderParameter();
		} else if (parameterType.equalsIgnoreCase("path")) {
			parameter = new PathParameter();
		} else if (parameterType.equalsIgnoreCase("query")) {
			parameter = new QueryParameter();
		}

		return parameter;
	}

	private static void setArrayType(String arrayType, SerializableParameter serializableParameter) {
		if (arrayType != null) {
			if (arrayType.equalsIgnoreCase(STRING_TYPE)) {
				serializableParameter.setItems(new StringProperty());
			}
			if (arrayType.equalsIgnoreCase("int") || arrayType.equalsIgnoreCase("integer")) {
				serializableParameter.setItems(new IntegerProperty());
			}
			if (arrayType.equalsIgnoreCase("long")) {
				serializableParameter.setItems(new LongProperty());
			}
			if (arrayType.equalsIgnoreCase("float")) {
				serializableParameter.setItems(new FloatProperty());
			}
			if (arrayType.equalsIgnoreCase("double")) {
				serializableParameter.setItems(new DoubleProperty());
			}
			if (arrayType.equalsIgnoreCase("boolean")) {
				serializableParameter.setItems(new BooleanProperty());
			}
		}
	}

	private static boolean isStringType(String dataType) {
		return dataType.equalsIgnoreCase("date") || dataType.equalsIgnoreCase("date-time")
				|| dataType.equalsIgnoreCase("object") || dataType.equalsIgnoreCase("password")
				|| dataType.equalsIgnoreCase("byte") || dataType.equalsIgnoreCase("binary")
				|| dataType.equalsIgnoreCase("email") || dataType.equalsIgnoreCase("uuid")
				|| dataType.equalsIgnoreCase("uri") || dataType.equalsIgnoreCase("hostname")
				|| dataType.equalsIgnoreCase("ipv4");
	}

	private static Property modelTypeAsProperty(String typeName, Swagger swagger) {
		boolean array = typeName.endsWith("[]");
		if (array) {
			typeName = typeName.substring(0, typeName.length() - 2);
		}

		final String ref = modelTypeAsRef(typeName, swagger);

		Property prop;

		if (ref != null) {
			prop = new RefProperty(ref);
		} else {
			prop = processSpecialByteArrays(typeName, array);
			array = isArray(typeName, array);

		}

		if (array) {
			return new ArrayProperty(prop);
		} else {
			return prop;
		}
	}

	private static boolean isArray(String typeName, boolean array) {
		if (array && ("byte".equals(typeName) || "java.lang.Byte".equals(typeName))) {
			array = false;
		}
		return array;
	}

	private static Property processSpecialByteArrays(String typeName, boolean array) {
		Property prop;
		if (array && ("byte".equals(typeName) || "java.lang.Byte".equals(typeName))) {
			prop = new ByteArrayProperty();
		} else if ("java.lang.String".equals(typeName)) {
			prop = new StringProperty();
		} else if ("int".equals(typeName) || "java.lang.Integer".equals(typeName)) {
			prop = new IntegerProperty();
		} else if ("long".equals(typeName) || "java.lang.Long".equals(typeName)) {
			prop = new LongProperty();
		} else if ("float".equals(typeName) || "java.lang.Float".equals(typeName)) {
			prop = new FloatProperty();
		} else if ("double".equals(typeName) || "java.lang.Double".equals(typeName)) {
			prop = new DoubleProperty();
		} else if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
			prop = new BooleanProperty();
		} else {
			prop = new StringProperty(typeName);
		}

		return prop;
	}

	private static String modelTypeAsRef(String typeName, Swagger swagger) {
		final boolean array = typeName.endsWith("[]");
		if (array) {
			typeName = typeName.substring(0, typeName.length() - 2);
		}

		final Model model = asModel(typeName, swagger);
		if (model != null) {
			typeName = ((ModelImpl) model).getName();
			return typeName;
		}

		return null;
	}

	private static Model asModel(String typeName, Swagger swagger) {
		final boolean array = typeName.endsWith("[]");
		if (array) {
			typeName = typeName.substring(0, typeName.length() - 2);
		}

		if (swagger.getDefinitions() != null) {
			for (final Model model : swagger.getDefinitions().values()) {
				final StringProperty modelType = (StringProperty) model.getVendorExtensions().get("x-className");
				if (modelType != null && typeName.equals(modelType.getFormat())) {
					return model;
				}
			}
		}
		return null;
	}

}
