import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class GF_5 {
	
	private HashMap<Integer, HashMap<String, Integer>> reviewVectorMap;	// stores review ID and corresponding vector
	private HashSet<String> features;									// stores feature set
	private StanfordCoreNLP scnlp;
	private HashSet<String> stopWords;
	private HashMap<String, String> synsMap;
	private HashMap<String, String> hypeMap;
	private static String TOKEN_FEATURE = "TOKEN_FEATURE";
	private static String LEMMA_FEATURE = "LEMMA_FEATURE";
	private static String POS_FEATURE = "POS_FEATURE";
	private static String SYNM_FEATURE = "SYNM_FEATURE";
	private static String HYPNM_FEATURE = "HYPNM_FEATURE";
	private static String DEPNDCY_FEATURE = "DEPNDCY_FEATURE";
	ArrayList<Integer> label = new ArrayList<Integer>();
	HashMap<Integer, String> hs = new HashMap<Integer, String>();
	
	
	GF_5() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		scnlp = new StanfordCoreNLP(props);
		reviewVectorMap = new HashMap<Integer, HashMap<String, Integer>>();
		synsMap = new HashMap<String, String>();
		hypeMap = new HashMap<String, String>();
		features = new HashSet<String>();
		stopWords = new HashSet<String>();
		hs.put(0, "neg");
		hs.put(1, "neu");
		hs.put(2, "pos");
	}
	
	public void createVectors() throws IOException{
		BufferedReader bfr; 
		
		// populate stop word hashset from stopwords.txt file
		bfr = new BufferedReader(new FileReader(new File("stopwords.txt")));
		String stword;
		while ((stword = bfr.readLine()) != null)
			stopWords.add(stword);
		bfr.close();
		
		//populate synonymy map
		String[] tempArr;
		bfr = new BufferedReader(new FileReader(new File("synonym.txt")));
		String syns;
		while ((syns = bfr.readLine()) != null) {
			tempArr = syns.split("\\s+");
			synsMap.putIfAbsent(tempArr[0], tempArr[1]);
		}
		bfr.close();
		
		//populate hypernymy map
		bfr = new BufferedReader(new FileReader(new File("hypernym.txt")));
		String hyps;
		while ((hyps = bfr.readLine()) != null) {
			tempArr = hyps.split("\\s+");
			hypeMap.putIfAbsent(tempArr[0], tempArr[1]);
		}
		bfr.close();
		
		// read reviews line by line from file train_scaled.txt
		String review;
		int reviewId = 0;
		HashMap<String, Integer> reviewVec;
		Annotation document;
		String lemma, token, postag;
		HashMap<String, String> tempFeatures;
		bfr = new BufferedReader(new FileReader(new File("train_scaled.txt")));
		
		while ((review = bfr.readLine()) != null ) {
			
			String[] temp = review.split("\t");
			review = temp[0];
			label.add(Integer.parseInt(temp[1].trim()));
			document = new Annotation(review);
	        scnlp.annotate(document);
	        tempFeatures = new HashMap<String, String>();
	        reviewVec = new HashMap<String, Integer>();
	        
            for (CoreLabel cl : document.get(CoreAnnotations.TokensAnnotation.class)) {
            	
	        	 token = cl.get(TextAnnotation.class);
	        	 postag = cl.get(PartOfSpeechAnnotation.class);
	        	 lemma = cl.get(CoreAnnotations.LemmaAnnotation.class);
	        	 tempFeatures.put(token, TOKEN_FEATURE);
	        	 tempFeatures.put(postag, POS_FEATURE);
	        	 tempFeatures.put(lemma, LEMMA_FEATURE);
	        	 syns = synsMap.get(token);
	        	 if (syns != null)
	        		 tempFeatures.put(syns, SYNM_FEATURE);
	        	 hyps = hypeMap.get(token);
	        	 if (hyps != null)
	        		 tempFeatures.put(hyps, HYPNM_FEATURE);

	        	 for (String t:tempFeatures.keySet()) {
		        	 if(!stopWords.contains(t.toLowerCase()) 										// filter stopwords
		        			 && !Pattern.compile("[0-9]+").matcher(t).find()		// filter numeric/alphanumeric values
		        			 && !Pattern.compile("[;#$&-.,=:!?/\\+\\*\\\\]").matcher(t).find()	// filter punctuations. 
		        			 && !t.startsWith("'")){		
		        			// TODO filter apostrophe '
		        		 if (!features.contains(t)) {
		        			 features.add(t);
		        			 reviewVec.put(t, 1);
		        		 } else {
		        			 Integer w = reviewVec.get(t);
		        			 if (w == null)
		        				 reviewVec.put(t, 1);
		        			 else
		        				 reviewVec.put(t, w+1);
		        		 }		        		  
		        	 }
	        	 }
            }
            reviewVectorMap.put(++reviewId, reviewVec);
		}
		bfr.close();
	}
	
	public void writeVectors() throws IOException{
		// iterate over map reviewVecMap
			// sort reviewVector hashmap keyset alphabetically
			// iterate over feateres hash set alphabetically
				// write out features values by reading from reviewVexctor hashmap
		
		BufferedWriter bfr = new BufferedWriter(new FileWriter(new File("features.txt")));
		List<String> sortedFeatures = new ArrayList<String>();
		sortedFeatures.addAll(features);
		Collections.sort(sortedFeatures);
		for (String s:sortedFeatures)
			bfr.write(s+"\n");
		bfr.close();
		
		bfr = new BufferedWriter(new FileWriter(new File("vectors.txt")));
		bfr.write("reviewId,");
		for(int i=0;i<sortedFeatures.size();++i)
		{
			bfr.write("f" + (i+1) + ",");
		}
		bfr.write("label" + "\n");
		List<String> sortedRevFeatures = new ArrayList<String>();
		HashMap<String, Integer> revVec;
		String feat;
		for (int i = 1; i <= reviewVectorMap.size(); i++) {
			revVec = reviewVectorMap.get(i);
			sortedRevFeatures.clear();
			sortedRevFeatures.addAll(revVec.keySet());
			Collections.sort(sortedRevFeatures);
			int c = 0;
			bfr.write(i + ",");
			for (String s:sortedFeatures) {
				if (c < sortedRevFeatures.size()) {
					feat = sortedRevFeatures.get(c);
					if (feat.equals(s)) {
						bfr.write(revVec.get(feat)+",");
						c++;
					}
					else
						bfr.write("0,");
				} else 
					bfr.write("0,");
			}
			bfr.write(hs.get(label.get(i-1)) + " \n");
		}
		bfr.close();
	}
	
	public static void main(String[] args) throws IOException {
		GF_5 gf = new GF_5();
		gf.createVectors();
		gf.writeVectors();
	}

}
