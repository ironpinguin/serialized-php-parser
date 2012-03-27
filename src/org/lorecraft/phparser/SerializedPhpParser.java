/*
Copyright (c) 2007 Zsolt Sz�sz <zsolt at lorecraft dot com>

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

import java.util.*;
import java.util.regex.Pattern;

/**
 * Deserializes a serialized PHP data structure into corresponding
 * Java objects. It supports the integer, float, boolean, string
 * primitives that are mapped to their Java equivalent, plus arrays
 * that are parsed into <code>Map</code> instances and objects that
 * are represented by {@link SerializedPhpParser.PhpObject} instances.
 * <p>
 * Example of use:
 *
 * <pre>
 * 		String input = "O:8:"TypeName":1:{s:3:"foo";s:3:"bar";}";
 * 		SerializedPhpParser serializedPhpParser = new SerializedPhpParser(input);
 * 	Object result = serializedPhpParser.parse();
 * </pre>
 *
 * The <code>result</code> object will be a <code>PhpObject</code>
 * with the name "TypeName" and the attribute "foo" = "bar".
 */
public class SerializedPhpParser
{

  private final String input;

  private final int inputLength;

  private int index;

  private boolean assumeUTF8 = true;

  private final ArrayList<Object> refArray = new ArrayList<Object>();

  private Pattern acceptedAttributeNameRegex = null;

  public SerializedPhpParser(String input)
  {
    this.input = input;
    this.inputLength = input.length();
  }

  public SerializedPhpParser(String input, boolean assumeUTF8)
  {
    this.input = input;
    this.inputLength = input.length();
    this.assumeUTF8 = assumeUTF8;
  }

  public Object parse() throws SerializedPhpParserException
  {
    Object result = this.parseInternal(false);
    this.cleanup();
    return result;
  }

  private void cleanup()
  {
    this.refArray.clear();
  }

  private Object parseInternal(boolean isKey)
      throws SerializedPhpParserException
  {
    checkUnexpectedLength(this.index + 2);
    char type = this.input.charAt(this.index);
    switch (type)
    {
    case 'i':
      this.index += 2;
      return parseInt(isKey);
    case 'd':
      this.index += 2;
      return parseFloat(isKey);
    case 'b':
      this.index += 2;
      return parseBoolean();
    case 's':
      this.index += 2;
      return parseString(isKey);
    case 'a':
      this.index += 2;
      return parseArray();
    case 'O':
      this.index += 2;
      return parseObject();
    case 'N':
      this.index += 2;
      return NULL;
    case 'R':
      this.index += 2;
      return parseReference();
    default:
      throw new SerializedPhpParserException("Encountered unknown type ["
          + type + "]");
    }
  }

  private Object parseReference() throws SerializedPhpParserException
  {
    int delimiter = this.input.indexOf(';', this.index);
    if (delimiter == -1)
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized Reference!", this.index);
    }
    checkUnexpectedLength(delimiter + 1);
    Integer refIndex = Integer.valueOf(this.input.substring(this.index,
        delimiter)) - 1;
    this.index = delimiter + 1;
    if ((refIndex + 1) > this.refArray.size())
    {
      throw new SerializedPhpParserException("Out of range reference index: "
          + (refIndex + 1) + " !", this.index);
    }
    Object value = this.refArray.get(refIndex);
    this.refArray.add(value);
    return value;
  }

  private Object parseObject() throws SerializedPhpParserException
  {
    PhpObject phpObject = new PhpObject();
    this.refArray.add(phpObject);
    int strLen = readLength();
    checkUnexpectedLength(strLen);
    phpObject.name = this.input.substring(this.index, this.index + strLen);
    this.index = this.index + strLen + 2;
    int attrLen = readLength();
    for (int i = 0; i < attrLen; i++)
    {
      Object key = parseInternal(true);
      Object value = parseInternal(false);
      if (isAcceptedAttribute(key))
      {
        phpObject.attributes.put(key, value);
      }
    }
    this.index++;
    return phpObject;
  }

  private Map<Object, Object> parseArray() throws SerializedPhpParserException
  {
    int arrayLen = readLength();
    checkUnexpectedLength(arrayLen);
    Map<Object, Object> result = new LinkedHashMap<Object, Object>();
    this.refArray.add(result);
    for (int i = 0; i < arrayLen; i++)
    {
      Object key = parseInternal(true);
      Object value = parseInternal(false);
      if (isAcceptedAttribute(key))
      {
        result.put(key, value);
      }
    }
    char endChar = this.input.charAt(this.index);
    if (endChar != '}')
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized Array, missing }!", this.index);
    }

    this.index++;
    return result;
  }

  private boolean isAcceptedAttribute(Object key)
  {
    if (this.acceptedAttributeNameRegex == null)
    {
      return true;
    }
    if (!(key instanceof String))
    {
      return true;
    }
    return this.acceptedAttributeNameRegex.matcher((String) key).matches();
  }

  private int readLength() throws SerializedPhpParserException
  {
    int delimiter = this.input.indexOf(':', this.index);
    if (delimiter == -1)
    {
      throw new SerializedPhpParserException(
          "Missing delimiter after string, array or object length field!",
          this.index);
    }
    checkUnexpectedLength(delimiter + 2);
    int arrayLen = Integer.valueOf(this.input.substring(this.index, delimiter));
    this.index = delimiter + 2;
    return arrayLen;
  }

  /**
   * Assumes strings are utf8 encoded
   *
   * @return
   */
  private String parseString(boolean isKey) throws SerializedPhpParserException
  {
    int strLen = readLength();
    checkUnexpectedLength(strLen);

    int utfStrLen = 0;
    int byteCount = 0;
    int nextCharIndex = 0;
    while (byteCount != strLen)
    {
      nextCharIndex = this.index + utfStrLen++;
      if (nextCharIndex >= this.inputLength)
      {
        throw new SerializedPhpParserException("Unexpected end of String ("
            + strLen + ") at position " + (nextCharIndex - this.index)
            + ", absolut position in Input String: " + nextCharIndex
            + ". The string: "
            + this.input.substring(this.index, nextCharIndex), nextCharIndex);
      }
      char ch = this.input.charAt(nextCharIndex);
      if (this.assumeUTF8)
      {
        if ((ch >= 0x0000) && (ch <= 0x007F))
        {
          byteCount++;
        }
        else if (ch > 0x07FF)
        {
          byteCount += 3;
        }
        else
        {
          byteCount += 2;
        }
      }
      else
      {
        byteCount++;
      }
    }
    String value = this.input.substring(this.index, this.index + utfStrLen);
    if ((this.index + utfStrLen + 2) > this.inputLength
        || (this.index + utfStrLen) > this.inputLength)
    {
      throw new SerializedPhpParserException(
          "Unexpected serialized string length!", this.index);
    }
    String endString = this.input.substring(this.index + utfStrLen, this.index
        + utfStrLen + 2);
    if (!endString.equals("\";"))
    {
      throw new SerializedPhpParserException(
          "Unexpected serialized string length!", this.index);
    }
    this.index = this.index + utfStrLen + 2;
    if (!isKey)
    {
      this.refArray.add(value);
    }
    return value;
  }

  private Boolean parseBoolean() throws SerializedPhpParserException
  {
    int delimiter = this.input.indexOf(';', this.index);
    if (delimiter == -1)
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized boolean!", this.index);
    }
    checkUnexpectedLength(delimiter + 1);
    String value = this.input.substring(this.index, delimiter);
    if (value.equals("1"))
    {
      value = "true";
    }
    else if (value.equals("0"))
    {
      value = "false";
    }
    this.index = delimiter + 1;
    this.refArray.add(Boolean.valueOf(value));
    return Boolean.valueOf(value);
  }

  private Double parseFloat(boolean isKey) throws SerializedPhpParserException
  {
    int delimiter = this.input.indexOf(';', this.index);
    if (delimiter == -1)
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized float!", this.index);
    }
    checkUnexpectedLength(delimiter + 1);
    String value = this.input.substring(this.index, delimiter);
    this.index = delimiter + 1;
    if (!isKey)
    {
      this.refArray.add(Double.valueOf(value));
    }
    return Double.valueOf(value);
  }

  private Long parseInt(boolean isKey) throws SerializedPhpParserException
  {
    int delimiter = this.input.indexOf(';', this.index);
    if (delimiter == -1)
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized integer!", this.index);
    }
    checkUnexpectedLength(delimiter + 1);
    String value = this.input.substring(this.index, delimiter);
    this.index = delimiter + 1;
    if (!isKey)
    {
      this.refArray.add(Long.valueOf(value));
    }
    return Long.valueOf(value);
  }

  public void setAcceptedAttributeNameRegex(String acceptedAttributeNameRegex)
  {
    this.acceptedAttributeNameRegex = Pattern
        .compile(acceptedAttributeNameRegex);
  }

  public static final Object NULL = new Object()
  {
    @Override
    public String toString()
    {
      return "NULL";
    }
  };

  /**
   * Represents an object that has a name and a map of attributes
   */
  public static class PhpObject
  {
    public String name;

    public Map<Object, Object> attributes = new HashMap<Object, Object>();

    @Override
    public String toString()
    {
      return "\"" + this.name + "\" : " + this.attributes.toString();
    }
  }

  private void checkUnexpectedLength(int newIndex)
      throws SerializedPhpParserException
  {
    if (this.index > this.inputLength || newIndex > this.inputLength)
    {
      throw new SerializedPhpParserException(
          "Unexpected end of serialized Input!", this.index);
    }
  }

}
