#!/bin/csh -f
#=================================================================
# BUILDING SCRIPT for COATJAVA PROJECT (first maven build)
# then the documentatoin is build from the sources and commited
# to the documents page
#=================================================================
# Maven Build

if(`filetest -e lib` == '0') then
    mkdir lib
endif

# LTCC calibration in source
echo "Building LTCCCalib..."
    mvn install
    mvn package
    cp target/LTCCCalib-1.0.jar lib/

