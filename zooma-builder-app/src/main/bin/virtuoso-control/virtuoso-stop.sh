#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: virtuoso-stop.sh [DIRECTORY]"
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

    if [ ! $build_dir ] ; then
        echo "You must supply a build directory"
        usage
        exit 1
    fi

    if [ ! -f $build_dir/virtuoso/db/virtuoso.ini ] ; then
            echo "Can't find virtuoso config in build directory $build_dir/virtuoso/db/virtuoso.ini"
            exit 1
    fi


lockfile=$build_dir/virtuoso/db/virtuoso.lck

if [ ! -f $lockfile ] ; then
    echo `date` "Virtuoso not running"
    exit 0
fi

tmp=`cat $lockfile`
pid=${tmp#VIRT_PID=};

if [ ! $pid ] ; then
    echo `date` "Unable to parse Virtuoso process ID"
    exit 2
fi

echo `date` "Stopping Virtuoso process $pid"

kill -2 $pid

if test $? -ne 0 ; then
    echo `date` "Unable to stop Virtuoso"
    exit 3
fi

i=0
while test -f "$lockfile" ; do
    sleep 1
    i=`expr $i + 1`
    if test $i -gt 20 ; then
        echo `date` "Virtuoso has not shut down after waiting 20 seconds"
        exit 4
        fi
done