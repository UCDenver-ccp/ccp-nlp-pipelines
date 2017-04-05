#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="GOMF"
CM_DICT_KEY="GO_MF"
OUTPUT_FILE_PREFIX="cm-craft-go_mf"
# note, the optimized parameter indexes below are from an evaluation on CRAFT without the 
# dynamically generated synonyms of Funk et al.
# precision-optimized param index = 111, 159, 15, or 63
# f-score-optimized param index = 111, 159, 15, or 63
PARAM_INDEX=111

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
