#!/usr/bin/env bash

set -e

cd run_dir

(
  java -jar ../out/artifacts/descriptors/metasar.jar MetaSAR_all_annotated.sdf \
  | tee descriptors.log
) 3>&1 1>&2 2>&3 | tee descriptors_errors.log

cd data
PATTERN='*.csv'
rm -f all_data.csv
cat $PATTERN | egrep -v 'Mol' > all_data.csv
FILES=( $PATTERN )
echo -e "$(head -1 ${FILES[0]})$(cat all_data.csv)" > all_data.csv