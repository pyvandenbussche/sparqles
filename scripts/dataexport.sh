#!/bin/bash

DATE=`date +%Y-%m-%d`

DIR=/usr/local/sparqles/dumps

for col in atasks dtasks ftasks ptasks 
do
 echo "export $col"
 mongoexport --db sparqles --collection $col | gzip -c > $DIR/$col.json.gz
done

