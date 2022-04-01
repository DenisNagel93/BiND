import spacy
import scispacy

from scispacy.linking import EntityLinker

import os

nlp = spacy.load("en_core_sci_md")
nlp.add_pipe("scispacy_linker", config={"resolve_abbreviations": True, "name": "umls"})
linker = nlp.get_pipe("scispacy_linker")

inputDir = "C:\\Users\\Denis\\Desktop\\JCDLEval\\Evaluation\\Narratives"
outputDir = "C:\\Users\\Denis\\Desktop\\JCDLEval\\Evaluation\\Annotation"
directory = os.fsencode(inputDir)
    
#Iterate over files in directory
for file in os.listdir(directory):
	filename = os.fsdecode(file)
	print(filename)
	n = open(inputDir + "\\" + filename,'r')
	outFile = open(outputDir + "\\Annotation_" + filename, "w")
	nodeSet = set()
	labelSet = set()
	for line in n:
		sub = line.split(',')
		nodeSet.add(sub[1])
		labelSet.add(sub[2])
		nodeSet.add(sub[3])
	for w in nodeSet:
		outFile.write("Node;" + w + ";")
		print("Word: " + w)
		a = nlp(w)
		entitySet = set()
		for entity in a.ents:
			entitySet.add(entity)
			aliasSet = set()
			print(entity)
			for link in entity._.kb_ents:
				aliasSet.add(link[0])
				print(link[0])
			for entry in aliasSet:
				outFile.write(entry + ",")
			outFile.write(".")
		outFile.write("\n")
	for l in labelSet:
		outFile.write("Label;" + l + ";")
		print("Word: " + l)
		a = nlp(l)
		entitySet = set()
		for entity in a.ents:
			entitySet.add(entity)
			aliasSet = set()
			print(entity)
			for link in entity._.kb_ents:
				aliasSet.add(link[0])
				print(link[0])
			for entry in aliasSet:
				outFile.write(entry + ",")
			outFile.write(".")
		outFile.write("\n")
	n.close()
	outFile.close()