#!/usr/bin/env bash

set -e

INPUT_FILE="input_data/CYP_DBs/HLM.sdf"
ERR_LOG="descriptors_errors_zaretzki.log"
STD_LOG="descriptors_zaretzki.log"

cd run_dir

(
  java -jar ../out/artifacts/descriptors/metasar.jar $INPUT_FILE \
  | tee $STD_LOG
) 3>&1 1>&2 2>&3 | tee $ERR_LOG

cd descriptors_zaretzki/
PATTERN='*.csv'
ALL_DATA=all.csv
rm -f $ALL_DATA
cat $PATTERN | egrep -v 'Mol' > $ALL_DATA
FILES=( $PATTERN )
echo -e "$(head -1 ${FILES[0]})\n$(cat $ALL_DATA)" > $ALL_DATA