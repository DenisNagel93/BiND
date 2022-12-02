import sys
import time

start_time = time.time()

from sentence_transformers import SentenceTransformer, util
model = SentenceTransformer('all-mpnet-base-v2')

events = "embeddings_path/EmbeddingEvents.txt"
attributes = "embeddings_path/EmbeddingAttributes.txt"
facts = "embeddings_path/EmbeddingFacts.txt"
properties = "embeddings_path/EmbeddingProperties.txt"
titles = "embeddings_path/EmbeddingTitles.txt"
values = "embeddings_path/EmbeddingValues.txt"


eventOutput = "embeddings_path/EmbeddingScoresReduced.txt"
factOutput = "embeddings_path/FactEmbeddingScoresReduced.txt"
titleOutput = "embeddings_path/TitleEmbeddingScoresReduced.txt"
valueOutput = "embeddings_path/ValueEmbeddingScoresReduced.txt"
vtOutput = "embeddings_path/VTEmbeddingScoresReduced.txt"

e = open(events,'r')
eventList = []

for line in e:
	line = line.rstrip('\n')
	eventList.append(line)

f = open(facts,'r')
p = open(properties,'r')
factList = []

for line in f:
	line = line.rstrip('\n')
	factList.append(line)
for line in p:
	line = line.rstrip('\n') 
	factList.append(line)

a = open(attributes,'r')
attributeList = []

for line in a:
	line = line.rstrip('\n')
	attributeList.append(line)

t = open(titles,'r')
titleList = []

for line in t:
	line = line.rstrip('\n')
	titleList.append(line)

v = open(values,'r')
valueList = []

for line in v:
	line = line.rstrip('\n')
	valueList.append(line)


#Sentences are encoded by calling model.encode()
embeddings1 = model.encode(eventList, convert_to_tensor=True)
embeddings2 = model.encode(attributeList, convert_to_tensor=True)
embeddings3 = model.encode(factList, convert_to_tensor=True)
embeddings4 = model.encode(titleList, convert_to_tensor=True)
embeddings5 = model.encode(valueList, convert_to_tensor=True)


cosine_scores = util.cos_sim(embeddings1, embeddings2)

outFile = open(eventOutput, "w")

for i in range(len(eventList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= float(sys.argv[1]):
			outFile.write("{},{},{:.4f}".format(eventList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")

cosine_scores = util.cos_sim(embeddings3, embeddings2)

outFile = open(factOutput, "w")

for i in range(len(factList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= float(sys.argv[2]):
			outFile.write("{},{},{:.4f}".format(factList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")

cosine_scores = util.cos_sim(embeddings1, embeddings4)

outFile = open(titleOutput, "w")

for i in range(len(eventList)):
	scores = []
	for j in range(len(titleList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= float(sys.argv[1]):
			outFile.write("{},{},{:.4f}".format(eventList[i], titleList[j], cosine_scores[i][j]))
			outFile.write("\n")

cosine_scores = util.cos_sim(embeddings5, embeddings2)

outFile = open(valueOutput, "w")

for i in range(len(valueList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= float(sys.argv[2]):
			outFile.write("{},{},{:.4f}".format(valueList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")

cosine_scores = util.cos_sim(embeddings5, embeddings4)

outFile = open(vtOutput, "w")

for i in range(len(valueList)):
	scores = []
	for j in range(len(titleList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= float(sys.argv[2]):
			outFile.write("{},{},{:.4f}".format(valueList[i], titleList[j], cosine_scores[i][j]))
			outFile.write("\n")

print("Embedding took %s" % (time.time() - start_time))
	
