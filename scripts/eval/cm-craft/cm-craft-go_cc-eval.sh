#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="GOCC"
CM_DICT_KEY="GO_CC"
OUTPUT_FILE_PREFIX="cm-craft-go_cc"
# precision-optimized param index = 31 
# f-score-optimized param index = 31
PARAM_INDEX=31

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
