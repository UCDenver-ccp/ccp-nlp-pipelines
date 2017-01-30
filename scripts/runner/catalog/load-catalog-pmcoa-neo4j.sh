#!/bin/bash

# This takes as input unpacked directories from the PMC OA bulk download and catalogs its contents in a neo4j catalog.
# In the process, the articles are transfered from the bulk directory to a two-level randomized directory structure
# mirroring that used on the PMC FTP servers (by using the structure detailed in the oa_file_list.txt 
# (ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_file_list.txt). Articles are assumed to be compressed (.gz) after they 
# were unpacked.
#
# NOTE: input arguments must be absolute paths

function print_usage {
    echo "Usage:"
    echo "$(basename $0) [OPTIONS]"
    echo "  [-l <library base directory>]: MUST BE ABSOLUTE PATH. the base file path where articles in the catalog are to be stored"
    echo "  [-c <catalog directory>]: MUST BE ABSOLUTE PATH. the base directory containing the neo4j repository"
    echo "  [-b <pmc bulk download directory>]: MUST BE ABSOLUTE PATH. the base directory housing the unpacked, but compressed, PMC OA .nxml files"
}

while getopts "l:c:b:h" OPTION; do
    case $OPTION in
        # The library base directory
        l) LIBRARY_DIR=$OPTARG
           ;;
        # The catalog directory
        c) CATALOG_DIR=$OPTARG
        ;;
        # The PMC OA bulk download directory
        b) PMC_BULK_DIR=$OPTARG
        ;;
        # HELP!
        h) print_usage; exit 0
        ;;
    esac
done

if [[ -z $LIBRARY_DIR || -z $CATALOG_DIR || -z $PMC_BULK_DIR ]]; then
	echo "missing input arguments!!!!!"
    print_usage
    exit 1
fi

if ! [[ -e README.md ]]; then
    echo "Please run from the root of the project."
    exit 1
fi

mvn -e -f nlp-pipelines-runner/pom.xml exec:java \
-Dexec.mainClass="edu.ucdenver.ccp.nlp.pipelines.runlog.init.Main_LoadCatalog_PMC_OA_neo4j" \
-Dexec.args="$LIBRARY_DIR $CATALOG_DIR $PMC_BULK_DIR" 
