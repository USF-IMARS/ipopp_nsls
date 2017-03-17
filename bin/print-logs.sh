#!/bin/bash
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

NSLS_HOME=${SHELLDIR}/..
NISGS_HOME=${NSLS_HOME}/..
LIB=$NSLS_HOME/lib

java -classpath $LIB/nsls.jar gov.nasa.gsfc.nisgs.nsls.LogPrinter $*
