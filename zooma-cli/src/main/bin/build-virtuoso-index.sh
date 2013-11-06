#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: build-virtuoso-index.sh [DIRECTORY] [PORT] [THREADS]"
  echo "Number of CPU processes is optional"
}

# parse user supplied arguments
exitVal=0
while getopts "r:d:h" opt; do
  case $opt in
    h)
      usage
      exit 0
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      usage
      (( exitVal++ ))
      ;;
  esac
done


    if [ ! $VIRTUOSO_HOME ] ; then
        echo "VIRTUOSO_HOME variable not set"
        exit 1
    fi
    if [ ! -f $VIRTUOSO_HOME/bin/isql ] ; then
        echo "Can't find virtuoso start scripts in $VIRTUOSO_HOME"
        exit 1
    fi

build_dir=$1
port=$2
threads=$3

    if [ ! $build_dir ] ; then
        echo "You must supply a build directory"
        usage
        exit 1
    fi
    if [ ! $port ] ; then
        echo "You must supply a port on localhost where virtuoso is running"
        usage
        exit 1
    fi
    if [ ! -f $build_dir/virtuoso/db/virtuoso.lck ] ; then
            echo "Virtuoso instance not running"
            exit 1
    fi
    if [ ! -d $build_dir/data/zooma ] ; then
            echo "Can't find data directory $build_dir/data/zooma"
            exit 1
    fi


loadfiles="ld_dir_all('$build_dir/data/zooma', '*.rdf', 'http://rdf.ebi.ac.uk/dataset/zooma');"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" || exit 4;

loadfiles="ld_dir_all('$build_dir/data/zooma', '*.owl', 'http://rdf.ebi.ac.uk/dataset/zooma');"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" || exit 4;

echo "finished setting files to load, starting loader..."


if [ $threads ] ; then

    # We want 1 fewer loader than the number of CPUs
    echo `date` "Starting $(($nc-1)) loader processes"
    for ((i=1; i<$threads; i++)); do
        $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="rdf_loader_run();"
    done
    echo `date` "Waiting for loaders"
    wait
else
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="rdf_loader_run();"
fi



echo `date` "Creating checkpoint"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="checkpoint;"

echo "finished indexing files, executing final virtuoso configuration scripts..."

echo "creating inference rules set"

# setting inference rules
loadrules="rdfs_rule_set ('default-rules', 'http://rdf.ebi.ac.uk/dataset/zooma')"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadrules" || exit 4;

echo `date` "Updating VoID graph with  number of triples, SPARQL description"

templatefile=$base/virtuoso-control/templates/update-provenance-template.sql
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $templatefile || exit $?

echo `date` "Creating checkpoint"
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="checkpoint;"

echo `date` "Done"

