package main.java;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import main.java.model.ChartComment;
import main.java.model.TCVertex;

public class TCVertexSerializer implements JsonSerializer<TCVertex> {
	
	public JsonElement serialize(TCVertex vert, Type typeOfId, JsonSerializationContext context) {
		Gson myGson = new Gson();
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("_id",vert.getId());
		jsonObject.addProperty("name", vert.getName());
		if (vert.getGraphId() != null) { //Virtual Node
			jsonObject.addProperty("graphId", vert.getGraphId());
			
		} else {
			jsonObject.addProperty("details", vert.getDetails());
			final JsonArray resources = new JsonArray();
			if (vert.getResources() != null) {
				for (String r : vert.getResources()) {
					resources.add(r);
				}
				jsonObject.add("resources", resources);
			}
			
			final JsonArray images = new JsonArray();
			if (vert.getImages() != null) {
				for (String i : vert.getImages()) {
					images.add(i);
				}
				jsonObject.add("images", images);
			}
			
			
			final JsonArray comments = new JsonArray();
			if (vert.getComments() != null) {
				for (ChartComment c : vert.getComments()) {
					comments.add(myGson.toJson(c));
				}
				jsonObject.add("comments", comments);
			}
		}
		
		
		return jsonObject;
		
		
	}

}
