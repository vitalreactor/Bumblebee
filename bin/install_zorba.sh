#!/usr/bin/env bash

if [ $# -ne 1 ];
then
echo "Usage: install_zorba.sh <path to zorba libs>"
  exit 1
fi

if [ ! -f $1/zorba_xqj.jar ]; then
    echo "Could not find $1/zorba_xqj.jar, exiting."
    exit 1
fi

if [ ! -f $1/zorba_api.jar ]; then
    echo "Could not find $1/zorba_api.jar, exiting."
    exit 1
fi

ZORBA_XQJ=$1/zorba_xqj.jar
ZORBA_API=$1/zorba_api.jar
SCRIPT_PATH="`dirname \"$0\"`"

echo "Installing zorba_xqj jar to local maven repository..."
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=$ZORBA_XQJ -DpomFile=$SCRIPT_PATH/../zorba_poms/zorba_xqj.pom.xml

echo "Installing zorba_api jar to local maven repository..."
mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -Dfile=$ZORBA_API -DpomFile=$SCRIPT_PATH/../zorba_poms/zorba_api.pom.xml

echo "Finished!"
