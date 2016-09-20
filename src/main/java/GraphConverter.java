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

import main.java.model.FlowChart;

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
    static Gson gson = new GsonBuilder()
    		.registerTypeAdapter(FlowChart.class, new FlowChartSerializer())
    		.registerTypeAdapter(FlowChart.class, new FlowChartDeserializer())
    		.setPrettyPrinting()
    		.disableHtmlEscaping()
    		.create(); 
    
    //static JsonArray all = new JsonArray(); //This will be the compiled array of JSON objects
    static Map<String,Vertex> entry_pts = new HashMap<String,Vertex>();//Entry point for the referenced map
    static Map<String,ArrayList<Vertex>> exit_pts = new HashMap<String,ArrayList<Vertex>>();//Exit points from referenced map

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
	        
	        
	        
	        //Now, I have to figure out how to convert the flowchart-ception mess within GraphSON
	        //in theory, it should be a LOT easier
	        for (Edge e: graph.getEdges() ) {
	        	Vertex up = e.getVertex(Direction.OUT);
	        	Vertex down = e.getVertex(Direction.IN);
	        	
	        	if (up.getProperty("resources") != null) {
	        		String name = up.getProperty("resources").toString().trim();
	        		String context; 
		        	if (name.endsWith("json")) {
		        		context = up.getProperty("details").toString().trim();
		        		System.out.println(name);
		        		if (entry_pts.containsKey(name)) { // We have already seen this dude before
		            		//Copy the head node to existing node in the chart
		            		Vertex toCopy = entry_pts.get(name); //This needs to be a Vertex instead
		            		copyVertex(toCopy,up);
		            		//Go to the end of the referenced chart and add the new next question option to the end
		            		for (Vertex exit_pt : exit_pts.get(name)) {
		            			//Check to see if this actually sets the new entry in the objectf
		            			//HERE WE JUST MAKE A NEW FREAKING EDGE. WHAT A CONCEPT!
		            			Edge temp = exit_pt.addEdge(context, down);
		            		}
		            		
		            	} else {
		            		
		            		graph = writeReferencedChartToFile(graph, name, context, up, e, down);
		            	}
		            } else {
		            	System.out.println("FAIL");
		            }
	            }
	        }
			
			FlowChart test_flowchart = new FlowChart();
	        test_flowchart.setId("Test String");
	        test_flowchart.setName("Name of chart");
	        test_flowchart.setDescription("This is the description");
	        test_flowchart.setUpdatedDate("DATE HERE");
	        test_flowchart.setVersion("VERSION 1.1");
	        test_flowchart.setOwner("Doran Walsten");
	        test_flowchart.setGraph((TinkerGraph) graph);
	        
	        try {
	            PrintWriter writer = new PrintWriter(new FileWriter(JSON_DIRECTORY + graph_file.replace(".graphml", ".json")));
	           //writer.print(gson.toJson(all));
	            writer.print(gson.toJson(test_flowchart));
	            writer.flush();
	            writer.close();
	        } catch (IOException er) {
	            er.printStackTrace();
	        }
	        
    	}
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
    
    private static void copyVertex(Vertex toCopy, Vertex dest) {
    	if (dest.getProperty("question") != null) {
    		dest.removeProperty("question");
    	}
    	dest.setProperty("name",toCopy.getProperty("name"));
    	dest.setProperty("details",toCopy.getProperty("details"));
    	dest.removeProperty("imageURL");
		dest.removeProperty("resources");
		dest.removeProperty("options");
		dest.removeProperty("next_question"); //Not removing next_question to have a label for the previous graph
		//type 
		if(toCopy.getProperty("image") != null) {
			dest.setProperty("imageURL", toCopy.getProperty("imageURL"));
		}
		if(toCopy.getProperty("resources") != null) {
			dest.setProperty("resources", toCopy.getProperty("resources"));
		}
    	
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
    private static Graph writeReferencedChartToFile(Graph parent, String name, String context, Vertex up, Edge e, Vertex down ) throws IOException{
    	//FileWriter jsonWriter = new FileWriter(JSON_DIRECTORY + graph_file.replaceAll(".graphml", ".json"));
    	JsonReader jsonReader = new JsonReader(new FileReader(JSON_DIRECTORY + name));
    	
    	//Clear out old edges
    	/*
    	for (Edge ed: up.getEdges(Direction.OUT)) {
			parent.removeEdge(ed);
		}
    	//Need to clear out old edges 
    	for (Edge ed : down.getEdges(Direction.IN)) {
			parent.removeEdge(ed);
		}
		*/
    	//In theory, this should be a new FlowChart in json form
		FlowChart child_flowchart = gson.fromJson(jsonReader, FlowChart.class);
		
		//This gets us our flow chart, now we just need to get the graph object
		Graph g = child_flowchart.getGraph();
		
		boolean start = false; //Has the first node been seen yet?
		//Instead of iterating over Vertices, let's do edges
		for (Edge curr : g.getEdges()) {
			Vertex prev = curr.getVertex(Direction.OUT);
			Vertex next = curr.getVertex(Direction.IN);
			
			//For adding a new edge
			String random_id = (int) (Math.random() * 999999999) + "";
			//Check if source is the first ndoe
			if (prev.getId().equals("q1")) {
				//System.out.println("First node");
				if (!start) {
					//If first time, need to reset the up node to look right
					System.out.println(prev.getProperty("name"));
					copyVertex(prev,up);
					entry_pts.put(name, up);
					//Remove old edge?
					
				}
				
				//Only add the next vertex if it has not been added yet
				Vertex inVertex;
				if (parent.getVertex(next.getId()) == null) {
					inVertex = parent.addVertex(next.getId());
					copyVertex(next,inVertex);
				} else {
					inVertex = parent.getVertex(next.getId());
				}
				Edge toAdd  = parent.addEdge(random_id, up, inVertex, curr.getLabel());
				//If the current edge has some details, be sure to share
				if (curr.getProperty("details") != null) {
					toAdd.setProperty("details", curr.getProperty("details") );
				}
				
				//Put the modified vertex in the entry pts. structure
				
			} else if(!next.getEdges(Direction.OUT).iterator().hasNext()) { //Check to see if this edge is on the way out
				//Connect previous node with the reentry to the parent
				Vertex outVertex;
				if(parent.getVertex(prev.getId()) == null) {
					outVertex = parent.addVertex(prev.getId());
					copyVertex(prev,outVertex);
				} else {
					outVertex = parent.getVertex(prev.getId());
				}
				
				parent.addEdge(random_id, outVertex, down, context);
				
				//Here's my space-saving thing again, need to put new Vertex in there
				if (!exit_pts.containsKey(name)) {//Not initialized yet
					System.out.println("Adding exit point");
					ArrayList<Vertex> exits = new ArrayList<Vertex>();
					exits.add(outVertex);
					exit_pts.put(name, exits);
				} else {
					exit_pts.get(name).add(outVertex);//Don't want to print this stuff to the file quite yet
				}
			} else {	//Ultimately, will need to add edges in the middle
				Vertex outVertex = parent.getVertex(prev.getId());
				Vertex inVertex = parent.getVertex(next.getId());
				if(outVertex == null) {
					outVertex = parent.addVertex(prev.getId());
					copyVertex(prev,outVertex);
				} 
				
				if(inVertex == null) {
					inVertex = parent.addVertex(next.getId());
					copyVertex(next,inVertex);
				}
				
				Edge toAdd = parent.addEdge(random_id, outVertex, inVertex, curr.getLabel());
				if(curr.getProperty("details") != null) {
					toAdd.setProperty("details", curr.getProperty("details"));
				}
			}
		}
		return parent;
		/*
		for (Vertex v : g.getVertices()) {
			//System.out.println(v.getId());
			//System.out.println(v.getPropertyKeys());
			boolean end = false;
			//boolean true_end = !v.getEdges(Direction.OUT).iterator().hasNext(); //These are the dummy "Done" nodes
			
			//We won't add this node, but we also won't delete it 
			//if (!true_end) {
				
				if(v.getId().equals("q1")) {
					System.out.println("First node");
					//This means that we have found the start
					entry_pts.put(name, up);
					//Don't change the id of the up vertex, but reset everything else
					copyVertex(v,up);
					//Now, we just need to correct the edges and add them to the graph
					for (Edge next : v.getEdges(Direction.OUT)) {
						String random_id = (int) (Math.random() * 999999999) + ""; //This is really not cool
						parent.addEdge(random_id, up, next.getVertex(Direction.IN), next.getLabel());
					}
				} else {//This is the end
					for (Edge next : v.getEdges(Direction.OUT)) {
						end = !next.getVertex(Direction.IN).getEdges(Direction.OUT).iterator().hasNext();
						if (!end) { //We want to add these edges to the graph
							String random_id = (int) (Math.random() * 999999999) + ""; //This is really not cool
							parent.addEdge(random_id, v, next.getVertex(Direction.IN), next.getLabel());
						} 
					}
					if (end) {
						System.out.println("End Vertex");
						//Here, we want to add some edges
						if (!exit_pts.containsKey(name)) {//Not initialized yet
							System.out.println("Adding exit point");
							ArrayList<Vertex> exits = new ArrayList<Vertex>();
							exits.add(v);
							exit_pts.put(name, exits);
						} else {
							exit_pts.get(name).add(v);//Don't want to print this stuff to the file quite yet
						}
						String random_id = (int) (Math.random() * 999999999) + "";
						Vertex n = parent.addVertex(v.getId());
						copyVertex(v,n);
						//Now, because of referncing need to replace incoming vertex
						for (Edge ed: v.getEdges(Direction.IN)) {
							Vertex prev = ed.getVertex(Direction.OUT);
							prev.addEdge(ed.getLabel(), n);
							String id = (int) (Math.random() * 999999999) + "";
							parent.addEdge(id, prev,n, ed.getLabel());
							parent.removeEdge(ed);
						}
						n.addEdge(context, down);
					} else {
						System.out.println("Normal Vertex"); //Now, we just need to add everything to the chart
						String random_id = (int) (Math.random() * 999999999) + "";
						Vertex n = parent.addVertex(v.getId());
						copyVertex(v,n);
						//Does this already add edges? let's see
					}
				}
			
		}
		return parent;
		*/
	}
    
    //Not needed because all edges exist already
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


