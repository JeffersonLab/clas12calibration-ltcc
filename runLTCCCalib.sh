#!/bin/sh -f

hostname=$(hostname)

if [[ $hostname == "clonsl2.jlab.org" ]]; then
	COATJAVA=/home/clasrun/LTCC/coatjava ; export COATJAVA
fi
if [[ $hostname == "MauriMP.local" ]]; then
	COATJAVA=/opt/projects/coatjava ; export COATJAVA
fi


# distribution directory: same as launched
DISTRO_DIR=`dirname $0`; export DISTRO_DIR
CLAS12DIR=$COATJAVA ;    export CLAS12DIR


echo "COATJAVA DIRECTORY = " $CLAS12DIR
echo "LIBRARY DIRECTORY  = " $DISTRO_DIR/lib


java -Dsun.java2d.pmoffscreen=false -Xmx2048m -Xms1024m -cp "$CLAS12DIR/lib/clas/*:$CLAS12DIR/lib/services/*:$DISTRO_DIR/lib/LTCCCalib-1.0.jar" viewer.CalibrationViewer $*
