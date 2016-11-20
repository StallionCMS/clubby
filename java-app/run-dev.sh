#!/bin/bash
mvn compile exec:java -Dexec.mainClass=io.stallion.clubhouse.MainRunner -Dexec.args="serve -env=local -targetPath=../site -devMode=true -logLevel=FINER"
