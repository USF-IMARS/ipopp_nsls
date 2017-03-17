/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  NISGS/NSLS
*
*  History:
*
*  28-Sep-05, 	Original version.
*   3-Jan-06, 	Fixed javadoc comments.
*  23-Aug-06, 	Host no longer determined here.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.io.*;
import java.util.*;

public class LogEvent implements Serializable {
  private Date date;
  private int level;
  private String host;
  private Log.Source source;
  private Log.Parameters parameters;
  private String threadName;
  private String[] stackTrace;
  private String text;
  private Throwable throwable;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * 
  ****************************************************************************/
  public LogEvent (int level, Log.Source source,
  		   Log.Parameters parameters, String threadName,
		   String[] stackTrace, String text, Throwable throwable) {
    date = new Date();
    this.level = level;
    this.host = null;
    this.source = source;
    this.parameters = parameters;
    this.threadName = threadName;
    this.stackTrace = stackTrace;
    this.text = text;
    this.throwable = throwable;
    /* THIS IS NOW DONE IN THE 'Log' OBJECT.
    try {
      InetAddress addr = InetAddress.getLocalHost();
      if (addr.isLoopbackAddress()) {
        host = null;
      } else {
        host = addr.getHostName();
      }
    } catch (Exception e) {
      host = null;
    }
    */
  }
  /**************************************************************************
  * setHost.
  **************************************************************************/
  public void setHost (String host) {
    this.host = host;
  }
  /**************************************************************************
  * @return The date/time at which the event occurred.
  **************************************************************************/
  public Date getDate () {
    return date;
  }
  /**************************************************************************
  * @return The severity level of the event.
  **************************************************************************/
  public int getLevel () {
    return level;
  }
  /**************************************************************************
  * @return The host machine on which the event occurred.
  **************************************************************************/
  public String getHost () {
    return host;
  }
  /**************************************************************************
  * @return The source of the event.  If <TT>null</TT>, no source was
  * specified.
  **************************************************************************/
  public Log.Source getSource () {
    return source;
  }
  /**************************************************************************
  * @return The event parameters.  If <TT>null</TT>, no parameters were
  * specified.
  **************************************************************************/
  public Log.Parameters getParameters () {
    return parameters;
  }
  /**************************************************************************
  * @return The thread that was executing when the event occurred.
  **************************************************************************/
  public String getThreadName () {
    return threadName;
  }
  /**************************************************************************
  * @return The stack trace at the moment the event occurred.
  **************************************************************************/
  public String[] getStackTrace () {
    return stackTrace;
  }
  /**************************************************************************
  * @return Text describing the event.  If <TT>null</TT>, no text was
  * specified.
  **************************************************************************/
  public String getText () {
    return text;
  }
  /**************************************************************************
  * @return The Throwable (eg. Exception) that occurred.  Or <TT>null</TT>
  * if no Throwable occurred.
  **************************************************************************/
  public Throwable getThrowable () {
    return throwable;
  }
  /****************************************************************************
  * @return A string representation of the event.
  ****************************************************************************/
  public String toString () {
    StringBuffer txt = new StringBuffer();
    txt.append(Util.encodeDate(date));
    txt.append(", " + encodeLevel(level));
    if (host != null) {
      txt.append(", " + host);
    }
    if (source != null) {
      txt.append(", " + source.toString());
    }
    if (parameters != null) {
      txt.append(", " + parameters.toString());
    }
    txt.append(", " + threadName);
    txt.append(", ");
    for (int i = 0; i < stackTrace.length; i++) {
       txt.append("|" + stackTrace[i] + "|");
    }
    if (text != null) {
      txt.append(", " + text);
    }
    if (throwable != null) {
      txt.append(", " + Util.encodeThrowable(throwable));
    }
    return txt.toString();
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public String encodeLevel () {
    return encodeLevel(level);
  }
  private String encodeLevel (int level) {
    switch (level) {
      case Log.INFO_EVENT: {
      	return "INFO";
      }
      case Log.WARNING_EVENT: {
      	return "WARNING";
      }
      case Log.ERROR_EVENT: {
      	return "ERROR";
      }
      default: {
      	return "?";
      }
    }
  }
}
