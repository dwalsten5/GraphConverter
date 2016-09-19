package main.java.model;

import java.util.List;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

public class FlowChart {

	private static String START_NODE = "someothernode9999"; //This is the key used to identify the start of the flowchart
	private String _id;
	private String name;
	private String description;
	private String updatedDate;
	private String version;
	private String owner;
	private TinkerGraph graph; 
	private List<String> all_res;
	private List<ChartComment> comments;
	
	
	/**
	* 
	* @return
	* The id
	*/
	public String getId() {
	return _id;
	}
	
	/**
	* 
	* @param id
	* The id
	*/
	public void setId(String id) {
	this._id = id;
	}
	
	/**
	* 
	* @return
	* The name
	*/
	public String getName() {
	return name;
	}
	
	/**
	* 
	* @param name
	* The name
	*/
	public void setName(String name) {
	this.name = name;
	}
	
	/**
	* 
	* @return
	* The description
	*/
	public String getDescription() {
	return description;
	}
	
	/**
	* 
	* @param description
	* The description
	*/
	public void setDescription(String description) {
	this.description = description;
	}
	
	/**
	* 
	* @return
	* The updateDate
	*/
	public String getUpdatedDate() {
	return updatedDate;
	}
	
	/**
	* 
	* @param updateDate
	* The updateDate
	*/
	public void setUpdatedDate(String updateDate) {
	this.updatedDate = updateDate;
	}
	
	/**
	* 
	* @return
	* The version
	*/
	public String getVersion() {
	return version;
	}
	
	/**
	* 
	* @param version
	* The version
	*/
	public void setVersion(String version) {
	this.version = version;
	}
	
	/**
	* 
	* @return
	* The author
	*/
	public String getOwner() {
	return owner;
	}
	
	/**
	* 
	* @param author
	* The author
	*/
	public void setOwner(String owner) {
	this.owner = owner;
	}
	
	/**
	* 
	* @return
	* The graph
	*/
	public TinkerGraph getGraph() {
	return graph;
	}
	
	/**
	* 
	* @param graph
	* The graph
	*/
	public void setGraph(TinkerGraph graph) {
		this.graph = graph;
	}
	
	public List<String> getAllRes() {
		return this.all_res;
	}
	
	public void setAllRes(List<String> ar) {
		this.all_res = ar;
	}
	
	public List<ChartComment> getComments() {
		return this.comments;
	}
	
	public void setComments(List<ChartComment> c) {
		this.comments = c;
	}

}
