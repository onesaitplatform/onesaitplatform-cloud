# Examples of filters for metrics

This file shows some examples to filter metrics.

To filter metrics use the environment variable ```SDC_TELEGRAF_METRICS_JQ_FILTERS```

script variable usage:

```bash
pipelineMetrics=$(echo $pipelineMetrics | jq '. | '$SDC_TELEGRAF_METRICS_JQ_FILTERS'')
```

It is mandatory that the previous line returns a valid json.

## Basic example creating a new json

```bash
cat full-json-metrics-example.json | jq '. | {version}'
```

SDC_TELEGRAF_METRICS_JQ_FILTERS='{version}'

## Example returning an object with a key with dots

```bash
cat full-json-metrics-example.json | jq '. | .gauges."runner.0.gauge"'
```

SDC_TELEGRAF_METRICS_JQ_FILTERS='.gauges."runner.0.gauge"'

## Example returning a json created from two different objects

```bash
cat full-json-metrics-example.json | jq '. | {max: .gauges."jvm.memory.total.max", used: .gauges."jvm.memory.total.used"}'
```

## Example for input, output and error records from pipelines.

```bash
cat full-json-metrics-example.json | jq '. | {input: .meters."pipeline.batchInputRecords.meter", output: .meters."pipeline.batchOutputRecords.meter", errors: .meters."pipeline.batchErrorRecords.meter"}'
```

SDC_TELEGRAF_METRICS_JQ_FILTERS='{input: .meters."pipeline.batchInputRecords.meter", output: .meters."pipeline.batchOutputRecords.meter", errors: .meters."pipeline.batchErrorRecords.meter"}'
