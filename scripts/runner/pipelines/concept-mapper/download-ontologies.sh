#!/bin/bash

EXPECTED_ARGS=2

if [ $# -ne $EXPECTED_ARGS ]
then
    echo "#NOTE: due to CD'ing in script use absolute file names!!"
    echo "Usage: LOG_FILE TARGET_DOWNLOAD_DIR"
    echo "current usage:"
    echo $@
    exit 1
fi

LOG_FILE=$1
TARGET_DIR=$2

URLS="http://purl.obolibrary.org/obo/chebi.owl
http://purl.obolibrary.org/obo/cl.owl
http://purl.obolibrary.org/obo/doid.owl
http://purl.obolibrary.org/obo/ncbitaxon.owl
http://purl.obolibrary.org/obo/pr.owl
http://purl.obolibrary.org/obo/so.owl
http://purl.obolibrary.org/obo/uberon/ext.owl
http://purl.obolibrary.org/obo/go.owl"

mkdir -p $TARGET_DIR
cd $TARGET_DIR

#append forwardslash to target directory if it doesn't end in a slash already
case "$TARGET_DIR" in
*/)
;;
*)
TARGET_DIR=$(echo "$TARGET_DIR/")
;;
esac

#verify the log file
touch $LOG_FILE

exit_code=0
for url in $URLS
do
 echo "downloading $url"
 echo "downloading $URL"
 date | tee -a $LOG_FILE
 curl --remote-name --write-out "file: %{filename_effective} final-url: %{url_effective} size: %{size_download} time: %{time_total} final-time: " -L $url | tee -a $LOG_FILE
  e=$?
  if [ $e -ne 0 ]; then
    exit_code=$e
  fi
done

exit $exit_code