unset JAVA_TOOL_OPTIONS
export JAVA_HOME=/usr/lib/jvm/java-1.8.0
JAVA_CC=$JAVA_HOME/bin/javac
JAVA=$JAVA_HOME/bin/java
$JAVA -cp src/:gen-java/:"lib/*":test/ Throughput $1 $2 $3

echo --- Analyzing linearizability
$JAVA -cp src/:gen-java/:"lib/*":test/ ca.uwaterloo.watca.LinearizabilityTest execution.log scores.txt > /dev/null
echo Number of get operations returning junk: `cat scores.txt | grep 'Score = 2' | wc -l`
echo Number of other linearizability violations: `cat scores.txt | grep 'Score = 1' | wc -l`