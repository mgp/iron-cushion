package co.adhoclabs.ironcushion.crud;

import java.util.Arrays;
import java.util.Collections;

import org.json.simple.JSONObject;

import co.adhoclabs.ironcushion.DocumentSchema;
import co.adhoclabs.ironcushion.ParsedArguments;
import co.adhoclabs.ironcushion.ValueGenerator;

/**
 * Specifies the CRUD operations to be performed by one connection, and maintains the document
 * identifiers to use for create and read operations.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class CrudOperations {
	/**
	 * An enumeration over all CRUD operations.
	 */
	public enum Type {
		CREATE,
		READ,
		UPDATE,
		DELETE
	}
	
	/**
	 * The number of CRUD operations to be performed by one connection.
	 */
	public static final class CrudOperationCounts {
		public final int numCreateOperations;
		public final int numReadOperations;
		public final int numUpdateOperations;
		public final int numDeleteOperations;
		public final int numOperations;
		
		private CrudOperationCounts(int numCreateOperations, int numReadOperations,
				int numUpdateOperations, int numDeleteOperations, int numOperations) {
			this.numCreateOperations = numCreateOperations;
			this.numReadOperations = numReadOperations;
			this.numUpdateOperations = numUpdateOperations;
			this.numDeleteOperations = numDeleteOperations;
			this.numOperations = numOperations;
		}
	}
	
	/**
	 * Creates the counts for all CRUD operations to be performed by each connection.
	 * 
	 * @param parsedArguments the parsed command line arguments
	 * @return the counts for all CRUD operations
	 */
	public static CrudOperationCounts createOperationCounts(ParsedArguments parsedArguments) {
		double totalWeight = parsedArguments.createWeight +
				parsedArguments.readWeight +
				parsedArguments.updateWeight +
				parsedArguments.deleteWeight;
		double createPercentage = parsedArguments.createWeight / totalWeight;
		double readPercentage = parsedArguments.readWeight / totalWeight;
		double deletePercentage = parsedArguments.deleteWeight / totalWeight;
		// Equal CREATE, READ, and DELETE weights translate to equal numbers of operations. 
		int numCreateOperations = (int) (createPercentage * parsedArguments.numCrudOperations);
		int numReadOperations = (int) (readPercentage * parsedArguments.numCrudOperations);
		int numDeleteOperations = (int) (deletePercentage * parsedArguments.numCrudOperations);
		// The number of UPDATE operations may be subject to rounding.
		int numUpdateOperations = parsedArguments.numCrudOperations -
				(numCreateOperations + numReadOperations + numDeleteOperations);
		
		return new CrudOperationCounts(
				numCreateOperations, numReadOperations, numUpdateOperations, numDeleteOperations,
				parsedArguments.numCrudOperations);
	}
	
	private final Type[] operations;
	
	private final DocumentSchema documentSchema;
	private final ValueGenerator valueGenerator;
	
	private int nextCreateDocumentId;
	private int nextReadBulkInsertedDocumentId;
	private int nextReadCreatedDocumentId;
	
	private CrudOperations(Type[] operations,
			DocumentSchema documentSchema, ValueGenerator valueGenerator,
			int firstBulkInsertedDocumentId, int firstCreateDocumentId) {
		this.operations = operations;
		
		this.documentSchema = documentSchema;
		this.valueGenerator = valueGenerator;
		
		this.nextCreateDocumentId = firstCreateDocumentId;
		this.nextReadBulkInsertedDocumentId = firstBulkInsertedDocumentId;
		this.nextReadCreatedDocumentId = firstCreateDocumentId;
	}
	
	/**
	 * Returns the CRUD operation at the given index.
	 * 
	 * @param operationIndex the index of the operation to return
	 * @return the CRUD operation
	 */
	public Type getOperation(int operationIndex) {
		return operations[operationIndex];
	}
	
	private void completedCreateOperation() {
		nextCreateDocumentId++;
	}
	
	private void completedReadOperation() {
		if (nextReadCreatedDocumentId < nextCreateDocumentId) {
			nextReadCreatedDocumentId++;
		} else {
			nextReadBulkInsertedDocumentId++;
		}
	}
	
	private void completedDeleteOperation() {
		if (nextReadCreatedDocumentId < nextCreateDocumentId) {
			// Do not attempt to read a deleted document.
			nextReadCreatedDocumentId = nextCreateDocumentId;
		}
	}
	
	public void completedOperation(int operationIndex) {
		switch (operations[operationIndex]) {
		case CREATE:
			completedCreateOperation();
			break;
		case READ:
			completedReadOperation();
			break;
		case DELETE:
			completedDeleteOperation();
			break;
		default:
			break;
		}
	}
	
	/**
	 * @return the number of operations to perform
	 */
	public int size() {
		return operations.length;
	}
	
	/**
	 * @return a new document that conforms to the schema
	 */
	public JSONObject getNewDocument() {
		return documentSchema.getNewDocument(valueGenerator);
	}
	
	/**
	 * Updates a randomly chosen value in the given document.
	 * 
	 * @param document the document to update
	 */
	public void updateDocument(JSONObject document) {
		documentSchema.updateDocument(document, valueGenerator);
	}
	
	/**
	 * @return the next identifier for a CREATE operation
	 */
	public int getNextCreateId() {
		// TODO: remove this, automatically insert ID in getNewDocument?
		return nextCreateDocumentId;
	}
	
	/**
	 * @return the next identifier for a READ operation
	 */
	public int getNextReadId() {
		if (nextReadCreatedDocumentId < nextCreateDocumentId) {
			// Read the identifier of a document created individually.
			return nextReadCreatedDocumentId;
		} else {
			// Return the identifier of a document created from a bulk insert.
			return nextReadBulkInsertedDocumentId;
		}
	}
	
	private static Type[] createCrudOperations(
			CrudOperationCounts operationCounts, ValueGenerator valueGenerator) {
		if (operationCounts.numDeleteOperations >
				(operationCounts.numCreateOperations + operationCounts.numReadOperations)) {
			throw new IllegalArgumentException();
		}
		
		Type[] operations = new Type[operationCounts.numOperations];
		Type[] createAndReadOperations = new Type[operationCounts.numCreateOperations + operationCounts.numReadOperations];
		
		// Randomize the order of all CREATE and READ operations.
		Arrays.fill(createAndReadOperations, 0, operationCounts.numCreateOperations, Type.CREATE);
		Arrays.fill(createAndReadOperations, operationCounts.numCreateOperations,
				createAndReadOperations.length, Type.READ);
		Collections.shuffle(Arrays.asList(createAndReadOperations));
		
		// Add the CREATE and READ operations that are not paired with DELETE operations.
		for (int i = 1 + operationCounts.numUpdateOperations, j = operationCounts.numDeleteOperations;
				j < createAndReadOperations.length; ++i, ++j) {
			operations[i] = createAndReadOperations[j];
		}
		// Randomize the order of these operations.
		Collections.shuffle(Arrays.asList(operations).subList(1, operations.length));
		
		// Determine the number of UPDATE operations to follow each CREATE and READ operation.
		int[] numFollowingUpdates = new int[operationCounts.numDeleteOperations];
		for (int i = 0; i < operationCounts.numUpdateOperations; ++i) {
			int deleteIndex = valueGenerator.nextInt(operationCounts.numDeleteOperations);
			numFollowingUpdates[deleteIndex]++;
		}
		
		// Add the UPDATE, DELETE, and remaining CREATE AND READ operations.
		for (int i = 0, j = 0; i < operationCounts.numDeleteOperations; ++i) {
			// Add the CREATE or READ operation.
			while (operations[j] != null) {
				++j;
			}
			operations[j] = createAndReadOperations[i];
			
			// Add any following UPDATE operations.
			for (int k = 0; k < numFollowingUpdates[i]; ++k) {
				while (operations[j] != null) {
					++j;
				}
				operations[j] = Type.UPDATE;
			}

			// Add the DELETE operation.
			while (operations[j] != null) {
				++j;
			}
			operations[j] = Type.DELETE;
		}
		
		return operations;
	}
	
	/**
	 * Returns the {@link CrudOperations} to be performed by a connection.
	 * 
	 * @param connectionNum the number of the connection
	 * @param documentSchema the document schema
	 * @param parsedArguments the parsed command line arguments
	 * @param crudOperationCounts the counts for all CRUD operations
	 * @return the CRUD operations to be performed
	 */
	public static CrudOperations createCrudOperations(int connectionNum,
			DocumentSchema documentSchema, ValueGenerator valueGenerator,
			ParsedArguments parsedArguments, CrudOperationCounts crudOperationCounts) {
		Type[] operations = createCrudOperations(crudOperationCounts, valueGenerator);
		// Compute the identifier of the first document bulk inserted by this connection.
		int nextReadBulkInsertDocumentId = connectionNum *
				parsedArguments.numDocumentsPerBulkInsert * parsedArguments.numBulkInsertOperations;
		// Compute the total number of documents bulk inserted.
		int numBulkInsertDocuments = (parsedArguments.numDocumentsPerBulkInsert *
				parsedArguments.numBulkInsertOperations *
				parsedArguments.numConnections);
		// Compute the identifier for the next document inserted by this connection.
		int nextCreateDocumentId = numBulkInsertDocuments +
				(crudOperationCounts.numCreateOperations * connectionNum);
		
		return new CrudOperations(operations, documentSchema, valueGenerator,
				nextReadBulkInsertDocumentId, nextCreateDocumentId);
	}
}
