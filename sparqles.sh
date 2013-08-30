#!/bin/bash

JAR=sparqles-core-0.1.jar
LIB=WebContent/WEB-INF/lib

OTHER=$LIB/apache-any23-core-0.7.0-incubating.jar
OTHER=$OTHER:$LIB/avro-1.7.4.jar:
OTHER=$OTHER:$LIB/h2-1.3.172.jar
OTHER=$OTHER:$LIB/slf4j-api-1.6.4.jar
OTHER=$OTHER:$LIB/xercesImpl-2.10.0.jar
OTHER=$OTHER:$LIB/xml-apis-1.4.01.jar
OTHER=$OTHER:$LIB/httpclient-4.1.2.jar
OTHER=$OTHER:$LIB/httpcore-4.1.3.jar
OTHER=$OTHER:$LIB/httpmime-4.2.1.jar
OTHER=$OTHER:$LIB/nxparser-1.2.3.jar
OTHER=$OTHER:$LIB/ldspider-trunk-lib.jar
OTHER=$OTHER:$LIB/tika-core-0.6.jar
OTHER=$OTHER:$LIB/tika-parsers-0.6.jar
OTHER=$OTHER:$LIB/json-lib-2.4-jdk15.jar
OTHER=$OTHER:$LIB/commons-cli-1.2.jar
OTHER=$OTHER:$LIB/slf4j-log4j12-1.6.4.jar
OTHER=$OTHER:$LIB/log4j-1.2.16.jar

SESAME=$(JARS=("$LIB"/sesame*.jar); IFS=:; echo "${JARS[*]}")
JACKSON=$(JARS=("$LIB"/jackson*.jar); IFS=:; echo "${JARS[*]}")
JENA=$(JARS=("$LIB"/jena*.jar); IFS=:; echo "${JARS[*]}")


java -Xmx2G -cp $OTHER:$SESAME:$JACKSON:$JENA:JAR sparqles.core.Main $*