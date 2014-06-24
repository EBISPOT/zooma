#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: build-virtuoso-index.sh [PORT] [THREADS]"
  echo "Number of CPU processes is optional"
}

loadProperty()
{
    local key="$1"
    local target=$zoomaHome/config/zooma.properties
    cat ${target} | sed -e '/^\#/d' | sed -e '/^\s*$/d' | sed -e 's/\s\+/=/g' |
        while read LINE
        do
            local KEY=`echo $LINE | cut -d "=" -f 1`
            local VALUE=`echo $LINE | cut -d "=" -f 2`

            printf "${KEY} = ${VALUE}"
            [ $key == ${KEY} ] && {
                local UNKNOWN_NAME=`echo $VALUE | grep '\${.*}' -o | sed 's/\${//' | sed 's/}//'`
                if [ $UNKNOWN_NAME ];then
                    local UNKNOWN_VALUE=`findStr ${UNKNOWN_NAME} ${file}`
                    echo ${VALUE} | sed s/\$\{${UNKNOWN_NAME}\}/${UNKNOWN_VALUE}/
                else
                    echo $VALUE
                fi
                return
            }
        done
    return
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

if [ ! $ZOOMA_HOME ];
then
  printf "\$ZOOMA_HOME not set - using $HOME/.zooma\n";
  zoomaHome=$HOME/.zooma;
else
  zoomaHome=$ZOOMA_HOME;
fi

if [ ! $VIRTUOSO_HOME ] ; then
    echo "VIRTUOSO_HOME variable not set"
    exit 1
fi
if [ ! -f $VIRTUOSO_HOME/bin/isql ] ; then
    echo "Can't find virtuoso start scripts in $VIRTUOSO_HOME"
    exit 1
fi

loadProperty "lode.sparqlendpoint.url";
server=$?
loadProperty "lode.sparqlendpoint.port";
port=$?

printf "Using server: $server and port: $port\n";

build_dir=$zoomaHome/index/virtuoso

#if [ ! -f $build_dir/db/virtuoso.lck ] ; then
#        echo "Virtuoso instance not running"
#        exit 1
#fi
if [ ! -d $build_dir ] ; then
        mkdir $build_dir;
fi


