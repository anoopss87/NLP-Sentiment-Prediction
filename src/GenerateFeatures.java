import java.io.*;
import java.util.*;
import java.util.regex.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class GenerateFeatures
{
	// stores review ID and corresponding vector
	private HashMap<Integer, HashMap<String, Integer>> reviewVectorMap;	
	// stores feature set
	private HashSet<String> features;									
	private StanfordCoreNLP scnlp;
	private HashSet<String> stopWords;
	private HashMap<String, String> synsMap;
	private HashMap<String, String> hypeMap;
	
	private static String TOKEN_FEATURE = "TOKEN_FEATURE";
	private static String LEMMA_FEATURE = "LEMMA_FEATURE";
	private static String POS_FEATURE = "POS_FEATURE";
	private static String SYNM_FEATURE = "SYNM_FEATURE";
	private static String HYPNM_FEATURE = "HYPNM_FEATURE";
	//private static String DEPNDCY_FEATURE = "DEPNDCY_FEATURE";
	
	ArrayList<Integer> label = new ArrayList<Integer>();
	HashMap<Integer, String> hs = new HashMap<Integer, String>();
	ArrayList<Integer> negCountFeat = new ArrayList<Integer>();
	ArrayList<Integer> sentiCountFeat = new ArrayList<Integer>();
	ArrayList<Integer> rootSentiFeat = new ArrayList<Integer>();
	
	HashSet<String> pos = new HashSet<String>();
    HashSet<String> neg = new HashSet<String>();
	
	GenerateFeatures()
	{
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, parse");
		scnlp = new StanfordCoreNLP(props);
		reviewVectorMap = new HashMap<Integer, HashMap<String, Integer>>();
		features = new HashSet<String>();
		stopWords = new HashSet<String>();
		synsMap = new HashMap<String, String>();
		hypeMap = new HashMap<String, String>();
		hs.put(0, "neg");
		hs.put(1, "neu");
		hs.put(2, "pos");
	}
	
	private void buildDS() throws IOException
    {
        String filePath = System.getProperty("user.dir") + "\\" + "positive-words.txt";
        String line = "";       
         
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        while((line=br.readLine()) != null)
        {
            pos.add(line.trim());
        }
        br.close();
         
        filePath = System.getProperty("user.dir") + "\\" + "negative-words.txt";
        br = new BufferedReader(new FileReader(filePath));
        while((line=br.readLine()) != null)
        {
            neg.add(line.trim());
        }
        br.close();
        
        // populate stop word hash set from stopwords.txt file
        filePath = System.getProperty("user.dir") + "\\" + "stopwords.txt";
 		br = new BufferedReader(new FileReader(filePath));
 		String stword;
 		while ((stword = br.readLine()) != null)
 		{
 			stopWords.add(stword);
 		}
 		br.close();
 		
 		//populate synonymy map
 		String[] tempArr;
 		filePath = System.getProperty("user.dir") + "\\" + "synonym.txt";
 		br = new BufferedReader(new FileReader(filePath));
 		String syns;
 		while ((syns = br.readLine()) != null)
 		{
 			tempArr = syns.split("\\s+");
 			synsMap.putIfAbsent(tempArr[0], tempArr[1]);
 		}
 		br.close();
 		
 		//populate hypernymy map
 		filePath = System.getProperty("user.dir") + "\\" + "hypernym.txt";
 		br = new BufferedReader(new FileReader(filePath));
 		String hyps;
 		while ((hyps = br.readLine()) != null)
 		{
 			tempArr = hyps.split("\\s+");
 			hypeMap.putIfAbsent(tempArr[0], tempArr[1]);
 		}
 		br.close();
    }
	
	public void createVectors() throws IOException
	{	
		buildDS();
		
		// read reviews line by line from file train_scaled.txt
		String review;
		int reviewId = 0;
		HashMap<String, Integer> reviewVec;
		Annotation document;
		String lemma, token, postag;
		HashMap<String, String> tempFeatures;
		
		BufferedReader bfr = new BufferedReader(new FileReader(new File("train_scaled.txt")));
		BufferedWriter f1 = new BufferedWriter(new FileWriter(new File("tokenise.txt")));
		BufferedWriter f2 = new BufferedWriter(new FileWriter(new File("pos.txt")));
		BufferedWriter f3 = new BufferedWriter(new FileWriter(new File("lemma.txt")));		
		PrintWriter f4 = new PrintWriter("typedDep.txt", "UTF-8");
		PrintWriter featVect = new PrintWriter("featureVector.txt", "UTF-8");
		
		while ((review = bfr.readLine()) != null )
		{			
			String[] temp = review.split("\t");
			review = temp[0];
			label.add(Integer.parseInt(temp[1].trim()));
			document = new Annotation(review);
	        scnlp.annotate(document);	        
	        reviewVec = new HashMap<String, Integer>();
	        tempFeatures = new HashMap<String, String>();
	        ArrayList<String> printFeatures;
	        int id = reviewId + 1;
	        featVect.println("Review No " + id);
	        
            for (CoreLabel cl : document.get(CoreAnnotations.TokensAnnotation.class))
            {
            	 printFeatures = new ArrayList<String>();
	        	 token = cl.get(TextAnnotation.class);
	        	 postag = cl.get(PartOfSpeechAnnotation.class);
	        	 lemma = cl.get(CoreAnnotations.LemmaAnnotation.class);
				 
	        	 if (stopWords.contains(token.toLowerCase()))
					 continue; 	
				 
	        	 tempFeatures.put(token, TOKEN_FEATURE);
	        	 printFeatures.add(token);
	        	 tempFeatures.put(postag, POS_FEATURE);
	        	 printFeatures.add(postag);
	        	 tempFeatures.put(lemma, LEMMA_FEATURE);
	        	 printFeatures.add(lemma);
	        	 String syns = synsMap.get(token);
	        	 if (syns != null) {
	        		 tempFeatures.put(syns, SYNM_FEATURE);
	        	 } 
	        	 printFeatures.add(syns);
	        	 String hyps = hypeMap.get(token);
	        	 if (hyps != null) {
	        		 tempFeatures.put(hyps, HYPNM_FEATURE);
	        	 }
	        	 printFeatures.add(hyps);
	        	 
	        	 for (String t:tempFeatures.keySet())
	        	 {
		        	 if(!Pattern.compile("[0-9]+").matcher(t).find()		// filter numeric/alphanumeric values
		        			 && !Pattern.compile("[;#$&-.,=:!?/\\+\\*\\\\]").matcher(t).find()	// filter punctuation. 
		        			 && !t.startsWith("'"))
		        	 {		
		        		 if(tempFeatures.get(t).equals(TOKEN_FEATURE)) {
		        			 f1.write(token + "\n");
		        		 } else if(tempFeatures.get(t).equals(POS_FEATURE)) {
		        			 f2.write(postag + "\n");
		        		 } else if(tempFeatures.get(t).equals(LEMMA_FEATURE)) {
		        			 f3.write(lemma + "\n");
		        		 }
		        		 if (!features.contains(t))
		        		 {
		        			 features.add(t);
		        			 reviewVec.put(t, 1);
		        		 }
		        		 else
		        		 {
		        			 Integer w = reviewVec.get(t);
		        			 if (w == null)
		        				 reviewVec.put(t, 1);
		        			 else
		        				 reviewVec.put(t, w+1);
		        		 }
		        	 }
	        	 }
	        	 
	        	 
	        	 if (Pattern.compile("[0-9]+").matcher(token).find()
	        			 || Pattern.compile("[;#$&-.,=:!?/\\+\\*\\\\]").matcher(token).find() 
	        			 || token.startsWith("'"))
	        		 continue;
	        	 
	        	// printing to featureVector.txt
	        	 featVect.print("Token : " + printFeatures.get(0) + "\t" + 
	        			 "POSTag : " + printFeatures.get(1) + "\t" + 
	        			 "Lemma : " + printFeatures.get(2) + "\t" + 
	        			 "Synonym : " + printFeatures.get(3) + "\t" + 
	        			 "Hypernym : " + printFeatures.get(4));
	        	 
	        	 featVect.println();
            }
            
            featVect.println();
            featVect.println("Dependency Relations");
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            int negCount = 0;
            int sentiCount = 0;
            f4.println("---------------------------------------------------------------------");
            for(CoreMap sentence: sentences)
            {
                SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
                IndexedWord root = dependencies.getFirstRoot();
                //System.out.printf("root(ROOT-0, %s-%d)%n", root.word(), root.index());
                String rootW = root.word();
                f4.printf("(root, %s)%n", rootW);
                if(pos.contains(rootW))
                {
                	rootSentiFeat.add(1);
                }
                else if(neg.contains(rootW))
                {
                	rootSentiFeat.add(-1);
                }
                else
                {
                	rootSentiFeat.add(0);
                }
                
                for (SemanticGraphEdge e : dependencies.edgeIterable())
                {
                	String relation = e.getRelation().toString();                	
                	if(relation.contains("neg"))
                	{
                		String feat = relation + "_" + e.getGovernor().word();
                		negCount++;
                		if (!features.contains(feat))
		        		{
		        			features.add(feat);
		        			reviewVec.put(feat, 1);
		        		}
		        		else
		        		{
		        			Integer w = reviewVec.get(feat);
		        			if (w == null)
		        				reviewVec.put(feat, 1);
		        			else
		        				reviewVec.put(feat, w+1);
		        		}
                		//System.out.println(feat);
                	}
                	
                	if(relation.contains("amod"))
                	{
                		String amod = e.getDependent().word();
                		if(pos.contains(amod))
                		{
                			sentiCount++;
                		}
                		else if(neg.contains(amod))
                		{
                			sentiCount--;
                		}
                	}
                	
                	if(relation.contains("advmod"))
                	{
                		String advmod = e.getGovernor().word();
                		if(pos.contains(advmod))
                		{
                			sentiCount++;
                		}
                		else if(neg.contains(advmod))
                		{
                			sentiCount--;
                		}
                	}
                	             	
                	featVect.printf("%s(%s, %s),", e.getRelation().toString(), e.getGovernor().word(), e.getDependent().word());
                }
            }
            featVect.println();
            featVect.print("------------------------------------------------\n");
            negCountFeat.add(negCount);
            sentiCountFeat.add(sentiCount);
            reviewVectorMap.put(++reviewId, reviewVec);
		}
		bfr.close();
		f1.close();
		f2.close();
		f3.close();
		f4.close();
		featVect.close();
	}
	
	public void writeVectors() throws IOException
	{
		// iterate over map reviewVecMap
		// sort reviewVector hash map key set alphabetically
		// iterate over features hash set alphabetically
		// write out features values by reading from reviewVexctor hash map		
		
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
		bfr.write("negCount,sentiCount,rootSenti,");
		bfr.write("label" + "\n");
		
		List<String> sortedRevFeatures = new ArrayList<String>();
		HashMap<String, Integer> revVec;
		String feat;
		for (int i = 1; i <= reviewVectorMap.size(); i++)
		{
			revVec = reviewVectorMap.get(i);
			sortedRevFeatures.clear();
			sortedRevFeatures.addAll(revVec.keySet());
			Collections.sort(sortedRevFeatures);
			int c = 0;
			bfr.write(i + ",");
			for (String s:sortedFeatures)
			{
				if (c < sortedRevFeatures.size())
				{
					feat = sortedRevFeatures.get(c);
					if (feat.equals(s))
					{
						bfr.write(revVec.get(feat)+",");
						c++;
					}
					else
						bfr.write("0,");
				} 
				else
				{
					bfr.write("0,");
				}
			}
			bfr.write(negCountFeat.get(i-1) + ",");
			bfr.write(sentiCountFeat.get(i-1) + ",");
			bfr.write(rootSentiFeat.get(i-1) + ",");
			bfr.write(hs.get(label.get(i-1)) + " \n");			
		}
		bfr.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		long startTime = System.currentTimeMillis();
		GenerateFeatures gf = new GenerateFeatures();
		gf.createVectors();
		gf.writeVectors();
		long endTime = System.currentTimeMillis();
		System.out.format("Run time is %d seconds\n", (endTime - startTime)/1000);
	}
}