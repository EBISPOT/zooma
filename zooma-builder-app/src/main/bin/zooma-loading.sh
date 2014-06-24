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
  libjars=$libjars:$base/lib/$file;
done

for file in `ls $zoomaHome/loaders`
do
  loaderFiles=$loaderFiles:$zoomaHome/loaders/$file;
done
loaderFiles=$loaderFiles:$zoomaHome/loaders

classpath="$base/config$libjars$loaderFiles:$zoomaHome/config/logging";

java $ZOOMA_OPTS -classpath $classpath uk.ac.ebi.fgpt.zooma.ZOOMA2LoaderDriver $@ 2>&1;
exit $?;