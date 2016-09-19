package main.java.model;


public class ChartComment {


	private String text;
	private String attachment;
	private String nodeId;
	
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
	
	/**
	* 
	* @return
	* The nodeId
	*/
	public String getNodeId() {
	return nodeId;
	}
	
	/**
	* 
	* @param nodeId
	* The node_id
	*/
	public void setNodeId(String nodeId) {
	this.nodeId = nodeId;
	}
}
