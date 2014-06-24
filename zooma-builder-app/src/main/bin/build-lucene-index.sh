#!/bin/sh

base=${0%/*}/..;
current=`pwd`;

if [ ! $ZOOMA_HOME ];
then
  printf "\$ZOOMA_HOME not set - using $HOME/.zooma\n";
  zoomaHome=$HOME/.zooma;
else
  zoomaHome=$ZOOMA_HOME;
fi

for file in `ls $base/lib`
do
  jars=$jars:$base/lib/$file;
done

classpath="$base/config$jars:$zoomaHome/config/logging";

java $ZOOMA_OPTS -classpath $classpath uk.ac.ebi.fgpt.zooma.ZOOMA2LuceneIndexDriver $@ 2>&1;
exit $?;