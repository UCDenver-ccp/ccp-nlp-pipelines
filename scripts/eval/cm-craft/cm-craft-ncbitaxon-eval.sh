#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="NCBITAXON"
CM_DICT_KEY="NCBI_TAXON"
OUTPUT_FILE_PREFIX="cm-craft-ncbitaxon"
# precision-optimized param index = 279 or 375 
# f-score-optimized param index = 535
PARAM_INDEX=279

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
