package co.adhoclabs;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Contains the documents to be inserted in bulk by one thread.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class BulkInsertDocuments {
	private final List<ChannelBuffer> insertBuffers;

	public BulkInsertDocuments(DocumentSchema schema,
			ValueGenerator valueGenerator, int connectionNum,
			int numDocumentsPerInsert, int numInsertOperations) {
		int numInsertedDocuments = numDocumentsPerInsert * numInsertOperations;
		int startId = connectionNum * numInsertedDocuments;

		insertBuffers = new ArrayList<ChannelBuffer>(numInsertOperations);
		for (int i = 0; i < numInsertOperations; ++i) {
			JSONArray documents = new JSONArray(numDocumentsPerInsert);
			for (int j = 0; j < numDocumentsPerInsert; ++j) {
				JSONObject document = schema.getNewDocument(valueGenerator);
				document.put("_id", startId++);
				documents.add(document);
			}

			JSONObject json = new JSONObject();
			json.put("docs", documents);
			ChannelBuffer insertBuffer = ChannelBuffers.copiedBuffer(
					json.toString(), CharsetUtil.ISO_8859_1);
			insertBuffers.add(insertBuffer);
		}
	}

	public ChannelBuffer getBuffer(int insertOperation) {
		return insertBuffers.get(insertOperation);
	}
	
	public int size() {
		return insertBuffers.size();
	}
}
