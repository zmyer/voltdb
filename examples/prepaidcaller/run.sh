#!/usr/bin/env bash

# find voltdb binaries
if [ -e ../../bin/voltdb ]; then
    # assume this is the examples folder for a kit
    VOLTDB_BIN="$(dirname $(dirname $(pwd)))/bin"
elif [ -n "$(which voltdb 2> /dev/null)" ]; then
    # assume we're using voltdb from the path
    VOLTDB_BIN=$(dirname "$(which voltdb)")
else
    echo "Unable to find VoltDB installation."
    echo "Please add VoltDB's bin directory to your path."
    exit -1
fi

# call script to set up paths, including
# java classpaths and binary paths
source $VOLTDB_BIN/voltenv

# leader host for startup purposes only
# (once running, all nodes are the same -- no leaders)
STARTUPLEADERHOST="localhost"
# list of cluster nodes separated by commas in host:[port] format
SERVERS="localhost"

# remove binaries, logs, runtime artifacts, etc... but keep the jars
function clean() {
    rm -rf voltdbroot log procedures/prepaidcaller/*.class client/prepaidcaller/*.class *.log
}

# remove everything from "clean" as well as the jarfiles
function cleanall() {
    clean
    rm -rf prepaidcaller-procs.jar prepaidcaller-client.jar
}

# compile the source code for procedures and the client into jarfiles
function jars() {
    # compile java source
    javac -classpath $APPCLASSPATH procedures/prepaidcaller/*.java
    javac -classpath $CLIENTCLASSPATH client/prepaidcaller/*.java
    # build procedure and client jars
    jar cf prepaidcaller-procs.jar -C procedures prepaidcaller
    jar cf prepaidcaller-client.jar -C client prepaidcaller
    # remove compiled .class files
    rm -rf procedures/prepaidcaller/*.class client/prepaidcaller/*.class
}

# run the voltdb server locally
function server() {
    voltdb init --force
    voltdb start -H $STARTUPLEADERHOST
}

# runs the simulation
# only run one at a time; CellTower itself spins up multiple VoltDB clients to test concurrent access.
function simulation() {
	echo "Loading schema and procedures"
    sqlcmd < ddl.sql >/dev/null
    java -classpath prepaidcaller-client.jar:$CLIENTCLASSPATH -Dlog4j.configuration=file://$LOG4J \
        prepaidcaller.CellTower $SERVERS
}

function simulation2() {
	echo "Loading schema and procedures"
    sqlcmd < ddl.sql >/dev/null
    java -classpath prepaidcaller-client.jar:$CLIENTCLASSPATH -Dlog4j.configuration=file://$LOG4J \
        prepaidcaller.CellTower2 $SERVERS
}

function help() {
    echo "Usage: ./run.sh {clean|cleanall|jars|server|simulation}"
}

echo "${0}: Running ${1}..."
${1}
