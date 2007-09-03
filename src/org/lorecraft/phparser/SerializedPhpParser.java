/*
Copyright (c) 2007 Zsolt Szász <zsolt at lorecraft dot com>

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.lorecraft.phparser;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Deserializes a serialized PHP data structure into corresponding Java objects
 *
 */
public class SerializedPhpParser {

	private final String input;

	private int index;

	public SerializedPhpParser(String input) {
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
			throw new IllegalStateException("Encountered unknown type [" + type
					+ "]");
		}
	}

	private Object parseObject() {
		PhpObject phpObject = new PhpObject();
		int strLen = readLength();
		phpObject.name = input.substring(index, index + strLen);
		index = index + strLen + 2;
		int attrLen = readLength();
		for (int i = 0; i < attrLen; i++) {
			Object key = parse();
			Object value = parse();
			phpObject.attributes.put(key, value);
		}
		index++;
		return phpObject;
	}

	private Map<Object, Object> parseArray() {
		int arrayLen = readLength();
		Map<Object, Object> result = new LinkedHashMap<Object, Object>();
		for (int i = 0; i < arrayLen; i++) {
			Object key = parse();
			Object value = parse();
			result.put(key, value);
		}
		index++;
		return result;
	}

	private int readLength() {
		int delimiter = input.indexOf(':', index);
		int arrayLen = Integer.valueOf(input.substring(index, delimiter));
		index = delimiter + 2;
		return arrayLen;
	}

	/**
	 * Assumes strings are utf8 encoded
	 *
	 * @return
	 */
	private String parseString() {
		int strLen = readLength();

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
		String value = input.substring(index, index + utfStrLen);
		index = index + utfStrLen + 2;
		return value;
	}

	private Boolean parseBoolean() {
		int delimiter = input.indexOf(';', index);
		String value = input.substring(index, delimiter);
		if (value.equals("1")) {
			value = "true";
		} else if (value.equals("0")) {
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

	/**
	 * Represents an object that has a name and a map of attributes
	 */
	public static class PhpObject {
		public String name;
		public Map<Object, Object> attributes = new HashMap<Object, Object>();

		@Override
		public String toString() {
			return "\"" + name + "\" : " + attributes.toString();
		}
	}
}
