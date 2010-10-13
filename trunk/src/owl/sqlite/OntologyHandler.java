package owl.sqlite;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

public class OntologyHandler extends OntologyTree {

	public OntologyHandler(String filename) throws Exception {
		super(filename);
		// TODO Auto-generated constructor stub
		
	}

	/**
	 * Initializes the database to be used as an Ontology tree by creating 2 
	 * tables - Ontology and Relationships, and also adds the root node to the Ontology table
	 * @param root
	 * @return void
	 */
	public void Initialize(String root){
		try{
			stat.executeUpdate("create table Ontology (ID INTEGER PRIMARY KEY AUTOINCREMENT, Title VARCHAR(500) UNIQUE)");
			stat.executeUpdate("create table Relationships (Parent VARCHAR(500), Child VARCHAR(500), PRIMARY KEY (Parent, Child))");
			stat.execute("INSERT INTO Ontology(title) VALUES ('" + root + "')");
			System.out.println("Created tables: Ontology, Relationships. Database is ready!");
		}catch(Exception e){
			System.out.println("Database & tables already exist");
		}		
	}
	
	/**
	 * Gives a count of how many words in keywords were matched in a single path 
	 * @param keywords - words to be matched
	 * @param path - String representing path in the tree to be searched for keywords
	 * @return count - number keywords found in path
	 */
	private int RankPath(String[] keywords, String path){

		int count = 0;
		for (int i = 0; i < keywords.length; i++){
			if (path.lastIndexOf((keywords[i])) > 0){
				count++;
			}
		}
		return count;
	}
	
	
	/**
	 * Returns paths in the tree that match a given set of keywords
	 * @param keywords - array of keywords to be matched
	 * @return String[] paths - array of paths that matched the keywords
	 */
	public String[][] SimilarityMatch(String[] keywords){
		
		String path = "";
		String[][] paths = new String[keywords.length][keywords.length+1]; // rank, path
		int cnt = 0;
		for (int i = 0; i < keywords.length; i++){
			
			// for each keyword, find a path in the tree through it
			path = PathToNode(keywords[i], "");
			paths[cnt][0] = Integer.toString(RankPath(keywords, path));
			paths[cnt][1] = path;
			cnt++;
		}
		return paths;
	}
	
	/**
	 * Returns the top n matching paths
	 * @param paths 2D String array representing paths in the tree and their ranks when matched 
	 * to some keywords (paths[x][0] = rank, paths[x][1] = path)
	 * @param n  number of highest ranked paths to return
	 * @return 1d array of top 'n' paths
	 */
	public String[] BestMatch(String[][] paths, int n){
		if (n > paths.length){
			// max can't be greater than the number of paths
			n = paths.length;
		}
		String[] best = new String[n];		
		Arrays.sort(paths, new RankComparator());
		
		for (int i = 0; i < n; i++){
			best[i] = paths[i][1];
		}
		return best;
	}
	
	class RankComparator implements Comparator<Object>
	{
	  public int compare(Object obj1, Object obj2)
	  {
		  String[] str1 = (String[])obj1;
		  String[] str2 = (String[])obj2;
		  
	    if (Integer.parseInt(str1[0]) > Integer.parseInt(str2[0]))
	    	return -1;
	    else if (Integer.parseInt(str1[0]) < Integer.parseInt(str2[0]))
	    	return 1;
	    else
	    	return 0;
	  }
	}
	
	public void PrintArray(String[] array){
		for (int i = 0; i < array.length; i++){
			System.out.println(array[i]);
		}
	}
	
	/**
	 * For detailed explanation of what this function does, see:
	 * http://owlapi.sourceforge.net/documentation.html -> Adding Axioms
	 * To summarize, this function generates the OWL file using the data stored
	 * in the Relationships table
	 * 
	 * @param filename file to save to (.owl)
	 * @return void
	 */
	public void SaveOWL(String filename) throws SQLException{

	        try {
	            // Create the manager that we will use to load ontologies.
	            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	            // Specify IRI used to identify the ontology
	            IRI ontologyIRI = IRI.create("http://code.google.com/p/cs556grp3/ontology.owl");
	            
	            // Create the document IRI for our ontology
	            String loc = System.getProperty("user.dir");
	            String path = "file:/" + loc.replace("\\", "/").substring(3) + "/res/" + filename;
	            
	            IRI documentIRI = IRI.create(path);
	            
	            // Set up a mapping, which maps the ontology to the document IRI
	            SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
	            manager.addIRIMapper(mapper);

	            // Now create the ontology - we use the ontology IRI (not the physical URI)
	            OWLOntology ontology = manager.createOntology(ontologyIRI);
	            
	            OWLDataFactory factory = manager.getOWLDataFactory();
	            
	            // Fetch the data in the Relationships table
	            ResultSet rs = stat.executeQuery("SELECT * FROM Relationships");
	    		String parent, child;
	    		OWLClass clsA = null, clsB;
	    		
	    		// for each relationship, create a B is a subclass of A relationship
	    		while (rs.next()){
	    			parent = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("parent"), "title");
	    			child = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("child"), "title");

	    			clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#" + child));
	    			clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#" + parent));
	    			
		            // Now create the axiom
		            OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);

		            // Add the Axiom to the Ontology
		            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		   
		            // Use the manager to apply changes
		            manager.applyChange(addAxiom);
	    		
		            // We should also find that B is an ASSERTED superclass of A
		            Set<OWLClassExpression> superClasses = clsA.getSuperClasses(ontology);
		            System.out.println("Asserted superclasses of " + clsA + ":");
		            for (OWLClassExpression desc : superClasses) {
		                System.out.println(desc);
		            }
	    		}
	    		
	    		// The ontology will now contain references to class A and class B 
	            for (OWLClass cls : ontology.getClassesInSignature()) {
	                System.out.println("Referenced class: " + cls);
	            }

	            // Now save the ontology (overwrite if file already exists). 
	            manager.saveOntology(ontology);

	        }
	        catch (OWLException e) {
	            e.printStackTrace();
	        }
	}	
	
	public String[] ExtractKeywords(String file){
		String[] keywords = null;
		try{
			FileReader input = new FileReader(file);
	        
			BufferedReader bufRead = new BufferedReader(input);
	        String line; 	// String that holds current file line
	        String tmp;
	        
	        // Read first line
	        line = bufRead.readLine();
	        
			// Read through file one line at time. Print line # and line
	        Pattern pattern = Pattern.compile("\\<keywords\\>.*\\<\\/keywords\\>");
	        
	        // if a match is found, we'll replace all commas followed by spaces with just commas
	        Pattern p = Pattern.compile(",\\s*");
	       
	        while (line != null){
	        	Matcher matcher = pattern.matcher(line);
	        	while(matcher.find()){
	        		String match = matcher.group();
	        		String m = p.matcher(match).replaceAll(",");		
	        		keywords = m.substring(m.indexOf(">")+1, m.indexOf("</")).split(",");
	        		
	        		//fix case for keywords
	        		for (int i = 0; i < keywords.length; i++){
	        			tmp = CapWords(keywords[i]);
	        			keywords[i] = tmp;
	        		}
	        	}
	            line = bufRead.readLine();
	        }
	        
	        bufRead.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return keywords;
	}
}
