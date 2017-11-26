#!/usr/bin/env bash

unset JAVA_TOOL_OPTIONS
export JAVA_HOME=/usr/lib/jvm/java-1.8.0
JAVA_CC=$JAVA_HOME/bin/javac
THRIFT_CC=/opt/bin/thrift

export CLASSPATH="src/:gen-java/:lib/*"

echo --- Cleaning
rm -f src/*.class
rm -fr gen-java

echo --- Compiling Thrift IDL
$THRIFT_CC --version
$THRIFT_CC --gen java rpc.thrift

echo --- Compiling Java
$JAVA_CC -version
$JAVA_CC gen-java/*.java
$JAVA_CC src/*.java