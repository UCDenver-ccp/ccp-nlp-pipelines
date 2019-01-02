#!/bin/bash

# Evaluates ConceptMapper output against CRAFT CHEBI annotations

ONT_KEY="UBERON_EXT"
CM_DICT_KEY="UBERON_EXT"
OUTPUT_FILE_PREFIX="cm-craft-uberon_ext"
PARAM_INDEX=$1

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

. ./scripts/eval/cm-craft/_run-craft-eval.sh "$@"
