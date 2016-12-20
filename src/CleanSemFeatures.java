import java.io.*;
import java.util.*;
import java.util.regex.*;

public class CleanSemFeatures
{
	HashMap<String, String> syn = new HashMap<String, String>();
    HashMap<String, String> hyp = new HashMap<String, String>();
    HashMap<String, HashMap<String, String>> feat = new HashMap<String, HashMap<String, String>>();
    
    HashMap<String, String> file = new HashMap<String, String>();
    
    CleanSemFeatures()
    {
    	feat.put("syn", syn);
    	feat.put("hyp", hyp);
    	
    	file.put("syn", "synonym.txt");
    	file.put("hyp", "hypernym.txt");
    }
    
    private void cleanSemFeat(String fType) throws IOException
    {
    	String filePath = System.getProperty("user.dir") + "\\" + fType + ".txt";
    	BufferedReader br = new BufferedReader(new FileReader(filePath));
	    String line = "";
	    Pattern p = Pattern.compile("'([^\\s']+)'");
	    
	    HashMap<String, String> hm = feat.get(fType);
    	while((line = br.readLine()) != null)
	    {
	    	String[] words = line.split("\t");
	    	Matcher regexMatcher = p.matcher(words[1]);
	        while (regexMatcher.find())
	        {	           
	        	String val = regexMatcher.group(1);
	            String[] st = val.split("\\.");	            
	            hm.put(words[0].toLowerCase(), st[0].toLowerCase());	            
	        } 
	    }
    	br.close();
    }
    
    private void writeFile(String fType) throws FileNotFoundException, UnsupportedEncodingException
    {
    	PrintWriter p1 = new PrintWriter(file.get(fType), "UTF-8");
    	HashMap<String, String> hm = feat.get(fType);
    	
    	for(String s : hm.keySet())
	    {
	    	p1.println(s + "\t" + hm.get(s));
	    }
    	p1.close();
    }
	public static void main(String[] args) throws IOException
	{
		CleanSemFeatures sf = new CleanSemFeatures();
		sf.cleanSemFeat("syn");
		sf.cleanSemFeat("hyp");
		
		sf.writeFile("syn");
		sf.writeFile("hyp");
	}
}
