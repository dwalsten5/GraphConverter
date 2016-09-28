package main.java.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCGraph {
	
	private String _id;
	private String owner;
	private Map<String,TCVertex> nodes;
	private Map<String, TCEdge> edges;
	private String firstNode;
	
	public TCGraph() {
		
	}
	
	//Best constructor, actually builds all connections in the graph
	public TCGraph(List<TCVertex> V, List<TCEdge> E) {
		nodes = new HashMap<String,TCVertex>();
		edges = new HashMap<String,TCEdge>();
		
		//Iterate over all vertices and add to the map
		for (TCVertex v : V) {
			nodes.put(v.getId(), v);
		}
		
		//Iterate over the edges and add the edges to the respective in and out
		//lists in the vertices
		for (TCEdge e : E) {
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
	
	public TCVertex getVertex(String id) {
		return nodes.get(id);
	}
	
	public TCEdge getEdge(String id) {
		return edges.get(id);
	}
	
	public List<TCVertex> getVertices() {
		ArrayList<TCVertex> verts = new ArrayList<TCVertex>();
		for (String key : this.nodes.keySet()) {
			verts.add(this.nodes.get(key));
		}
		return verts;
	}
	
	public List<TCEdge> getEdges() {
		ArrayList<TCEdge> edg = new ArrayList<TCEdge>();
		for (String key : this.edges.keySet()) {
			edg.add(this.edges.get(key));
		}
		return edg;
	}
	
	//Remove given vertex as well as any associated edges in the graph
	public TCVertex removeVertex(String id) {
		TCVertex toRemove = nodes.remove(id);
		for (String key: toRemove.getOutEdges()) {
			edges.remove(key);
		}
		return toRemove;
	}
	
	//Remove given edge, but not the nodes on either side
	public TCEdge removeEdge(String id) {
		return edges.remove(id);
	}

}
