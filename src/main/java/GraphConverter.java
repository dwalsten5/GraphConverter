package main.java;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

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

    public static void main(String[] args) throws IOException, FileNotFoundException {
    	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(); //Disable the issues with reading "="
        JsonArray all = new JsonArray(); //This will be the compiled array of JSON objects
        
        
        try {
            graph_file = args[0];
            System.out.println("File: " + graph_file);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Must include a GraphML file to parse");//Probably will need to catch this error at some point
            System.exit(0);
        }

        //Read in the GraphML file
        Graph graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);
        try {
            reader.inputGraph(graph_file);
        } catch (IOException e) {
            System.out.println("INVALID FILE");
            e.printStackTrace();
        }

        Map<Vertex, JsonObject> verts = new HashMap<Vertex, JsonObject>();
        for (Vertex v : graph.getVertices()) {
            verts.put(v, vertexToJsonObject(v));
        }
        for (Edge e : graph.getEdges()) {
            JsonObject up = verts.get(e.getVertex(Direction.OUT));
            JsonObject down = verts.get(e.getVertex(Direction.IN));
            
            //Getting the Next Question Ids from the JSON object for vertex
            JsonArray questionIds = up.getAsJsonArray("next_question");
            JsonArray toAddQuestionIds = new JsonArray();
            JsonElement toAddQ = JsonNull.INSTANCE;
            
            //If the up node has a JSON file that it's referencing, need to add those nodes to 
            //the existing JSON file.
            //
            
            if (up.get("attachment") != null) {
            	System.out.println(up.get("attachment"));
            	JsonArray a = up.get("attachment").getAsJsonArray();
            	String temp = a.get(0).toString().replaceAll("\"", "");
	            if (temp.endsWith("json")) { //Attachment is a json file
	            	//Now, we gotta add the json file into it
	            	//Just for fun, try writing the entire file first
	            	Path source =  Paths.get("/Users/doranwalsten/AndroidStudioProjects/TechConnectApp/JSON/" + temp);
	            	Path dest = Paths.get(graph_file.replace(".graphml", ".json"));
	            	/*
	            	try {
	            		Files.copy(source,dest);
	            	} catch (IOException i) {
	            		i.printStackTrace();
	            	}
	            	*/
	            	FileWriter jsonWriter = new FileWriter(graph_file.replaceAll(".graphml", ".json"));
	            	JsonReader jsonReader = new JsonReader(new FileReader("/Users/doranwalsten/AndroidStudioProjects/TechConnectApp/JSON/" + temp));
            		
	            	jsonReader.beginArray();
	            	jsonReader.beginObject();
	            	RepairNode rn = new RepairNode(); //Will modify every time a new node is encountered
            		while (jsonReader.hasNext()) {
            			String n = jsonReader.nextName(); //This is the key
            			System.out.println(n);
            			if (n.equals("id")) {
            				String id = jsonReader.nextString();
            				if(id.equals("q1")) {
            					System.out.println("FOUND THE START");
            					String temp2 = (int) (Math.random() * 999999999) +"";
            					toAddQ = gson.fromJson(String.format("\"%s\"", temp2), JsonElement.class);
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
            				if(!jsonReader.hasNext()) {
            					System.out.println("FOUND THE END");
            					List<String> nextQ = new ArrayList<String>();
            					nextQ.add(down.get("id").toString());//Next is the next node
            					rn.setNextQuestion(nextQ);
            				} else {
            					List<String> nextQ = new ArrayList<String>();
            					jsonReader.beginArray();
            					while(jsonReader.hasNext()) {
            						nextQ.add(jsonReader.nextString());
            					}
            					jsonReader.endArray();
            					rn.setNextQuestion(nextQ);
            				}
            				//Now want to write the created Object to the JSON file

            				String json_Node = gson.toJson(rn);
            				JsonObject toAdd = gson.fromJson(json_Node, JsonObject.class);
            				all.add(toAdd);
            				//jsonWriter.write(json_Node);
            				//Now, clear all of the entries for the Repair node
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
            		jsonReader.endArray();
            		jsonWriter.close();
	            	jsonReader.close();	
	            }
            } else {
            	if (e.getVertex(Direction.IN).getProperty("done") != null && ((Boolean) e.getVertex(Direction.IN).getProperty("done"))) {
            		toAddQ = JsonNull.INSTANCE;
            	} else {
            		toAddQ = down.get("id");
            	}
            }
            
            
            
            
            //Getting the past options from existing JSON file
            JsonArray optionText = up.getAsJsonArray("options");
            JsonArray toAddOptionText = new JsonArray();
            String toAdd = e.getProperty("option");
            
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

        
        for (Vertex v : verts.keySet()) {
            all.add(verts.get(v));
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(graph_file.replace(".graphml", ".json"),true));
            
            
            writer.print(gson.toJson(all));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JsonObject vertexToJsonObject(Vertex v) {
        JsonObject obj = new JsonObject();
        String id;
        if (v.getProperty("start") != null && ((Boolean) v.getProperty("start"))) {
            id = "q1";
        } else {
            id = (int) (Math.random() * 999999999) + "";
        }
        obj.addProperty("id", id);
        obj.addProperty("question", v.getProperty("question") == null ? "" : v.getProperty("question").toString());
        obj.addProperty("details", v.getProperty("details") == null ? "" : v.getProperty("details").toString());
        if (v.getPropertyKeys().contains("imageURL")) {
        	JsonArray images = new JsonArray();
        	//Splitting by the semicolon
        	for (String im: v.getProperty("imageURL").toString().split(";")) {
        		im = im.trim();
        		images.add(im);
        	}
            obj.add("image", images);
        } 
        /*else {
        	obj.add("image", JsonNull.INSTANCE);
        }
        */
        if (v.getPropertyKeys().contains("resources")) { //Attachments to add
        	JsonArray attachments = new JsonArray();
        	//Splitting by the semicolon
        	for (String att: v.getProperty("resources").toString().split(";")) {
        		att = att.trim();
        		attachments.add(att);
        	}
        	obj.add("attachment",attachments);
        } 
        /* else {
        	obj.add("attachment", JsonNull.INSTANCE);
        }
        */
        obj.add("options", new JsonArray());
        obj.add("next_question", new JsonArray());
        return obj;
    }
}
