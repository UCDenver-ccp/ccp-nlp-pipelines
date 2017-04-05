#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="SO"
CM_DICT_KEY="SO"
OUTPUT_FILE_PREFIX="cm-craft-so"
# precision-optimized param index = 191
# f-score-optimized param index = 31
PARAM_INDEX=191

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
