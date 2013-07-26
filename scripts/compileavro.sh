#!/bin/bash


LIB=WebContent/WEB-INF/lib
AVRO_JAR=avro-tools-1.7.4.jar
AVRO_DIR=src/core/avro

EP=$AVRO_DIR/Endpoint.avsc
EPR=$AVRO_DIR/EndpointResult.avsc
PR=$AVRO_DIR/PResult.avsc
AR=$AVRO_DIR/AResult.avsc
DR=$AVRO_DIR/DResult.avsc

SRC=src

java -jar $LIB/$AVRO_JAR compile schema $EP $EPR $PR $AR $DR $SRC  