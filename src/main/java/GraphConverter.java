import com.google.gson.*;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class GraphConverter {
    static String graph_file;

    public static void main(String[] args) {
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

            JsonArray questionIds = up.getAsJsonArray("next_question");
            if (e.getVertex(Direction.IN).getProperty("done") != null && ((Boolean) e.getVertex(Direction.IN).getProperty("done"))) {
                questionIds.add(JsonNull.INSTANCE);
            } else {
                questionIds.add(down.get("id"));
            }
            up.add("next_question", questionIds);

            JsonArray optionText = up.getAsJsonArray("options");
            optionText.add(e.getProperty("option").toString());
            up.add("options", optionText);
        }

        JsonArray all = new JsonArray();
        for (Vertex v : verts.keySet()) {
            all.add(verts.get(v));
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(graph_file.replace(".graphml", ".json")));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
            obj.addProperty("image", v.getProperty("imageURL").toString());
        }
        obj.add("attachment", JsonNull.INSTANCE);
        obj.add("options", new JsonArray());
        obj.add("next_question", new JsonArray());
        return obj;
    }
}
