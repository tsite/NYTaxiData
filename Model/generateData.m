function data = generateData()

rawData1 = dlmread('TaxiData/trip_data_1_matrix.txt');
rawData2 = dlmread('TaxiData/trip_data_2_matrix.txt');
rawData3 = dlmread('TaxiData/trip_data_3_matrix.txt');
rawData4 = dlmread('TaxiData/trip_data_4_matrix.txt');
rawData5 = dlmread('TaxiData/trip_data_5_matrix.txt');
rawData6 = dlmread('TaxiData/trip_data_6_matrix.txt');
rawData7 = dlmread('TaxiData/trip_data_7_matrix.txt');
rawData8 = dlmread('TaxiData/trip_data_8_matrix.txt');
rawData9 = dlmread('TaxiData/trip_data_9_matrix.txt');
rawData10 = dlmread('TaxiData/trip_data_10_matrix.txt');
rawData11 = dlmread('TaxiData/trip_data_11_matrix.txt');
rawData12 = dlmread('TaxiData/trip_data_12_matrix.txt');

data = [rawData1;rawData2;rawData3;rawData4;rawData5;rawData6;rawData7;rawData8;rawData9;rawData10;rawData11;rawData12];

clear('rawData1');
clear('rawData2');
clear('rawData3');
clear('rawData4');
clear('rawData5');
clear('rawData6');
clear('rawData7');
clear('rawData8');
clear('rawData9');
clear('rawData10');
clear('rawData11');
clear('rawData12');

end