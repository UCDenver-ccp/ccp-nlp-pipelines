#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="CL"
CM_DICT_KEY="CL"
OUTPUT_FILE_PREFIX="cm-craft-cl"
# precision-optimized param index = 143 
# f-score-optimized param index = 31
PARAM_INDEX=143

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
