from sentence_transformers import SentenceTransformer, util
model = SentenceTransformer('all-mpnet-base-v2')

attributes = "/home/nagel/DataNarrationsEval/SBERT/EmbeddingAttributes.txt"
output = "/home/nagel/DataNarrationsEval/SBERT/ReducedDataEmbeddingScores.txt"

a = open(attributes,'r')
attributeList = []

for line in a:
	line = line.rstrip('\n')
	attributeList.append(line)

outFile = open(output, "w")

#Sentences are encoded by calling model.encode()
embeddings = model.encode(attributeList, convert_to_tensor=True)

cosine_scores = util.cos_sim(embeddings, embeddings)

for i in range(len(attributeList)):
	scores = []
	for j in range(len(attributeList)):
		scores.append([cosine_scores[i][j], i, j])
	scores = sorted(scores, key=lambda x: x[0], reverse=True)
	for sc, i, j in scores:
		if cosine_scores[i][j] <= 0.6 and cosine_scores[i][j] >= 0.55 :
			outFile.write("{},{},{:.4f}".format(attributeList[i], attributeList[j], cosine_scores[i][j]))
			outFile.write("\n")
	