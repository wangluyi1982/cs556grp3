package org.vijjana.ontology.file;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.extractor.*;
import java.io.*;
 

public class WordReader {

	public static String getText(String fileLocation, boolean isFile)
	{
		File f =null;
		if(isFile)
		{
			 f= new File(fileLocation);
		}else
			return null;
		return getText(f);
	}
	
	
	public static String getText(File readFile)
	{
		if(!readFile.isFile()){
			System.out.println("File error");
		}
		
		POIFSFileSystem fs = null;
		StringBuilder sb = new StringBuilder();
		try
		{
                  fs = new POIFSFileSystem(new FileInputStream(readFile));
                  
                  HWPFDocument doc = new HWPFDocument(fs);
 
		  WordExtractor we = new WordExtractor(doc);
 
		  String[] paragraphs = we.getParagraphText();
 
		  
		  for( int i=0; i<paragraphs .length; i++ ) {
			paragraphs[i] = paragraphs[i].replaceAll("~[a-zA-Z_0-9]","");
			sb.append(paragraphs[i]);
		  }
		}
		catch(Exception e) { 
			e.printStackTrace();
		}
        return sb.toString();       
	}
	
	
	public static void main(String[] args)
	{
		try{
		System.out.println(getText("data/test.doc",true));
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}
