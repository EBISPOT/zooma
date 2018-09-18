#!/bin/sh

#  Prepare tarball of Solr configs, for copying into Docker Solr image

#  Useful but obsolete experiments
# tar -cf - -C ./src/main --exclude data solr-5-config | tar -xf - --directory ../
# tar -cf - -C ./src/main/solr-5-config --exclude data `ls ./src/main/solr-5-config` | tar -xf - --directory ../mysolrhome/

#  Files-only option: does not archive parent directory
# tar -czf ../solr-5-config.tar.gz -C ./src/main/solr-5-config --exclude data `ls ./src/main/solr-5-config`

#  Set switch depending on whether we are using BSD or GNU tar
xfswitch="--transform s"
[ "`tar --version | awk {'print $1'}`" == "bsdtar" ] && xfswitch="-s "

#  Tar it up, including renamed parent directory
# tar -czf ../solr-5-config.tar.gz -C ./src/main --exclude data `echo "${xfswitch}"`/solr-5-config/mysolrhome/ solr-5-config

#  Or just pipe to untar it again, but in the right place
tar -czf - -C ./src/main --exclude data/?* `echo "${xfswitch}"`/solr-5-config/mysolrhome/ solr-5-config | tar -xzf - -C ..
