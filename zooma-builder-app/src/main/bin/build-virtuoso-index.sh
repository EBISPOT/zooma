#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: build-virtuoso-index.sh"
  echo "You should ensure \$VIRTUOSO_HOME is set to the base directory of your Virtuoso installation."
  echo "You can also set \$ZOOMA_HOME if you require this to be something other than the default $HOME/.zooma"
}

loadProperty()
{
    local key="$1"
    local target=$zoomaHome/config/zooma.properties
    cat ${target} | sed -e '/^\#/d' | sed -e '/^\s*$/d' | sed -e 's/\s\+/=/g' | grep ${key} |
        while read LINE
        do
            local KEY=`echo ${LINE} | cut -d "=" -f 1`
            local VALUE=`echo ${LINE} | cut -d "=" -f 2`
            [ ${key} == ${KEY} ] && {
                local UNKNOWN_NAME=`echo $VALUE | grep '\${.*}' -o | sed 's/\${//' | sed 's/}//'`
                if [ ! $UNKNOWN_NAME ];
                then
                    echo ${VALUE}
                fi
                return
            }
        done
    return
}

checkEnvironment()
{
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

    server=$(loadProperty "lode.sparqlendpoint.url");
    port=$(loadProperty "lode.sparqlendpoint.port");

    printf "Using server: $server and port: $port\n";

    build_dir=$zoomaHome/index/virtuoso

    if [ ! -d $build_dir ] ; then
        mkdir -p $build_dir;
    fi
}

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

checkEnvironment;




