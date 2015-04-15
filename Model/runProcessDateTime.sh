#!/bin/bash

for n in {1..12};do python ProcessDateTime.py "TaxiData/RawData/trip_data_${n}.csv" "TaxiData/trip_data_${n}_matrix.txt";done
cat TaxiData/*_matrix.txt > TaxiData/trip_data_matrix_consolidated.txt
