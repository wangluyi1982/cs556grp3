package owl.sqlite;

import java.sql.*;
import java.util.Stack;

public class OntologyTree {
	Connection conn;
	Statement stat;
	
	public OntologyTree(String filename) throws Exception{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
		stat = conn.createStatement();	
		conn.setAutoCommit(true);
	}
	
	/**
	 * Converts the first character of each word in the given string to upper case
	 * and replaces spaces with underscores
	 * @param str - String of words
	 * @return newword - String of words with first character of each word converted to upper case
	 */
	public String CapWords(String str){
		String[] words = str.split(" ");
		String tmp, newword = "";
		for (int i = 0; i < words.length; i++){
			tmp = words[i];
			newword += tmp.substring(0,1).toUpperCase() + tmp.substring(1).toLowerCase();
			if (i == (words.length - 2)){
				newword += " ";
			}
		}
		return newword.replace(" ", "_");
	}
	
	/**
	 * Adds a new node to the database
	 * @param node - title of the node to be added
	 * @param parent - title of the node's parent
	 * @return cid - ID of node in the Ontology table after it's been added to the database
	 * , or -1 if failed to add node
	 * @throws SQLException
	 */
	
	public int AddNode(String node, String parent) throws SQLException{
		/*
		 * Adds a new node to the database
		 * @param node - title of node to be added
		 * @param parent - title of node's parent
		 * @return cid - id of node in the Ontology table after it's been added to the database
		 * @return -1 if node was not inserted
		 */
		
		Integer cid = -1;
		node = CapWords(node);
		parent = CapWords(parent);
		
		if (node != parent){
		
			try{
				stat.execute("INSERT INTO Ontology(title) VALUES ('" + node + "')");
			}catch(Exception e){
				System.out.println("Unable to add node. " + e.toString());
				return -1;
			}
			
			if (!parent.equals("null")){
				// get parent id and child id
				String res;
				Integer pid = null;
				
				res = getSingleValue("SELECT id FROM Ontology WHERE title = '" + parent + "'", "id");
				if (res != null){
					pid = Integer.parseInt(res);
				}
				res = getSingleValue("SELECT id FROM Ontology WHERE title = '" + node + "'", "id");
				if (res != null){
					cid = Integer.parseInt(res);
				}
				if (pid != null && cid != null){
					stat.execute("INSERT INTO Relationships(parent, child) VALUES (" + pid + ", " + cid + ")");
					System.out.println("Added '" + node + "' to Ontology");
				}else{
					System.out.println("Unable add '" + node + "'");
				}
			}
		}
		return cid;
	}
	
	/**
	 * Returns a single value from the query to the database
	 * @param query - a sql statement to fetch something from the database
	 * @param want - desired attribute from the query
	 * @return res - result from the query
	 */
	protected String getSingleValue(String query, String want){
		/*
		 * Wrapper function that returns a single value from the database
		 */
		try{
			Statement tmp = conn.createStatement();
			String res = null;
			try{
				ResultSet rs = tmp.executeQuery(query);
				while (rs.next()){
					res = rs.getString(want);
				}
			}catch(Exception e){
				System.out.println(e.toString());	
			}
			return res;
		}catch(Exception e){
			return null;
		}
	}
	
	
	/**
	 * Removes a node from both the Ontology and Relationships table
	 * @param node - title of node to be removed from the Ontology
	 * @return void
	 */
	public void RemoveNode(String node){
		node = CapWords(node);
		try{
			stat.execute("DELETE FROM Ontology WHERE title = '" + node + "'");
			stat.execute("DELETE FROM Relationships WHERE parent = '" + node + "' OR child = '" + node + "'");
		}catch(Exception e){
			System.out.println("Failed to remove node " + e.toString());
		}
	}
	
	
	/**
	 * Returns a single path to a given node in the Ontology tree
	 * @param node - node whose path is to be returned
	 * @return path - String representing path to node
	 */
	public String PathToNode(String node, String path){

		if (node != null){
			path = path + " < " + node;
			path = PathToNode(getSingleValue("SELECT title FROM Ontology WHERE id = (SELECT Parent FROM Relationships WHERE Child = (SELECT id FROM Ontology WHERE title = '" + node + "'))", "title"), path);
		}
		return path;
	}
	
	/**
	 * Initiates traversal of the tree by first determining the root node
	 */
	public void Traverse() throws SQLException{
		Stack<Statement> stack = new Stack<Statement>();
		String root = getSingleValue("SELECT title FROM Ontology WHERE id = 1", "title");
		System.out.println(root);
		TraverseChildren(root, stack);
	}
	
	
	/**
	 * Recursively traverses the Ontology tree starting from the root node
	 * @param node current parent node whose children is to be traversed
	 * @stack stack to hold our statements so the result sets don't get overwritten during recursion
	 * @return void 
	 * @throws SQLException 
	 */
	public void TraverseChildren(String node, Stack<Statement> stack) throws SQLException{
		conn.setAutoCommit(false);
		String str = "";
		for (int i=0; i <= stack.size(); i++){
			str = str + "  ";
		}
		stack.push(conn.createStatement());
		ResultSet rs = stack.peek().executeQuery("SELECT title FROM Ontology WHERE id in (SELECT Child FROM Relationships WHERE Parent = (SELECT id FROM ontology WHERE title = '" + node + "'))");
		while (rs.next()){
			System.out.println(str + rs.getString("title"));
			TraverseChildren(rs.getString("title"), stack);
		}
		stack.pop();
	}
	
	/**
	 * Returns the children of a node
	 * @param node - node whose children is to be returned
	 * @return children - String representing node's children
	 */
	public String NodeChildren(String node){

		String children = "";
		try{
			ResultSet rs = stat.executeQuery("SELECT title FROM Ontology WHERE id in (SELECT child FROM Relationships WHERE Parent = (SELECT id FROM Ontology WHERE title = '" + node + "'))");
			while(rs.next()){
				children = children + " < " + rs.getString("title");
			}
		}catch(Exception e){
			return null;
		}
		
		return children;
	}
	
	/**
	 * Returns a String comprised of a node's parents and children 
	 */
	public String PathThrough(String node){
		return this.NodeChildren(node) + this.PathToNode(node, "");
	}
	
	/**
	 * Closes the database connection
	 * @throws SQLException
	 */
	public void Close() throws SQLException{
		/*
		 * Closes the database connection
		 */
		conn.close();
	}

}
