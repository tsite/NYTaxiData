from sys import argv
import random
import linecache

#Inf is a filename, of is an open file pointer
def writeSample(inf,of,sampleList):

	sequenceCount = 0
	linecount = 0
	seqLen = len(sampleList)
	line = inf.readline()

	while line and sequenceCount < seqLen:
		if linecount == sampleList[sequenceCount]:
	
			lineArr = line.split()
		
			if lineArr[-1] == str(0) or lineArr[-2] == str(0) or lineArr[-3] == str(0) or lineArr[-4] == str(0):
				sampleList[sequenceCount] += 1
				continue

			of.write(str(line))


			print("\rSamples Generated:\t",sequenceCount,end=" ")
			sequenceCount += 1
		
		line = inf.readline()
		linecount += 1



scriptfile,infile = argv

totalExamples = 173179759



#GENERATE TRAINING DATA

inf = open(infile,'r')
of = open('trip_data_sample.txt','w')

trainExamples = 50000

sampleList = sorted(random.sample(range(totalExamples),trainExamples))

print(len(sampleList))
print(sampleList[0],sampleList[-1])


print("GENERATING TRAINING DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")

of.close()


#GENERATE CROSS VALIDATION DATA
numValidate = 10000
inf.seek(0)

of = open('trip_data_valid.txt','w')

sampleList = sorted(random.sample(range(totalExamples),numValidate))

print("GENERATING VALIDATION DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")

#GENERATE TEST DATA

numTests = 10000
inf.seek(0)

of = open('trip_data_test.txt','w')

sampleList = sorted(random.sample(range(totalExamples),numTests))

print("GENERATING TEST DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")