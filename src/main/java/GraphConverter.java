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
import main.java.model.LibraryEntry;
import main.java.model.TCEdge;

import java.io.BufferedReader;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.nio.file.StandardCopyOption.*;

public class GraphConverter {
    static String graph_file;
    static String JSON_DIRECTORY = "/Users/doranwalsten/Google_Drive/CBID/TechConnect/AppResources/json/";
    static String GRAPHML_DIRECTORY = "/Users/doranwalsten/Documents/CBID/TechConnect/yEd/Detailed_Maps/";
    //Store all existing graph Ids
    static Map<String,LibraryEntry> LIBRARY = new HashMap<String,LibraryEntry>();
    static ArrayList<String> GRAPH_LIBRARY = new ArrayList<String>();
    static String LIBRARY_FILE = "/Users/doranwalsten/Google_Drive/CBID/TechConnect/AppResources/json/index_graph.json";
    //URL of the S3 repo, specifically the resources folder with all of the pictures and the like.
    static String S3_URL = "http://tech-connect-database.s3-website-us-west-2.amazonaws.com/resources/";
    
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
    	
    	//Want to read in the LIBRARY in order to have all referenced charts
    	BufferedReader file_reader = new BufferedReader( new FileReader(LIBRARY_FILE));
    	String graph_pair;
    	JsonObject json = gson.fromJson(file_reader,JsonObject.class);
    	JsonArray lib_names = json.get("library").getAsJsonArray();
    	for (JsonElement j : lib_names ) {
    		String name = j.toString().replaceAll("\"", "");
    		LibraryEntry e = gson.fromJson(json.get(name), LibraryEntry.class);
    		System.out.println(e.getDescription());
    		//Key of the map is the graphml file (For now), format
    		//graphml, flowchart ID, graph ID, description, image
    		LIBRARY.put(name,e);
    		GRAPH_LIBRARY.add(e.getGraph_id()); //Add the referenced graph file 
    		//System.out.println(graph_pair);
    	}
    	System.out.println(GRAPH_LIBRARY);
    	
    	file_reader.close();
    	
    	//Now iterate through the remaining files to setup the flowcharts
    	
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
		        	if (GRAPH_LIBRARY.contains(name)) { 
		        		context = up.getProperty("details").toString().trim();
		        		System.out.println(name);
		        		if (entry_pts.containsKey(name)) { // We have already seen this dude before
		        			//Copy the head node to existing node in the chart
		            		Vertex ref = entry_pts.get(name); //This needs to be a Vertex instead
		            		
		            		//For existing edges, need to make sure that the label is the context
		            		for (Edge curr_edge : ref.getEdges(Direction.OUT)) {
		            			graph.addEdge(randomId(), ref, curr_edge.getVertex(Direction.IN), ref.getProperty("details").toString().trim());
		            			graph.removeEdge(curr_edge);
		            		}
		            		//Remove old edges coming in, but add a new edge to the reference point
		            		for(Edge old_edge : up.getEdges(Direction.IN)) {
		            			Vertex source = old_edge.getVertex(Direction.OUT);
		            			String random_id = randomId();
		            			graph.addEdge(random_id,source,ref,old_edge.getProperty("option").toString());
		            			graph.removeEdge(old_edge);
		            		}
		            		//Remove old edges coming out, add a new edge from ref to the next edge
		            		for (Edge old_edge: up.getEdges(Direction.OUT)) {
		            			Vertex target = old_edge.getVertex(Direction.IN);
		            			String random_id = randomId();
		            			graph.addEdge(random_id, ref, target, context);
		            			graph.removeEdge(old_edge);
		            		}
		            		
		            		
		            		/*
		            		for (Edge new_edge : toCopy.getEdges(Direction.OUT)) {
		            			//Want to copy the edges over, but with new ID
		            			String random_id = randomId();
		            			graph.addEdge(random_id, up, new_edge.getVertex(Direction.IN), new_edge.getLabel());
		            		}
		            		//Go to the end of the referenced chart and add the new next question option to the end
		            		for (Vertex exit_pt : exit_pts.get(name)) {
		            			//Check to see if this actually sets the new entry in the objectf
		            			//HERE WE JUST MAKE A NEW FREAKING EDGE. WHAT A CONCEPT!
		            			String random_id = randomId();
		            			graph.addEdge(random_id,exit_pt,down,context);
		            		}
		            		*/
		            		graph.removeVertex(up);
		            	} else {
		            		entry_pts.put(name,up);
		            		//graph = writeReferencedChartToFile(graph, name, context, up, e, down);
		            	}
		            } else {
		            	System.out.println("FAIL");
		            }
	            }
	        }
			
			FlowChart test_flowchart = new FlowChart();
			if (LIBRARY.containsKey(g)) {
				test_flowchart.setId(LIBRARY.get(g).getFlowchart_id());
				test_flowchart.setName(LIBRARY.get(g).getName());
				test_flowchart.setDescription(LIBRARY.get(g).getDescription());
				if (LIBRARY.get(g).getImage() != null) {
					test_flowchart.setImage(LIBRARY.get(g).getImage());
				}
			} else {
				test_flowchart.setId(randomId());
				test_flowchart.setName("");
				test_flowchart.setDescription("");
			}
	        
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
	        String firstNode = "";
	        
	        //Create all of the vertices to add, for preparation of joining with Edges
	        for (Vertex v : graph.getVertices()) {
	        	boolean isVirtual = false;
	        	TCVertex toAddV = new TCVertex();
	        	String id = randomId();
	        	 if (v.getProperty("start") != null && ((Boolean) v.getProperty("start"))) {
	                 firstNode = id;
	             } 
	        	 //Every Vertex needs an ID
	        	 toAddV.setId(id);
	        	//If resources present, add to object
	        	if (v.getPropertyKeys().contains("resources")) { //Attachments to add, maybe even virtual graph
	        		String name = v.getProperty("resources").toString().trim();
	        		if (GRAPH_LIBRARY.contains(name)) { //This is a virtual graph, only set GraphID and name
	        			toAddV.setGraphId(name);
	        			toAddV.setName(v.getProperty("question") == null ? v.getProperty("name").toString().trim() : v.getProperty("question").toString().trim());
	        			isVirtual = true;
	        		} else {
		            	ArrayList<String> resources = new ArrayList<String>();
		            	//Splitting by the semicolon
		            	for (String r: v.getProperty("resources").toString().split(";")) {
		            		if (!r.endsWith(".json")) {
			            		r = r.trim();
			            		//Here, need to add the S3 URL
			            		if(!r.startsWith("http")) {
			            			r = S3_URL + r;
			            		}
			            		resources.add(r);
		            		}
		            	}
		            	toAddV.setResources(resources);
	        		}
	            } 
	        	  
	        	
	        	
	        	if (!isVirtual) {
		        	System.out.println(v.getPropertyKeys());
		        	toAddV.setName( v.getProperty("question") == null ? v.getProperty("name").toString().trim() : v.getProperty("question").toString().trim());
		        	toAddV.setDetails(v.getProperty("details") == null ? "" : v.getProperty("details").toString().trim());
		        	
		        	//If images present, add to object
		        	if (v.getPropertyKeys().contains("imageURL")) {
		            	ArrayList<String> images = new ArrayList<String>();
		            	//Splitting by the semicolon
		            	for (String im: v.getProperty("imageURL").toString().split(";")) {
		            		im = im.trim();
		            		//Here, need to add the S3 URL
		            		if (!im.startsWith("http")) {
		            			im = S3_URL + im;
		            		}
		            		images.add(im);
		             	}
		                toAddV.setImages(images);
		            } else if (v.getPropertyKeys().contains("images")) {
		            	ArrayList<String> images = new ArrayList<String>();
		            	//Splitting by the semicolon
		            	for (String im: v.getProperty("images").toString().split(";")) {
		            		im = im.trim();
		            		//Here, need to add the S3 URL
		            		if(!im.startsWith("http")) {
		            			im = S3_URL + im;
		            		}
		            		images.add(im);
		            	}
		            	toAddV.setImages(images);
		            }
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
		    		//System.out.println(e.getProperty("option").toString());
		    		toAddE.setLabel(e.getProperty("option").toString());
		    	} else {
		    		//System.out.println(e.getLabel());
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
	        if (LIBRARY.containsKey(g)) {
	        	toAddG.setId(LIBRARY.get(g).getGraph_id());
	        } else {
		        toAddG.setId(randomId());
		        toAddG.setOwner("TechConnect");
	        }
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


