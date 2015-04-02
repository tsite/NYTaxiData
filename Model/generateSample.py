from sys import argv
import random


def writeSample(inf,of,sampleList):
	lineCount = 0
	sequenceCount = 0

	seqLen = len(sampleList)

	line = inf.readline()

	while line:
		if lineCount % 500000 == 0:
			print("\rLines Processed:\t",lineCount,end=" ")

		if lineCount == sampleList[sequenceCount]:
			lineArr = line.split()
			if len(lineArr) != 8 or lineArr[-1] == str(0):
				sampleList[sequenceCount] += 1
				continue

			of.write(line)
			sequenceCount += 1
		
			if sequenceCount >= seqLen:
				break

		line = inf.readline()
		lineCount += 1

	print("")



scriptfile,infile = argv

totalExamples = 173179759



#GENERATE TRAINING DATA

inf = open(infile,'r')
of = open('trip_data_sample.txt','w')

trainExamples = 1000000

sampleList = sorted(random.sample(range(totalExamples),trainExamples))

print("GENERATING TRAINING DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")

of.close()


#GENERATE CROSS VALIDATION DATA
numValidate = 500000
inf.seek(0)

of = open('trip_data_valid.txt','w')

sampleList = sorted(random.sample(range(totalExamples),numValidate))

print("GENERATING VALIDATION DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")

#GENERATE TEST DATA

numTests = 500000
inf.seek(0)

of = open('trip_data_test.txt','w')

sampleList = sorted(random.sample(range(totalExamples),numTests))

print("GENERATING TEST DATA...")

writeSample(inf,of,sampleList)

print("\nDONE.")
