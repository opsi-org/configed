#!/bin/sh
echo =========================================================================
date
JAVAC=javac
which $JAVAC
$JAVAC -version
echo compile...
$JAVAC -encoding UTF-8  -sourcepath src -classpath swingx.jar:itext-pdf.jar:commons-io.jar:jsch-0.1.52.jar  -d classes src/de/uib/configed/configed.java ||  exit 1
echo ExitCode echo 
