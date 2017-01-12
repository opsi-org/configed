#!/bin/sh
date > keepdate
java -Xmx1024m -enableassertions -ea -cp aspectjrt.jar:classes:swingx.jar:itext-pdf.jar:commons-io.jar:jsch-0.1.52.jar de.uib.configed.configed $@
echo started program: 
cat keepdate
echo ended program:
date

