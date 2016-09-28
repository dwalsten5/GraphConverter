package main.java.model;


public class ChartComment {

	private String _id;
	private String owner;
	private String text;
	private String createdDate;
	private String attachment;
	
	
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
	
	public String getCreatedDate() {
		return this.createdDate;
	}
	
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	/**
	* 
	* @return
	* The text
	*/
	public String getText() {
	return text;
	}
	
	/**
	* 
	* @param text
	* The text
	*/
	public void setText(String text) {
	this.text = text;
	}
	
	/**
	* 
	* @return
	* The attachment
	*/
	public String getAttachment() {
	return attachment;
	}
	
	/**
	* 
	* @param attachment
	* The attachment
	*/
	public void setAttachment(String attachment) {
	this.attachment = attachment;
	}
	
}
