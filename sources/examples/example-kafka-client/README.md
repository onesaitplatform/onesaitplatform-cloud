# Example of kafka client to insert data into an ontology

This example shows how to use a Kafka producer to insert data into an ontology in *onesait Platform*.

The IotBroker module implements a Kafka consumer that process the data inserted.
All the validations are performed by the IotBroker and after that by the Router.

## How to configure the example

The example implements only one class.
This is just a demonstrative example, so all the configuration is hard-coded in the class implemented.
In a more realistic example, all this configuration can be externalized.

The data used connect to Kafka topics are:

```java
private static String url = "host:port";
private static String deviceTemplate = "";
private static String token = "";
private static String prefix = "ontology_";
private static String ontology = "";
```

Where url is the url to connect to Kafka, deviceTemplate is the device temple used to connect to the platform, token is the token used by the device to authorize the insertion, prefix is used by the platform to identify the ontology topics, and ontology is the ontology name the created the topic.

NOTE: it is necessary to create the ontology with the Kafka insertion option enabled.

In the src/main/resources directory there is a file with data to make insertion tests.
The ontology used in this example uses the following schema:

```json
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "GestampMeasures",
    "type": "object",
    "required": [
        "value",
        "timeStamp",
        "signal",
        "deviceId",
        "profile",
        "deviceType"
    ],
    "properties": {
        "value": {
            "type": "string"
        },
        "timeStamp": {
            "type": "number"
        },
        "description": {
            "type": "string"
        },
        "signal": {
            "type": "string"
        },
        "deviceId": {
            "type": "string"
        },
        "profile": {
            "type": "string"
        },
        "deviceType": {
            "type": "string"
        },
        "signalId": {
            "type": "string"
        }
    },
    "description": "Ontology for inserting measures using kafka",
    "additionalProperties": true
}
```

# Things to highlight

It is important to use `producer.flush()` before end the program to be sure that all insertions have been done.