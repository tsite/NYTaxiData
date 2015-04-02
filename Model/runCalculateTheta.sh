#!/bin/bash

python generateSample.py TaxiData/trip_data_matrix_consolidated.txt
octave --silent --eval 'train'

