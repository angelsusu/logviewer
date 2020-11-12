#!/bin/bash

echo "start params:$1"

jarPath=$1

if [ $# == 0 ] ; then
  jarPath=../lib/logviewer.jar
fi

#执行对应jar包
java -jar $jarPathe