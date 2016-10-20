#!/usr/bin/env bash

set -e

cd run_dir

(
  java -jar ../out/artifacts/descriptors/metasar.jar input_data/MetaSAR_all_annotated_rxns.sdf \
  | tee descriptors.log
) 3>&1 1>&2 2>&3 | tee descriptors_errors.log

cd descriptors/
PATTERN='*.csv'
ALL_DATA=all.csv
rm -f $ALL_DATA
cat $PATTERN | egrep -v 'Mol' > $ALL_DATA
FILES=( $PATTERN )
echo -e "$(head -1 ${FILES[0]})\n$(cat $ALL_DATA)" > $ALL_DATA