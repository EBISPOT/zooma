#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: build-virtuoso-index.sh"
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

    rdf_dir=$zoomaHome/rdf

    if [ ! -d $rdf_dir ]; then
        echo "No rdf directory present at $rdf_dir - please generate some RDF files to build a Virtuoso index";
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

    build_dir=$zoomaHome/index/virtuoso;
    port=$(loadProperty "virtuoso.builder.port");
    httpport=$(loadProperty "virtuoso.builder.httpport");
    threads=$(loadProperty "virtuoso.builder.threads");

    echo "A new Virtuoso instance will be created in $build_dir and will run on port $port. Loading is enabled with $threads parallel processes.";
}

startVirtuoso() {
    $VIRTUOSO_HOME/bin/virtuoso-t -c  $build_dir/db/virtuoso.ini || exit 3

    echo "Starting Virtuoso on port $port, waiting for ready state...";

    i=0
    status=1
    while [ $status -ne 0 ] ; do
	    i=`expr $i + 1`
	    if test $i -gt 300 ; then
		    echo "Virtuoso is not ready after waiting 5 minutes";
		    exit 4
	    fi
	    sleep 1
        if [ ! -f $build_dir/db/virtuoso.lck ] ; then
		    echo "Failed to start Virtuoso";
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
        echo "Virtuoso not running";
        return
    fi

    tmp=`cat $lockfile`;
    pid=${tmp#VIRT_PID=};

    if [ ! $pid ] ; then
        echo "Unable to parse Virtuoso process ID";
        exit 2;
    fi

    if ps -p $pid > /dev/null;
    then
        echo "Stopping running Virtuoso instance, process $pid"
        kill -2 $pid;
        if test $? -ne 0 ;
        then
            echo "Failed to stop Virtuoso"
            exit 3
        fi
    else
        echo "No Virtuoso process $pid running, deleting $lockfile";
        rm $lockfile;
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

createCleanInstance() {
    local sql_dir=$base/virtuoso;
    local templatefile=$zoomaHome/config/virtuoso/template-config.ini

    if [ ! -e $templatefile ] ; then
	    echo "Missing template file: $templatefile";
	    exit 2;
    fi

    if [ -f $build_dir/db/virtuoso.lck ] ; then
        echo "Stopping virtuoso";
        stopVirtuoso;
    fi

    echo "Creating clean Virtuoso instance in $build_dir";
    rm -rf $build_dir || exit 3;
    mkdir -p $build_dir || exit 3;
    mkdir -p $build_dir/db || exit 3;
    mkdir -p $build_dir/log || exit 3;

    configfile=$build_dir/db/virtuoso.ini

    echo "Generating Virtuoso config file at $configfile";

    (cat $templatefile | sed  -e "s#\$VIRTUOSO_HOME#$VIRTUOSO_HOME#g" -e "s#\$DBDIR#$build_dir#g" -e "s#\$SERVERPORT#$port#g" -e "s#\$HTTPPORT#$httpport#g" -e "s#\$ZOOMA_HOME#$zoomaHome#g" > $configfile) || exit 3

    startVirtuoso;

    echo "Enabling federated queries"
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $sql_dir/enable-federated.sql &>> $build_dir/log/virtuoso-zooma.log || die $?

    echo "Removing default Virtuoso SPARQL description graph"
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $sql_dir/remove-sparqldesc.sql &>> $build_dir/log/virtuoso-zooma.log || die $?

    #echo "Enabling Virtuoso text index"
    #$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $sql_dir/enable-text-index.sql &>> $build_dir/log/virtuoso-zooma.log || die $?

    echo "Successfully created clean Virtuoso instance";
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

# check and setup environment for virtuoso building
checkEnvironment;

# create clean virtuoso running on localhost and the port specified in config
createCleanInstance;

# now run commands to load zooma data into local virtuoso
loadfiles="ld_dir_all('$rdf_dir', '*.rdf', 'http://rdf.ebi.ac.uk/dataset/zooma');"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" &>> $build_dir/log/virtuoso-zooma.log || die 4;

loadfiles="ld_dir_all('$rdf_dir', '*.owl', 'http://rdf.ebi.ac.uk/dataset/zooma');"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" &>> $build_dir/log/virtuoso-zooma.log || die 4;

echo "Finished setting files to load, starting loader..."

if [ $threads ] ; then
    # We want 1 fewer loader than the number of CPUs
    echo "Starting $(($threads-1)) loader processes"
    for ((i=1; i<$threads; i++)); do
        $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="rdf_loader_run();" &>> $build_dir/log/virtuoso-zooma.log &
    done
    echo "Waiting for loaders"
    wait
else
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="rdf_loader_run();" &>> $build_dir/log/virtuoso-zooma.log
fi


echo "Creating checkpoint"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="checkpoint;" &>> $build_dir/log/virtuoso-zooma.log

echo "Finished indexing files, executing final virtuoso configuration scripts..."

echo "Creating inference rules set"

# setting inference rules
loadrules="rdfs_rule_set ('default-rules', 'http://rdf.ebi.ac.uk/dataset/zooma')"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadrules" &>> $build_dir/log/virtuoso-zooma.log || die 4;

echo "Updating VoID graph with number of triples, SPARQL description"

templatefile=$base/virtuoso/update-provenance-template.sql
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $templatefile &>> $build_dir/log/virtuoso-zooma.log || die $?

echo "Creating checkpoint"
$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="checkpoint;" &>> $build_dir/log/virtuoso-zooma.log || die $?

echo "Data loading finished"

# finished data loading, stop virtuoso now we've built all our indexes
stopVirtuoso;

echo "Virtuoso indexes have been built and saved in $build_dir";
echo "Done.";
