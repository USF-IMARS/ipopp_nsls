#!/bin/bash -e
#
# The usual crap to locate ourselves
#
case $0 in
         /*)  SHELLFILE=$0 ;;
        ./*)  SHELLFILE=${PWD}${0#.} ;;
        ../*) SHELLFILE=${PWD%/*}${0#..} ;;
          *)  SHELLFILE=$(type -P $0) ; if [ ${SHELLFILE:0:1} != "/" ]; then SHELLFILE=${PWD}/$SHELLFILE ; fi ;;
esac
SHELLDIR=${SHELLFILE%/*}

# SHELLDIR is nsls, so...
NSLS_HOME=${SHELLDIR}

( cd ${NSLS_HOME};
    rm -rf classes/*
    javac -d ./classes -classpath ./classes \
	-Xlint:unchecked \
	-Xlint:deprecation \
	src/gov/nasa/gsfc/nisgs/nsls/util/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/filter/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/message/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/server/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/console/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/test/*.java \
	src/gov/nasa/gsfc/nisgs/nsls/*.java
    jar -cf ./lib/nsls.jar -C ./classes . -C . images
)
