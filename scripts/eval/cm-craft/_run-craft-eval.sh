#!/bin/bash

# Evaluates ConceptMapper output against CRAFT annotations

function print_usage {
    echo "Usage:"
    echo "$(basename $0) [OPTIONS]"
    echo "  [-d <dictionary-directory>]: The directory where ConceptMapper dictionaries will be cached."
    echo "  [-o <output-directory>>: The directory where evaluation output will be written."
    echo "  [-c <clean-dictionary-flag>]: If set then existing ConceptMapper dictionaries are overwritten and reconstructed."
}

function print_env_usage {
    echo "One or more of the following variables has not been set: "
    echo "ONT_KEY, CM_DICT_KEY, OUTPUT_FILE_PREFIX, PARAM_INDEX"
}

CLEAN_DICT_FLAG=false

while getopts "d:o:ch" OPTION; do
    case $OPTION in
        # The directory where dictionaries are cached.
        d) DICT_DIR=$OPTARG
           ;;
        # The directory where evaluation output is cached.
        o) OUTPUT_DIR=$OPTARG
           ;;
    	# The dictionary file clean flag
        c) CLEAN_DICT_FLAG=true
           ;;
        # HELP!
        h) print_usage; exit 0
           ;;
    esac
done

if [[ -z $DICT_DIR || -z $OUTPUT_DIR ]]; then
    print_usage
    exit 1
fi

if [[ -z $ONT_KEY || -z $CM_DICT_KEY || -z $OUTPUT_FILE_PREFIX || -z $PARAM_INDEX ]]; then
    print_env_usage
    exit 1
fi


if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

mvn -e -f scripts/eval/cm-craft/pom-cm-craft-eval.xml exec:exec \
        -DparamIndex=$PARAM_INDEX \
        -DdictDir=$DICT_DIR \
        -DoutputDir=$OUTPUT_DIR \
        -DcleanDictFlag=$CLEAN_DICT_FLAG \
        -DdictKey=$CM_DICT_KEY \
        -DcraftOntKey=$ONT_KEY \
        -DoutputFilePrefix=$OUTPUT_FILE_PREFIX

