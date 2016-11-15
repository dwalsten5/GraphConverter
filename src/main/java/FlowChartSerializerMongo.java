package main.java;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import main.java.TCVertexSerializer;

import main.java.model.FlowChart;
import main.java.model.TCVertex;

//This class is needed in order to write this information, so let's get this shit going
public class FlowChartSerializerMongo implements JsonSerializer<FlowChart> {

    
	public JsonElement serialize(FlowChart flowchart, Type typeOfId, JsonSerializationContext context) {
		Gson myGson = buildGson();
		final JsonObject jsonObject = new JsonObject();//This is the new object which will be written to the file
		jsonObject.addProperty("_id", flowchart.getId());
		jsonObject.addProperty("name", flowchart.getName());
		jsonObject.addProperty("description", flowchart.getDescription());
		jsonObject.addProperty("createdDate", flowchart.getCreatedDate());
		jsonObject.addProperty("updatedDate", flowchart.getUpdatedDate());
		jsonObject.addProperty("version", flowchart.getVersion());
		jsonObject.addProperty("owner", flowchart.getOwner());
		
		//Add empty resource array, can add manually later
		final JsonArray res = new JsonArray();
		jsonObject.add("resources", res);
		
		//Determine type
		switch(flowchart.getType()) {
			case DEVICE:
				jsonObject.addProperty("type", "device");
				break;
			case PROBLEM:
				jsonObject.addProperty("type", "problem");
			default :
				jsonObject.addProperty("type", "misc");
		}
		
		//If flowchart has an image, add it
		if (flowchart.getImage() != null ) {
			jsonObject.addProperty("image", flowchart.getImage());
		} else {
			jsonObject.add("image", JsonNull.INSTANCE);
		}
		
		//Upvotes and DownVotes. For now, will guarantee null because we're converting from
		//fresh charts
		final JsonArray upvoted = new JsonArray();
		final JsonArray downvoted = new JsonArray();
		jsonObject.add("upvoted",upvoted);
		jsonObject.add("downvoted", downvoted);	
		
		//Now, we get to have fun with the other objects
		
		//Comments, will set null for now as no comments possible
		final JsonArray comments = new JsonArray();
		jsonObject.add("comments", comments);
		
		//Now, we have to convert the Graph, which is beautiful
		JsonObject graph = new JsonObject();
		JsonArray vertices = new JsonArray();
		//Use Vertex Serializer to do it right
        for (TCVertex v : flowchart.getGraph().getVertices()) {
        	vertices.add(myGson.toJsonTree(v,TCVertex.class));
        }
        graph.addProperty("_id",flowchart.getGraph().getId());
        graph.addProperty("owner", flowchart.getGraph().getOwner());
        graph.addProperty("firstNode", flowchart.getGraph().getFirstNode());
        graph.add("nodes", myGson.toJsonTree(vertices));//Must be nodes for the Mongo dude
        graph.add("edges", myGson.toJsonTree(flowchart.getGraph().getEdges())); //Trying a more direct call
        jsonObject.add("graph", graph);
       
		
		return jsonObject;
	}
	
	private static Gson buildGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		gsonBuilder.registerTypeAdapter(TCVertex.class, new TCVertexSerializer());
		Gson myGson = gsonBuilder.create();
		
		return myGson;
	}
}
