#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="GOBP"
CM_DICT_KEY="GO_BP"
OUTPUT_FILE_PREFIX="cm-craft-go_bp"
# precision-optimized param index = 47 
# f-score-optimized param index = 28
PARAM_INDEX=47

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
