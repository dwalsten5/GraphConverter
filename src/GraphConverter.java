import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReader;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONWriter;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.Edge;

import java.io.IOException;

import com.tinkerpop.blueprints.Direction;

public class GraphConverter {
	static String graph_file;

	public static void main(String[] args) {
		try {
			graph_file = args[0];
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
		}
		
		//Now convert to graph to JSON Format
		//Not exactly what we need, but we might be able to leverage
		//at some point. What we'll probably have to
		//find a general JSON library to store all of this stuff
		//Phani, do you know something?
		GraphSONWriter writer = new GraphSONWriter(graph);
		try {
			writer.outputGraph(graph, "default.json");
		} catch (IOException e) {
			System.out.println("INVALID OUTPUT FILE");
		}
		

		
	}
	

}
