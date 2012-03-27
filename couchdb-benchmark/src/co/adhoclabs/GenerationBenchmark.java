package co.adhoclabs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONObject;

public class GenerationBenchmark {
	public static void main(String[] args) throws BenchmarkException {
		String filename = args[0];
		File file = new File(filename);
		DocumentSchema schema = DocumentSchema.createSchema(file);
		System.out.println("schema=" + schema);
		Random rng = new Random(2012);
		ValueGenerator valueGenerator = new ValueGenerator(rng);
		
		Timer timer = new Timer();
		timer.start();
		List<JSONObject> documents = new ArrayList<JSONObject>(10000);
		for (int i = 0; i < 10000; ++i) {
			documents.add(schema.getNewDocument(valueGenerator));
		}
		timer.stop();
		System.out.println("document creation totalTimeMillis=" + timer.getTotalTimeMillis());
		
		/*
		HttpReactor reactor = new HttpReactor(0);
		timer.reset();
		timer.start();
		String bulkInsertBody = reactor.getBulkInsertBody(documents);
		timer.stop();
		System.out.println("bulk insert body totalTimeMillis=" + timer.getTotalTimeMillis());
		System.out.println("bulkInsertBody.length()=" + bulkInsertBody.length());
		*/
	}
}
