#!/bin/sh

base=${0%/*}/..;
current=`pwd`;
java=/ebi/research/software/Linux_x86_64/opt/java/jdk1.7/bin/java;
args="-Xmx1g -Dorg.xml.sax.driver=uk.ac.ebi.fgpt.zooma.xml.ZoomaXMLReaderProxy -DentityExpansionLimit=1000000000 -Dhttp.proxyHost=wwwcache.ebi.ac.uk -Dhttp.proxyPort=3128 -Dhttp.nonProxyHosts=*.ebi.ac.uk -DproxyHost=wwwcache.ebi.ac.uk -DproxyPort=3128 -DproxySet=true -agentpath:/ebi/microarray/home/fgpt/tools/yjp-12.0.5/bin/linux-x86-64/libyjpagent.so=disablestacktelemetry,disableexceptiontelemetry,builtinprobes=none,delay=10000";

for file in `ls $base/lib`
do
  jars=$jars:$base/lib/$file;
done

classpath="$base/config$jars";

$java $args -classpath $classpath uk.ac.ebi.fgpt.zooma.search.ZoomageSearchDriver $@ 2>&1;
exit $?;