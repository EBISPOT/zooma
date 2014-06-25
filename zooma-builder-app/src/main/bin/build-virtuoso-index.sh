#!/bin/bash

base=${0%/*};

usage() {
  echo "Usage: build-virtuoso-index.sh"
  echo "You should ensure \$VIRTUOSO_HOME is set to the base directory of your Virtuoso installation."
  echo "You can also set \$ZOOMA_HOME if you require this to be something other than the default $HOME/.zooma"
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

    port=$(loadProperty "virtuoso.builder.port");
    threads=$(loadProperty "virtuoso.builder.threads");

    build_dir=$zoomaHome/index/virtuoso;
}

createCleanInstance() {
    local templatefile=$base/templates/template-config.ini

    if [ ! -e $templatefile ] ; then
	    echo `date` "Missing template file: $templatefile";
	    exit 2;
    fi

    if [ -f $build_dir/db/virtuoso.lck ] ; then
        echo "Stopping virtuoso";
        stopVirtuoso;
    fi

    echo `date` "Creating clean instance for $build_dir"
    rm -rf $build_dir || exit 3;
    mkdir -p $build_dir || exit 3;
    mkdir -p $build_dir/db || exit 3;

    configfile=$build_dir/db/virtuoso.ini

    echo `date` "Generating config file"

    (cat $templatefile | sed  -e "s#\$VIRTUOSO_HOME#$VIRTUOSO_HOME#g" -e "s#\$DBDIR#$build_dir#g" -e "s#\$SERVERPORT#$port#g" -e "s#\$HTTPPORT#$httpport#g" > $configfile) || exit 3

    startVirtuoso;

    echo `date` "Enabling federated queries"
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $base/templates/enable-federated.sql > /dev/null || exit $?

    echo `date` "Removing default Virtuoso SPARQL description graph"
    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $base/templates/remove-sparqldesc.sql >/dev/null || exit $?

    #echo `date` "Enabling Virtuoso text index"
    #$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba $base/templates/enable-text-index.sql >/dev/null || exit $?

    echo `date` "Done"
}

startVirtuoso() {
    $VIRTUOSO_HOME/bin/virtuoso-t -c  $build_dir/db/virtuoso.ini || exit 3

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
        if [ ! -f $build_dir/db/virtuoso.lck ] ; then
		    echo `date` "Failed to start Virtuoso"
		    exit 4
	    fi
	    $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="status();" &> /dev/null
	    status=$?
    done

    echo `date` "Virtuoso ready on ports $port"
}

stopVirtuoso() {
    lockfile=$build_dir/db/virtuoso.lck

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

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" || exit 4;

loadfiles="ld_dir_all('$rdf_dir', '*.owl', 'http://rdf.ebi.ac.uk/dataset/zooma');"

$VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="$loadfiles" || exit 4;

echo "Finished setting files to load, starting loader..."

if [ $threads ] ; then
    # We want 1 fewer loader than the number of CPUs
    echo `date` "Starting $(($threads-1)) loader processes"
    for ((i=1; i<$threads; i++)); do
        $VIRTUOSO_HOME/bin/isql 127.0.0.1:$port dba dba exec="rdf_loader_run();" &
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

echo `date` "Data loading finished"
# finished data loading, stop virtuoso now we've built all our indexes

stopVirtuoso;

echo `date` "Virtuoso indexes have been built and saved in $build_dir";
echo `date` "Done.";

