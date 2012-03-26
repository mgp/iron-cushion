package co.adhoclabs;

import java.io.File;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A schema that describes the format of documents.
 * 
 * @author Michael Parker (michael.g.parker@gmail.com)
 */
public class DocumentSchema {
	private static abstract class Tag {
		public enum Type {
			DICTIONARY,
			ARRAY,
			BOOLEAN,
			INTEGER,
			FLOAT
		}
		
		public abstract Type getType();
	}
	
	/**
	 * A {@code <dict>} tag.
	 */
	private static final class DictionaryTag extends Tag {
		private static final class Entry {
			private final String keyName;
			private final Tag value;
			
			private Entry(String keyName, Tag value) {
				this.keyName = keyName;
				this.value = value;
			}
		}
		
		private final List<Entry> entries;
		
		private DictionaryTag(List<Entry> entries) {
			this.entries = entries;
		}
		
		public Type getType() {
			return Type.DICTIONARY;
		}
	}
	
	/**
	 * A {@code <array>} tag.
	 */
	private static final class ArrayTag extends Tag {
		private final List<Tag> elements;
		
		private ArrayTag(List<Tag> elements) {
			this.elements = elements;
		}
		
		public Type getType() {
			return Type.ARRAY;
		}
	}
	
	/**
	 * A {@code <boolean>} tag.
	 */
	private static final class BooleanTag extends Tag {
		public Type getType() {
			return Type.BOOLEAN;
		}
	}
	
	/**
	 * A {@code <integer>} tag.
	 */
	private static final class IntegerTag extends Tag {
		public Type getType() {
			return Type.INTEGER;
		}
	}
	
	/**
	 * A {@code <float>} tag.
	 */
	private static final class FloatTag extends Tag {
		public Type getType() {
			return Type.FLOAT;
		}
	}
	
	@SuppressWarnings("unchecked")
	private final JSONArray getArray(ArrayTag tag, ValueGenerator generator) {
		JSONArray array = new JSONArray();
		for (Tag element : tag.elements) {
			array.add(getObject(element, generator));
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	private final JSONObject getDictionary(DictionaryTag tag, ValueGenerator generator) {
		JSONObject object = new JSONObject();
		for (DictionaryTag.Entry entry : tag.entries) {
			Object value = getObject(entry.value, generator);
			object.put(entry.keyName, value);
		}
		return object;
	}
	
	private final Object getObject(Tag tag, ValueGenerator generator) {
		switch (tag.getType()) {
		case ARRAY:
			return getArray((ArrayTag) tag, generator);
		case DICTIONARY:
			return getDictionary((DictionaryTag) tag, generator);
		case BOOLEAN:
			return generator.getBoolean();
		case INTEGER:
			return generator.getInt();
		case FLOAT:
			return generator.getFloat();
		default:
			break;
		}
		return null;
	}
	
	private final DictionaryTag root;
	
	private DocumentSchema(DictionaryTag root) {
		this.root = root;
	}
	
	public static DocumentSchema createSchema(File schemaFile) {
		// TODO
		DictionaryTag root = null;
		return new DocumentSchema(root);
	}
	
	public JSONObject getNewDocument(ValueGenerator generator) {
		return getDictionary(root, generator);
	}
}
