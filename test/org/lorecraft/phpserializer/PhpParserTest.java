package org.lorecraft.phpserializer;
import java.util.Map;

import junit.framework.TestCase;

public class PhpParserTest extends TestCase {

	public void testParseNull() throws Exception {
		String input = "N;";
		PhpParser phpParser = new PhpParser(input);
		Object result = phpParser.parse();
		assertEquals(PhpParser.NULL, result);
	}

	public void testParseInteger() throws Exception {
		assertPrimitive("i:123;", 123);
	}

	public void testParseFloat() throws Exception {
		assertPrimitive("d:123.123;", 123.123d);
	}

	public void testParseBoolean() throws Exception {
		assertPrimitive("b:1;", Boolean.TRUE);
	}

	public void testParseString() throws Exception {
		assertPrimitive("s:6:\"string\";", "string");
	}

	public void testParseArray() throws Exception {
		String input = "a:1:{i:1;i:2;}";
		PhpParser phpParser = new PhpParser(input);
		Object result = phpParser.parse();
		assertTrue(result instanceof Map);
		assertEquals(1, ((Map)result).size());
		assertEquals(2, ((Map)result).get(1));
	}

	public void testParseObject() throws Exception {
		String input = "O:7:\"MyClass\":1:{s:5:\"pippo\";s:4:\"test\";}";
		PhpParser phpParser = new PhpParser(input);
		Object result = phpParser.parse();
		assertTrue(result instanceof PhpParser.PhpObject);
		assertEquals(1, ((PhpParser.PhpObject)result).attributes.size());
		assertEquals("test", ((PhpParser.PhpObject)result).attributes.get("pippo"));

	}

	public void testParseComplexDataStructure() throws Exception {
		String input = "a:2:{i:0;a:8:{s:5:\"class\";O:7:\"MyClass\":1:{s:5:\"pippo\";s:4:\"test\";}i:0;i:1;i:1;d:0.19999998807907104;i:2;b:1;i:3;b:0;i:4;N;i:5;a:1:{i:0;s:42:\"\";\";\";\";\";Œ—TÀR—≈TÃ‘Ò¡L\";\";\";\";\";\";}i:6;O:6:\"Object\":0:{}}i:1;a:8:{s:5:\"class\";O:7:\"MyClass\":1:{s:5:\"pippo\";s:4:\"test\";}i:0;i:1;i:1;d:0.19999998807907104;i:2;b:1;i:3;b:0;i:4;N;i:5;a:1:{i:0;s:42:\"\";\";\";\";\";Œ—TÀR—≈TÃ‘Ò¡L\";\";\";\";\";\";}i:6;O:6:\"Object\":0:{}}}";
		Object result = new PhpParser(input).parse();
	}

	private void assertPrimitive(String input, Object expected) {
		assertEquals(expected, new PhpParser(input).parse());
	}


}
