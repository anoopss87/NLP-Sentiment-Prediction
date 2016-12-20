import java.io.*;

public class preProcessing
{
	private static void cleanData(String fName, String fType) throws IOException
    {
        String filePath = System.getProperty("user.dir") + "\\" + fName;       
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        PrintWriter writer = new PrintWriter(fType + "_cleaned.txt", "UTF-8");
        String line = "";
        int curId = -1;
                 
        br.readLine();
        while((line=br.readLine()) != null)
        {
            String[] words = line.split("\t");            
             
            if(curId != Integer.parseInt(words[1]) && !(words[2].trim().isEmpty()))
            {
                if(words.length >= 4)
                {
                    writer.println(words[2] + "\t" + words[3]);                                       
                }
                else
                {
                    writer.println(words[2]);
                }
                curId = Integer.parseInt(words[1]);
            }            
        }
        writer.close();
        br.close();
    }
     
    private static void scaleDown(String fType) throws IOException
    {       
        if(fType.toLowerCase().contains("train"))
        {
            String filePath = System.getProperty("user.dir") + "\\" + fType + "_cleaned.txt";
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            PrintWriter writer = new PrintWriter(fType + "_scaled.txt", "UTF-8");
            String line = "";
             
            while((line=br.readLine()) != null)
            {
                String[] words = line.split("\t");
                int rating = Integer.parseInt(words[1]);
                if(rating >= 0 && rating <=1)
                {
                    writer.println(words[0] + "\t" + 0); 
                }
                else if(rating == 2)
                {
                    writer.println(words[0] + "\t" + 1); 
                }
                else if(rating >=3 && rating <= 4)
                {
                    writer.println(words[0] + "\t" + 2); 
                }
            }
            writer.close();
            br.close();
        }       
    }
	public static void main(String[] args) throws IOException
	{
		String fileName = args[0];
        String fileType = fileName.substring(0, fileName.length()-4);
         
        cleanData(fileName, fileType);
        scaleDown(fileType);
	}
}
