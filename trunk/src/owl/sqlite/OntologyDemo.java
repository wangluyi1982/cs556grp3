package owl.sqlite;

public class OntologyDemo {
	
	public static void main(String[] args) throws Exception{
		OntologyHandler ont = new OntologyHandler("res/ontology.db");
		ont.Initialize("Professor");
		ont.AddNode("Teaching", "Professor");
		ont.AddNode("Research", "Professor");
		ont.AddNode("Proposals", "Research");
		
		//System.out.println(dbHandler.PathToNode("Research", "")); // path from root to Research
		//System.out.println(dbHandler.NodeChildren("Professor")); // children of Professor
		
		System.out.println(ont.PathThrough("Research"));
		
		//ont.RemoveNode("Proposals");
		//System.out.println(ont.PathThrough("Research"));
		
		System.out.println("\nPaths:");
		String[] keywords = {"Research", "Professor", "Teaching", "Proposals"};
		String[][] paths = ont.SimilarityMatch(keywords);
		
		for (int i = 0; i < paths[0].length-1; i++){
			System.out.println(paths[i][0] + ": " + paths[i][1]);
		}
		
		System.out.println("\nBest Path: ");
		String[] best = ont.BestMatch(paths, 1);
		ont.PrintArray(best);
		
		System.out.println("\nTree:");
		ont.ShowRelationships();
		
		ont.SaveOWL();
		ont.Close();
	}
}