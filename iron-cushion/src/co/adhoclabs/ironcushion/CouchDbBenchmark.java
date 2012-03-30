package co.adhoclabs.ironcushion;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.adhoclabs.ironcushion.BenchmarkResults.BulkInsertBenchmarkResults;
import co.adhoclabs.ironcushion.BenchmarkResults.CrudBenchmarkResults;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertConnectionStatistics;
import co.adhoclabs.ironcushion.bulkinsert.BulkInsertDocumentGenerator;
import co.adhoclabs.ironcushion.crud.CrudConnectionStatistics;
import co.adhoclabs.ironcushion.crud.CrudOperations;
import co.adhoclabs.ironcushion.crud.CrudOperations.CrudOperationCounts;

/**
 * Benchmark utility for CouchDB.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CouchDbBenchmark {
	private static void performCrudOperations(ParsedArguments parsedArguments,
			DocumentSchema schema, HttpReactor httpReactor, String[] words,
			Random rng) throws BenchmarkException {
		// Create the CRUD operation path.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(parsedArguments.databaseName);
		String crudPath = sb.toString();
		
		// Create the CRUD operations to perform.
		List<CrudOperations> allCrudOperations = new ArrayList<CrudOperations>(
				parsedArguments.numConnections);
		CrudOperationCounts crudOperationCounts = CrudOperations.createOperationCounts(
				parsedArguments);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			CrudOperations crudOperations = CrudOperations.createCrudOperations(
					i, schema, new ValueGenerator(words, rng), parsedArguments, crudOperationCounts);
			allCrudOperations.add(crudOperations);
		}

		// Perform the CRUD operations.
		List<CrudConnectionStatistics> allCrudConnectionStatistics = httpReactor.performCrudOperations(
				allCrudOperations, crudPath);
		CrudBenchmarkResults crudBenchmarkResults = BenchmarkResults.getCrudResults(
				parsedArguments.numConnections, crudOperationCounts, allCrudConnectionStatistics);
		System.out.println("CRUD BENCHMARK RESULTS:");
		System.out.println(crudBenchmarkResults);
		System.out.println();
	}
	
	private static void performBulkInserts(ParsedArguments parsedArguments,
			DocumentSchema schema, HttpReactor httpReactor, String[] words,
			Random rng) throws BenchmarkException {
		
		// Create the bulk insert path.
		StringBuilder sb = new StringBuilder();
		sb.append('/').append(parsedArguments.databaseName);
		sb.append('/').append("_bulk_docs");
		String bulkInsertPath = sb.toString();

		List<BulkInsertDocumentGenerator> allBulkInsertDocumentGenerators = new ArrayList<BulkInsertDocumentGenerator>(
				parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			BulkInsertDocumentGenerator bulkInsertDocumentGenerator = BulkInsertDocumentGenerator.onDemand(
					schema, new ValueGenerator(words, rng), i,
					parsedArguments.numDocumentsPerBulkInsert,
					parsedArguments.numBulkInsertOperations);
			allBulkInsertDocumentGenerators.add(bulkInsertDocumentGenerator);
		}
		
		// Perform the bulk insert operations.
		List<BulkInsertConnectionStatistics> allBulkInsertConnectionStatistics = httpReactor.performBulkInserts(
				allBulkInsertDocumentGenerators, bulkInsertPath);
		BulkInsertBenchmarkResults bulkInsertBenchmarkResults =
				BenchmarkResults.getBulkInsertResults(parsedArguments, allBulkInsertConnectionStatistics);
		System.out.println("BULK INSERT BENCHMARK RESULTS:");
		System.out.println(bulkInsertBenchmarkResults);
		System.out.println();
	}
	
	public static void main(String[] args) throws BenchmarkException {
		ParsedArguments parsedArguments = ParsedArguments.parseArguments(args);
		Random rng = new Random(2012);

		// Create the document schema.
		DocumentSchema schema = null;
		if (parsedArguments.jsonDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromJson(parsedArguments.jsonDocumentSchemaFile);
		} else if (parsedArguments.xmlDocumentSchemaFile != null) {
			schema = DocumentSchema.createSchemaFromXml(parsedArguments.xmlDocumentSchemaFile);
		}

		// Create the address of the server.
		URI databaseUri;
		try {
			databaseUri = new URI(parsedArguments.databaseAddress);
		} catch (URISyntaxException e) {
			throw new BenchmarkException(e);
		}
		InetSocketAddress databaseAddress = new InetSocketAddress(
				databaseUri.getHost(), databaseUri.getPort());
		HttpReactor httpReactor = new HttpReactor(parsedArguments.numConnections, databaseAddress);
		String[] words = ValueGenerator.createWords(rng);
		
		// Perform the bulk inserts.
		performBulkInserts(parsedArguments, schema, httpReactor, words, rng);
		// Perform the CRUD operations.
		performCrudOperations(parsedArguments, schema, httpReactor, words, rng);
	}
}
