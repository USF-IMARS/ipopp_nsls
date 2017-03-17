#!/bin/bash
javadoc \
-d ./javadoc \
-doctitle "NISGS Status/Event Logging System (NSLS)" \
-header "NSLS Version 1.2" \
-windowtitle "NSLS Version 1.2 API Specification" \
-sourcepath ./src \
-subpackages gov.nasa.gsfc.nisgs.nsls \
-exclude gov.nasa.gsfc.nisgs.nsls.console \
-exclude gov.nasa.gsfc.nisgs.nsls.server \
-exclude gov.nasa.gsfc.nisgs.nsls.test \
-exclude gov.nasa.gsfc.nisgs.nsls.util \
-exclude gov.nasa.gsfc.nisgs.nsls.message
