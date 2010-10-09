package owl.sqlite;

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
