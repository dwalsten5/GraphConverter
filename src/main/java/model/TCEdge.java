package main.java.model;

public class TCEdge {
	//Fields defined as Edge by our GraphSON
	private String _id;
	private String _label;
	private String source; //source vertex
	private String target; //target vertex
	private String details;
	
	public String getId() {
		return this._id;
	}
	
	public void setId(String id) {
		this._id = id;
	}
	
	public String getLabel() {
		return this._label;
	}
	
	public void setLabel(String label) {
		this._label = label;
	}
	
	public String getOutV() {
		return this.source;
	}
	
	public void setOutV(String outV) {
		this.source = outV;
	}
	
	public String getInV() {
		return this.target;
	}
	
	public void setInV(String inV) {
		this.target = inV;
	}
	
	public String getDetails() {
		return this.details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	

}
