#!/bin/bash

# Directory to store temporal files.
tempDir=/tmp/sdc_telegraf_tmpFiles

# Remove any file in the directory.
rm -f $tempDir/*

# Remove any temp file when the program exits
trap "rm -f $tempDir/*; exit" EXIT TERM

# to create the named pipe if it does not exist
if [[ ! -p tempDir ]]; then
    mkdir -p $tempDir
fi

# number of processes 
SDC_TELEGRAF_NUM_PROCESSES="${SDC_TELEGRAF_NUM_PROCESSES:-20}"

# use $LABEL value or "monitored" if $LABEL is not defined
label="${LABEL:-monitored}"

# JSON array of hosts, instance names must be unique
# valid examples
# STREAMSETS_INSTANCES='{"sdc1": {"name": "sdc1", "host": "streamsets", "port": 18630}}'
# STREAMSETS_INSTANCES='{"sdc1": {"name": "sdc1", "host": "streamsets", "port": 18630},"sdc2": {"name": "sdc2", "host": "streamsets", "port": 18630}}'
metrics=$STREAMSETS_INSTANCES

if [ -z "$metrics" ]; then
    echo "It is mandatory to define almost one streamsets instance"
    exit 1
fi

instances=$(echo $metrics | jq -r '. | keys[]')
#echo $instances

# Export from one instance
# $1 is host
# $2 is port
# $3 is label
function getInstancePipelines () {
    host=$1
    port=$2
    label=$3
    pipelinesJSON=$(curl -s "http://$host:$port/rest/v1/pipelines?label=$label" -u $SDC_USER:$SDC_PASS --connect-timeout 10)
    #echo $pipelinesJSON

    #In tow steps for debug
    #pipelines1=$(echo $pipelinesJSON | jq '[.[] | {title, pipelineId}]')
    #pipelines2=$(echo $pipelines | jq  'map({(.title): .}) | add')

    pipelines=$(echo $pipelinesJSON | jq '[.[] | {title, pipelineId}] | map({(.title): .}) | add')

    echo $pipelines
}

# Export one pipeline 
# $1 is the streamsets instance name
# $2 is the pipeline title
# $3 is the pipeline id
# $4 is the host
# $5 is the port
# $6 is the named pipe to write the output
function getPipeLineMetrics () {
    local instance=$1
    local pipelineTitle=$2
    local pipelineId=$3
    local host=$4
    local port=$5
    local tempFile=$6

    #DEBUG

    pipelineMetrics=$(curl -s "http://$host:$port/rest/v1/pipeline/$pipelineId/metrics?onlyIfExists=true" -u $SDC_USER:$SDC_PASS --connect-timeout 10)
    
    #If streamsets api does not returns anything 
    if [ ! -z "$pipelineMetrics" ]; then

        

        if [ ! -z "$SDC_TELEGRAF_METRICS_JQ_FILTERS" ]; then            
            pipelineMetrics=$(echo $pipelineMetrics | jq ". | $SDC_TELEGRAF_METRICS_JQ_FILTERS")
        fi

        pipelineMetrics=$(echo '{}' | jq --arg name "$instance" \
                                        --arg host "$host" \
                                        --argjson port "$port" \
                                        --arg pipelineTitle "$pipelineTitle" \
                                        --arg pipelineId "$pipelineId" \
                                        --argjson pipelineMetrics "$pipelineMetrics" \
                                        '. + {'$instance': {name: $name, host: $host, port: $port, pipelines: {}}} | 
                                        .[].pipelines[$pipelineTitle] = {title: $pipelineTitle, id: $pipelineId, metrics: $pipelineMetrics}')

        # it writes the output usng base64 to avoid special characters problems.
        echo $pipelineMetrics >$tempFile
    fi
}

# matrix (in fact it is not a matrix) of temporal files
declare -A temporalFiles

# variables to manage number of concurrent jobs
num_procs=$SDC_TELEGRAF_NUM_PROCESSES    # number of concurrent calls
num_jobs="\j"  # The prompt escape for number of jobs currently running

#indexes for arrays
i=0
for instance in $instances; do
    host=$(echo $metrics | jq -r ".$instance.host")
    port=$(echo $metrics | jq -r ".$instance.port")
    #echo $instance "###" $host "###" $port
    
    pipelines=$(getInstancePipelines "$host" "$port" "$label")
    #echo $pipelines

    pipelineTitles=$(echo $pipelines | jq "keys[]")
    #echo $pipelineTitles

    #To deal with spaces in the titles
    eval "array=($pipelineTitles)"

    for pipelineTitle in "${array[@]}"; do
        # For parallel calls
        while (( ${num_jobs@P} >= num_procs )); do
            wait -n
        done

        pipelineId=$(echo $pipelines | jq -r ".\"$pipelineTitle\".pipelineId")
        #echo $pipelineId

        temporalFiles[$i]=$(mktemp --tmpdir=$tempDir)

        getPipeLineMetrics "$instance" "$pipelineTitle" "$pipelineId" "$host" "$port" "${temporalFiles[$i]}" &
        ((i++))
    done
done
wait # it waits for all the processes inside this subshell

result='{}'

for pip in "${!temporalFiles[@]}"; do
    #echo "key  : $pip"
    #echo "value: ${temporalFiles[$pip]}"
    partialjson=$(cat ${temporalFiles[$pip]})
    if [ ! -z "$partialjson" ]; then
        #echo $partialjson
        result=$( (echo $result; echo $partialjson) | jq -s '.[0] * .[1]')
    fi
done

#this is the final result. Only will be wroten when the finish token is receive
echo $result


exit 0

