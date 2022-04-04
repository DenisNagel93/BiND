from sentence_transformers import SentenceTransformer, util
model = SentenceTransformer('all-mpnet-base-v2')

events = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingEvents.txt"
attributes = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingAttributes.txt"
output = "/home/nagel/DataNarrationsEval/SBERT/embeddingScoresReduced.txt"

e = open(events,'r')
eventList = []

for line in e:
	line = line.rstrip('\n')
	eventList.append(line)

a = open(attributes,'r')
attributeList = []

for line in a:
	line = line.rstrip('\n')
	attributeList.append(line)

outFile = open(output, "w")

#Sentences are encoded by calling model.encode()
embeddings1 = model.encode(eventList, convert_to_tensor=True)
embeddings2 = model.encode(attributeList, convert_to_tensor=True)

cosine_scores = util.cos_sim(embeddings1, embeddings2)

for i in range(len(eventList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= 0.65:
			outFile.write("{},{},{:.4f}".format(eventList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")
	