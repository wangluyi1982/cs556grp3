package owl.sqlite;
import java.util.Arrays;
import java.util.Comparator;

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
	
	
	
}
