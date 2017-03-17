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
*  13-Oct-05, 	Original version.
*  20-Sep-06, 	Increase "server ready" interval.
*  20-Feb-06, 	Added min/max for Date objects.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.text.*;
import java.util.*;

public abstract class Util {
  private static final SimpleDateFormat utcFmt = new DateFormatUTC("yyyy-MM-dd HH:mm:ss.SSS zzz");
  private static final SimpleDateFormat utcFmtNoMS = new DateFormatUTC("yyyy-MM-dd HH:mm:ss zzz");
  public static final long ONE_KB = 1024;
  public static final long ONE_MB = 1024 * 1024;
  public static final long ONE_GB = 1024 * 1024 * 1024;
  public static final int MS_PER_MIN = 60 * 1000;
  public static final int MS_PER_HOUR = 60 * MS_PER_MIN;
  public static final int MS_PER_DAY = 24 * MS_PER_HOUR;
  public static final int SERVER_READY_INTERVAL_SEC = 60;
  /****************************************************************************
  * indexOf.
  ****************************************************************************/
  public static int indexOf (String string, String[] strings) {
    for (int i = 0; i < strings.length; i++) {
       if (string.equals(strings[i])) {
	 return i;
       }
    }
    return -1;
  }
  /****************************************************************************
  * parseLines.
  ****************************************************************************/
  public static String[] parseLines (String text, String delimiter) {
    LinkedList<String> list = new LinkedList<String>();
    int at = 0;
    while (true) {
      int index = text.indexOf(delimiter,at);
      if (index == -1) {
        list.add(text.substring(at));
        break;
      }
      list.add(text.substring(at,index));
      at = index + delimiter.length();
    }
    return (String[]) list.toArray(new String[list.size()]);
  }
  /****************************************************************************
  * min.
  ****************************************************************************/
  public static int min (int a, int b) {
    if (a < b) {
      return a;
    }
    return b;
  }
  public static Date min (Date a, Date b) {
    if (a.before(b)) {
      return a;
    }
    return b;
  }
  /****************************************************************************
  * max
  ****************************************************************************/
  public static int max (int a, int b) {
    if (a > b) {
      return a;
    }
    return b;
  }
  public static Date max (Date a, Date b) {
    if (a.after(b)) {
      return a;
    }
    return b;
  }
  /****************************************************************************
  * encodeDate.
  ****************************************************************************/
  public static String encodeDate (Date date) {
    return utcFmt.format(date);
  }
  /****************************************************************************
  * encodeDateNoMS.
  ****************************************************************************/
  public static String encodeDateNoMS (Date date) {
    return utcFmtNoMS.format(date);
  }
  /****************************************************************************
  * parseDate.
  ****************************************************************************/
  public static Date parseDate (String date) throws Exception {
    return utcFmt.parse(date);
  }
  /****************************************************************************
  * encodeThrowable.
  ****************************************************************************/
  public static String encodeThrowable (Throwable throwable) {
    StringBuffer txt = new StringBuffer();
    txt.append(throwable.toString() + " (");
    StackTraceElement[] elements = throwable.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
       if (i > 0) {
       	 txt.append("|");
       }
       txt.append(elements[i].toString());
    }
    txt.append(")");
    Throwable cause = throwable.getCause();
    if (cause != null) {
      txt.append(" < " + encodeThrowable(cause));
    }
    txt.append(")");
    return txt.toString();
  }
  /****************************************************************************
  * encodeThrowableLines.
  ****************************************************************************/
  public static String[] encodeThrowableLines (Throwable throwable) {
    return encodeThrowableLines(throwable,false);
  }
  private static String[] encodeThrowableLines (Throwable throwable, boolean isCause) {
    LinkedList<String> lines = new LinkedList<String>();
    lines.add((isCause ? "Caused by: " : "") + throwable.toString());
    StackTraceElement[] elements = throwable.getStackTrace();
    for (int i = 0; i < elements.length; i++) {
       lines.add("        at " + elements[i].toString());
    }
    Throwable cause = throwable.getCause();
    if (cause != null) {
      append(lines,encodeThrowableLines(cause,true));
    }
    return (String[]) lines.toArray(new String[lines.size()]);
  }
  /****************************************************************************
  * append.
  ****************************************************************************/
  public static void append (LinkedList<String> list, String[] strings) {
    for (int i = 0; i < strings.length; i++) {
       list.add(strings[i]);
    }
  }
  /****************************************************************************
  * sleep.
  ****************************************************************************/
  public static void sleep (double seconds) {
    sleep(Math.round(1000*seconds));
  }
  public static void sleep (int ms) {
    sleep((long)ms);
  }
  public static void sleep (long ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception e) {
    	
    }
  }
  /****************************************************************************
  * startThread.
  ****************************************************************************/
  public static Thread startThread (Runnable runnable) {
    return startThread(runnable,false);
  }
  public static Thread startThread (Runnable runnable, boolean daemon) {
    Thread thread = new Thread(runnable);
    thread.setDaemon(daemon);
    thread.start();
    return thread;
  }
}
