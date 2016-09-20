package main.java;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import main.java.model.ChartComment;
import main.java.model.FlowChart;

//This class is needed in order to write this information, so let's get this shit going
public class FlowChartSerializer implements JsonSerializer<FlowChart> {

	public JsonElement serialize(FlowChart flowchart, Type typeOfId, JsonSerializationContext context) {
		Gson myGson = new Gson();
		final JsonObject jsonObject = new JsonObject();//This is the new object which will be written to the file
		jsonObject.addProperty("_id", flowchart.getId());
		jsonObject.addProperty("name", flowchart.getName());
		jsonObject.addProperty("description", flowchart.getDescription());
		jsonObject.addProperty("updatedDate", flowchart.getUpdatedDate());
		jsonObject.addProperty("version", flowchart.getVersion());
		jsonObject.addProperty("owner", flowchart.getOwner());
		
		//Now, we get to have fun with the other objects
		//Resources
		final JsonArray all_res = new JsonArray();
		
		//Comments
		final JsonArray comments = new JsonArray();
		if (flowchart.getComments() != null) {
			for (ChartComment c : flowchart.getComments()) {
				comments.add(myGson.toJson(c));
			}
			jsonObject.add("comments", comments);
		}
		
		//Now, we have to convert the TinkerGraph, which is beautiful
		JsonObject graph = new JsonObject();
		JsonArray vertices = new JsonArray();
        JsonArray edges = new JsonArray();
        Map<Vertex, JsonObject> verts = new HashMap<Vertex, JsonObject>();
        for (Vertex v : flowchart.getGraph().getVertices()) {
        	//Have to use this dumb approach because blueprints doesn't work with Gson
        	verts.put(v, vertexToJsonObject(v,all_res));
        }
        //Now that we have the map, can iterate over the edges
        for (Vertex v : verts.keySet()) {
        	vertices.add(verts.get(v));
        }
        for (Edge e : flowchart.getGraph().getEdges()) {
        	edges.add(edgeToJsonObject(e,verts));
        }
        
        //Now that I've parsed over the graph, I'll have the resources! What a concept!
		jsonObject.add("all_res", all_res);
		graph.add("vertices", vertices);
		graph.add("edges", edges);
		jsonObject.add("graph", graph);
		
		return jsonObject;
	}
	
	//I need my helper methods from the old class in order to convert to JsonObjects
	private static JsonObject vertexToJsonObject(Vertex v, JsonArray res) {
        JsonObject obj = new JsonObject();
        //Need to separate vertices I made versus those made by old runs
        
        //NOT IMPLEMENTED - issues with crossover between types. Crossing fingers
        String id;
        if (v.getProperty("start") != null && ((Boolean) v.getProperty("start"))) {
            id = "q1";
        } else {
            id = (int) (Math.random() * 999999999) + "";
        }
        obj.addProperty("_id", id);
        obj.addProperty("name", v.getProperty("question") == null ? v.getProperty("name").toString() : v.getProperty("question").toString());
        obj.addProperty("details", v.getProperty("details") == null ? "" : v.getProperty("details").toString());
        
        if (v.getPropertyKeys().contains("imageURL")) {
        	JsonArray images = new JsonArray();
        	//Splitting by the semicolon
        	for (String im: v.getProperty("imageURL").toString().split(";")) {
        		im = im.trim();
        		images.add(im);
        		res.add(im);
        	}
            obj.add("images", images);
        } else if (v.getPropertyKeys().contains("images")) {
        	JsonArray images = new JsonArray();
        	//Splitting by the semicolon
        	for (String im: v.getProperty("images").toString().split(";")) {
        		im = im.trim();
        		images.add(im);
        		res.add(im);
        	}
            obj.add("images", images);
        }
        /*else {
        	obj.add("image", JsonNull.INSTANCE);
        }
        */
        if (v.getPropertyKeys().contains("resources")) { //Attachments to add
        	JsonArray resources = new JsonArray();
        	//Splitting by the semicolon
        	for (String r: v.getProperty("resources").toString().split(";")) {
        		r = r.trim();
        		resources.add(r);
        		if (!r.endsWith(".json")) {
        			res.add(r);
        		}
        	}
        	obj.add("resources",resources);
        } 
        /* else {
        	obj.add("attachment", JsonNull.INSTANCE);
        }
        */
        //No longer need the options or next question fields
        //obj.add("options", new JsonArray());
        //obj.add("next_question", new JsonArray());
        return obj;
    }
	
	private static JsonObject edgeToJsonObject(Edge e, Map<Vertex, JsonObject> verts) {
    	JsonObject obj = new JsonObject();
    	String id = (int) (Math.random() * 999999999) + "";
    	obj.addProperty("_id", id);
    	if (e.getProperty("option") != null) {
    		obj.addProperty("_label", e.getProperty("option").toString());
    	} else {
    		obj.addProperty("_label", e.getLabel());
    	}
    	//System.out.println(obj.get("_id"));
    	//System.out.println(obj.get("_label"));
    	//Get the associated JsonObjects for each vertex
    	if (e.getVertex(Direction.OUT) == null) {
    		System.out.println("No source vertex");
    		
    	}
    	//System.out.println(e.getVertex(Direction.OUT).getProperty("name"));
    	JsonObject up = verts.get(e.getVertex(Direction.OUT));
        JsonObject down = verts.get(e.getVertex(Direction.IN));
    	obj.addProperty("_outV", up.get("_id").getAsString());
    	obj.addProperty("_inV", down.get("_id").getAsString());
    	
    	return obj;
    }
}
