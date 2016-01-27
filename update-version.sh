#!/usr/bin/env bash

usage() {
  echo "Usage: update-version.sh -v [VERSION NUMBER]"
  echo "Required options:"
  echo "  -v    The version to increment the next release to"
}

while getopts "v:h" opt; do
  case $opt in
    v)
      echo "Updating GOCI project version number to $OPTARG"
      VERSION=$OPTARG
      ;;
    h)
      usage
      exit 0
      ;;
    \?)
      usage
      exit 1
      ;;
    :)
      echo "Missing option argument for -$OPTARG"
      exit 1
      ;;
    *)
      echo "Unimplemented option: -$OPTARG"
      exit 1
      ;;
  esac
done

if [ -z "$VERSION" ] ;
then
  echo "No version number supplied - please give a non-empty version number to increment to"
  exit 1
fi

base=${0%/*}/;

# invoke maven versions plugin to increment project structure versions
mvn -f $base/pom.xml versions:set -DnewVersion=$VERSION || exit 1

# switch into goci-dependencies and increment all project module versions
mvn -f $base/goci-dependencies/pom.xml versions:set -DnewVersion=$VERSION || exit 1

# finally replace version number property in goci-dependencies pom with new version
sed -i "s/\(<goci.version>\)\([^<]*\)\(<\/goci.version>\)/\1$VERSION\3/g" $base/goci-dependencies/pom.xml || exit 1


