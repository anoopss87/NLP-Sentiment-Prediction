import java.io.*;
import java.util.*;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.util.*;

public class Parser
{
	private StanfordCoreNLP scnlp;
	Parser()
	{
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, parse");
		scnlp = new StanfordCoreNLP(props);
	}
	
	private void createVec() throws IOException
	{
		BufferedReader bfr = new BufferedReader(new FileReader(new File("train_scaled.txt")));
		String review = "";
		Annotation document;
		PrintWriter p1 = new PrintWriter("parser.txt", "UTF-8");
		
		while ((review = bfr.readLine()) != null )
		{
			System.out.println("-------------------------------------------------------------");
			p1.println("---------------------------------------------------------------------");
			String[] temp = review.split("\t");
			review = temp[0];
			document = new Annotation(review);
	        scnlp.annotate(document);
	        
	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            for(CoreMap sentence: sentences)
            {
                SemanticGraph dependencies = sentence.get(BasicDependenciesAnnotation.class);
                IndexedWord root = dependencies.getFirstRoot();
                System.out.printf("root(ROOT-0, %s-%d)%n", root.word(), root.index());
                for (SemanticGraphEdge e : dependencies.edgeIterable())
                {                	
                	p1.printf("%s(%s, %s)%n", e.getRelation().toString(), e.getGovernor().word(), e.getDependent().word());
                    System.out.printf ("%s(%s, %s)%n", e.getRelation().toString(), e.getGovernor().word(), e.getDependent().word());
                }
            }
            System.out.println("-------------------------------------------------------------");
            p1.println("---------------------------------------------------------------------");
		}
		bfr.close();
		p1.close();
	}
	
	public static void main(String[] args) throws IOException
	{		
		Parser p = new Parser();
		double startTime = System.currentTimeMillis();
		p.createVec();
		double endTime = System.currentTimeMillis();
		System.out.format("Total compute time is %f minutes", (endTime - startTime)/60000);
	}
}
