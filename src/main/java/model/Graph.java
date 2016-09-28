package main.java.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	
	private String _id;
	private String owner;
	private Map<String,Vertex> nodes;
	private Map<String, Edge> edges;
	private String firstNode;
	
	public Graph() {
		
	}
	
	//Best constructor, actually builds all connections in the graph
	public Graph(List<Vertex> V, List<Edge> E) {
		nodes = new HashMap<String,Vertex>();
		edges = new HashMap<String,Edge>();
		
		//Iterate over all vertices and add to the map
		for (Vertex v : V) {
			nodes.put(v.getId(), v);
		}
		
		//Iterate over the edges and add the edges to the respective in and out
		//lists in the vertices
		for (Edge e : E) {
			System.out.println(e.getId());
			edges.put(e.getId(), e);
			nodes.get(e.getOutV()).addOutEdge(e.getId());
			nodes.get(e.getInV()).addInEdge(e.getId());
		}
	}
	
	public String getId() {
		return this._id;
	}
	
	public void setId(String id) {
		this._id = id;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getFirstNode() {
		return this.firstNode;
	}
	
	public void getFirstNode(String firstNode) {
		this.firstNode = firstNode;
	}
	
	public Vertex getVertex(String id) {
		return nodes.get(id);
	}
	
	public Edge getEdge(String id) {
		return edges.get(id);
	}
	
	public List<Vertex> getVertices() {
		ArrayList<Vertex> verts = new ArrayList<Vertex>();
		for (String key : this.nodes.keySet()) {
			verts.add(this.nodes.get(key));
		}
		return verts;
	}
	
	public List<Edge> getEdges() {
		ArrayList<Edge> edg = new ArrayList<Edge>();
		for (String key : this.edges.keySet()) {
			edg.add(this.edges.get(key));
		}
		return edg;
	}
	
	//Remove given vertex as well as any associated edges in the graph
	public Vertex removeVertex(String id) {
		Vertex toRemove = nodes.remove(id);
		for (String key: toRemove.getOutEdges()) {
			edges.remove(key);
		}
		return toRemove;
	}
	
	//Remove given edge, but not the nodes on either side
	public Edge removeEdge(String id) {
		return edges.remove(id);
	}

}
