package co.adhoclabs;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
		
		public abstract void toString(StringBuilder sb);
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			toString(sb);
			return sb.toString();
		}
	}
	
	/**
	 * A {@code <dictionary>} tag.
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
		
		public void toString(StringBuilder sb) {
			sb.append("{");
			int index = 0;
			final int lastIndex = entries.size() - 1;
			for (Entry entry : entries) {
				sb.append(entry.keyName).append(": ");
				entry.value.toString(sb);
				if (index++ < lastIndex) {
					sb.append(", ");
				}
			}
			sb.append("}");
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
		
		public void toString(StringBuilder sb) {
			sb.append("[");
			int index = 0;
			final int lastIndex = elements.size() - 1;
			for (Tag element : elements) {
				element.toString(sb);
				if (index++ < lastIndex) {
					sb.append(", ");
				}
			}
			sb.append("]");
		}
	}
	
	/**
	 * A {@code <boolean>} tag.
	 */
	private static final class BooleanTag extends Tag {
		private static final BooleanTag INSTANCE = new BooleanTag();
		
		public Type getType() {
			return Type.BOOLEAN;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("boolean");
		}
	}
	
	/**
	 * A {@code <integer>} tag.
	 */
	private static final class IntegerTag extends Tag {
		private static final IntegerTag INSTANCE = new IntegerTag();
		
		public Type getType() {
			return Type.INTEGER;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("integer");
		}
	}
	
	/**
	 * A {@code <float>} tag.
	 */
	private static final class FloatTag extends Tag {
		private static final FloatTag INSTANCE = new FloatTag();
		
		public Type getType() {
			return Type.FLOAT;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("float");
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
			return generator.nextBoolean();
		case INTEGER:
			return generator.nextInt();
		case FLOAT:
			return generator.nextFloat();
		default:
			break;
		}
		return null;
	}
	
	private final DictionaryTag root;
	
	private DocumentSchema(DictionaryTag root) {
		this.root = root;
	}
	
	private static Tag parseTag(Element element) {
		if (element.getTagName().equals("array")) {
			return parseArrayTag(element);
		} else if (element.getTagName().equals("dictionary")) {
			return parseDictionaryTag(element);
		} else if (element.getTagName().equals("integer")) {
			return IntegerTag.INSTANCE;
		} else if (element.getTagName().equals("float")) {
			return FloatTag.INSTANCE;
		} else if (element.getTagName().equals("boolean")) {
			return BooleanTag.INSTANCE;
		}
		return null;
	}
	
	private static ArrayTag parseArrayTag(Element element) {
		List<Tag> elements = new LinkedList<DocumentSchema.Tag>();
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element childNodeElement = (Element) childNode;
				NodeList elementChildNodes = childNodeElement.getChildNodes();
				for (int j = 0; j < elementChildNodes.getLength(); ++j) {
					Node elementChildNode = elementChildNodes.item(j);
					if (elementChildNode instanceof Element) {
						elements.add(parseTag((Element) elementChildNode));
					}
				}
			}
		}
		return new ArrayTag(elements);
	}
	
	private static DictionaryTag.Entry parseDictionaryEntryTag(Element element) {
		String keyName = null;
		Tag value = null;
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element childNodeElement = (Element) childNode;
				if (childNodeElement.getTagName().equals("keyName")) {
					keyName = childNodeElement.getChildNodes().item(0).getNodeValue();
				} else if (childNodeElement.getTagName().equals("value")) {
					NodeList valueChildNodes = childNodeElement.getChildNodes();
					for (int j = 0; j < valueChildNodes.getLength(); ++j) {
						Node valueChildNode = valueChildNodes.item(j);
						if (valueChildNode instanceof Element) {
							value = parseTag((Element) valueChildNode);
						}
					}
				}
			}
		}
		return new DictionaryTag.Entry(keyName, value);
	}
	
	private static DictionaryTag parseDictionaryTag(Element element) {
		List<DictionaryTag.Entry> entries = new LinkedList<DocumentSchema.DictionaryTag.Entry>();
		NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); ++i) {
			// Each child node is an <entry> element.
			Node childNode = childNodes.item(i);
			if (childNode instanceof Element) {
				Element entryElement = (Element) childNode;
				entries.add(parseDictionaryEntryTag(entryElement));
			}
		}
		return new DictionaryTag(entries);
		
	}
	
	private static DictionaryTag parseDocument(Document document) {
		Element root = document.getDocumentElement();
		return parseDictionaryTag(root);
	}
	
	/**
	 * Returns a {@link DocumentSchema} parsed from the XML in the given file.
	 * 
	 * @param schemaFile the file containing the XML of the schema
	 * @return the document schema
	 */
	public static DocumentSchema createSchema(File schemaFile) throws BenchmarkException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(schemaFile);
			DictionaryTag root = parseDocument(document);
			return new DocumentSchema(root);
		} catch (ParserConfigurationException e) {
			throw new BenchmarkException(e);
		} catch (SAXException e) {
			throw new BenchmarkException(e);
		} catch (IOException e) {
			throw new BenchmarkException(e);
		}
	}
	
	/**
	 * Returns the JSON for a new document that conforms to the schema.
	 * 
	 * @param generator the generator for values in the document
	 * @return the JSON for the new document
	 */
	public JSONObject getNewDocument(ValueGenerator generator) {
		return getDictionary(root, generator);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		root.toString(sb);
		return sb.toString();
	}
}
