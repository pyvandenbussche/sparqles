#!/bin/bash


LIB=target/dependency
AVRO_JAR=avro-tools-1.7.5.jar
AVRO_DIR=src/main/avro

EP=$AVRO_DIR/Endpoint.avsc
EPR=$AVRO_DIR/EndpointResult.avsc
PR=$AVRO_DIR/PResult.avsc
AR=$AVRO_DIR/AResult.avsc
DR=$AVRO_DIR/DResult.avsc
FR=$AVRO_DIR/FResult.avsc
S=$AVRO_DIR/Schedule.avsc
EPVIEW=$AVRO_DIR/EPView.avsc
AVIEW=$AVRO_DIR/AvailabilityView.avsc
IDX=$AVRO_DIR/Index.avsc
PVIEW=$AVRO_DIR/PerformanceView.avsc
FVIEW=$AVRO_DIR/FeatureView.avsc
DVIEW=$AVRO_DIR/DiscoverabilityView.avsc

SRC=src/main/java

java -jar $LIB/$AVRO_JAR compile schema $EP $EPR $PR $AR $DR $FR $S $FVIEW  $EPVIEW $AVIEW $PVIEW $IDX $DVIEW  $SRC  
