package co.adhoclabs.crud;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import co.adhoclabs.ParsedArguments;

public class CrudOperations {
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
		
		public CrudOperationCounts(int numCreateOperations, int numReadOperations,
				int numUpdateOperations, int numDeleteOperations, int numOperations) {
			this.numCreateOperations = numCreateOperations;
			this.numReadOperations = numReadOperations;
			this.numUpdateOperations = numUpdateOperations;
			this.numDeleteOperations = numDeleteOperations;
			this.numOperations = numOperations;
		}
	}
	
	private static CrudOperationCounts createOperationCounts(ParsedArguments parsedArguments) {
		double totalWeight = parsedArguments.createWeight +
				parsedArguments.readWeight +
				parsedArguments.updateWeight +
				parsedArguments.deleteWeight;
		double createPercentage = parsedArguments.createWeight / totalWeight;
		double readPercentage = parsedArguments.readWeight / totalWeight;
		double updatePercentage = parsedArguments.updateWeight / totalWeight;
		int numCreateOperations = (int) (createPercentage * parsedArguments.numCrudOperations);
		int numReadOperations = (int) (readPercentage * parsedArguments.numCrudOperations);
		int numUpdateOperations = (int) (updatePercentage * parsedArguments.numCrudOperations);
		int numDeleteOperations = parsedArguments.numCrudOperations -
				(numCreateOperations + numReadOperations + numUpdateOperations);
		
		return new CrudOperationCounts(
				numCreateOperations, numReadOperations, numUpdateOperations, numDeleteOperations,
				parsedArguments.numCrudOperations);
	}
	
	public static Type[] getOperations(CrudOperationCounts operationCounts) {
		if (operationCounts.numDeleteOperations >
				(operationCounts.numCreateOperations + operationCounts.numReadOperations)) {
			throw new IllegalArgumentException();
		}
		Random rng = new Random();
		
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
			int deleteIndex = rng.nextInt(operationCounts.numDeleteOperations);
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
}
