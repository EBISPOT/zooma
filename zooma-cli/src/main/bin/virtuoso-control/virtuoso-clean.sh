#!/bin/bash

# Step 1: Stop Virtuoso
# Step 2: Wipe existing data directory (if it exists)
# Step 3: Create a fresh data directory containing Virtuoso config from a template
# Step 4: Start Virtuoso

base=${0%/*};

usage() {
  echo "Usage: virtuoso-clean.sh [DIRECTORY] [PORT] [HTTPPORT]"
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
httpport=$3

    if [ ! $build_dir ] ; then
        echo "You must supply a build directory"
        usage
        exit 1
    fi
    if [ ! $port ] ; then
        echo "You must supply a jdbc port on localhost where virtuoso will run"
        usage
        exit 1
    fi
    if [ ! $httpport ] ; then
        echo "You must supply a http port on localhost where virtuoso will run"
        usage
        exit 1
    fi

templatefile=$base/templates/template-config.ini

if [ ! -e $templatefile ] ; then
	echo `date` "Missing template file: $templatefile"
	exit 2
fi

if [ -f $build_dir/virtuoso/db/virtuoso.lck ] ; then
    echo "Stopping virtuoso"
    $base/virtuoso-stop.sh $build_dir || exit $?
fi

echo `date` "Creating clean instance for $build_dir/virtuoso"
rm -rf $build_dir/virtuoso || exit 3
mkdir -p $build_dir/virtuoso || exit 3
mkdir -p $build_dir/virtuoso/db || exit 3

configfile=$build_dir/virtuoso/db/virtuoso.ini

echo `date` "Generating config file"

(cat $templatefile | sed  -e "s#\$VIRTUOSO_HOME#$VIRTUOSO_HOME#g" -e "s#\$DBDIR#$build_dir#g" -e "s#\$SERVERPORT#$port#g" -e "s#\$HTTPPORT#$httpport#g" > $configfile) || exit 3

$base/virtuoso-start.sh $build_dir ?port || exit $?

echo `date` "Enabling federated queries"
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $base/templates/enable-federated.sql > /dev/null || exit $?

echo `date` "Removing default Virtuoso SPARQL description graph"
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $base/templates/remove-sparqldesc.sql >/dev/null || exit $?

echo `date` "Done"
