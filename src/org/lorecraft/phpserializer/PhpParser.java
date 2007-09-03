package org.lorecraft.phpserializer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PhpParser {

	private final String input;
	
	private int index;

	public PhpParser(String input) {
		this.input = input;
	}

	public Object parse() {
		char type = input.charAt(index);
		switch (type) {
		case 'i':
			index += 2;
			return parseInt();
		case 'd':
			index += 2;
			return parseFloat();
		case 'b':
			index += 2;
			return parseBoolean();
		case 's':
			index += 2;
			return parseString();
		case 'a':
			index += 2;
			return parseArray();
		case 'O':
			index += 2;
			return parseObject();
		case 'N':
			index += 2;
			return NULL;
		default:
			throw new IllegalStateException("Encountered unknown type [" + type + "]");
		}
	}

	private Object parseObject() {
		PhpObject phpObject = new PhpObject();
		int delimiter = input.indexOf(':', index);
		int strLen = Integer.valueOf(input.substring(index, delimiter));
		index = delimiter + 2;		
		phpObject.name = input.substring(index, index + strLen);
		index = index + strLen + 2;
		delimiter = input.indexOf(':', index);
		int attrLen = Integer.valueOf(input.substring(index, delimiter));
		index = delimiter + 2;
		for (int i = 0; i < attrLen; i++) {
			Object key = parse();
			Object value = parse();
			phpObject.attributes.put(key, value);
		}
		index++;
		return phpObject;
	}

	private Map<Object, Object> parseArray() {
		int delimiter = input.indexOf(':', index);
		int arrayLen = Integer.valueOf(input.substring(index, delimiter));
		index = delimiter + 2;
		Map<Object, Object> result = new LinkedHashMap<Object, Object>();
		for (int i = 0; i < arrayLen; i++) {
			Object key = parse();
			Object value = parse();
			result.put(key, value);
		}
		index++;
		return result;
	}

	/**
	 * Assumes strings are utf8 encoded
	 * @return
	 */
	private String parseString() {
		int delimiter = input.indexOf(':', index);
		String value = input.substring(index, delimiter);
		int strLen = Integer.valueOf(value); // this is in bytes
		index = delimiter + 2;
		
		int utfStrLen = 0;
		int byteCount = 0;
		while (byteCount != strLen) {
			char ch = input.charAt(index + utfStrLen++);
			if ((ch >= 0x0001) && (ch <= 0x007F)) {
				byteCount++;
			} else if (byteCount > 0x07FF) {
				byteCount += 3;
			} else {
				byteCount += 2;
			}						
		}
		value = input.substring(index, index + utfStrLen);
		index = index + utfStrLen + 2;
		return value;
	}

	private Boolean parseBoolean() {
		int delimiter = input.indexOf(';', index);
		String value = input.substring(index, delimiter);
		if (value.equals("1")) {
			value = "true";
		} else if(value.equals("0")) {
			value = "false";
		}
		index = delimiter + 1;
		return Boolean.valueOf(value);
	}

	private Double parseFloat() {
		int delimiter = input.indexOf(';', index);
		String value = input.substring(index, delimiter);
		index = delimiter + 1;
		return Double.valueOf(value);
	}

	private Integer parseInt() {
		int delimiter = input.indexOf(';', index);
		String value = input.substring(index, delimiter);
		index = delimiter + 1;
		return Integer.valueOf(value);
	}
	
	public static final Object NULL = new Object() {
		@Override
		public String toString() {
			return "NULL";
		}
	};

	public static class PhpObject {
		public String name;
		public Map<Object, Object> attributes = new HashMap<Object, Object>();
		
		@Override
		public String toString() {
			return "\"" + name + "\" : " + attributes.toString();			
		}
	}
}
