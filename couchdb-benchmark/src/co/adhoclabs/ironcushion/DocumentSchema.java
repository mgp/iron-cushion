package co.adhoclabs.ironcushion;

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
	private static abstract class Value {
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
	 * A value for {@link JSONObject}.
	 */
	private static final class ObjectValue extends Value {
		private static final class Entry {
			private final String name;
			private final Value value;
			
			private Entry(String name, Value value) {
				this.name = name;
				this.value = value;
			}
		}
		
		private final List<Entry> entries;
		
		private ObjectValue(List<Entry> entries) {
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
	 * A value for {@link JSONArray}.
	 */
	private static final class ArrayValue extends Value {
		private final List<Value> elements;
		
		private ArrayValue(List<Value> elements) {
			this.elements = elements;
		}
		
		public Type getType() {
			return Type.ARRAY;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("[");
			int index = 0;
			final int lastIndex = elements.size() - 1;
			for (Value element : elements) {
				element.toString(sb);
				if (index++ < lastIndex) {
					sb.append(", ");
				}
			}
			sb.append("]");
		}
	}
	
	/**
	 * A value for {@link String}.
	 */
	private static final class StringValue extends Value {
		private static final StringValue INSTANCE = new StringValue();
		
		public Type getType() {
			return Type.STRING;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("string");
		}
	}
	
	/**
	 * A value for {@link Boolean}.
	 */
	private static final class BooleanValue extends Value {
		private static final BooleanValue INSTANCE = new BooleanValue();
		
		public Type getType() {
			return Type.BOOLEAN;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("boolean");
		}
	}
	
	/**
	 * A value for {@link Integer}.
	 */
	private static final class IntegerValue extends Value {
		private static final IntegerValue INSTANCE = new IntegerValue();
		
		public Type getType() {
			return Type.INTEGER;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("integer");
		}
	}
	
	/**
	 * A value for {@link Float}.
	 */
	private static final class FloatValue extends Value {
		private static final FloatValue INSTANCE = new FloatValue();
		
		public Type getType() {
			return Type.FLOAT;
		}
		
		public void toString(StringBuilder sb) {
			sb.append("float");
		}
	}
	
	/**
	 * A value for {@code null}. 
	 */
	private static final class NullValue extends Value {
		private static final NullValue INSTANCE = new NullValue();
		
		public Type getType() {
			return Type.NULL;
		}
		
		public void toString(StringBuilder sb ) {
			sb.append("null");
		}
	}
	
	@SuppressWarnings("unchecked")
	private final JSONArray getArray(ArrayValue value, ValueGenerator generator) {
		JSONArray array = new JSONArray();
		for (Value element : value.elements) {
			array.add(getObject(element, generator));
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	private final JSONObject getObject(ObjectValue value, ValueGenerator generator) {
		JSONObject object = new JSONObject();
		for (ObjectValue.Entry entry : value.entries) {
			object.put(entry.name, getObject(entry.value, generator));
		}
		return object;
	}
	
	private final Object getObject(Value value, ValueGenerator generator) {
		switch (value.getType()) {
		case ARRAY:
			return getArray((ArrayValue) value, generator);
		case OBJECT:
			return getObject((ObjectValue) value, generator);
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
	
	private final ObjectValue root;
	
	private DocumentSchema(ObjectValue root) {
		this.root = root;
	}
	
	/**
	 * Private namespace for methods that parse XML.
	 */
	private static final class XmlParser {
		private static Value parseTag(Element element) {
			if (element.getTagName().equals("array")) {
				return parseArrayTag(element);
			} else if (element.getTagName().equals("object")) {
				return parseObjectTag(element);
			} else if (element.getTagName().equals("string")) {
				return StringValue.INSTANCE;
			} else if (element.getTagName().equals("integer")) {
				return IntegerValue.INSTANCE;
			} else if (element.getTagName().equals("float")) {
				return FloatValue.INSTANCE;
			} else if (element.getTagName().equals("boolean")) {
				return BooleanValue.INSTANCE;
			} else if (element.getTagName().equals("null")) {
				return NullValue.INSTANCE;
			}
			return null;
		}
		
		private static ArrayValue parseArrayTag(Element element) {
			List<Value> elements = new LinkedList<Value>();
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
			return new ArrayValue(elements);
		}
		
		private static ObjectValue.Entry parseObjectEntryTag(Element element) {
			String name = null;
			Value value = null;
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
			return new ObjectValue.Entry(name, value);
		}
		
		private static ObjectValue parseObjectTag(Element element) {
			List<ObjectValue.Entry> entries = new LinkedList<DocumentSchema.ObjectValue.Entry>();
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); ++i) {
				// Each child node is an <entry> element.
				Node childNode = childNodes.item(i);
				if (childNode instanceof Element) {
					Element entryElement = (Element) childNode;
					entries.add(parseObjectEntryTag(entryElement));
				}
			}
			return new ObjectValue(entries);
		}
		
		/**
		 * Given the parsed XML {@link Document}, returns the root {@link ObjectValue}. 
		 * 
		 * @param document the parsed XML document
		 * @return the root JSON object
		 */
		private static ObjectValue parseDocument(Document document) {
			Element root = document.getDocumentElement();
			return parseObjectTag(root);
		}
	}
	
	/**
	 * Private namespace for methods tat parse JSON.
	 */
	private static final class JsonParser {
		private static Value parseValue(Object object) {
			if (object == null) {
				return NullValue.INSTANCE;
			} else if (object instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject) object;
				return parseObjectValue(jsonObject);
			} else if (object instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) object;
				return parseArrayValue(jsonArray);
			} else if (object instanceof String) {
				return StringValue.INSTANCE;
			} else if (object instanceof Integer) {
				return IntegerValue.INSTANCE;
			} else if (object instanceof Float) {
				return FloatValue.INSTANCE;
			} else if (object instanceof Boolean) {
				return BooleanValue.INSTANCE;
			}
			return null;
		}
		
		private static ArrayValue parseArrayValue(JSONArray json) {
			List<Value> elements = new ArrayList<Value>(json.size());
			for (Object jsonElement : json) {
				elements.add(parseValue(jsonElement));
			}
			return new ArrayValue(elements);
		}
		
		@SuppressWarnings("unchecked")
		private static ObjectValue parseObjectValue(JSONObject json) {
			List<ObjectValue.Entry> entries = new ArrayList<DocumentSchema.ObjectValue.Entry>(json.size());
			Set<Map.Entry<String, Object>> entrySet = json.entrySet();
			for (Map.Entry<String, Object> entry : entrySet) {
				String name = entry.getKey();
				Value value = parseValue(entry.getValue());
				entries.add(new ObjectValue.Entry(name, value));
			}
			return new ObjectValue(entries);
		}
		
		/**
		 * Given the root {@link JSONObject} of the parsed JSON, returns the root {@link ObjectValue}. 
		 * 
		 * @param jsonRoot the root JSON object
		 * @return the root JSON object
		 */
		private static ObjectValue parseJson(JSONObject jsonRoot) {
			return parseObjectValue(jsonRoot);
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
			ObjectValue root = XmlParser.parseDocument(document);
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
			ObjectValue root = JsonParser.parseJson(json);
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
