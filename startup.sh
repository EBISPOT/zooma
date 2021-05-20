set -e

echo "Data dir:"
ls $ZOOMA_DATA_DIR

SCRIPT_DIR=/opt/tmp/bin/
cd $SCRIPT_DIR
bash build-rdf.sh
bash build-virtuoso-index.sh
bash virtuoso-start.sh
bash build-labels.sh
bash build-lucene-index.sh

catalina.sh run
