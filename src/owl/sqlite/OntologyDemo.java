package owl.sqlite;


public class OntologyDemo {
	
	public static void main(String[] args) throws Exception{
		OntologyTree ont = new OntologyTree("res/ontology.db");
		ont.Initialize();
		ont.AddNode("Teaching", "Professor");
		ont.AddNode("Research", "Professor");
		ont.AddNode("Proposals", "Research");
		//System.out.println(dbHandler.PathToNode("Research", "")); // path from root to Research
		//System.out.println(dbHandler.NodeChildren("Professor")); // children of Professor
		System.out.println(ont.PathThrough("Research"));
		ont.RemoveNode("Proposals");
		System.out.println("removed Proposals");
		System.out.println(ont.PathThrough("Research"));
		ont.Close();
	}
}
