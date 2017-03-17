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

# If we can find the station root, input it
SR=""
SRD=""
if [[ -d ${NISGS_HOME}/ncs/stations ]]; then
    SR="-stationRoot"
    SRD="${NISGS_HOME}/ncs/stations"
fi

STATIONROOT=${NISGS_HOME}/ncs/stations

# Allow -processingMonitor on the command line to override the default
OVERRIDE=false
for x in $*
do
    if [[ "$x" == -processingMonitor ]]; then
	OVERRIDE=true
    fi
done
PM=""
PMF=""
if [[ "$OVERRIDE" == false ]]; then
    if [[ -f $NSLS_HOME/config/processingMonitor.xml ]]; then
	PM="-processingMonitor"
	PMF="$NSLS_HOME/config/processingMonitor.xml"
	## There was code here to regenerate processingMonitor.xml
	## if we see any new station.cfgfile.xml files.  If you want
	## it, it's in svn...
    fi
fi

# Allow -configuration on the command line to override the default
OVERRIDE=false
for x in $*
do
    if [[ "$x" == -configuration ]]; then
	OVERRIDE=true
    fi
done
CM=""
CMF=""
if [[ "$OVERRIDE" == false ]]; then
    if [[ -f $NISGS_HOME/ncs/configs/default_config.file ]]; then
	CM="-configuration"
	CMF="$NISGS_HOME/ncs/configs/default_config.file"
    fi
fi

# Allow -configuration on the command line to override the default
OVERRIDE=false
for x in $*
do
    if [[ "$x" == -tilelist ]]; then
        OVERRIDE=true
    fi
done
TL=""
TLF=""
if [[ "$OVERRIDE" == false ]]; then
    if [[ -f $NISGS_HOME/nsls/config/landTilesList.cfg ]]; then
        TL="-tilelist"
        TLF="$NISGS_HOME/nsls/config/landTilesList.cfg"
    fi
fi
	
java \
-classpath $LIB/nsls.jar \
gov.nasa.gsfc.nisgs.nsls.console.Console \
$PM $PMF $CM $CMF $SR $SRD $TL $TLF $*
