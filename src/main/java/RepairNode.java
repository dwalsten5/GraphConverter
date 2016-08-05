package main.java;

import java.util.List;

public class RepairNode {
	String id;
	String question;
	String details;
	List<String> image;
	List<String> attachment;
	List<String> options;
	List<String> next_question;
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String i) {
		this.id = i;
	}
	
	public String getQuestion() {
		return this.question;
	}
	
	public void setQuestion(String q) {
		this.question = q;
	}
	
	public String getDetails() {
		return this.details;
	}
	
	public void setDetails(String d) {
		this.details = d;
	}
	
	public List<String> getImage() {
		return this.image;
	}
	
	public void setImage(List<String> i) {
		this.image = i;
	}
	
	public List<String> getAttachment() {
		return this.attachment;
	}
	
	public void setAttachment(List<String> a) {
		this.attachment = a;
	}
	
	public List<String> getOptions() {
		return this.options;
	}
	
	public void setOptions(List<String> o) {
		this.options = o;
	}
	
	public List<String> getNextQuestion() {
		return this.next_question;
	}
	
	public void setNextQuestion(List<String> n) {
		this.next_question = n;
	}
}
