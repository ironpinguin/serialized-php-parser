package org.lorecraft.phparser;


@SuppressWarnings("serial")
public class SerializedPhpParserException extends Exception
{
  public int position;

  public SerializedPhpParserException()
  {
    super();
  }

  public SerializedPhpParserException(String message)
  {
    super(message);
  }

  public SerializedPhpParserException(String message, int position)
  {
    super(message + " String Position: " + position);
    this.position = position;
  }

  public SerializedPhpParserException(String message, int position,
      Throwable cause)
  {
    super(message + " String Position: " + position, cause);
    this.position = position;
  }

  public SerializedPhpParserException(String message, Throwable cause)
  {
    super(message, cause);
  }

}
