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
package com.minsait.onesait.platform.config.services.ontologydata;

public class TestResources {
		
	static final String LONG_SCHEMA_WITH_ENCRYPTED = "{\n" + 
			"    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" + 
			"    \"title\": \"aaaaaa\",\n" + 
			"    \"type\": \"object\",\n" + 
			"    \"required\": [\n" + 
			"        \"Feed\"\n" + 
			"    ],\n" + 
			"    \"properties\": {\n" + 
			"        \"Feed\": {\n" + 
			"            \"type\": \"string\",\n" + 
			"            \"$ref\": \"#/datos\"\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"datos\": {\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"assetId\",\n" + 
			"            \"assetType\",\n" + 
			"            \"assetSource\",\n" + 
			"            \"type\",\n" + 
			"            \"timestamp\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"feedId\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"feedSource\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"feedGroup\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"assetId\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"assetType\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"assetSource\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"assetGroup\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"assetName\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            },\n" + 
			"            \"type\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"FIXED\",\n" + 
			"                    \"MOBILE\",\n" + 
			"                    \"VIRTUAL\",\n" + 
			"                    \"FORECAST\"\n" + 
			"                ]\n" + 
			"            },\n" + 
			"            \"timestamp\": {\n" + 
			"                \"type\": \"object\",\n" + 
			"                \"required\": [\n" + 
			"                    \"$date\"\n" + 
			"                ],\n" + 
			"                \"properties\": {\n" + 
			"                    \"$date\": {\n" + 
			"                        \"type\": \"string\",\n" + 
			"                        \"format\": \"date-time\"\n" + 
			"                    }\n" + 
			"                },\n" + 
			"                \"additionalProperties\": false\n" + 
			"            },\n" + 
			"            \"image\": {\n" + 
			"                \"type\": \"object\",\n" + 
			"                \"required\": [\n" + 
			"                    \"data\",\n" + 
			"                    \"media\"\n" + 
			"                ],\n" + 
			"                \"properties\": {\n" + 
			"                    \"data\": {\n" + 
			"                        \"type\": \"string\"\n" + 
			"                    },\n" + 
			"                    \"media\": {\n" + 
			"                        \"type\": \"object\",\n" + 
			"                        \"required\": [\n" + 
			"                            \"name\",\n" + 
			"                            \"storageArea\",\n" + 
			"                            \"binaryEncoding\",\n" + 
			"                            \"mime\"\n" + 
			"                        ],\n" + 
			"                        \"properties\": {\n" + 
			"                            \"name\": {\n" +
			"                                \"encrypted\": true,\n" + //Encrypted several levels
			"                                \"type\": \"string\"\n" + 
			"                            },\n" + 
			"                            \"storageArea\": {\n" + 
			"                                \"type\": \"string\",\n" + 
			"                                \"enum\": [\n" + 
			"                                    \"SERIALIZED\",\n" + 
			"                                    \"DATABASE\",\n" + 
			"                                    \"URL\"\n" + 
			"                                ]\n" + 
			"                            },\n" + 
			"                            \"binaryEncoding\": {\n" + 
			"                                \"type\": \"string\",\n" + 
			"                                \"enum\": [\n" + 
			"                                    \"Base64\"\n" + 
			"                                ]\n" + 
			"                            },\n" + 
			"                            \"mime\": {\n" + 
			"                                \"type\": \"string\",\n" + 
			"                                \"enum\": [\n" + 
			"                                    \"application/pdf\",\n" + 
			"                                    \"image/jpeg\",\n" + 
			"                                    \"image/png\",\n" + 
			"                                    \"image/bmp\"\n" + 
			"                                ]\n" + 
			"                            }\n" + 
			"                        }\n" + 
			"                    }\n" + 
			"                },\n" + 
			"                \"additionalProperties\": false\n" + 
			"            },\n" + 
			"            \"attribs\": {\n" + 
			"                \"type\": \"array\",\n" + 
			"                \"items\": {\n" + 
			"                    \"type\": \"object\",\n" + 
			"                    \"required\": [\n" + 
			"                        \"name\",\n" + 
			"                        \"value\"\n" + 
			"                    ],\n" + 
			"                    \"properties\": {\n" + 
			"                        \"name\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"value\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        }\n" + 
			"                    },\n" + 
			"                    \"additionalProperties\": false\n" + 
			"                },\n" + 
			"                \"minItems\": 0\n" + 
			"            },\n" + 
			"            \"geometry\": {\n" + 
			"                \"type\": \"object\",\n" + 
			"                \"oneOf\": [\n" + 
			"                    {\n" + 
			"                        \"$ref\": \"#/point\"\n" + 
			"                    },\n" + 
			"                    {\n" + 
			"                        \"$ref\": \"#/linestring\"\n" + 
			"                    },\n" + 
			"                    {\n" + 
			"                        \"$ref\": \"#/polygon\"\n" + 
			"                    }\n" + 
			"                ]\n" + 
			"            },\n" + 
			"            \"measuresTimestamp\": {\n" + 
			"                \"type\": \"object\",\n" + 
			"                \"required\": [\n" + 
			"                    \"$date\"\n" + 
			"                ],\n" + 
			"                \"properties\": {\n" + 
			"                    \"$date\": {\n" + 
			"                        \"type\": \"string\",\n" + 
			"                        \"format\": \"date-time\"\n" + 
			"                    }\n" + 
			"                },\n" + 
			"                \"additionalProperties\": false\n" + 
			"            },\n" + 
			"            \"measuresTimestampEnd\": {\n" + 
			"                \"type\": \"object\",\n" + 
			"                \"required\": [\n" + 
			"                    \"$date\"\n" + 
			"                ],\n" + 
			"                \"properties\": {\n" + 
			"                    \"$date\": {\n" + 
			"                        \"type\": \"string\",\n" + 
			"                        \"format\": \"date-time\"\n" + 
			"                    }\n" + 
			"                },\n" + 
			"                \"additionalProperties\": false\n" + 
			"            },\n" + 
			"            \"measuresType\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"INSTANT\",\n" + 
			"                    \"CUMULATIVE\",\n" + 
			"                    \"PULSE\"\n" + 
			"                ]\n" + 
			"            },\n" + 
			"            \"measuresPeriod\": {\n" + 
			"                \"type\": \"number\"\n" + 
			"            },\n" + 
			"            \"measuresPeriodUnit\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"m\",\n" + 
			"                    \"s\",\n" + 
			"                    \"h\",\n" + 
			"                    \"d\"\n" + 
			"                ]\n" + 
			"            },\n" + 
			"            \"measures\": {\n" + 
			"                \"type\": \"array\",\n" + 
			"                \"encrypted\": true,\n" +
			"                \"items\": {\n" + 
			"                    \"type\": \"object\",\n" + 
			"                    \"required\": [\n" + 
			"                        \"measure\",\n" + 
			"                        \"unit\",\n" + 
			"                        \"method\"\n" + 
			"                    ],\n" + 
			"                    \"properties\": {\n" + 
			"                        \"name\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"surName\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"desc\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"unit\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"measure\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"method\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"oldMeasure\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"changeDesc\": {\n" + 
			"                            \"type\": \"string\"\n" + 
			"                        },\n" + 
			"                        \"changeTimestamp\": {\n" + 
			"                            \"type\": \"object\",\n" + 
			"                            \"required\": [\n" + 
			"                                \"$date\"\n" + 
			"                            ],\n" + 
			"                            \"properties\": {\n" + 
			"                                \"$date\": {\n" + 
			"                                    \"type\": \"string\",\n" + 
			"                                    \"format\": \"date-time\"\n" + 
			"                                }\n" + 
			"                            },\n" + 
			"                            \"additionalProperties\": false\n" + 
			"                        }\n" + 
			"                    },\n" + 
			"                    \"additionalProperties\": false\n" + 
			"                }\n" + 
			"            }\n" + 
			"        },\n" + 
			"        \"additionalProperties\": false\n" + 
			"    },\n" + 
			"    \"point\": {\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"coordinates\",\n" + 
			"            \"type\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"coordinates\": {\n" + 
			"                \"type\": \"array\",\n" + 
			"                \"encrypted\": true, \n" + //Encryption of arrays is supported
			"                \"items\": [\n" + 
			"                    {\n" + 
			"                        \"type\": \"number\",\n" + 
			"                        \"encrypted\": true, \n" +  //Direct Encryption into Array is not supported
			"                        \"maximum\": 180,\n" + 
			"                        \"minimum\": -180\n" + 
			"                    },\n" + 
			"                    {\n" + 
			"                        \"type\": \"number\",\n" + 
			"                        \"encrypted\": true, \n" +  //Direct Encryption into Array is not supported
			"                        \"maximum\": 90,\n" + 
			"                        \"minimum\": -90\n" + 
			"                    }\n" + 
			"                ],\n" + 
			"                \"minItems\": 2,\n" + 
			"                \"maxItems\": 2\n" + 
			"            },\n" + 
			"            \"type\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"Point\"\n" + 
			"                ]\n" + 
			"            }\n" + 
			"        },\n" + 
			"        \"additionalProperties\": false\n" + 
			"    },\n" + 
			"    \"linestring\": {\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"coordinates\",\n" + 
			"            \"type\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"coordinates\": {\n" + 
			"                \"type\": \"array\",\n" + 
			"                \"items\": {\n" + 
			"                    \"type\": \"array\",\n" + 
			"                    \"items\": [\n" + 
			"                        {\n" + 
			"                            \"type\": \"number\",\n" + 
			"                            \"maximum\": 180,\n" + 
			"                            \"minimum\": -180\n" + 
			"                        },\n" + 
			"                        {\n" + 
			"                            \"type\": \"number\",\n" + 
			"                            \"maximum\": 90,\n" + 
			"                            \"minimum\": -90\n" + 
			"                        }\n" + 
			"                    ],\n" + 
			"                    \"minItems\": 2,\n" + 
			"                    \"maxItems\": 2\n" + 
			"                },\n" + 
			"                \"minItems\": 2\n" + 
			"            },\n" + 
			"            \"type\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"LineString\"\n" + 
			"                ]\n" + 
			"            }\n" + 
			"        },\n" + 
			"        \"additionalProperties\": false\n" + 
			"    },\n" + 
			"    \"polygon\": {\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"coordinates\",\n" + 
			"            \"type\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"coordinates\": {\n" + 
			"                \"type\": \"array\",\n" + 
			"                \"items\": {\n" + 
			"                    \"type\": \"array\",\n" + 
			"                    \"items\": [\n" + 
			"                        {\n" + 
			"                            \"type\": \"number\",\n" + 
			"                            \"maximum\": 180,\n" + 
			"                            \"minimum\": -180\n" + 
			"                        },\n" + 
			"                        {\n" + 
			"                            \"type\": \"number\",\n" + 
			"                            \"maximum\": 90,\n" + 
			"                            \"minimum\": -90\n" + 
			"                        }\n" + 
			"                    ],\n" + 
			"                    \"minItems\": 3\n" + 
			"                }\n" + 
			"            },\n" + 
			"            \"type\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"enum\": [\n" + 
			"                    \"Polygon\"\n" + 
			"                ]\n" + 
			"            }\n" + 
			"        },\n" + 
			"        \"additionalProperties\": false\n" + 
			"    },\n" + 
			"    \"description\": \"aaaaaa\"\n" + 
			"}";
	
	
	
	static final String SMALL_SCHEMA_WITH_ENCRYPTION = "{\n" + 
			"    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" + 
			"    \"title\": \"TMS-Commands\",\n" + 
			"    \"type\": \"object\",\n" + 
			"    \"required\": [\n" + 
			"        \"Command\"\n" + 
			"    ],\n" + 
			"    \"properties\": {\n" + 
			"        \"Command\": {\n" + 
			"            \"type\": \"string\",\n" + 
			"            \"$ref\": \"#/datos\"\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"datos\": {\n" + 
			"        \"description\": \"Info EmptyBase\",\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"id\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"id\": {\n" + 
			"                \"type\": \"string\",\n" + 
			"                \"encrypted\": true\n" +  //Encrypted property
			"            }\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"description\": \"Ontologia para comandos de TMS\",\n" + 
			"    \"additionalProperties\": true\n" + 
			"}";
	
	static final String BAD_JSON_SCHEMA = "{\n" + 
			"    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" + 
			"    \"title\": \"TMS-Commands\",\n" + 
			"    \"type\": \"object\",\n" + 
			"    \"required\": [\n" + 
			"        \"Command\"\n" + 
			"    ],\n" + 
			"    \"properties\": {\n" + 
			"        \"Command\": {\n" + 
			"            \"type\": \"string\",\n" + 
			"            \"$ref\": \"#/datos\"\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"datos\": {\n" + 
			"        \"description\": \"Info EmptyBase\",\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"ERROR_ID_NAME\"\n" +   //Error in the id name
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"id\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            }\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"description\": \"Ontologia para comandos de TMS\",\n" + 
			"    \"additionalProperties\": true\n" + 
			"}";
	
	static final String GOOD_JSON_SCHEMA = "{\n" + 
			"    \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" + 
			"    \"title\": \"TMS-Commands\",\n" + 
			"    \"type\": \"object\",\n" + 
			"    \"required\": [\n" + 
			"        \"Command\"\n" + 
			"    ],\n" + 
			"    \"properties\": {\n" + 
			"        \"Command\": {\n" + 
			"            \"type\": \"string\",\n" + 
			"            \"$ref\": \"#/datos\"\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"datos\": {\n" + 
			"        \"description\": \"Info EmptyBase\",\n" + 
			"        \"type\": \"object\",\n" + 
			"        \"required\": [\n" + 
			"            \"id\"\n" + 
			"        ],\n" + 
			"        \"properties\": {\n" + 
			"            \"id\": {\n" + 
			"                \"type\": \"string\"\n" + 
			"            }\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"description\": \"Ontologia para comandos de TMS\",\n" + 
			"    \"additionalProperties\": true\n" + 
			"}";
	
	
	//DATA
	static final String DATA_FOR_GOOD_JSON = "{\"Command\":{ \"id\":\"string\"}}";
	static final String DATA_FOR_GOOD_ARRAY_JSON = "[{\"Command\":{ \"id\":\"string\"}},{\"Command\":{ \"id\":\"string\"}}]";
	
	static final String DATA_FOR_NONVALID_JSON = "{\"Something\":{ \"id\":\"string\"}}"; //Something is not declared in the schema
	
	static final String DATA_FOR_BAD_JSON = "{Something\":{ \"id\":\"string\"}}"; //invalid JSON. Something should be surrounded by quotes.
	
	static final String DATA_FOR_LONG_SCHEMA_TO_ENCRYPT = "{"
			+ "\"Feed\": {"
			+ "     \"feedId\":\"string\","
			+ "     \"feedSource\":\"string\","
			+ "     \"feedGroup\":\"string\","
			+ "     \"assetId\":\"string\","
			+ "     \"assetType\":\"string\","
			+ "     \"assetSource\":\"string\","
			+ "     \"assetGroup\":\"string\","
			+ "     \"assetName\":\"string\","
			+ "     \"type\":\"FIXED\","
			+ "     \"timestamp\": {\"$date\": \"2014-01-30T17:14:00Z\"},"
			+ "     \"image\":{"
			+ "         \"data\":\"string\","
			+ "         \"media\":{"
			+ "            \"name\":\"fichero.pdf\","
			+ "            \"storageArea\":\"SERIALIZED\","
			+ "            \"binaryEncoding\":\"Base64\","
			+ "            \"mime\":\"application/pdf\"}},"
			+ "     \"attribs\":[],"
			+ "     \"geometry\":{ \"type\":\"Point\", \"coordinates\":[9,19.3]},"
			+ "         \"measuresTimestamp\":{\"$date\": \"2014-01-30T17:14:00Z\"},"
			+ "         \"measuresTimestampEnd\":{\"$date\": \"2014-01-30T17:14:00Z\"},"
			+ "         \"measuresType\":\"INSTANT\","
			+ "         \"measuresPeriod\":28.6,"
			+ "         \"measuresPeriodUnit\":\"m\","
			+ "         \"measures\":["
			+ "	           {\"name\":\"string1\","
			+ "             \"surName\":\"string\","
			+ "             \"desc\":\"string\","
			+ "             \"unit\":\"string\","
			+ "             \"measure\":\"string\","
			+ "             \"method\":\"string\","
			+ "             \"oldMeasure\":\"string\","
			+ "             \"changeDesc\":\"string\","
			+ "             \"changeTimestamp\":{\"$date\": \"2014-01-30T17:14:00Z\"}"
			+ "            },"
			+ "	           {\"name\":\"string2\","
			+ "             \"surName\":\"string\","
			+ "             \"desc\":\"string\","
			+ "             \"unit\":\"string\","
			+ "             \"measure\":\"string\","
			+ "             \"method\":\"string\","
			+ "             \"oldMeasure\":\"string\","
			+ "             \"changeDesc\":\"string\","
			+ "             \"changeTimestamp\":{\"$date\": \"2014-01-30T17:14:00Z\"}"
			+ "            }"
			+ "          ]"
			+ "     }}";
}
