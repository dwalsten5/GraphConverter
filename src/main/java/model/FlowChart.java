package main.java.model;

import java.util.List;


public class FlowChart {

	private String _id;
	private String owner;
	private String name;
	private String description;
	private String createdDate;
	private String updatedDate;
	private String version;
	private List<String> upvoted;
	private List<String> downvoted;
	private TCGraph graph; 
	private List<String> all_res;
	private List<ChartComment> comments;
	private String image;
	private List<String> resources;
	private ChartType type;
	
	public String getCreatedDate() {
		return this.createdDate;
	}
	
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	public List<String> getUpvoted() {
		return this.upvoted;
	}
	
	public void setUpvoted(List<String> upvoted) {
		this.upvoted = upvoted;
	}
	
	public List<String> getDownvoted() {
		return this.downvoted;
	}
	
	public void setDownvoted(List<String> downvoted) {
		this.downvoted = downvoted;
	}
	 
	public String getId() {
	return _id;
	}
	
	public void setId(String id) {
	this._id = id;
	}
	

	public String getName() {
	return name;
	}
	
	public void setName(String name) {
	this.name = name;
	}
	
	public String getDescription() {
	return description;
	}
	
	public void setDescription(String description) {
	this.description = description;
	}
	
	public String getUpdatedDate() {
	return updatedDate;
	}
	
	public void setUpdatedDate(String updateDate) {
	this.updatedDate = updateDate;
	}
	
	public String getVersion() {
	return version;
	}
	
	public void setVersion(String version) {
	this.version = version;
	}
	
	public String getOwner() {
	return owner;
	}
	
	public void setOwner(String owner) {
	this.owner = owner;
	}
	
	public TCGraph getGraph() {
	return graph;
	}
	
	public void setGraph(TCGraph graph) {
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
	
	public String getImage() {
		return this.image;
	}
	
	public void setImage(String im) {
		this.image = im;
	}
	
	public List<String> getResources() {
		return this.resources;
	}
	
	public void setResources(List<String> res) {
		this.resources = res;
	}
	
	public ChartType getType() {
		return this.type;
	}
	
	public void setType(ChartType type) {
		this.type = type;
	}
	public enum ChartType {
		DEVICE, MISC, PROBLEM
	}
}


