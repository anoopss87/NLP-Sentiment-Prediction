# NLP-Sentiment-Prediction

Preprocessing
The annotated movie dataset obtained had extra parameters along with review text and its rating. So the first step was to clean the data. The cleaning of dataset was done using “preProcessing.Java” which takes “train.tsv” as the input. Also, the annotated dataset had 5 class as shown below:
0 – negative, 1 - somewhat negative, 2 – neutral, 3 - somewhat positive, 4 – positive
It was scaled down to 3 class as shown:
0 – negative, 1 – neutral, 2 – positive
This step will generate the output file called “train_scaled.txt” which will used as the input for feature generation step.
How to compile:
1)	Copy preprocessing.java and train.tsv into the same directory and run javac preProcessing.java
2)	Run java –cp . preProcessing train.tsv

Baseline Strategy
Our baseline strategy use the standard list of positive and negative words to predict the sentiment of the reviews. Our program baselineNLP.java will the read train_scaled.txt and outputs the prediction in result.txt and prints the accuracy in terms of precision and recall of each class.
How to compile:
a)	Copy positive-words.txt, negative-words.txt, train_scaled.txt and baselineNLP.java into the same directory and run the command javac baselineNLP.java
b)	Run the command java baselineNLP

Feature Generation for improved strategy
1)	The project uses “synonymy” and “hypernymy” as the semantic features. We have used python nltk library to generate these features.
The program Semantic_Feature.py will use train_scaled.txt as the input and will generate syn.txt and hyp.txt which contains the synonym and hypernym per token for the entire review text.
How to compile:
a)	Copy Semantic_Feature.py and train_scaled.txt in the same directory
b)	Run the command python Semantic_Feature.py
The output of synonym and hypernym is of the format as below which requires cleaning to be used while creating feature vector.
escapades	Synset('undertaking.n.01')
2)	The program cleanSemFeatures.java will clean the output of synonym and hypernym. It reads syn.txt and hyp.txt and creates synonymn.txt and hypernym.txt as the output.
How to compile:
a)	Copy syn.txt, hyp.txt and cleanSemFeatures.java into the same directory and run the command javac cleanSemFeatures.java
b)	Run the command java cleanSemFeatures
3)	We have used Stanford NLP library to generate other 4 features namely tokenization, lemmatization, part of speech tagging and type dependencies. Our program GenerateFeatures.java will generate the above mentioned 4 features and also uses the synonym.txt and hypernym.txt to generate the feature vector of all the six features which will be used by the machine learning model. The program also does stop words removal, new feature generation which uses positive.txt and negative.txt which contains the standard list of positive and negative words.
How to compile:
a)	Copy hypernym.txt, synonym.txt, positive-words.txt, negative-words.txt, stopwords.txt, train_scaled.txt and GenerateFeatures.java into the same directory and run the command javac GenerateFeatures.java
b)	Run the command java GenerateFeatures
The above program will generate feature vector called “vectors.txt”.

Machine Learning Model
1)	We have used R programming environment for predict the sentiment of the review text. Our program NLP.r uses vectors.txt as the input and predict the sentiment of the reviews. We have used precision and recall as the accuracy measurement which will printed at the end of the program as below:
How to compile:
a)	Copy NLP.r and vector.txt into R home directory.
b)	Run the command Rscript NLP.r
