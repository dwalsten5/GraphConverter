import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

import java.io.IOException;

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
    }
}
