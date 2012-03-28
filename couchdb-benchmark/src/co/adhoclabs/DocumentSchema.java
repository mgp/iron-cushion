package co.adhoclabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
			OBJECT,
			ARRAY,
			STRING,
			BOOLEAN,
			INTEGER,
			FLOAT,
			NULL
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
	 * A tag for {@link JSONObject}.
	 */
	private static final class ObjectTag extends Tag {
		private static final class Entry {
			private final String name;
			private final Tag value;
			
			private Entry(String name, Tag value) {
				this.name = name;
				this.value = value;
			}
		}
		
		private final List<Entry> entries;
		
		private ObjectTag(List<Entry> entries) {
			this.entries = entries;
		}
		
		public Type getType() {
			return Type.OBJECT;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("{");
			int index = 0;
			final int lastIndex = entries.size() - 1;
			for (Entry entry : entries) {
				sb.append(entry.name).append(": ");
				entry.value.toString(sb);
				if (index++ < lastIndex) {
					sb.append(", ");
				}
			}
			sb.append("}");
		}
	}
	
	/**
	 * A tag for {@link JSONArray}.
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
	 * A tag for {@link String}.
	 */
	private static final class StringTag extends Tag {
		private static final StringTag INSTANCE = new StringTag();
		
		public Type getType() {
			return Type.STRING;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("string");
		}
	}
	
	/**
	 * A tag for {@link Boolean}.
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
	 * A tag for {@link Integer}.
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
	 * A tag for {@link Float}.
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
	
	/**
	 * A tag for {@code null}. 
	 */
	private static final class NullTag extends Tag {
		private static final NullTag INSTANCE = new NullTag();
		
		public Type getType() {
			return Type.NULL;
		}
		
		public void toString(StringBuilder sb ) {
			sb.append("null");
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
	private final JSONObject getObject(ObjectTag tag, ValueGenerator generator) {
		JSONObject object = new JSONObject();
		for (ObjectTag.Entry entry : tag.entries) {
			Object value = getObject(entry.value, generator);
			object.put(entry.name, value);
		}
		return object;
	}
	
	private final Object getObject(Tag tag, ValueGenerator generator) {
		switch (tag.getType()) {
		case ARRAY:
			return getArray((ArrayTag) tag, generator);
		case OBJECT:
			return getObject((ObjectTag) tag, generator);
		case STRING:
			return generator.nextString();
		case BOOLEAN:
			return generator.nextBoolean();
		case INTEGER:
			return generator.nextInt();
		case FLOAT:
			return generator.nextFloat();
		case NULL:
			return null;
		default:
			break;
		}
		return null;
	}
	
	private final ObjectTag root;
	
	private DocumentSchema(ObjectTag root) {
		this.root = root;
	}
	
	/**
	 * Private namespace for methods that parse XML.
	 */
	private static final class XmlParser {
		private static Tag parseTag(Element element) {
			if (element.getTagName().equals("array")) {
				return parseArrayTag(element);
			} else if (element.getTagName().equals("object")) {
				return parseObjectTag(element);
			} else if (element.getTagName().equals("string")) {
				return StringTag.INSTANCE;
			} else if (element.getTagName().equals("integer")) {
				return IntegerTag.INSTANCE;
			} else if (element.getTagName().equals("float")) {
				return FloatTag.INSTANCE;
			} else if (element.getTagName().equals("boolean")) {
				return BooleanTag.INSTANCE;
			} else if (element.getTagName().equals("null")) {
				return NullTag.INSTANCE;
			}
			return null;
		}
		
		private static ArrayTag parseArrayTag(Element element) {
			List<Tag> elements = new LinkedList<Tag>();
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
		
		private static ObjectTag.Entry parseObjectEntryTag(Element element) {
			String name = null;
			Tag value = null;
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); ++i) {
				Node childNode = childNodes.item(i);
				if (childNode instanceof Element) {
					Element childNodeElement = (Element) childNode;
					if (childNodeElement.getTagName().equals("name")) {
						name = childNodeElement.getChildNodes().item(0).getNodeValue();
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
			return new ObjectTag.Entry(name, value);
		}
		
		private static ObjectTag parseObjectTag(Element element) {
			List<ObjectTag.Entry> entries = new LinkedList<DocumentSchema.ObjectTag.Entry>();
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); ++i) {
				// Each child node is an <entry> element.
				Node childNode = childNodes.item(i);
				if (childNode instanceof Element) {
					Element entryElement = (Element) childNode;
					entries.add(parseObjectEntryTag(entryElement));
				}
			}
			return new ObjectTag(entries);
		}
		
		/**
		 * Given the parsed XML {@link Document}, returns the root {@link ObjectTag}. 
		 * 
		 * @param document the parsed XML document
		 * @return the root JSON object
		 */
		private static ObjectTag parseDocument(Document document) {
			Element root = document.getDocumentElement();
			return parseObjectTag(root);
		}
	}
	
	/**
	 * Private namespace for methods tat parse JSON.
	 */
	private static final class JsonParser {
		private static Tag parseTag(Object object) {
			if (object == null) {
				return NullTag.INSTANCE;
			} else if (object instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject) object;
				return parseObjectTag(jsonObject);
			} else if (object instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) object;
				return parseArrayTag(jsonArray);
			} else if (object instanceof String) {
				return StringTag.INSTANCE;
			} else if (object instanceof Integer) {
				return IntegerTag.INSTANCE;
			} else if (object instanceof Float) {
				return FloatTag.INSTANCE;
			} else if (object instanceof Boolean) {
				return BooleanTag.INSTANCE;
			}
			return null;
		}
		
		private static ArrayTag parseArrayTag(JSONArray json) {
			List<Tag> elements = new ArrayList<Tag>(json.size());
			for (Object jsonElement : json) {
				elements.add(parseTag(jsonElement));
			}
			return new ArrayTag(elements);
		}
		
		@SuppressWarnings("unchecked")
		private static ObjectTag parseObjectTag(JSONObject json) {
			List<ObjectTag.Entry> entries = new ArrayList<DocumentSchema.ObjectTag.Entry>(json.size());
			Set<Map.Entry<String, Object>> entrySet = json.entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				String name = entry.getKey();
				Tag value = parseTag(entry.getValue());
				entries.add(new ObjectTag.Entry(name, value));
			}
			return new ObjectTag(entries);
		}
		
		/**
		 * Given the root {@link JSONObject} of the parsed JSON, returns the root {@link ObjectTag}. 
		 * 
		 * @param jsonRoot the root JSON object
		 * @return the root JSON object
		 */
		private static ObjectTag parseJson(JSONObject jsonRoot) {
			return parseObjectTag(jsonRoot);
		}
	}
	
	/**
	 * Returns a {@link DocumentSchema} parsed from the XML in the given file.
	 * 
	 * @param schemaFile the file containing the XML of the schema
	 * @return the document schema
	 * @throws BenchmarkException if the file could not be parsed
	 */
	public static DocumentSchema createSchemaFromXml(File schemaFile) throws BenchmarkException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(schemaFile);
			ObjectTag root = XmlParser.parseDocument(document);
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
	 * Returns a {@link DocumentSchema} parsed from the JSON in the given file.
	 * 
	 * @param schemaFile the file containing the JSON of the schema
	 * @return the document schema
	 * @throws BenchmarkException if the file could not be parsed
	 */
	public static DocumentSchema createSchemaFromJson(File schemaFile) throws BenchmarkException {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(schemaFile));
			JSONParser jsonParser = new JSONParser();
			JSONObject json = (JSONObject) jsonParser.parse(bufferedReader);
			ObjectTag root = JsonParser.parseJson(json);
			return new DocumentSchema(root);
		} catch (IOException e) {
			throw new BenchmarkException(e);
		} catch (ParseException e) {
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
		return getObject(root, generator);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		root.toString(sb);
		return sb.toString();
	}
}
