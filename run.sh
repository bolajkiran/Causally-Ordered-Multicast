#!/bin/bash


echo "\n****** C A U S A L L Y   O R D E R E D   M U L T I C A S T *********"
echo "*                                                                    *"
echo "* We are executing causally ordered multicasting using               *"
echo "* vector clock algorithm                                             *"
echo "*                                                                    *"
echo "**********************************************************************"

echo "\nEnter the no. of processes:"
read n


echo "\nEnter the total no. of send events (good to enter >= no. of processes):"
read ne


process_id=$((n - 1))


for var in $(seq 0 $process_id)
do
   gnome-terminal -- java -jar process.jar $n $ne $var &
done
wait

