#!/bin/csh -f

# Maven Build

if(`filetest -e lib` == '0') then
    mkdir lib
endif

# LTCC calibration
echo "Building LTCCCalib..."
    mvn install
    mvn package
    cp target/LTCCCalib-1.0.jar lib/

# copying jar to jlab
scp lib/LTCCCalib-1.0.jar ftp:

