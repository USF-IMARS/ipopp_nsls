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
*  18-Mar-05, 	Original version.
*  20-Feb-07, 	More accurate handling of start/stop times.
*
*  Comments:
* 
*  File name has format: LOG<seq>.sjo
* 
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.io.*;
import java.util.*;

public class LogFile {
  private File file;
  private int seqNumber;
  private Date startDate;
  private Date stopDate;
  private int eventCount;
  /****************************************************************************
  * LogFile.
  * Scans an existing log file.
  ****************************************************************************/
  public LogFile (File file) {
    this.file = file;
    String fileName = file.getName();
    seqNumber = Integer.parseInt(fileName.substring(3,fileName.length()-4));
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(file));
      LogEvent event = (LogEvent) in.readObject();
      startDate = event.getDate();
      stopDate = event.getDate();
      eventCount = 1;
      while (true) {
        event = (LogEvent) in.readObject();
        startDate = Util.min(startDate,event.getDate());
        stopDate = Util.max(stopDate,event.getDate());
        eventCount++;
      }
    } catch (EOFException eof) {
      try {
      	in.close();
      } catch (Exception e) {
	System.out.println("LogFile(File)/EOFException/Exception: " + e.toString());
      }
    } catch (Exception e) {
      System.out.println("LogFile(File)/Exception: " + e.toString());
    } finally {
      try {in.close();} catch (Exception e) {}
    }
  }
  /****************************************************************************
  * LogFile.
  * Creates a new log file and appends one event.
  ****************************************************************************/
  public LogFile (File file, LogEvent event) {
    this.file = file;
    String fileName = file.getName();
    seqNumber = Integer.parseInt(fileName.substring(3,fileName.length()-4));
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file));
      out.close();
    } catch (Exception e) {
      System.out.println("LogFile(File,LogEvent)/Exception: " + e.toString());
    } finally {
      try {out.close();} catch (Exception e) {}
    }
    startDate = event.getDate();
    stopDate = event.getDate();
    eventCount = 0;
    appendEvent(event);
  }
  /****************************************************************************
  * getFile.
  ****************************************************************************/
  public File getFile () {
    return file;
  }
  /****************************************************************************
  * getSeqNumber.
  ****************************************************************************/
  public int getSeqNumber () {
    return seqNumber;
  }
  /****************************************************************************
  * getStartDate.
  ****************************************************************************/
  public Date getStartDate () {
    return startDate;
  }
  /****************************************************************************
  * getStopDate.
  ****************************************************************************/
  public Date getStopDate () {
    return stopDate;
  }
  /****************************************************************************
  * getEventCount.
  ****************************************************************************/
  public int getEventCount () {
    return eventCount;
  }
  /****************************************************************************
  * appendEvent.
  ****************************************************************************/
  public void appendEvent (LogEvent event) {
    AppendObjectOutputStream out = null;
    try {
      out = new AppendObjectOutputStream(new FileOutputStream(file,true));
      out.writeObject(event);
      out.reset();
      out.close();
      startDate = Util.min(startDate,event.getDate());
      stopDate = Util.max(stopDate,event.getDate());
      eventCount++;
    } catch (Exception e) {
      System.out.println("LogFile.appendEvent/Exception: " + e.toString());
    } finally {
      try {out.close();} catch (Exception e) {}
    }
  }
  /****************************************************************************
  * getFileName.
  ****************************************************************************/
  public static String getFileName (int sn) {
    return "LOG" + Integer.toString(sn) + "." + "sjo";
  }
}
