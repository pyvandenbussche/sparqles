#!/bin/bash

DATE=`date +%Y-%m-%d`

DIR=/usr/local/sparqles/db.dumps
mkdir -p $DIR/$DATE

for col in atasks atasks_agg dtasks dtasks_agg endpoints epview ftasks ftasks_agg index ptasks ptasks_agg schedule 
do
 echo "export $col"
 mongoexport --db sparqles --collection $col | gzip -c > $DIR/$DATE/$col.json.gz
done

