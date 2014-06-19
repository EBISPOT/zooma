#!/bin/sh

base=${0%/*}/..;
current=`pwd`;
java=${java.location};
args="${java.args}";

for file in `ls $base/lib`
do
  jars=$jars:$base/lib/$file;
done

classpath="$base/config$jars";

$java $args -classpath $classpath uk.ac.ebi.fgpt.zooma.ZOOMA2SearchDriver $@ 2>&1;
exit $?;