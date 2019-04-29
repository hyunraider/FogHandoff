#!/bin/bash

#### Main
num_fogs=`expr $1 - 1`
simulation=$2
cd target
for i in `seq 0 $num_fogs`;
do
    nohup java -cp foghandoff.jar foghandoff.fog.Main `expr 9050 + $i` $simulation &
done
