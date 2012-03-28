package co.adhoclabs;

import java.io.File;

/**
 * The parsed and validated command line arguments.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class ParsedArguments {
	/**
	 * The address of the database.
	 */
	public final String databaseAddress;
	/**
	 * The name of the database.
	 */
	public final String databaseName;
	/**
	 * The number of connections to open concurrently.
	 */
	public final int numConnections;
	
	/**
	 * The number of documents in each bulk insert operation.
	 */
	public final int numDocumentsPerBulkInsert;
	/**
	 * The number of bulk insert operations performed by each connection.
	 */
	public final int numBulkInsertOperations;
	
	/**
	 * The number of CRUD operations by each connection after bulk inserting finishes.
	 */
	public final int numCrudOperations;
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
	 * The JSON file containing field names in documents and their respective types,
	 * used for create and update operations.
	 */
	public final File jsonDocumentSchemaFile;
	/**
	 * The XML file containing field names in documents and their respective types,
	 * used for create and update operations.
	 */
	public final File xmlDocumentSchemaFile;
	/**
	 * The file containing view names, used for read operations.
	 */
	public final File viewsFile;

	/**
	 * Use {@link #parseArguments(String[])} below.
	 */
	private ParsedArguments(String databaseAddress,
			String databaseName,
			int numConnections,
			int numDocumentsPerBulkInsert,
			int numBulkInsertOperations,
			int numCrudOperations,
			int createWeight,
			int readWeight,
			int updateWeight,
			int deleteWeight,
			File jsonDocumentSchemaFile,
			File xmlDocumentSchemaFile,
			File viewsFile) {
		this.databaseAddress = databaseAddress;
		this.databaseName = databaseName;
		this.numConnections = numConnections;
		this.numDocumentsPerBulkInsert = numDocumentsPerBulkInsert;
		this.numBulkInsertOperations = numBulkInsertOperations;
		this.numCrudOperations = numCrudOperations;
		this.createWeight = createWeight;
		this.readWeight = readWeight;
		this.updateWeight = updateWeight;
		this.deleteWeight = deleteWeight;
		this.jsonDocumentSchemaFile = jsonDocumentSchemaFile;
		this.xmlDocumentSchemaFile = xmlDocumentSchemaFile;
		this.viewsFile = viewsFile;
	}

	private static final String DATABASE_ADDRESS_PREFIX = "--database_address=";
	private static final String DATABASE_NAME_PREFIX = "--database_name=";
	private static final String NUM_CONNECTIONS_PREFIX = "--num_connections=";

	private static final String NUM_DOCUMENTS_PER_BULK_INSERT_PREFIX = "--num_documents_per_bulk_insert=";
	private static final String NUM_BULK_INSERT_OPERATIONS_PREFIX = "--num_bulk_insert_operations=";
	
	private static final String NUM_CRUD_OPERATIONS_PREFIX = "--num_crud_operations=";
	private static final String CREATE_WEIGHT_PREFIX = "--create_weight=";
	private static final String READ_WEIGHT_PREFIX = "--read_weight=";
	private static final String UPDATE_WEIGHT_PREFIX = "--update_weight=";
	private static final String DELETE_WEIGHT_PREFIX = "--delete_weight=";

	private static final String JSON_DOCUMENT_SCHEMA_FILENAME_PREFIX = "--json_document_schema_filename=";
	private static final String XML_DOCUMENT_SCHEMA_FILENAME_PREFIX = "--xml_document_schema_filename=";
	private static final String VIEWS_FILENAME_PREFIX = "--views_filename=";
	
	private static String valueForArgument(String arg, String argumentPrefix) {
		return arg.substring(argumentPrefix.length());
	}
	
	private static int intValueForArgument(String arg, String argumentPrefix) {
		String value = valueForArgument(arg, argumentPrefix);
		return Integer.valueOf(value).intValue();
	}
	
	public static ParsedArguments parseArguments(String[] args) {
		String databaseAddress = null;
		String databaseName = null;
		int numConnections = 1;
		int numDocumentsPerBulkInsert = 0;
		int numBulkInsertOperations = 0;
		int numCrudOperations = 0;
		int createWeight = 0;
		int readWeight = 0;
		int updateWeight = 0;
		int deleteWeight = 0;
		String jsonDocumentSchemaFilename = null;
		String xmlDocumentSchemaFilename = null;
		String viewsFilename = null;
		
		for (String arg : args) {
			if (arg.startsWith(DATABASE_ADDRESS_PREFIX)) {
				databaseAddress = valueForArgument(arg, DATABASE_ADDRESS_PREFIX);
			} else if (arg.startsWith(DATABASE_NAME_PREFIX)) {
				databaseName = valueForArgument(arg, DATABASE_NAME_PREFIX);
			} else if (arg.startsWith(NUM_CONNECTIONS_PREFIX)) {
				numConnections = intValueForArgument(arg, NUM_CONNECTIONS_PREFIX);
			} else if (arg.startsWith(NUM_DOCUMENTS_PER_BULK_INSERT_PREFIX)) {
				numDocumentsPerBulkInsert = intValueForArgument(arg, NUM_DOCUMENTS_PER_BULK_INSERT_PREFIX);
			} else if (arg.startsWith(NUM_BULK_INSERT_OPERATIONS_PREFIX)) {
				numBulkInsertOperations = intValueForArgument(arg, NUM_BULK_INSERT_OPERATIONS_PREFIX);
			} else if (arg.startsWith(NUM_CRUD_OPERATIONS_PREFIX)) {
				numCrudOperations = intValueForArgument(arg, NUM_CRUD_OPERATIONS_PREFIX);
			} else if (arg.startsWith(CREATE_WEIGHT_PREFIX)) {
				createWeight = intValueForArgument(arg, CREATE_WEIGHT_PREFIX);
			} else if (arg.startsWith(READ_WEIGHT_PREFIX)) {
				readWeight = intValueForArgument(arg, READ_WEIGHT_PREFIX);
			} else if (arg.startsWith(UPDATE_WEIGHT_PREFIX)) {
				updateWeight = intValueForArgument(arg, UPDATE_WEIGHT_PREFIX);
			} else if (arg.startsWith(DELETE_WEIGHT_PREFIX)) {
				deleteWeight = intValueForArgument(arg, DELETE_WEIGHT_PREFIX);
			} else if (arg.startsWith(JSON_DOCUMENT_SCHEMA_FILENAME_PREFIX)) {
				jsonDocumentSchemaFilename = valueForArgument(arg, JSON_DOCUMENT_SCHEMA_FILENAME_PREFIX);
			} else if (arg.startsWith(XML_DOCUMENT_SCHEMA_FILENAME_PREFIX)) {
				xmlDocumentSchemaFilename = valueForArgument(arg, XML_DOCUMENT_SCHEMA_FILENAME_PREFIX);
			} else if (arg.startsWith(VIEWS_FILENAME_PREFIX)) {
				viewsFilename = valueForArgument(arg, VIEWS_FILENAME_PREFIX);
			} else {
				throw new IllegalArgumentException("Unrecognized command line argument: "  + arg);
			}
		}
		
		// Validate the arguments.
		if ((databaseAddress == null) || databaseAddress.isEmpty()) {
			throw new IllegalArgumentException("Value --database_address must be provided");
		}
		if ((databaseName == null) || databaseName.isEmpty()) {
			throw new IllegalArgumentException("Value --database_name must be provided");
		}
		if (numConnections <= 0) {
			throw new IllegalArgumentException("Value --num_connections must be > 0");
		}
		if (numDocumentsPerBulkInsert < 0) {
			throw new IllegalArgumentException("Value --num_documents_per_bulk_insert must be >= 0");
		}
		if (numBulkInsertOperations < 0) {
			throw new IllegalArgumentException("Value --num_bulk_insert_operations must be >= 0");
		}
		if (numCrudOperations < 0) {
			throw new IllegalArgumentException("Value --num_crud_operations must be >= 0");
		}
		if (createWeight < 0) {
			throw new IllegalArgumentException("Value --create_weight must be >= 0");
		}
		if (readWeight < 0) {
			throw new IllegalArgumentException("Value --read_weight must be >= 0");
		}
		if (updateWeight < 0) {
			throw new IllegalArgumentException("Value --update_weight must be >= 0");
		}
		if (deleteWeight < 0) {
			throw new IllegalArgumentException("Value --delete_weight must be >= 0");
		}
		if (numCrudOperations > 0) {
			int totalWeight = createWeight + readWeight + updateWeight + deleteWeight;
			if (totalWeight == 0) {
				throw new IllegalArgumentException("Sum of weights must be > 0");
			}
		}
		if ((jsonDocumentSchemaFilename == null) == (xmlDocumentSchemaFilename == null)) {
			throw new IllegalArgumentException(
					"Either value --json_document_schema_filename or --xml_document_schema_filename must be provided");
		}
		
		File jsonDocumentSchemaFile = null;
		if (jsonDocumentSchemaFilename != null) {
			jsonDocumentSchemaFile = new File(jsonDocumentSchemaFilename);
			if (!jsonDocumentSchemaFile.exists()) {
				throw new IllegalArgumentException("Filename --json_document_schema_filename does not exist");
			}
		}
		File xmlDocumentSchemaFile = null;
		if (xmlDocumentSchemaFilename != null) {
			xmlDocumentSchemaFile = new File(xmlDocumentSchemaFilename);
			if (!xmlDocumentSchemaFile.exists()) {
				throw new IllegalArgumentException("Filename --xml_document_schema_filename does not exist");
			}
		}
		File viewsFile = null;	// new File(viewsFilename);
		
		return new ParsedArguments(databaseAddress,
				databaseName,
				numConnections,
				numDocumentsPerBulkInsert,
				numBulkInsertOperations,
				numCrudOperations,
				createWeight,
				readWeight,
				updateWeight,
				deleteWeight,
				jsonDocumentSchemaFile,
				xmlDocumentSchemaFile,
				viewsFile);
	}
}
