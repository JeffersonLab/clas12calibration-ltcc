#!/bin/bash
declare -a sector=("SEC1" "SEC2" "SEC3" "SEC5" "SEC6")
declare -a side=("L_" "R_")
declare -a pmt=("E01" "E02" "E03" "E04" "E05" "E06" "E07" "E08" "E09" "E10" "E11" "E12" "E13" "E14" "E15" "E16" "E17" "E18")
file="LTCC_HV-2018_02_13-08_39_43.snp "

for i in "${sector[@]}"
   do
   for j in "${side[@]}"
   do
     for k in "${pmt[@]}"
     do
     var=`grep vset $file | grep $i | grep $j | grep $k  | awk '{ print $3 }' `
     i1=${i:${#i} - 1}
     k1=${k:${#k} - 2}
     
     if [ $j = L_ ] 
	then
	j1=1
        else
        j1=2
     fi
     printf '%-25s%-20s%-10s%-10s\n' "$i1" "$j1" "$k1" "$var" 
     done
   done
done >> LTCC_HV.txt
