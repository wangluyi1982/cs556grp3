package org.vijjana.ontology.file;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vijjana.util.keyphase.Iterator.UnStremTokenIterator;

public class FileModel {

	private static final String patternstr = "([a-zA-Z]+){2,100}";
	private static final Pattern pattern = Pattern.compile(patternstr);
	private static Matcher matcher;
	
	public static Hashtable<String,ArrayList<Integer>> ReadFile(String fileContent,String encoding)
	{
		StringBuffer StringBuffer = new StringBuffer(fileContent);
		ByteArrayInputStream bis =null;
		try {
		bis= new ByteArrayInputStream(StringBuffer.toString().getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ReadFile(bis);
	}
	
	public static Hashtable<String,ArrayList<Integer>> ReadFile(String readFileLocation, boolean isFile) throws FileNotFoundException{
		File f = new File(readFileLocation);
		return ReadFile(f);
	}
	
	public static Hashtable<String,ArrayList<Integer>> ReadFile(File readFile) throws FileNotFoundException{
		if(!readFile.isFile()){
			System.out.println("File Error");
			return null;
		}
		
		FileInputStream is =  new FileInputStream(readFile);
		return ReadFile(is);	
	}
	
	public static Hashtable<String,ArrayList<Integer>> ReadFile(InputStream inputStream){
		Hashtable<String,ArrayList<Integer>> fileTable = new Hashtable<String,ArrayList<Integer>>();
		String readLine;
		InputStreamReader is = new InputStreamReader(inputStream);
		
		try {
			BufferedReader br = new BufferedReader(is);
			int index =0;
			String pstr ;
			while((readLine=br.readLine())!=null)
			{
				Iterator unStreamTokenIterator = new UnStremTokenIterator(readLine, patternstr);
				
				for (; unStreamTokenIterator.hasNext();) {
					index++;
					pstr= (String) unStreamTokenIterator.next();
					if(!fileTable.containsKey(pstr))
					{
						fileTable.put(pstr, new ArrayList<Integer>());
					}
					fileTable.get(pstr).add(index);
				}
			}
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return fileTable;
	}
	public static String printTable(Hashtable<String, ArrayList<Integer>> table)
	{
		StringBuilder sb = new StringBuilder();
		Set<String> set = table.keySet();
		String tokenStr;
	    Iterator<String> itr = set.iterator();
	    while (itr.hasNext()) {
	      tokenStr = itr.next();
	      sb.append(tokenStr + ": " + table.get(tokenStr).size()+"\n");
	    }
	    return sb.toString();	    
	}
	 public static void main(String[] args)
	 {
		 Hashtable<String,ArrayList<Integer>> testTable;
		try {
			/*
			testTable = ReadFile("data/bostid_b12sae.txt",true);	
			 */
			String str = "The project that is the subject of this report was approved by the Governing Board of the National Research Council, whose members are drawn from the councils of the"
				+ "National Academy of Sciences' the National Academy of Engineering, and the Institute of Medicine. The members of the committee responsible for the report were chosen for their special competences and with regard for appropriate balance.";
		
			testTable = ReadFile(str,"UTF-8");
			FileWriter out;
			
			out = new FileWriter("Output/Log.txt");
			 out.write(printTable(testTable));
			 out.close();
			
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	 }
	 
}
