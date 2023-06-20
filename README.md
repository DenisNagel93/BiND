# BiND

![System Overview](/BiND-Pipeline.png)

### Data Sets

The data sets used to evaluate BiND are available through the following links:

1. **Global Health Observatory (GHO):** We used the API provided at https://www.who.int/data/gho/info/gho-odata-api to download the GHO data
2. **HEAT Plus Data Repository:** https://www.who.int/data/inequality-monitor/data
3. **TabFact Data Set:** We used the test set of the data provided at https://github.com/wenhuchen/Table-Fact-Checking
4. **C19_s Data Set:** We used the Coronavirus data provided at https://zenodo.org/record/5128604/#.YskYIoTP2Ul

### Narratives

The narrative graphs used to evaluate BiND are provided in the folder *NarrativeGraphs*. The same narratives expressed in natural language are provided in the folder *Narratives_NaturalLanguage*

### Using BiND

#### Setup

The following steps need to be done before running the application for the first time

1. **Provide Data Sets:** Create a folder which contains all data sets that will be used in the evaluation. These need to be provided as .json or .csv files
2. **Provide Narrative Graphs:** Create a second folder which contains all narrative graphs, for which BiND will compute narrative bindings. These graphs can be defined in .txt files
3. **Prepare the config file:** Modify the config.txt file by adding the paths to the folders created in the previous steps, as well as the paths for the output-files created by BiND. Additionally you can define the delimiter used in the csv files and the thresholds for the individual matching steps
4. **Prepare SBERT Input:** Run DataNarrations.jar using the option *4: PrepareEmbeddings* in the first dialog. BiND will create lists containing the input to be used by SBERT in the following step (the files will be created in the folder defined by the *embedding_path* in the config.txt)
5. **Run SBERT:** Run *SBERTCompleteReduced.py* as found in the *Scripts* folder (replace path by the *embedding_path* defined in the config.txt)

#### Running BiND

1. **Start Application:** Run DataNarrations.jar (the application will load all data sets and narratives first - this can take a few seconds)
2. **Select Option:** Choose the option *0: Run BiND* in the first dialog
3. **Specify Narrative Graph:** Specify the filename of a narrative graph for which you want to compute bindings (only type the filename, e.g. WHO_DS_1.txt, not the complete path)
4. **Results:** The results will be created at the specified output path

#### Evaluating BiND

1. **Provide Ground Truth Files:** Create a third folder where you can define the ground truth used for the evaluation. To do so, you have to create one .txt file per narrative graph (the file names need to be identical to the naming used in step 2). Add the path of the folder to the config.txt
2. **Start Application:** Run DataNarrations.jar (the application will load all data sets and narratives first - this can take a few seconds)
3. **Select Option:** In the first dialog choose option *1: Complete Pipeline* or *2: Partial Execution* (in the second option you then have to specify which parts of BiND's pipeline should be used)
4. **Select Evaluation Mode:** If you choose option *0: Full Evaluation* BiND evaluates all thresholds from 0 to 1 (in steps of 0.05) for the last step of the pipeline (for the previous steps you are asked to choose a specific threshold). With option *1: Single Threshold* you can also set a specific threshold for the last matching step (this mode also creates more detailed output files)
5. **Results:** The results will be created at the specified output path
