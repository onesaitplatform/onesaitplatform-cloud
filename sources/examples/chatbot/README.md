# Introduction

This project is an example of how to create a chatbot using Onesite Open Platform.

The example is based on an ontology about air quality:

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "GeoAirQuality",
    "type": "object",
    "required": [
        "GeoAirQuality"
    ],
    "properties": {
        "GeoAirQuality": {
            "type": "string",
            "$ref": "#/datos"
        }
    },
    "datos": {
        "description": "Info EmptyBase",
        "type": "object",
        "required": [
            "stationName",
            "stationId",
            "timestamp",
            "city",
            "pm25",
            "pm10",
            "o3",
            "no2",
            "so2",
            "geometry"
        ],
        "properties": {
            "stationName": {
                "type": "string"
            },
            "stationId": {
                "type": "string"
            },
            "timestamp": {
                "type": "object",
                "required": [
                    "$date"
                ],
                "properties": {
                    "$date": {
                        "type": "string",
                        "format": "date-time"
                    }
                }
            },
            "city": {
                "type": "string"
            },
            "pm25": {
                "type": "number"
            },
            "pm10": {
                "type": "number"
            },
            "o3": {
                "type": "number"
            },
            "no2": {
                "type": "number"
            },
            "so2": {
                "type": "number"
            },
            "geometry": {
                "type": "object"
            }
        }
    },
    "description": "GeoAirQuality",
    "additionalProperties": true
}
```

There is another example that populates this ontology. To be used, this example required data in this ontology.

The example includes several key points that have to be implemented.

## API REST

The creation of an API rest for the previous described ontology allows the chatbot to query some information about air quality. The queries used are in the file API-queries.md.

## Ontology for subscriptions

An ontology has been created to store subscription to the GeoAirQualityOntology.
It is named GeoAirQualitySubscriptions.
It has associated an API to Insert Subscriptions.

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "GeoAirQualitySubscriptions",
    "type": "object",
    "required": [
        "Subscription"
    ],
    "properties": {
        "Subscription": {
            "type": "string",
            "$ref": "#/datos"
        }
    },
    "datos": {
        "description": "Info EmptyBase",
        "type": "object",
        "required": [
            "stationName",
            "email",
            "no2Threshold",
            "o3Threshold",
            "so2Threshold"
        ],
        "properties": {
            "stationName": {
                "type": "string"
            },
            "email": {
                "type": "string"
            },
            "no2Threshold": {
                "type": "number"
            },
            "o3Threshold": {
                "type": "number"
            },
            "so2Threshold": {
                "type": "number"
            }
        }
    },
    "description": "Ontology to store subscritions to the GeoAirQuality ontology",
    "additionalProperties": true
}
```

## Dataflow for processing subscriptions

A dataflow has been created to process all the inserts in GeoAirQuality.
If a new data is inserted a process is executed that evaluates if there is a subscription for that data.
To do that, it is checked if any of the threshold of the subscription is exceded.
In that case an email is sent with the alert.

The dataflow json is in the file FlowEngine.json.

## Spring-boot aplication

An spring boot aplication has been created with a REST controller to allow the chat web page to send commands.
The spring boot application has the reponsability of process all the information and to interact with the chatbot engine.