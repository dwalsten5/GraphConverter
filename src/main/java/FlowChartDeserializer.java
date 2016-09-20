package main.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReader;

import main.java.model.ChartComment;
import main.java.model.FlowChart;

public class FlowChartDeserializer implements JsonDeserializer<FlowChart> {
	
	public FlowChart deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) {
		final JsonObject jsonObject = json.getAsJsonObject();
		Gson gson = new Gson(); //Need a Gson object to complete conversions if needed
		
		final String id = jsonObject.get("_id").getAsString();
		final String name = jsonObject.get("name").getAsString();
		final String desc = jsonObject.get("description").getAsString();
		final String upD = jsonObject.get("updatedDate").getAsString();
		final String v = jsonObject.get("version").getAsString();
		final String owner = jsonObject.get("owner").getAsString();
		
		//Now, getting the resources and comments fields
		final ArrayList<String> all_res = new ArrayList<String>();
		final ArrayList<ChartComment> comments = new ArrayList<ChartComment>();
		for (JsonElement j : jsonObject.get("all_res").getAsJsonArray()) {
			all_res.add(j.getAsString());
		}
		if (jsonObject.has("comments")) {
			for (JsonElement j : jsonObject.get("comments").getAsJsonArray()) {
				comments.add(gson.fromJson(j,ChartComment.class));
			}
		}
		
		//Now, set all of the fields of the FlowChart object
		final FlowChart flowchart = new FlowChart();
		flowchart.setId(id);;
		flowchart.setName(name);
		flowchart.setDescription(desc);
		flowchart.setUpdatedDate(upD);
		flowchart.setVersion(v);
		flowchart.setOwner(owner);
		flowchart.setAllRes(all_res);
		flowchart.setComments(comments);
		
		if (jsonObject.has("graph")) {
			final JsonElement temp_graph = jsonObject.get("graph");
		
			// Convert the last JsonObject to a TinkerGraph object
			final TinkerGraph real_graph = new TinkerGraph();
			GraphSONReader reader = new GraphSONReader(real_graph);
			
			InputStream stream = new ByteArrayInputStream(gson.toJson(temp_graph).getBytes());
			try {
				reader.inputGraph(stream);
			} catch (IOException e) {
				e.printStackTrace();
			} //This means real_graph is a Tinker Graph
			
			flowchart.setGraph(real_graph);
		}
	
		return flowchart;
		
	}

}
