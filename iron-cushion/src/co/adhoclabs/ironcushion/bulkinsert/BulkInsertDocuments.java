package co.adhoclabs.ironcushion.bulkinsert;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import co.adhoclabs.ironcushion.DocumentSchema;
import co.adhoclabs.ironcushion.ValueGenerator;

/**
 * Returns {@link ChannelBuffer} instances containing JSON documents to be
 * inserted in bulk by one connection.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public abstract class BulkInsertDocuments {
	private BulkInsertDocuments() {
	}

	/**
	 * Returns a {@link ChannelBuffer} containing documents to be bulk inserted.
	 * 
	 * @param insertOperation
	 *            the index of the bulk insert operation, greater than {@code 0}
	 *            but less than the value returned by {@link #size()}
	 * @return the buffer containing the documents
	 */
	public abstract ChannelBuffer getBuffer(int insertOperation);

	/**
	 * Returns number of {@link ChannelBuffer} instances that can be returned.
	 * 
	 * @return the number of buffers to return
	 */
	public abstract int size();

	@SuppressWarnings("unchecked")
	protected ChannelBuffer getNewBuffer(int numDocumentsPerInsert,
			DocumentSchema schema, ValueGenerator valueGenerator,
			int nextDocumentId) {
		JSONArray documents = new JSONArray(numDocumentsPerInsert);
		for (int j = 0; j < numDocumentsPerInsert; ++j) {
			JSONObject document = schema.getNewDocument(valueGenerator);
			document.put("_id", String.valueOf(nextDocumentId));
			documents.add(document);
		}

		JSONObject json = new JSONObject();
		json.put("docs", documents);
		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(json.toString(),
				CharsetUtil.UTF_8);
		return buffer;
	}

	private static final class PreComputedBulkInsertDocuments extends
			BulkInsertDocuments {
		private final List<ChannelBuffer> insertBuffers;

		private PreComputedBulkInsertDocuments(DocumentSchema schema,
				ValueGenerator valueGenerator, int connectionNum,
				int numDocumentsPerInsert, int numInsertOperations) {
			int numInsertedDocuments = numDocumentsPerInsert
					* numInsertOperations;
			int nextDocumentId = connectionNum * numInsertedDocuments;

			insertBuffers = new ArrayList<ChannelBuffer>(numInsertOperations);
			for (int i = 0; i < numInsertOperations; ++i, ++nextDocumentId) {
				insertBuffers.add(getNewBuffer(numDocumentsPerInsert, schema,
						valueGenerator, nextDocumentId));
			}
		}

		public ChannelBuffer getBuffer(int insertOperation) {
			return insertBuffers.get(insertOperation);
		}

		public int size() {
			return insertBuffers.size();
		}
	}

	/**
	 * Returns a {@link BulkInsertDocuments} implementation where all returned
	 * {@link ChannelBuffer} instances are pre-computed.
	 * 
	 * @param schema 
	 * @param valueGenerator
	 * @param connectionNum
	 * @param numDocumentsPerInsert
	 * @param numInsertOperations
	 * @return the generator of pre-computed documents
	 */
	public static BulkInsertDocuments preComputed(DocumentSchema schema,
			ValueGenerator valueGenerator, int connectionNum,
			int numDocumentsPerInsert, int numInsertOperations) {
		return new PreComputedBulkInsertDocuments(schema, valueGenerator,
				connectionNum, numDocumentsPerInsert, numInsertOperations);
	}

	private static final class OnDemandBulkInsertDocuments extends
			BulkInsertDocuments {
		private final DocumentSchema schema;
		private final ValueGenerator valueGenerator;
		private final int numDocumentsPerInsert;
		private final int numInsertOperations;
		private int nextDocumentId;

		private OnDemandBulkInsertDocuments(DocumentSchema schema,
				ValueGenerator valueGenerator, int connectionNum,
				int numDocumentsPerInsert, int numInsertOperations) {
			this.schema = schema;
			this.valueGenerator = valueGenerator;
			this.numDocumentsPerInsert = numDocumentsPerInsert;
			this.numInsertOperations = numInsertOperations;

			int numInsertedDocuments = numDocumentsPerInsert * numInsertOperations;
			nextDocumentId = connectionNum * numInsertedDocuments;
		}

		public ChannelBuffer getBuffer(int insertOperation) {
			return getNewBuffer(numDocumentsPerInsert, schema, valueGenerator,
					nextDocumentId);
		}

		public int size() {
			return numInsertOperations;
		}
	}

	/**
	 * Returns a {@link BulkInsertDocuments} implementation where all returned
	 * {@link ChannelBuffer} instances are computed on-demand.
	 * 
	 * @param schema
	 * @param valueGenerator
	 * @param connectionNum
	 * @param numDocumentsPerInsert
	 * @param numInsertOperations
	 * @return an on-demand generator of documents
	 */
	public static BulkInsertDocuments onDemand(DocumentSchema schema,
			ValueGenerator valueGenerator, int connectionNum,
			int numDocumentsPerInsert, int numInsertOperations) {
		return new OnDemandBulkInsertDocuments(schema, valueGenerator,
				connectionNum, numDocumentsPerInsert, numInsertOperations);
	}
}
