pythimport datetime
from sys import argv

scriptfile,infile,ofile = argv


inf = open(infile, 'r')
of = open(ofile,'w')

isWeekday = 0
tInSec = 0;

line = inf.readline()

outArr = []

progressCounter = 0;

while line:
	lineArr = line.split(',')
	#print(lineArr[5])
	d = datetime.datetime.strptime(lineArr[5],"%Y-%m-%d %H:%M:%S")
	

	if d.weekday() < 5:
		isWeekday = 1
	else:
		isWeekday = 0

	tInSec = d.second + d.minute*60 + d.hour*3600


	of.write(lineArr[8] + " ")
	of.write(str(isWeekday) + " ")
	of.write(str(tInSec) + " ")
	
	for col in lineArr[9:-1]:
		of.write(str(col) + " ")

	of.write(str(lineArr[-1]))

	progressCounter += 1

	if progressCounter % 50000 == 0:
		print("\r Lines Processed:\t",progressCounter,end=" ")

	line = inf.readline()


print("")

of.close()
inf.close()