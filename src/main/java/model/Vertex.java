package main.java.model;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	//These are fields which are defined by the JSON files
	private String _id;
	private String name;
	private String details;
	private List<String> resources;
	private List<String> images;
	private List<ChartComment> comments;
	
	//These are fields which will be generated once the file is read
	//Don't want to rip these to Json
	private List<String> outEdges;
	private List<String> inEdges;
	
	//Default constructor, helps to initialize the fields which are not initialized directly with
	//the deserializer
	public Vertex() {
		outEdges = new ArrayList<String>();
		inEdges = new ArrayList<String>();
	}
	public String getId() {
		return this._id;
	}
	
	public void setId(String id) {
		this._id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDetails() {
		return this.details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	public List<String> getResources() {
		return this.resources;
	}
	
	public void setResources(List<String> res) {
		this.resources = res;
	}
	
	public List<String> getImages() {
		return this.images;
	}
	
	public void setImages(List<String> im) {
		this.images = im;
	}
	
	public List<ChartComment> getComments() {
		return this.comments;
	}
	
	public void setComments(List<ChartComment> comments) {
		this.comments = comments;
	}
	
	public List<String> getOutEdges() {
		return this.outEdges;
	}
	
	public void addOutEdge(String e) {
		this.outEdges.add(e);
	}
	
	public void setOutEdges(List<String> e) {
		this.outEdges = e;
	}
	
	public List<String> getInEdges() {
		return this.inEdges;
	}
	
	public void addInEdge(String e) {
		this.inEdges.add(e);
	}
	
	public void setInEdges(List<String> e) {
		this.inEdges = e;
	}
	
	

}
