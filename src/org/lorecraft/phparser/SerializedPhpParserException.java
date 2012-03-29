package org.lorecraft.phparser;


public class SerializedPhpParserException extends Exception
{
  /**
   * Serial Version UID
   */
  private static final long serialVersionUID = 2L;

  public static final int NO_CODE_SET = 0;
  /**
   * code if serialized String is to Long (example s:5;"Hello Next"; )
   */
  public static final int TO_LONG_STRING = 1;

  /**
   * code if serialized string is to short (example: input string is
   * latin1 with special char but assumeUTF8 is set true)
   */
  public static final int TO_SHORT_STRING = 2;

  /**
   * code if serialized reference is pointing to a value out of the
   * reference index. (example:
   * a:3:{i:1;i:20;i:2;R:4;i:3;s:5;"Hello";} )
   */
  public static final int OUT_OF_RANG_REFERENCE = 3;

  /**
   * code if serialized array missing closer char }.
   */
  public static final int MISSING_CLOSER_STRING = 4;

  /**
   * code if serialized value missing delimiter ( : or ; )
   */
  public static final int MISSING_DELIMITER_STRING = 5;

  /**
   * code if serialized string is to short (example: s:8;"Hello"; )
   */
  public static final int TO_SHORT_INPUT_STRING = 6;

  /**
   * code if serialized string has a unknown type used ( known types:
   * i, d, b, s, a, O, N, R )
   */
  public static final int UNKNOWN_TYPE = 9;

  /**
   * code if serialized string failed with unknown reason
   */
  public static final int UNEXPECTED_FAIL = 99;

  /**
   *
   */
  public int position = 0;

  /**
   *
   */
  public int code = NO_CODE_SET;

  /**
   *
   */
  public SerializedPhpParserException()
  {
    super();
  }

  /**
   * @param message
   */
  public SerializedPhpParserException(String message)
  {
    super(message);
  }

  /**
   * @param message
   * @param position
   */
  public SerializedPhpParserException(String message, int position)
  {
    super(message + " String Position: " + position);
    this.position = position;
  }

  /**
   * @param message
   * @param position
   * @param cause
   */
  public SerializedPhpParserException(String message, int position,
      Throwable cause)
  {
    super(message + " String Position: " + position, cause);
    this.position = position;
  }

  /**
   * @param message
   * @param position
   * @param code
   */
  public SerializedPhpParserException(String message, int position, int code)
  {
    super(message + " String Position: " + position + " Code: " + code);
    this.position = position;
    this.code = code;
  }

  /**
   * @param message
   * @param position
   * @param code
   * @param cause
   */
  public SerializedPhpParserException(String message, int position, int code,
      Throwable cause)
  {
    super(message + " String Position: " + position + " Code: " + code, cause);
    this.position = position;
    this.code = code;
  }

  /**
   * @param message
   * @param cause
   */
  public SerializedPhpParserException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
