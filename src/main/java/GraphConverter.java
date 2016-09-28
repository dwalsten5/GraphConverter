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
import main.java.model.TCGraph;
import main.java.model.TCVertex;
import main.java.model.FlowChart.ChartType;
import main.java.model.TCEdge;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.nio.file.StandardCopyOption.*;

public class GraphConverter {
    static String graph_file;
    static String JSON_DIRECTORY = "/Users/doranwalsten/Google_Drive/CBID/TechConnect/AppResources/json/";
    static String GRAPHML_DIRECTORY = "/Users/doranwalsten/Documents/CBID/TechConnect/yEd/Detailed_Maps/";
    static Gson gson = new GsonBuilder()
    		.registerTypeAdapter(FlowChart.class, new FlowChartDeserializerMongo())
    		.registerTypeAdapter(FlowChart.class, new FlowChartSerializerMongo())
    		.setPrettyPrinting()
    		.disableHtmlEscaping()
    		.create(); 
    
    //static JsonArray all = new JsonArray(); //This will be the compiled array of JSON objects
    static Map<String,Vertex> entry_pts = new HashMap<String,Vertex>();//Entry point for the referenced map
    static Map<String,ArrayList<Vertex>> exit_pts = new HashMap<String,ArrayList<Vertex>>();//Exit points from referenced map

    public static void main(String[] args) throws IOException, FileNotFoundException {
        
    	for (String g: args) {
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
	        test_flowchart.setId(randomId());
	        test_flowchart.setName("");
	        test_flowchart.setDescription("");
	        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());
	        test_flowchart.setCreatedDate(date);
	        test_flowchart.setUpdatedDate(date);
	        test_flowchart.setVersion("VERSION 1.0");
	        test_flowchart.setOwner("TechConnect");
	        test_flowchart.setType(ChartType.DEVICE);
	        
	        //Now, need to build proper graph from TinkerGraph
	        ArrayList<TCVertex> nodes = new ArrayList<TCVertex>();
	        ArrayList<TCEdge> edgs = new ArrayList<TCEdge>();
	        HashMap<Vertex,TCVertex> node_map = new HashMap<Vertex,TCVertex>();
	        ArrayList<String> all_res = new ArrayList<String>();
	        String firstNode = "";
	        
	        //Create all of the vertices to add, for preparation of joining with Edges
	        for (Vertex v : graph.getVertices()) {
	        	TCVertex toAddV = new TCVertex();
	        	String id = randomId();
	        	 if (v.getProperty("start") != null && ((Boolean) v.getProperty("start"))) {
	                 firstNode = id;
	                 
	             } 
	        	  
	        	toAddV.setId(id);
	        	System.out.println(v.getPropertyKeys());
	        	toAddV.setName( v.getProperty("question") == null ? v.getProperty("name").toString().trim() : v.getProperty("question").toString().trim());
	        	toAddV.setDetails(v.getProperty("details") == null ? "" : v.getProperty("details").toString().trim());
	        	
	        	//If images present, add to object
	        	if (v.getPropertyKeys().contains("imageURL")) {
	            	ArrayList<String> images = new ArrayList<String>();
	            	//Splitting by the semicolon
	            	for (String im: v.getProperty("imageURL").toString().split(";")) {
	            		im = im.trim();
	            		images.add(im);
	            		all_res.add(im);
	            	}
	                toAddV.setImages(images);
	            } else if (v.getPropertyKeys().contains("images")) {
	            	ArrayList<String> images = new ArrayList<String>();
	            	//Splitting by the semicolon
	            	for (String im: v.getProperty("images").toString().split(";")) {
	            		im = im.trim();
	            		images.add(im);
	            		all_res.add(im);
	            	}
	            	toAddV.setImages(images);
	            }
	        	
	        	//If resources present, add to object
	        	if (v.getPropertyKeys().contains("resources")) { //Attachments to add
	            	ArrayList<String> resources = new ArrayList<String>();
	            	//Splitting by the semicolon
	            	for (String r: v.getProperty("resources").toString().split(";")) {
	            		r = r.trim();
	            		resources.add(r);
	            		if (!r.endsWith(".json")) {
	            			all_res.add(r);
	            		}
	            	}
	            	toAddV.setResources(resources);
	            }
	        	node_map.put(v, toAddV);
	        	nodes.add(toAddV);
	        }
	        
	        //Now, we gotta make all of the connections
	        for (Edge e : graph.getEdges()) {
		        TCEdge toAddE = new TCEdge();
		    	String id = randomId();
		    	toAddE.setId(id);
		    	if (e.getProperty("option") != null) {
		    		toAddE.setLabel(e.getProperty("option").toString());
		    	} else {
		    		toAddE.setLabel(e.getLabel());
		    	}
		    	//Get the associated JsonObjects for each vertex
		    	if (e.getVertex(Direction.OUT) == null) {
		    		System.out.println("No source vertex");
		    	}
		    	//System.out.println(e.getVertex(Direction.OUT).getProperty("name"));
		    	TCVertex up = node_map.get(e.getVertex(Direction.OUT));
		        TCVertex down = node_map.get(e.getVertex(Direction.IN));
		    	toAddE.setOutV(up.getId());
		    	//up.addOutEdge(toAddE.getId());
		    	toAddE.setInV(down.getId());
		    	//down.addInEdge(toAddE.getId());
		    	edgs.add(toAddE);
	        }
	        
	        TCGraph toAddG = new TCGraph(nodes,edgs);
	        toAddG.setId(randomId());
	        toAddG.setOwner("TechConnect");
	        toAddG.setFirstNode(firstNode);
	        test_flowchart.setGraph(toAddG);
	        
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
    
    //This is for copying between blueprints Vertex objects
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
		//In this case, I think that we can directly copy the images and resources if they're there
		if(toCopy.getProperty("images") != null) {
			dest.setProperty("images", toCopy.getProperty("images").toString());
		}
		if(toCopy.getProperty("resources") != null) {
			dest.setProperty("resources",toCopy.getProperty("resources") .toString());
		}
    	
    }
    
    //This is for copying from a previously made Mongo vertex
    private static void copyTCVertex(TCVertex toCopy, Vertex dest) {
    	if (dest.getProperty("question") != null) {
    		dest.removeProperty("question");
    	}
    	dest.setProperty("name",toCopy.getName());
    	dest.setProperty("details",toCopy.getDetails());
    	dest.removeProperty("imageURL");
		dest.removeProperty("resources");
		dest.removeProperty("options");
		dest.removeProperty("next_question"); //Not removing next_question to have a label for the previous graph
		//type 
		if(toCopy.getImages() != null) {
			List<String> im_toCopy = toCopy.getImages();
			String images = String.join(";", im_toCopy);
			dest.setProperty("images", images);
		}
		if(toCopy.getResources() != null) {
			List<String> res_toCopy = toCopy.getResources();
			
			/*ArrayList<String> res_new = new ArrayList<String>();
			for (JsonElement s : res_toCopy) {
				res_new.add(s.toString());
			}*/
			String resources = String.join(";", res_toCopy);
			dest.setProperty("resources", resources);
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
		//Now, it's a TCGraph, which is a little different
		TCGraph g = child_flowchart.getGraph();
		
		boolean start = false; //Has the first node been seen yet?
		//Instead of iterating over Vertices, let's do edges
		for (TCEdge curr : g.getEdges()) {
			TCVertex prev = g.getVertex(curr.getOutV());
			TCVertex next = g.getVertex(curr.getInV());
			
			//For adding a new edge
			String random_id = randomId();
			//Check if source is the first ndoe
			if (prev.getId().equals("q1")) {
				//System.out.println("First node");
				if (!start) {
					//If first time, need to reset the up node to look right
					System.out.println(prev.getName());
					copyTCVertex(prev,up);
					entry_pts.put(name, up);
					//Remove old edge?
					
				}
				
				//Only add the next vertex if it has not been added yet
				Vertex inVertex;
				if (parent.getVertex(next.getId()) == null) {
					inVertex = parent.addVertex(next.getId());
					copyTCVertex(next,inVertex);
				} else {
					inVertex = parent.getVertex(next.getId());
				}
				Edge toAdd  = parent.addEdge(random_id, up, inVertex, curr.getLabel());
				//If the current edge has some details, be sure to share
				if (curr.getDetails() != null) {
					toAdd.setProperty("details", curr.getDetails() );
				}
				
				//Put the modified vertex in the entry pts. structure
				
			} else if(next.getOutEdges() != null) { //Check to see if this edge is on the way out
				//Connect previous node with the reentry to the parent
				Vertex outVertex;
				if(parent.getVertex(prev.getId()) == null) {
					outVertex = parent.addVertex(prev.getId());
					copyTCVertex(prev,outVertex);
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
					copyTCVertex(prev,outVertex);
				} 
				
				if(inVertex == null) {
					inVertex = parent.addVertex(next.getId());
					copyTCVertex(next,inVertex);
				}
				
				Edge toAdd = parent.addEdge(random_id, outVertex, inVertex, curr.getLabel());
				if(curr.getDetails() != null) {
					toAdd.setProperty("details", curr.getDetails());
				}
			}
		}
		return parent;
	}
    
    private static String randomId(){
    	String validChars = "23456789ABCDEFGHJKLMNPQRSTWXYZabcdefghijkmnopqrstuvwxyz";
    	char chars[] = new char[17];
    	for(int i = 0; i < chars.length; i++){
    		int rand = (int)(Math.random()*chars.length);
    		chars[i] = validChars.charAt(rand);
    	}
    	return new String(chars);
    }
}


