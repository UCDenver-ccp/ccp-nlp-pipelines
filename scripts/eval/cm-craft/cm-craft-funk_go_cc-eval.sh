#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="GOCC"
CM_DICT_KEY="FUNK_GO_CC"
OUTPUT_FILE_PREFIX="cm-craft-funk_go_cc"
# note, the optimized parameter indexes below are from an evaluation on CRAFT without the 
# dynamically generated synonyms of Funk et al.
# precision-optimized param index = 31 
# f-score-optimized param index = 31
PARAM_INDEX=31

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
