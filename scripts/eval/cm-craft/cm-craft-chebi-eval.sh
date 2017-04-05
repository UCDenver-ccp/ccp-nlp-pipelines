#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="CHEBI"
CM_DICT_KEY="CHEBI"
OUTPUT_FILE_PREFIX="cm-craft-chebi"
# precision-optimized param index = 189 or 93 
# f-score-optimized param index = 13
PARAM_INDEX=189

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
