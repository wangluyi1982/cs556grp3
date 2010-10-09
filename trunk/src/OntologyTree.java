package org.coode.owlapi.examples;

import java.sql.*;

public class OntologyTree {
	Connection conn;
	Statement stat;
	public OntologyTree(String filename) throws Exception{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + filename);
		stat = conn.createStatement();	
		conn.setAutoCommit(true);
	}
	
	public void Initialize(){
		try{
			stat.executeUpdate("create table Ontology (ID INTEGER PRIMARY KEY AUTOINCREMENT, Title VARCHAR(500) UNIQUE)");
			stat.executeUpdate("create table Relationships (Parent VARCHAR(500), Child VARCHAR(500), PRIMARY KEY (Parent, Child))");
			stat.execute("INSERT INTO Ontology(title) VALUES ('Professor')");
			System.out.println("Created tables: Ontology, Relationships. Database is ready!");
		}catch(Exception e){
			System.out.println("Database & tables already exist");
		}
		
	}
	
	public int AddNode(String node, String parent) throws SQLException{
		/*
		 * Adds a new node to the database
		 * @param node - title of node to be added
		 * @param parent - title of node's parent
		 * @return cid - id of node in the Ontology table after it's been added to the database
		 * @return -1 if node was not inserted
		 */
		
		Integer cid = null;
		
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
		return cid;
	}
	
	private String getSingleValue(String query, String want){
		/*
		 * Wrapper function that returns a single value from the database
		 */
		String res = null;
		try{
			ResultSet rs = stat.executeQuery(query);
			while (rs.next()){
				res = rs.getString(want);
			}
		}catch(Exception e){
			System.out.println(e.toString());	
		}
		return res;
	}
	
	public void RemoveNode(String node){
		/*
		 * Removes a node from the Ontology and Relationships table
		 * @param node - title of node to be removed from the Ontology
		 * @return void
		 */
		try{
			stat.execute("DELETE FROM Ontology WHERE title = '" + node + "'");
			stat.execute("DELETE FROM Relationships WHERE parent = '" + node + "' OR child = '" + node + "'");
		}catch(Exception e){
			System.out.println("Failed to remove node " + e.toString());
		}
	}
	
	public String PathToNode(String node, String path){
		/*
		 * Returns a path to a given node
		 * @param node - node whose path is to be returned
		 * @return String[] - string array as path to node
		 */
		if (node != null){
			path = path + " < " + node;
			path = PathToNode(getSingleValue("SELECT title FROM Ontology WHERE id = (SELECT Parent FROM Relationships WHERE Child = (SELECT id FROM Ontology WHERE title = '" + node + "'))", "title"), path);
		}
		return path;
	}
	
	public String NodeChildren(String node){
		/*
		 * Returns the children of a node
		 * @param node - node whose children is to be returned
		 * @return node - string of node's children
		 */
		String res = "";
		try{
			ResultSet rs = stat.executeQuery("SELECT title FROM Ontology WHERE id in (SELECT child FROM Relationships WHERE Parent = (SELECT id FROM Ontology WHERE title = '" + node + "'))");
			while(rs.next()){
				res = res + " < " + rs.getString("title");
			}
		}catch(Exception e){
			return null;
		}
		
		return res;
	}
	
	public String PathThrough(String node){
		/*
		 * Returns a list made up a node's parent nodes and children 
		 */
		return this.NodeChildren(node) + this.PathToNode(node, "");
	}
	
	public String TraverseTree(){
		/*
		 * Displays the Ontology Tree
		 */
		return null;
	}
	
	public void ConvertToOwl(){
		/*
		 * Converts the contents of the database to an OWL file
		 */
	}
	
	public void Close() throws SQLException{
		/*
		 * Closes the database connection
		 */
		conn.close();
	}

}
