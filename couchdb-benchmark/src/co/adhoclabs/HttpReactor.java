package co.adhoclabs;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The networking engine that asynchronously executes HTTP requests.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class HttpReactor {
	private final int numConnections;
	
	public HttpReactor(int numConnections) {
		this.numConnections = numConnections;
		
		start();
	}
	
	String getBulkInsertBody(List<JSONObject> documents) {
		JSONObject body = new JSONObject();
		JSONArray docs = new JSONArray();
		int id = 0;
		for (JSONObject document : documents) {
			document.put("_id", String.valueOf(id));
			docs.add(document);
		}
		body.put("docs", docs);
		return body.toString();
	}
	
	public void start() {
		// TODO: start up the async engine
	}
	
	public void performBulkInserts() {
		// TODO
	}
	
	public void performCrudOperations(ScheduledOperations scheduledOperations) {
		// TODO
	}
	
	public BenchmarkResults end() {
		// TODO: stop the async engine
		return null;
	}
}
