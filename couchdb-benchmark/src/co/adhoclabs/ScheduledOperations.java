package co.adhoclabs;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the scheduled operations for each connection.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ScheduledOperations {
	private List<List<Operation>> operationsForConnections;
	
	private ScheduledOperations(List<List<Operation>> operationsForConnections) {
		this.operationsForConnections = operationsForConnections;
	}
	
	public List<Operation> getOperationsForThread(int threadNum) {
		return operationsForConnections.get(threadNum);
	}
	
	private static List<Operation> getConnectionOperations(
			int connectionNum, ParsedArguments parsedArguments,
			int numCreateOps, int numReadOps, int numUpdateOps, int numDeleteOps) {
		List<Operation> operations = new ArrayList<Operation>(parsedArguments.operationsPerConnection);
		
		return operations;
	}
	
	public static ScheduledOperations scheduleOperations(ParsedArguments parsedArguments) {
		double totalWeight = parsedArguments.createWeight +
				parsedArguments.readWeight +
				parsedArguments.updateWeight +
				parsedArguments.deleteWeight;
		double createPercentage = parsedArguments.createWeight / totalWeight;
		double readPercentage = parsedArguments.readWeight / totalWeight;
		double updatePercentage = parsedArguments.updateWeight / totalWeight;
		int numCreateOps = (int) (createPercentage * parsedArguments.operationsPerConnection);
		int numReadOps = (int) (readPercentage * parsedArguments.operationsPerConnection);
		int numUpdateOps = (int) (updatePercentage * parsedArguments.operationsPerConnection);
		int numDeleteOps = parsedArguments.operationsPerConnection -
				(numCreateOps + numReadOps + numUpdateOps);
		
		List<List<Operation>> operationsForConnections =
				new ArrayList<List<Operation>>(parsedArguments.numConnections);
		for (int i = 0; i < parsedArguments.numConnections; ++i) {
			List<Operation> connectionOperations = getConnectionOperations(
					i, parsedArguments,
					numCreateOps, numReadOps, numUpdateOps, numDeleteOps);
			operationsForConnections.add(connectionOperations);
		}
		return new ScheduledOperations(operationsForConnections);
	}
}
