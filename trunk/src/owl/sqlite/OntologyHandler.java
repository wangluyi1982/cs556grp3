package owl.sqlite;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

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
	
	public void ConvertToOwl(){
		/*
		 * Converts the contents of the database to an OWL file
		 */
	}
	
	public void ShowRelationships() throws SQLException{
		ResultSet rs = stat.executeQuery("SELECT * FROM Relationships");
		String parent, child;
		System.out.println("\nRelationships");
		while (rs.next()){
			parent = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("parent"), "title");
			child = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("child"), "title");
			System.out.println(child + " is a subclass of " + parent);
		}
	}
	
	private int RankPath(String[] keywords, String path){
		/*
		 * Gives a count of how many words in keywords were matched in path		 * 
		 * @param keywords - words to be matched
		 * @param path - string to be searched for keywords
		 * @return count - number of matches found
		 * hello world
		 */
		int count = 0;
		for (int i = 0; i < keywords.length; i++){
			if (path.lastIndexOf((keywords[i])) > 0){
				count++;
			}
		}
		return count;
	}
	
	public String[][] SimilarityMatch(String[] keywords){
		/*
		 * Returns the best path-match in the tree containing keywords
		 * @param keywords - array of keywords to be matched
		 * @return paths - paths that match the keywords
		 */
		
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
	
	public String[] BestMatch(String[][] paths, int max){
		/*
		 * Returns the top 'max' matching paths
		 * @param paths - ranked paths. path[x][0] = rank, path[x][1] = path
		 * @param max - number of top paths to return
		 * @return - 1d array of top 'max' paths
		 */
		if (max > paths.length){
			// max can't be greater than the number of paths
			max = paths.length;
		}
		String[] best = new String[max];		
		Arrays.sort(paths, new RankComparator());
		
		for (int i = 0; i < max; i++){
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
	
	public void SaveOWL() throws SQLException{
	        try {
	            // Create the manager that we will use to load ontologies.
	            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	            // Ontologies cna have an IRI, which is used to identify the ontology.  You should
	            // think of the ontology IRI as the "name" of the ontology.  This IRI frequently
	            // resembles a Web address (i.e. http://...), but it is important to realise that
	            // the ontology IRI might not necessarily be resolvable.  In other words, we
	            // can't necessarily get a document from the URL corresponding to the ontology
	            // IRI, which represents the ontology.
	            // In order to have a concrete representation of an ontology (e.g. an RDF/XML
	            // file), we MAP the ontology IRI to a PHYSICAL URI.  We do this using an IRIMapper

	            // Let's create an ontology and name it "http://www.co-ode.org/ontologies/testont.owl"
	            // We need to set up a mapping which points to a concrete file where the ontology will
	            // be stored. (It's good practice to do this even if we don't intend to save the ontology).
	            IRI ontologyIRI = IRI.create("http://code.google.com/p/cs556grp3/ontology.owl");
	            // Create the document IRI for our ontology
	            String loc = System.getProperty("user.dir");
	            String path = "file:/" + loc.replace("\\", "/").substring(3) + "/res/ontology.owl";
	            IRI documentIRI = IRI.create(path);
	            // Set up a mapping, which maps the ontology to the document IRI
	            SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
	            manager.addIRIMapper(mapper);

	            // Now create the ontology - we use the ontology IRI (not the physical URI)
	            OWLOntology ontology = manager.createOntology(ontologyIRI);
	            // Now we want to specify that A is a subclass of B.  To do this, we add a subclass
	            // axiom.  A subclass axiom is simply an object that specifies that one class is a
	            // subclass of another class.
	            // We need a data factory to create various object from.  Each manager has a reference
	            // to a data factory that we can use.
	            OWLDataFactory factory = manager.getOWLDataFactory();
	            // Get hold of references to class A and class B.  Note that the ontology does not
	            // contain class A or classB, we simply get references to objects from a data factory that represent
	            // class A and class B
	            
	            
	            //This will be modified so A and B will be a parent and a child
	            
	            ResultSet rs = stat.executeQuery("SELECT * FROM Relationships");
	    		String parent, child;
	    		System.out.println("\nCreating OWL file (" + path + ")...");
	    		OWLClass clsA = null, clsB;
	    		while (rs.next()){
	    			parent = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("parent"), "title");
	    			child = getSingleValue("SELECT title FROM ontology WHERE id = " + rs.getString("child"), "title");

	    			clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#" + child));
	    			clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#" + parent));
	    			
	    		
		            //OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
		            //OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
		            // Now create the axiom
		            OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);
		            // We now add the axiom to the ontology, so that the ontology states that
		            // A is a subclass of B.  To do this we create an AddAxiom change object.
		            // At this stage neither classes A or B, or the axiom are contained in the ontology. We have to
		            // add the axiom to the ontology.
		            AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		   
		            // We now use the manager to apply the change
		            manager.applyChange(addAxiom);
	    		

		            // We should also find that B is an ASSERTED superclass of A
		            Set<OWLClassExpression> superClasses = clsA.getSuperClasses(ontology);
		            System.out.println("Asserted superclasses of " + clsA + ":");
		            for (OWLClassExpression desc : superClasses) {
		                System.out.println(desc);
		            }
	    		}
	    		// The ontology will now contain references to class A and class B - that is, class A and class B
	            // are contained within the SIGNATURE of the ontology let's print them out
	            for (OWLClass cls : ontology.getClassesInSignature()) {
	                System.out.println("Referenced class: " + cls);
	            }

	            // Now save the ontology.  The ontology will be saved to the location where
	            // we loaded it from, in the default ontology format
	            manager.saveOntology(ontology);

	        }
	        catch (OWLException e) {
	            e.printStackTrace();
	        }
	}
	
	
	
}
