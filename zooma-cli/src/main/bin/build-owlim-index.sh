#!/bin/bash

base=${0%/*}/..;
current=`pwd`;

usage() {
  echo "Usage: build-owlim-index.sh -r [DIRECTORY] -d [DIRECTORY]"
  echo "  -r, the directory containing RDF files to convert to OWLIM indices"
  echo "  -d, the directory where OWLIM indices should be written"
  echo "  -h, show this help"
}

# parse user supplied arguments
exitVal=0
while getopts "r:d:h" opt; do
  case $opt in
    r)
      rdfDir=$OPTARG
      ;;
    d)
      owlimDir=$OPTARG
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      (( exitVal++ ))
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      usage
      (( exitVal++ ))
      ;;
  esac
done

if [ -z "$rdfDir" ]
then
  echo "No -r (RDF input directory) supplied"
  (( exitVal++ ))
fi

if [ -z "$owlimDir" ]
then
  echo "No -d (OWLIM output directory) supplied"
  (( exitVal++ ))
fi

if [ $exitVal -ne 0 ]
then
  usage
  exit $exitVal
fi

# passed all validation checks, so continue
rm -rf $owlimDir

${owlim.loader.script} config=$base/config/owlim.ttl context=http://rdf.ebi.ac.uk/dataset/zooma/${sparql.repository.version} preload=$rdfDir repopath=$owlimDir/openrdf-sesame