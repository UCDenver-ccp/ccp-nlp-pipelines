#!/bin/bash

# <ontology>
JAVA_ARGS="$@"

JAVA_ARGS="/home/dev/input /home/dev/output ${JAVA_ARGS} /home/dev/ontology.file /home/dev/dict false"

echo $JAVA_ARGS
mvn -f /home/dev/ccp-nlp-pipelines.git/nlp-pipelines-conceptmapper/pom.xml exec:java -Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.conceptmapper.EntityFinder" -Dexec.args="${JAVA_ARGS}"
