#!/bin/bash


LIB=WebContent/WEB-INF/lib
AVRO_JAR=avro-tools-1.7.4.jar
AVRO_DIR=src/sparqles/core/avro

EP=$AVRO_DIR/Endpoint.avsc
EPR=$AVRO_DIR/EndpointResult.avsc
PR=$AVRO_DIR/PResult.avsc
AR=$AVRO_DIR/AResult.avsc
DR=$AVRO_DIR/DResult.avsc
FR=$AVRO_DIR/FResult.avsc


SRC=src

java -jar $LIB/$AVRO_JAR compile schema $EP $EPR $PR $AR $DR $FR $SRC  
