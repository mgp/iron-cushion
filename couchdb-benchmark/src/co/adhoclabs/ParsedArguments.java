package co.adhoclabs;
import java.io.File;

/**
 * The parsed and validated command line arguments.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ParsedArguments {
	/**
	 * The number of documents to insert in bulk before performing CRUD operations.
	 */
	public final int numBulkDocuments;
	/**
	 * The number of connections to open concurrently.
	 */
	public final int numConnections;
	/**
	 * The number of CRUD operations to run per connection after bulk inserting finishes.
	 */
	public final int operationsPerConnection;
	
	/**
	 * The relative weight of create operations to other operations.
	 */
	public final int createWeight;
	/**
	 * The relative weight of read operations to other operations.
	 */
	public final int readWeight;
	/**
	 * The relative weight of update operations to other operations.
	 */
	public final int updateWeight;
	/**
	 * The relative weight of delete operations to other operations.
	 */
	public final int deleteWeight;
	
	/**
	 * The file containing field names in documents and their respective types,
	 * used for create and update operations.
	 */
	public final File fieldsFile;
	/**
	 * The file containing view names, used for read operations.
	 */
	public final File viewsFile;

	/**
	 * Use {@link #parseArguments(String[])} below.
	 */
	private ParsedArguments(int numBulkDocuments,
			int numConnections,
			int operationsPerConnection,
			int createWeight,
			int readWeight,
			int updateWeight,
			int deleteWeight,
			File fieldsFile,
			File viewsFile) {
		this.numBulkDocuments = numBulkDocuments;
		this.numConnections = numConnections;
		this.operationsPerConnection = operationsPerConnection;
		this.createWeight = createWeight;
		this.readWeight = readWeight;
		this.updateWeight = updateWeight;
		this.deleteWeight = deleteWeight;
		this.fieldsFile = fieldsFile;
		this.viewsFile = viewsFile;
	}

	private static final String NUM_BULK_DOCUMENTS_PREFIX = "--num_bulk_documents";
	private static final String NUM_CONNECTIONS_PREFIX = "--num_connections";
	private static final String OPERATIONS_PER_CONNECTION_PREFIX = "--operations_per_connection";
	
	private static final String CREATE_WEIGHT_PREFIX = "--create_weight";
	private static final String READ_WEIGHT_PREFIX = "--read_weight";
	private static final String UPDATE_WEIGHT_PREFIX = "--update_weight";
	private static final String DELETE_WEIGHT_PREFIX = "--delete_weight";

	private static final String FIELDS_FILENAME_PREFIX = "--fields_filename";
	private static final String VIEWS_FILENAME_PREFIX = "--views_filename";
	
	private static String valueForArgument(String arg, String argumentPrefix) {
		return arg.substring(argumentPrefix.length());
	}
	
	private static int intValueForArgument(String arg, String argumentPrefix) {
		String value = valueForArgument(arg, argumentPrefix);
		return Integer.valueOf(value).intValue();
	}
	
	public static ParsedArguments parseArguments(String[] args) {
		int numBulkDocuments = 10000;
		int numConnections = 1;
		int operationsPerConnection = 1000;
		int createWeight = 1;
		int readWeight = 1;
		int updateWeight = 1;
		int deleteWeight = 1;
		String fieldsFilename = null;
		String viewsFilename = null;
		
		for (String arg : args) {
			if (arg.startsWith(NUM_BULK_DOCUMENTS_PREFIX)) {
				numBulkDocuments = intValueForArgument(arg, NUM_BULK_DOCUMENTS_PREFIX);
			} else if (arg.startsWith(NUM_CONNECTIONS_PREFIX)) {
				numConnections = intValueForArgument(arg, NUM_CONNECTIONS_PREFIX);
			} else if (arg.startsWith(OPERATIONS_PER_CONNECTION_PREFIX)) {
				operationsPerConnection = intValueForArgument(arg, OPERATIONS_PER_CONNECTION_PREFIX);
			} else if (arg.startsWith(CREATE_WEIGHT_PREFIX)) {
				createWeight = intValueForArgument(arg, CREATE_WEIGHT_PREFIX);
			} else if (arg.startsWith(READ_WEIGHT_PREFIX)) {
				readWeight = intValueForArgument(arg, READ_WEIGHT_PREFIX);
			} else if (arg.startsWith(UPDATE_WEIGHT_PREFIX)) {
				updateWeight = intValueForArgument(arg, UPDATE_WEIGHT_PREFIX);
			} else if (arg.startsWith(DELETE_WEIGHT_PREFIX)) {
				deleteWeight = intValueForArgument(arg, DELETE_WEIGHT_PREFIX);
			} else if (arg.startsWith(FIELDS_FILENAME_PREFIX)) {
				fieldsFilename = valueForArgument(arg, FIELDS_FILENAME_PREFIX);
			} else if (arg.startsWith(VIEWS_FILENAME_PREFIX)) {
				viewsFilename = valueForArgument(arg, VIEWS_FILENAME_PREFIX);
			} else {
				throw new IllegalArgumentException("Unrecognized command line argument: "  + arg);
			}
		}
		
		// TODO: validate the args
		
		File fieldsFile = new File(fieldsFilename);
		File viewsFile = new File(viewsFilename);
		
		return new ParsedArguments(numBulkDocuments,
				numConnections,
				operationsPerConnection,
				createWeight,
				readWeight,
				updateWeight,
				deleteWeight,
				fieldsFile,
				viewsFile);
	}
}
