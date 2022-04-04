from sentence_transformers import SentenceTransformer, util
model = SentenceTransformer('all-mpnet-base-v2')

facts = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingFacts.txt"
properties = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingProperties.txt"
attributes = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingAttributes.txt"
output = "/home/nagel/DataNarrationsEval/SBERT/FactEmbeddingScoresReduced.txt"

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

outFile = open(output, "w")

#Sentences are encoded by calling model.encode()
embeddings1 = model.encode(factList, convert_to_tensor=True)
embeddings2 = model.encode(attributeList, convert_to_tensor=True)

cosine_scores = util.cos_sim(embeddings1, embeddings2)

for i in range(len(factList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] >= 0.5:
			outFile.write("{},{},{:.4f}".format(factList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")
	