package main.java;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.nio.file.StandardCopyOption.*;

public class GraphConverter {
    static String graph_file;
    static String JSON_DIRECTORY = "/Users/doranwalsten/Google_Drive/CBID/TechConnect/AppResources/json/";
    static String GRAPHML_DIRECTORY = "/Users/doranwalsten/Documents/CBID/TechConnect/yEd/Detailed_Maps/";
    static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(); 
    //static JsonArray all = new JsonArray(); //This will be the compiled array of JSON objects
    static Map<String,JsonObject> entry_pts = new HashMap<String,JsonObject>();//Entry point for the referenced map
    static Map<String,ArrayList<JsonObject>> exit_pts = new HashMap<String,ArrayList<JsonObject>>();//Exit points from referenced map

    public static void main(String[] args) throws IOException, FileNotFoundException {
        
    	for (String g: args) {
    		JsonArray all = new JsonArray(); //This will be the compiled array of JSON objects for each file
    		JsonArray all_res = new JsonArray();
    		entry_pts.clear();
    		exit_pts.clear();
	    	try {
	            graph_file = g;
	            System.out.println("File: " + graph_file);
	        } catch (ArrayIndexOutOfBoundsException e) {
	            System.out.println("Must include a GraphML file to parse");//Probably will need to catch this error at some point
	            System.exit(0);
	        }
	
	        //Read in the GraphML file
	        Graph graph = new TinkerGraph();
	        GraphMLReader reader = new GraphMLReader(graph);
	        //Store any referenced charts for flowchart-ception
	        
	        try {
	            reader.inputGraph(GRAPHML_DIRECTORY + graph_file);
	        } catch (IOException e) {
	            System.out.println("INVALID FILE");
	            e.printStackTrace();
	        }
	        
	        //Test output as graph
	        JsonObject toWrite = new JsonObject();
	        JsonArray vertices = new JsonArray();
	        JsonArray edges = new JsonArray();
	        Map<Vertex, JsonObject> verts = new HashMap<Vertex, JsonObject>();
	        for (Vertex v : graph.getVertices()) {
	        	//Have to use this dumb approach because blueprints doesn't work with Gson
	        	verts.put(v, vertexToJsonObject(v,all_res));
	        }
	        //Now that we have the map, can iterate over the edges
	        for (Edge e : graph.getEdges()) {
	        	edges.add(edgeToJsonObject(e,verts));
	        }
	        for (Vertex v : verts.keySet()) {
	        	vertices.add(verts.get(v));
	        }
	        toWrite.add("all_res", all_res);
	        toWrite.add("vertices",vertices);
	        toWrite.add("edges", edges);
	        
	        /*
	        Map<Vertex, JsonObject> verts = new HashMap<Vertex, JsonObject>();
	        for (Vertex v : graph.getVertices()) {
	            verts.put(v, vertexToJsonObject(v,resources));
	        }
	        for (Edge e : graph.getEdges()) {
	            JsonObject up = verts.get(e.getVertex(Direction.OUT));
	            JsonObject down = verts.get(e.getVertex(Direction.IN));
	            
	            //If the up node has a JSON file that it's referencing, need to add those nodes to 
	            //the existing JSON file.
	            
	            if (up.get("attachment") != null) {
	            	System.out.println(up.get("attachment"));
	            	JsonArray a = up.get("attachment").getAsJsonArray();
	            	String name = a.get(0).toString().replaceAll("\"", "");
	            	String context = up.get("details").toString().replaceAll("\"", "");
		            if (name.endsWith("json")) { //Attachment is a json file, need to REPLACE node with the json
		            	System.out.println(context);
		            	//First, check to see if we already have this chart in the file
		            	if (entry_pts.containsKey(name)) { // We have already seen this dude before
		            		//Copy the head node to existing node in the chart
		            		JsonObject toCopy = entry_pts.get(name);
		            		copyJsonObject(toCopy,up);
		            		//Go to the end of the referenced chart and add the new next question option to the end
		            		for (JsonObject exit_pt : exit_pts.get(name)) {
		            			//Check to see if this actually sets the new entry in the objectf
		            			exit_pt.get("options").getAsJsonArray().add(context);
		            			exit_pt.get("next_question").getAsJsonArray().add(down.get("id").toString().replaceAll("\"", ""));
		            		}
		            	} else {
		            		//For the first time seeing the referenced chart, copy entirely into the new file
		            		//However, do not write the exit points until every possible reference is explored
		            		writeReferencedChartToFile(all, name, context, up, e, down);
		            	}
		            } else {
		            	addNewOptionToJsonObject(up,e,down);
		            }
	            } else {
	            	addNewOptionToJsonObject(up,e,down);
	            }
	        }
	        
	        for (String key: exit_pts.keySet()) {
	        	System.out.println(key);
	        	for (JsonObject obj : exit_pts.get(key)) {
	        		all.add(obj);
	        	}
	        }
	        for (Vertex v : verts.keySet()) {
	            all.add(verts.get(v));
	        }
			*/
	        try {
	            PrintWriter writer = new PrintWriter(new FileWriter(JSON_DIRECTORY + graph_file.replace(".graphml", ".json")));
	           //writer.print(gson.toJson(all));
	            writer.print(gson.toJson(toWrite));
	            writer.flush();
	            writer.close();
	        } catch (IOException er) {
	            er.printStackTrace();
	        }
	        
    	}
    }

    private static JsonObject vertexToJsonObject(Vertex v, JsonArray res) {
        JsonObject obj = new JsonObject();
        String id;
        if (v.getProperty("start") != null && ((Boolean) v.getProperty("start"))) {
            id = "q1";
        } else {
            id = (int) (Math.random() * 999999999) + "";
        }
        obj.addProperty("_id", id);
        obj.addProperty("name", v.getProperty("question") == null ? "" : v.getProperty("question").toString());
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
    
    //Using this method to convert to JsonObject edges
    private static JsonObject edgeToJsonObject(Edge e, Map<Vertex, JsonObject> verts) {
    	JsonObject obj = new JsonObject();
    	String id = (int) (Math.random() * 999999999) + "";
    	obj.addProperty("_id", id);
    	obj.addProperty("_label", e.getLabel());
    	//Get the associated JsonObjects for each vertex
    	JsonObject up = verts.get(e.getVertex(Direction.OUT));
        JsonObject down = verts.get(e.getVertex(Direction.IN));
    	obj.addProperty("_outV", up.get("_id").getAsString());
    	obj.addProperty("_inV", down.get("_id").getAsString());
    	
    	return obj;
    }
    private static void setJsonObjectFromRepairNode(JsonObject orig, RepairNode r) {
    	//orig.addProperty("id", r.getId());
		orig.addProperty("question", r.getQuestion());
		orig.addProperty("details", r.getDetails());
		//Clear the old arrays (if necessary)
		orig.remove("image");
		orig.remove("attachment");
		orig.remove("options");
		orig.remove("next_question");
		
		if(r.getImage() != null) {
			JsonArray images = new JsonArray();
        	//Splitting by the semicolon
        	for (String im: r.getImage()) {
        		im = im.trim();
        		images.add(im);
        	}
            orig.add("image", images);
		}
		if(r.getAttachment() != null) {
			JsonArray attachments = new JsonArray();
        	//Splitting by the semicolon
        	for (String att: r.getAttachment()) {
        		att = att.trim();
        		attachments.add(att);
        	}
        	orig.add("attachment",attachments);
		}
		JsonArray options = new JsonArray();
		for (String opt: r.getOptions()) {
			opt = opt.trim();
			System.out.println(opt);
			options.add(opt);
		}
		
		orig.add("options", options);
		JsonArray next_q = new JsonArray();
		for (String q: r.getNextQuestion()) {
			q = q.trim();
			next_q.add(q);
		}
		orig.add("next_question", next_q);
    }
    
    private static void copyJsonObject(JsonObject toCopy, JsonObject dest) {
    	dest.add("question",toCopy.get("question"));
    	dest.add("details", toCopy.get("details"));
    	dest.remove("image");
		dest.remove("attachment");
		dest.remove("options");
		dest.remove("next_question");
		
		if(toCopy.get("image") != null) {
			JsonArray images = new JsonArray();
        	//Splitting by the semicolon
        	for (JsonElement im: toCopy.get("image").getAsJsonArray()) {
        		images.add(im.toString().replaceAll("\"", ""));
        	}
            dest.add("image", images);
		}
		if(toCopy.get("attachment") != null) {
			JsonArray attachments = new JsonArray();
        	//Splitting by the semicolon
        	for (JsonElement att: toCopy.get("attachment").getAsJsonArray()) {
        		attachments.add(att.toString().replaceAll("\"", ""));
        	}
        	dest.add("attachment",attachments);
		}
		JsonArray options = new JsonArray();
		for (JsonElement opt: toCopy.get("options").getAsJsonArray()) {
			//System.out.println(opt);
			options.add(opt.toString().replaceAll("\"", ""));
		}
		
		dest.add("options", options);
		JsonArray next_q = new JsonArray();
		for (JsonElement q: toCopy.get("next_question").getAsJsonArray()) {
			next_q.add(q.toString().replaceAll("\"", ""));
		}
		dest.add("next_question", next_q);
    	
    }
    
    /**
     * This is a function which is called whenever a referenced flowchart is inserted into the existing chart
     * @param name - Name of the json file referenced. Used as the key in the maps which store all of the referenced charts
     * @param context - The context in which the referenced chart is called. Used to generate the options at end
     * @param up - Vertex which referenced the chart
     * @param e - edge between that vertex and next vertex in original chart
     * @param down - Next vertex in original chart
     * @throws IOException
     */
    private static void writeReferencedChartToFile(JsonArray all, String name, String context, JsonObject up, Edge e, JsonObject down ) throws IOException{
    	FileWriter jsonWriter = new FileWriter(JSON_DIRECTORY + graph_file.replaceAll(".graphml", ".json"));
    	JsonReader jsonReader = new JsonReader(new FileReader(JSON_DIRECTORY + name));
		
    	boolean first = false; //Whether current JSON element is the first question. Need to replace the fields in existing object
    	boolean end = false; //Whether current JSON element is an exit point. Need to wait to print to output file until confident no more options
    	
    	//This is the way we take steps through the entire JSON file
    	jsonReader.beginArray();
    	while(jsonReader.hasNext()) {
    		jsonReader.beginObject();//This is each element!
    		RepairNode rn = new RepairNode(); //Will modify every time a new node is encountered
    		while (jsonReader.hasNext()) {
    			String n = jsonReader.nextName(); //This is the key
    			if (n.equals("id")) {
    				String id = jsonReader.nextString();
    				if(id.equals("q1")) {
    					first = true;
    					System.out.println("FOUND THE START");
    					String temp2 = (int) (Math.random() * 999999999) +"";
    					rn.setId(temp2);
    				} else {
    					rn.setId(id);
    				}
    			} else if (n.equals("question")) {
    				String q = jsonReader.nextString();
    				rn.setQuestion(q);
    			} else if (n.equals("details")) {
    				String d = jsonReader.nextString();
    				rn.setDetails(d);
    			} else if (n.equals("image")) {
    				if(!jsonReader.hasNext()) {
    					jsonReader.nextNull();
    				} else {
    					List<String> imgs = new ArrayList<String>();
    					jsonReader.beginArray();
    					while(jsonReader.hasNext()) {
    						imgs.add(jsonReader.nextString());
    					}
    					jsonReader.endArray();
    					rn.setImage(imgs);
    				}
    				
    			} else if (n.equals("attachment")) {
    				if(!jsonReader.hasNext()) {
    					System.out.println("TEST");
    					jsonReader.nextNull();
    				} else {
    					//System.out.println("TEST");
        				jsonReader.beginArray();
        				List<String> atts = new ArrayList<String>();
        				while(jsonReader.hasNext()) {
        					atts.add(jsonReader.nextString());
        				}
        				jsonReader.endArray();
        				rn.setAttachment(atts);
    				}
    				
    			} else if (n.equals("options")) {
    				if (!jsonReader.hasNext()) {
    					jsonReader.nextNull();
    				} else {
        				List<String> opts = new ArrayList<String>();
        				jsonReader.beginArray();
        				while(jsonReader.hasNext()) {
        					opts.add(jsonReader.nextString());
        				}
        				jsonReader.endArray();
        				rn.setOptions(opts);
    				}
    			} else { //n.equals("next_question")
    				jsonReader.beginArray();
    				try {
    					jsonReader.nextNull();//Will succeed if null
    					System.out.println("FOUND AN END");
    					end = true;
    					//If the next node in the file is done, want to maintain that in the copied node
    					List<String> nextQ = new ArrayList<String>();
    					System.out.println(down.get("id").toString().replaceAll("\"", ""));
    					if (e.getVertex(Direction.IN).getProperty("done") != null && ((Boolean) e.getVertex(Direction.IN).getProperty("done"))) {	
        					//System.out.println(down.get("id").toString().replaceAll("\"", ""));
        					nextQ.add(null);
    					} else {
    						nextQ.add(down.get("id").toString().replaceAll("\"", ""));//Next is the next node
    					}
    					rn.setNextQuestion(nextQ);
    					
    					//Set the Option to be the context of the flowchart-ception
    					List<String> nextOpt = new ArrayList<String>();
    					nextOpt.add(context);
    					rn.setOptions(nextOpt);
    					
    				} catch (IllegalStateException g) {
    					System.out.println("HERE");
    					List<String> nextQ = new ArrayList<String>();
    					while(jsonReader.hasNext()) {
    						nextQ.add(jsonReader.nextString());
    					}
    					rn.setNextQuestion(nextQ);
    				}
    				jsonReader.endArray();
    				//Now want to write the created Object to the JSON file

    				String json_Node = gson.toJson(rn);
    				JsonObject toAdd = gson.fromJson(json_Node, JsonObject.class);
    				//jsonWriter.write(json_Node);
    				//Now, clear all of the entries for the Repair node
    				if (first) { //If this element is q1, we gotta set everything
    					setJsonObjectFromRepairNode(up,rn);
    					//Need to add JsonObject to HashMap
    					entry_pts.put(name, up);
    					//Now, we can 
    					first = false;
    				} else if (end) {
    					if (!exit_pts.containsKey(name)) {//Not initialized yet
    						System.out.println("Adding exit point");
    						ArrayList<JsonObject> exits = new ArrayList<JsonObject>();
    						exits.add(toAdd);
    						exit_pts.put(name, exits);
    					} else {
    						exit_pts.get(name).add(toAdd);//Don't want to print this stuff to the file quite yet
    					}
    					end = false;
    				} else {
    					all.add(toAdd);//Only add a new node if it isn't the first one
    				}
    				rn.setId("");
    				rn.setQuestion("");
    				rn.setDetails("");
    				rn.setImage(null);
    				rn.setAttachment(null);
    				rn.setOptions(null);
    				rn.setNextQuestion(null);
    			}
    		}
    		jsonReader.endObject();
    	}
		jsonReader.endArray();
		jsonWriter.close();
    	jsonReader.close();
	}
    private static void addNewOptionToJsonObject(JsonObject up, Edge e, JsonObject down) {
    	//Set up all of the option arrays I need and get the initial entires
    	JsonArray questionIds = up.getAsJsonArray("next_question");
    	JsonArray toAddQuestionIds = new JsonArray();
    	JsonArray optionText = up.getAsJsonArray("options");
        JsonArray toAddOptionText = new JsonArray();
        
        //Get the new next question to add
        JsonElement toAddQ;
    	//System.out.println(e.getVertex(Direction.IN).getProperty("done"));
    	if (e.getVertex(Direction.IN).getProperty("done") != null && ((Boolean) e.getVertex(Direction.IN).getProperty("done"))) {
    		toAddQ = JsonNull.INSTANCE;
    	} else {
    		toAddQ = down.get("id");
    	}
    	
    	//Get the new next option to add
        String toAdd = e.getProperty("option");
        
        //System.out.println(up.get("question"));
        int total_counter = 0;
        if (optionText.size() == 0) {
        	toAddOptionText.add(toAdd);
        	toAddQuestionIds.add(toAddQ);
        } else {
            while (total_counter < optionText.size()) {
            	String temp = optionText.get(total_counter).toString().replaceAll("[\\p{Punct}&&[^0-9]&&[^,]]", "");
            	if (toAdd.toString().compareToIgnoreCase(temp) > 0) {
            		//This means that the string to add is towards the end of the alphabet
            		toAddOptionText.add(toAdd);
            		toAddQuestionIds.add(toAddQ);
            	} 
        		toAddOptionText.add(optionText.get(total_counter));
        		toAddQuestionIds.add(questionIds.get(total_counter));
            	total_counter++;
            }
            if (total_counter == toAddOptionText.size()) {//This means that nothing was added
            	toAddOptionText.add(toAdd);
            	toAddQuestionIds.add(toAddQ);
            }
        }
        //
        //In this step, we simply add the first edge seen to the options array
        //We just need to insert the text in the proper alphabetical order
        up.add("options", toAddOptionText);
        up.add("next_question", toAddQuestionIds);
    }
}


