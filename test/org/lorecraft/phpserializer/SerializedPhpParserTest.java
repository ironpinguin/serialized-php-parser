package org.lorecraft.phpserializer;
import java.util.Map;

import junit.framework.TestCase;

import org.lorecraft.phparser.SerializedPhpParser;

public class SerializedPhpParserTest extends TestCase {

	public void testParseNull() throws Exception {
		String input = "N;";
		SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
		Object result = serializedPhpParser.parse();
		assertEquals(SerializedPhpParser.NULL, result);
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
		SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
		Object result = serializedPhpParser.parse();
		assertTrue(result instanceof Map);
		assertEquals(1, ((Map)result).size());
		assertEquals(2, ((Map)result).get(1));
	}

	public void testParseObject() throws Exception {
		String input = "O:8:\"TypeName\":1:{s:3:\"foo\";s:3:\"bar\";}";
		SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
		Object result = serializedPhpParser.parse();
		assertTrue(result instanceof SerializedPhpParser.PhpObject);
		assertEquals(1, ((SerializedPhpParser.PhpObject)result).attributes.size());
		assertEquals("bar", ((SerializedPhpParser.PhpObject)result).attributes.get("foo"));

	}

	public void testParseComplexDataStructure() throws Exception {
		String input = "a:2:{i:0;a:8:{s:5:\"class\";O:7:\"MyClass\":1:{s:5:\"pippo\";s:4:\"test\";}i:0;i:1;i:1;d:0.19999998807907104;i:2;b:1;i:3;b:0;i:4;N;i:5;a:1:{i:0;s:42:\"\";\";\";\";\";ÎÑTËRÑÅTÌÔñÁL\";\";\";\";\";\";}i:6;O:6:\"Object\":0:{}}i:1;a:8:{s:5:\"class\";O:7:\"MyClass\":1:{s:5:\"pippo\";s:4:\"test\";}i:0;i:1;i:1;d:0.19999998807907104;i:2;b:1;i:3;b:0;i:4;N;i:5;a:1:{i:0;s:42:\"\";\";\";\";\";ÎÑTËRÑÅTÌÔñÁL\";\";\";\";\";\";}i:6;O:6:\"Object\":0:{}}}";
		new SerializedPhpParser(input).parse();

		// sample output of a yahoo web image search api call
		input = "a:1:{s:9:\"ResultSet\";a:4:{s:21:\"totalResultsAvailable\";s:7:\"1177824\";s:20:\"totalResultsReturned\";" +
				"i:2;s:19:\"firstResultPosition\";i:1;s:6:\"Result\";a:2:{i:0;a:10:{s:5:\"Title\";s:12:\"corvette.jpg\";" +
				"s:7:\"Summary\";s:150:\"bluefirebar.gif 03-Nov-2003 19:02 22k burning_frax.jpg 05-Jul-2002 14:34 169k corvette.jpg " +
				"21-Jan-2004 01:13 101k coupleblack.gif 03-Nov-2003 19:00 3k\";s:3:\"Url\";" +
				"s:48:\"http://www.vu.union.edu/~jaquezk/MG/corvette.jpg\";s:8:\"ClickUrl\";" +
				"s:48:\"http://www.vu.union.edu/~jaquezk/MG/corvette.jpg\";s:10:\"RefererUrl\";" +
				"s:35:\"http://www.vu.union.edu/~jaquezk/MG\";s:8:\"FileSize\";" +
				"s:7:\"101.5kB\";s:10:\"FileFormat\";s:4:\"jpeg\";s:6:\"Height\";s:3:\"768\";" +
				"s:5:\"Width\";s:4:\"1024\";s:9:\"Thumbnail\";a:3:{s:3:\"Url\";s:42:\"http://sp1.mm-a1.yimg.com/image/2178288556\";" +
				"s:6:\"Height\";s:3:\"120\";s:5:\"Width\";s:3:\"160\";}}i:1;a:10:{s:5:\"Title\";" +
				"s:23:\"corvette_c6_mini_me.jpg\";s:7:\"Summary\";s:48:\"Corvette I , Corvette II , Diablo , Enzo , Lotus\";" +
				"s:3:\"Url\";s:54:\"http://www.ku4you.com/minicars/corvette_c6_mini_me.jpg\";s:8:\"ClickUrl\";" +
				"s:54:\"http://www.ku4you.com/minicars/corvette_c6_mini_me.jpg\";s:10:\"RefererUrl\";" +
				"s:61:\"http://mik-blog.blogspot.com/2005_03_01_mik-blog_archive.html\";s:8:\"FileSize\";s:4:\"55kB\";" +
				"s:10:\"FileFormat\";s:4:\"jpeg\";s:6:\"Height\";s:3:\"518\";s:5:\"Width\";s:3:\"700\";" +
				"s:9:\"Thumbnail\";a:3:{s:3:\"Url\";s:42:\"http://sp1.mm-a2.yimg.com/image/2295545420\";" +
				"s:6:\"Height\";s:3:\"111\";s:5:\"Width\";s:3:\"150\";}}}}}";
		Map results = (Map)new SerializedPhpParser(input).parse();
		assertEquals(2, ((Map)((Map)results.get("ResultSet")).get("Result")).size());
	}

	private void assertPrimitive(String input, Object expected) {
		assertEquals(expected, new SerializedPhpParser(input).parse());
	}
	
	public void testParseStructureWithSpecialChars() throws Exception {
		String input = "a:1:{i:0;O:9:\"albumitem\":19:{s:5:\"image\";O:5:\"image\":12:{s:4:\"name\";" +
				"s:26:\"top_story_promo_transition\";s:4:\"type\";s:3:\"png\";s:5:\"width\";i:640;" +
				"s:6:\"height\";i:212;s:11:\"resizedName\";s:32:\"top_story_promo_transition.sized\";" +
				"s:7:\"thumb_x\";N;s:7:\"thumb_y\";N;s:11:\"thumb_width\";N;s:12:\"thumb_height\";N;" +
				"s:9:\"raw_width\";i:900;s:10:\"raw_height\";i:298;s:7:\"version\";i:37;}s:9:\"thumbnail\";O:5:\"image\":12:{s:4:\"name\";" +
				"s:32:\"top_story_promo_transition.thumb\";s:4:\"type\";s:3:\"png\";s:5:\"width\";i:150;s:6:\"height\";" +
				"i:50;s:11:\"resizedName\";N;s:7:\"thumb_x\";N;s:7:\"thumb_y\";N;s:11:\"thumb_width\";" +
				"N;s:12:\"thumb_height\";N;s:9:\"raw_width\";i:150;s:10:\"raw_height\";i:50;s:7:\"version\";i:37;}s:7:\"preview\";" +
				"N;s:7:\"caption\";s:6:\"supérb\";s:6:\"hidden\";N;s:9:\"highlight\";b:1;s:14:\"highlightImage\";O:5:\"image\":12:{s:4:\"name\";" +
				"s:36:\"top_story_promo_transition.highlight\";s:4:\"type\";s:3:\"png\";s:5:\"width\";i:150;s:6:\"height\";i:50;" +
				"s:11:\"resizedName\";N;s:7:\"thumb_x\";N;s:7:\"thumb_y\";N;s:11:\"thumb_width\";N;s:12:\"thumb_height\";N;s:9:\"raw_width\";" +
				"i:150;s:10:\"raw_height\";i:50;s:7:\"version\";i:37;}s:11:\"isAlbumName\";N;s:6:\"clicks\";N;s:8:\"keywords\";s:0:\"\";" +
				"s:8:\"comments\";N;s:10:\"uploadDate\";i:1196339460;s:15:\"itemCaptureDate\";i:1196339460;s:8:\"exifData\";N;s:5:\"owner\";" +
				"s:20:\"1156837966_352721747\";s:11:\"extraFields\";a:1:{s:11:\"Description\";s:0:\"\";}s:4:\"rank\";N;s:7:\"version\";i:37;s:7:\"emailMe\";N;}}";
		Map results = (Map)new SerializedPhpParser(input, false).parse();
		assertTrue(results.toString().indexOf("supérb") > 0);
	}
	
	public void testAcceptedAttributeNames() throws Exception {
		// sample output of a yahoo web image search api call
		String input = "a:1:{s:9:\"ResultSet\";a:4:{s:21:\"totalResultsAvailable\";s:7:\"1177824\";s:20:\"totalResultsReturned\";" +
			"i:2;s:19:\"firstResultPosition\";i:1;s:6:\"Result\";a:2:{i:0;a:10:{s:5:\"Title\";s:12:\"corvette.jpg\";" +
			"s:7:\"Summary\";s:150:\"bluefirebar.gif 03-Nov-2003 19:02 22k burning_frax.jpg 05-Jul-2002 14:34 169k corvette.jpg " +
			"21-Jan-2004 01:13 101k coupleblack.gif 03-Nov-2003 19:00 3k\";s:3:\"Url\";" +
			"s:48:\"http://www.vu.union.edu/~jaquezk/MG/corvette.jpg\";s:8:\"ClickUrl\";" +
			"s:48:\"http://www.vu.union.edu/~jaquezk/MG/corvette.jpg\";s:10:\"RefererUrl\";" +
			"s:35:\"http://www.vu.union.edu/~jaquezk/MG\";s:8:\"FileSize\";" +
			"s:7:\"101.5kB\";s:10:\"FileFormat\";s:4:\"jpeg\";s:6:\"Height\";s:3:\"768\";" +
			"s:5:\"Width\";s:4:\"1024\";s:9:\"Thumbnail\";a:3:{s:3:\"Url\";s:42:\"http://sp1.mm-a1.yimg.com/image/2178288556\";" +
			"s:6:\"Height\";s:3:\"120\";s:5:\"Width\";s:3:\"160\";}}i:1;a:10:{s:5:\"Title\";" +
			"s:23:\"corvette_c6_mini_me.jpg\";s:7:\"Summary\";s:48:\"Corvette I , Corvette II , Diablo , Enzo , Lotus\";" +
			"s:3:\"Url\";s:54:\"http://www.ku4you.com/minicars/corvette_c6_mini_me.jpg\";s:8:\"ClickUrl\";" +
			"s:54:\"http://www.ku4you.com/minicars/corvette_c6_mini_me.jpg\";s:10:\"RefererUrl\";" +
			"s:61:\"http://mik-blog.blogspot.com/2005_03_01_mik-blog_archive.html\";s:8:\"FileSize\";s:4:\"55kB\";" +
			"s:10:\"FileFormat\";s:4:\"jpeg\";s:6:\"Height\";s:3:\"518\";s:5:\"Width\";s:3:\"700\";" +
			"s:9:\"Thumbnail\";a:3:{s:3:\"Url\";s:42:\"http://sp1.mm-a2.yimg.com/image/2295545420\";" +
			"s:6:\"Height\";s:3:\"111\";s:5:\"Width\";s:3:\"150\";}}}}}";

		SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
		serializedPhpParser.setAcceptedAttributeNameRegex("ResultSet|totalResultsReturned");
		Object result = serializedPhpParser.parse();
		// available
		assertEquals(2, ((Map)((Map)result).get("ResultSet")).get("totalResultsReturned"));
		// not available
		assertNull(((Map)((Map)result).get("ResultSet")).get("totalResultsAvailable"));
	}

}
