#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: virtuoso-start.sh [DIRECTORY] [PORT]"
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

    if [ ! $build_dir ] ; then
        echo "You must supply a build directory"
        usage
        exit 1
    fi
    if [ ! $port ] ; then
        echo "You must supply a jdbc port"
        usage
        exit 1
    fi
    if [ ! -f $build_dir/virtuoso/db/virtuoso.ini ] ; then
            echo "Can't find virtuoso config in build directory $build_dir/virtuoso/db/virtuoso.ini"
            exit 1
    fi


$VIRTUOSO_HOME/bin/virtuoso-t -c  $build_dir/virtuoso/db/virtuoso.ini || exit 3

echo `date` "Virtuoso started, waiting for ready state"

i=0
status=1
while [ $status -ne 0 ] ; do
	i=`expr $i + 1`
	if test $i -gt 300 ; then
		echo `date` "Virtuoso is not ready after waiting 5 minutes"
		exit 4
	fi
	sleep 1
	if [ ! -f $build_dir/virtuoso/db/virtuoso.lck ] ; then
		echo `date` "Failed to start Virtuoso"
		exit 4
	fi
	$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="status();" &> /dev/null
	status=$?
done

echo `date` "Virtuoso ready on ports $port"





