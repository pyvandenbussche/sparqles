#!/bin/bash

LOG=/usr/local/sparqles/cronCheck.out
now="$(date)"
echo ">> $now"


if [ -z "$(pgrep -f SPARQLES)" ]
then
	echo "Process not running, sending email" >> $LOG
	echo "process not running"
	echo "Cron job detected that sparqles is not running" | mailx -s "SPARQLES backend down" jueumb@gmail.com py.vandenbussche@gmail.com
else
	PID=`pgrep -f SPARQLES`
	now="$(date)"
	ps aux | grep $PID  >> $LOG
	echo "Alles gut"
fi



#echo "Cron job detected that sparqlres is not running" | mailx -s "SPARQLES backend down" xyz@gmail.com
