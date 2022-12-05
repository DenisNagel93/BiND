# BiND

![System Overview](/BiND-Pipeline.png)

### Data Sets

The data sets used to evaluate BiND are available through the following links:

1. **Global Health Observatory (GHE):** We used the API provided at https://www.who.int/data/gho/info/gho-odata-api to download the GHO data
2. **HEAT Plus Data Repository:** https://www.who.int/data/inequality-monitor/data
3. **TabFact Data Set:** We used the test set of the data provided at https://github.com/wenhuchen/Table-Fact-Checking
4. **C19_s Data Set:** We used the Coronavirus data provided at https://zenodo.org/record/5128604/#.YskYIoTP2Ul

### Narratives

The narrative graphs used to evaluate BiND are provided in the folder *NarrativeGraphs*. The same narratives expressed in natural language are provided in the folder *Narratives_NaturalLanguage*

### Using BiND

#### Evaluation

To evaluate BiND you need to follow these steps.

1. **Provide Data Sets:** Create a folder which contains all data sets that will be used in the evaluation. These need to be provided as .json or .csv files
2. **Provide Narrative Graphs:** Create a second folder which contains all narrative graphs, for which BiND will compute narrative bindings. These graphs can be defined in .txt files
3. **Provide a Ground Truth:** Create a third folder where you can define the ground truth used for the evaluation. To do so, you have to create one .txt file per narrative graph (the file names need to be identical to the naming used in step 2)
5. **Prepare the config file:** Modify the config.txt file by adding the paths to the folders created in the previous steps, as well as the paths for the output-files created by BiND. Additionally you can define the delimiter used in the csv files
6. **Prepare SBERT Input:** Run DataNarrations.jar using the option *PrepareEmbeddings* in the first dialog. BiND will create lists containing the input to be used by SBERT in the following step (the files will be created in the folder defined by the *embedding_path* in the config.txt)
7. **Run SBERT:** Run *SBERTCompleteReduced.py* as found in the *Scripts* folder (replace path by the *embedding_path* defined in the config.txt)
8. **Run BiND:** Run DataNarrations.jar using the fitting evaluation option
