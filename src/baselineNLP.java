import java.io.*;
import java.util.*;
  
public class baselineNLP 
{
    HashSet<String> pos = new HashSet<String>();
    HashSet<String> neg = new HashSet<String>();
        
    int corCount = 0;
    int totalCount = 0;
    
    int zeroTPCount = 0; 
    int zeroFPCount = 0;
    int zeroFNCount = 0;
    int oneTPCount = 0;
    int oneFPCount = 0;
    int oneFNCount = 0;
    int twoTPCount = 0;
    int twoFPCount = 0;
    int twoFNCount = 0;   
     
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
    }
     
    private void predict() throws IOException
    {       
        String line = "";       
             
        String filePath = System.getProperty("user.dir") + "\\"  + "train_scaled.txt";         
         
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        PrintWriter writer = new PrintWriter("result.txt", "UTF-8");           
           
        while((line=br.readLine()) != null)
        {
            String[] words = line.split("\t");
            String[] tags = words[0].split("\\s+");
            totalCount++;
            int posCount = 0;
            int negCount = 0;            
            
            for(String w : tags)
            {
                if(pos.contains(w))
                {
                    posCount++;
                }
                else if(neg.contains(w))
                {
                    negCount++;
                }
            }
            int total = posCount + negCount;
            int lab = Integer.parseInt(words[1]);             
            
            if(total == 0)
            {               
                writer.println(lab + " " + "1");
                
                if(lab == 1)
                {
                    corCount++;
                    oneTPCount++;
                }
                else
                {
                	oneFPCount++;
                }
                
                if(lab == 0)
                {
                	zeroFNCount++;
                }
                else if(lab == 2)
                {
                	twoFNCount++;
                }
            }
            else if(total > 0)
            {
                writer.println(lab + " " + "2");
                if(lab == 2)
                {
                    corCount++;
                    twoTPCount++;
                }
                else
                {
                	twoFPCount++;
                }
                
                if(lab == 0)
                {
                	zeroFNCount++;
                }
                else if(lab == 1)
                {
                	oneFNCount++;
                }
            }
            else
            {
                writer.println(lab + " " + "0");
                if(lab == 0)
                {
                    corCount++;
                    zeroTPCount++;
                }
                else
                {
                	zeroFPCount++;
                }
                
                if(lab == 2)
                {
                	twoFNCount++;
                }
                else if(lab == 1)
                {
                	oneFNCount++;
                }
            }
        }
        writer.close();
        br.close();       
    }
    
    private void calcAccuracy()
    {
    	System.out.println("===================================================================");
    	System.out.println("The baseline model accuracy is as below:\n");
    	//System.out.format("Total Accuracy is %f %s\n\n", (corCount/(double)totalCount) * 100, "%");
    	double val = 0;
    	if((zeroFPCount + zeroTPCount) != 0)
    		val = (double)zeroTPCount / (zeroFPCount + zeroTPCount) * 100;
    	
        System.out.format("Precision of label neg is %f %s\n", val, "%");
        System.out.format("Precision of label neu is %f %s\n", ((double)oneTPCount / (oneFPCount + oneTPCount)) * 100, "%");
        System.out.format("Precision of label pos is %f %s\n\n", ((double)twoTPCount / (twoFPCount + twoTPCount)) * 100, "%");
         
        System.out.format("Recall of label neg is %f %s\n", ((double)zeroTPCount / (zeroFNCount + zeroTPCount)) * 100, "%");
        System.out.format("Recall of label neu is %f %s\n", ((double)oneTPCount / (oneFNCount + oneTPCount)) * 100, "%");
        System.out.format("Recall of label pos is %f %s\n", ((double)twoTPCount / (twoFNCount + twoTPCount)) * 100, "%");
        System.out.println("===================================================================");
    }
    
    public static void main(String[] args) throws IOException
    {
    	baselineNLP bLine = new baselineNLP();
    	bLine.buildDS();
    	bLine.predict();
    	bLine.calcAccuracy();
    }   
}