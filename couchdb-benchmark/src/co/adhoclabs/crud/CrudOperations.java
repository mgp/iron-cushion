package co.adhoclabs.crud;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class CrudOperations {
	public enum Type {
		CREATE,
		READ,
		UPDATE,
		DELETE
	}
	
	public static Type[] getOperations(int numCreateOperations, int numReadOperations,
			int numUpdateOperations, int numDeleteOperations) {
		if (numDeleteOperations > (numCreateOperations + numReadOperations)) {
			throw new IllegalArgumentException();
		}
		Random rng = new Random();
		
		int numOperations = numCreateOperations + numReadOperations + numUpdateOperations + numDeleteOperations;
		Type[] operations = new Type[numOperations];
		Type[] createAndReadOperations = new Type[numCreateOperations + numReadOperations];
		
		// Randomize the order of all CREATE and READ operations.
		Arrays.fill(createAndReadOperations, 0, numCreateOperations, Type.CREATE);
		Arrays.fill(createAndReadOperations, numCreateOperations, createAndReadOperations.length, Type.READ);
		Collections.shuffle(Arrays.asList(createAndReadOperations));
		
		// Add the CREATE and READ operations that are not paired with DELETE operations.
		// TODO: Use System.arrayCopy.
		for (int i = 1 + numUpdateOperations, j = numDeleteOperations; j < createAndReadOperations.length; ++i, ++j) {
			operations[i] = createAndReadOperations[j];
		}
		// Randomize the order of these operations.
		Collections.shuffle(Arrays.asList(operations).subList(1, operations.length));
		
		// Determine the number of UPDATE operations to follow each CREATE and READ operation.
		int[] numFollowingUpdates = new int[numDeleteOperations];
		for (int i = 0; i < numUpdateOperations; ++i) {
			int deleteIndex = rng.nextInt(numDeleteOperations);
			numFollowingUpdates[deleteIndex]++;
		}
		
		// Add the UPDATE, DELETE, and remaining CREATE AND READ operations.
		for (int i = 0, j = 0; i < numDeleteOperations; ++i) {
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
	
	public static void main(String[] args) {
		Type[] operations = getOperations(2, 2, 3, 3);
		System.out.println(Arrays.toString(operations));
	}
}
