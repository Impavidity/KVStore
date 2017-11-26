unset JAVA_TOOL_OPTIONS
export JAVA_HOME=/usr/lib/jvm/java-1.8.0
JAVA_CC=$JAVA_HOME/bin/javac
JAVA=$JAVA_HOME/bin/java
$JAVA -cp src/:gen-java/:"lib/*" StorageNode 0 10010 1:ecelinux8.uwaterloo.ca:10011