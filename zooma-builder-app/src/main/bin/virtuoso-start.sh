#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: virtuoso-start.sh"
  echo "You should ensure \$VIRTUOSO_HOME is set to the base directory of your Virtuoso installation."
  echo "You can also set \$ZOOMA_HOME if you require this to be something other than the default $HOME/.zooma."
  echo "Virtuoso configuration used for building ZOOMA indexes can be updated from \$ZOOMA_HOME/config/zooma.properties."
}

loadProperty()
{
    local key="$1";
    local target=$zoomaHome/config/zooma.properties;
    cat ${target} | sed -e '/^\#/d' | sed -e '/^\s*$/d' | sed -e 's/\s\+/=/g' | grep ${key} |
        while read LINE
        do
            local KEY=`echo ${LINE} | cut -d "=" -f 1`;
            local VALUE=`echo ${LINE} | cut -d "=" -f 2`;
            [ ${key} == ${KEY} ] && {
                local UNKNOWN_NAME=`echo $VALUE | grep '\${.*}' -o | sed 's/\${//' | sed 's/}//'`
                if [ ! $UNKNOWN_NAME ];
                then
                    echo ${VALUE};
                fi
                return;
            }
        done
    return
}

checkEnvironment() {
    if [ ! $ZOOMA_HOME ];
    then
        printf "\$ZOOMA_HOME not set - using $HOME/.zooma\n";
        zoomaHome=$HOME/.zooma;
    else
        zoomaHome=$ZOOMA_HOME;
    fi

    if [ ! -d $zoomaHome ] ; then
        echo "Can't find $zoomaHome";
        exit 1;
    fi

    if [ ! $ZOOMA_DATA_DIR ];
    then
        printf "\$ZOOMA_DATA_DIR not set - using $HOME/.zooma/data\n";
        zoomaDataDir=$HOME/.zooma/data;
    else
        zoomaDataDir=$ZOOMA_HOME;
    fi

    if [ ! -d $zoomaDataDir ] ; then
        echo "Can't find $zoomaDataDir";
        exit 1;
    fi

    if [ ! $VIRTUOSO_HOME ] ; then
        echo "VIRTUOSO_HOME variable not set";
        exit 1;
    fi

    if [ ! -f $VIRTUOSO_HOME/bin/isql ] ; then
        echo "Can't find virtuoso start scripts in $VIRTUOSO_HOME";
        exit 1;
    fi

    build_dir=$zoomaDataDir/index/virtuoso;
    port=$(loadProperty "virtuoso.builder.port");
    httpport=$(loadProperty "virtuoso.builder.httpport");

    echo "Virtuoso instance at $build_dir will be started on port $port.";
}

startVirtuoso() {
    $VIRTUOSO_HOME/bin/virtuoso-t -c  $build_dir/db/virtuoso.ini || exit 3

    echo "Starting Virtuoso on port $port, waiting for ready state...";

    i=0
    status=1
    while [ $status -ne 0 ] ; do
	    i=`expr $i + 1`
	    if test $i -gt 300 ; then
		    echo "Virtuoso is not ready after waiting 5 minutes"
		    exit 4
	    fi
	    sleep 1
        if [ ! -f $build_dir/db/virtuoso.lck ] ; then
		    echo "Failed to start Virtuoso"
		    exit 4
	    fi
	    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="status();" &> /dev/null
	    status=$?
    done

    echo "Virtuoso ready on ports $port"
}

stopVirtuoso() {
    lockfile=$build_dir/db/virtuoso.lck

    if [ ! -f $lockfile ] ; then
        echo "Virtuoso not running"
        exit 0
    fi

    tmp=`cat $lockfile`
    pid=${tmp#VIRT_PID=};

    if [ ! $pid ] ; then
        echo "Unable to parse Virtuoso process ID"
        exit 2
    fi

    echo "Stopping Virtuoso process $pid"

    kill -2 $pid

    if test $? -ne 0 ; then
        echo "Unable to stop Virtuoso"
        exit 3
    fi

    i=0
    while test -f "$lockfile" ; do
        sleep 1
        i=`expr $i + 1`
        if test $i -gt 20 ; then
            echo "Virtuoso has not shut down after waiting 20 seconds"
            exit 4
        fi
    done
}

die() {
    stopVirtuoso;
    exit $1;
}

# main body of script

# parse user supplied arguments
exitVal=0
while getopts "h" opt; do
  case $opt in
    h)
      usage
      exit 0
      ;;
  esac
done

# check and setup environment for virtuoso
checkEnvironment;

# start virtuoso
startVirtuoso || die $?;
