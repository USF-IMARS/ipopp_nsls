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
*   8-Feb-06, 	Original version.
*  21-Feb-07, 	More flexible limiting of log files.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.io.*;
import java.util.*;

public class LogRecorder implements Runnable {
  private String logDir;
  private long sizeLimit = 0;				// Bytes.
  private long timeLimit = 0;				// Milliseconds.
  private ObjectQueue toLog = new ObjectQueue();
  private static final int MAX_EVENTS_PER_LOG_FILE = 100;
  /****************************************************************************
  * LogRecorder.
  ****************************************************************************/
  public LogRecorder (String logDir, String limit) throws Exception {
    this.logDir = logDir;
    parseLimit(limit);
  }
  private void parseLimit (String limit) throws Exception {
    if (limit.endsWith("kb")) {
      sizeLimit = Long.parseLong(limit.substring(0,limit.length()-2)) * Util.ONE_KB;
      return;
    }
    if (limit.endsWith("mb")) {
      sizeLimit = Long.parseLong(limit.substring(0,limit.length()-2)) * Util.ONE_MB;
      return;
    }
    if (limit.endsWith("gb")) {
      sizeLimit = Long.parseLong(limit.substring(0,limit.length()-2)) * Util.ONE_GB;
      return;
    }
    if (limit.endsWith("m")) {
      timeLimit = Long.parseLong(limit.substring(0,limit.length()-1)) * Util.MS_PER_MIN;
      return;
    }
    if (limit.endsWith("h")) {
      timeLimit = Long.parseLong(limit.substring(0,limit.length()-1)) * Util.MS_PER_HOUR;
      return;
    }
    if (limit.endsWith("d")) {
      timeLimit = Long.parseLong(limit.substring(0,limit.length()-1)) * Util.MS_PER_DAY;
      return;
    }
    throw new Exception("unexpected limit: " + limit);
  }
  /****************************************************************************
  * record.
  ****************************************************************************/
  public void record (LogEvent event) {
    toLog.put(event);
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    while (true) {
      LogEvent event = (LogEvent) toLog.get();
      appendEvent(event);
    }
  }
  /****************************************************************************
  * appendEvent.
  ****************************************************************************/
  private void appendEvent (LogEvent event) {
    LinkedList<LogFile> logFiles = Server.logManager.getLogFiles();
    if (logFiles.size() > 0) {
      LogFile logFile = (LogFile) logFiles.getLast();
      if (logFile.getEventCount() < MAX_EVENTS_PER_LOG_FILE) {
      	logFile.appendEvent(event);
      } else {
        String fileName = LogFile.getFileName(logFile.getSeqNumber()+1);
        String filePath = logDir + File.separator + fileName;
        purgeLogFiles(logFiles);
        logFiles.addLast(new LogFile(new File(filePath),event));
      }
    } else {
      String fileName = LogFile.getFileName(1);
      String filePath = logDir + File.separator + fileName;
      logFiles.addLast(new LogFile(new File(filePath),event));
    }
    Server.logManager.returnLogFiles(logFiles);
  }
  /****************************************************************************
  * purgeLogFiles.
  ****************************************************************************/
  private void purgeLogFiles (LinkedList<LogFile> logFiles) {
    if (sizeLimit > 0) {
      long totalSize = 0;
      for (LogFile logFile : logFiles) {
	 totalSize += logFile.getFile().length();
      }
      while (totalSize > sizeLimit) {
	LogFile logFile = logFiles.removeFirst();
	totalSize -= logFile.getFile().length();
	deleteLogFile(logFile);
      }
      return;
    }
    if (timeLimit > 0) {
      long now = System.currentTimeMillis();
      Iterator<LogFile> it = logFiles.iterator();
      while (it.hasNext()) {
	LogFile logFile = it.next();
	if (logFile.getStopDate().getTime() < (now - timeLimit)) {
	  deleteLogFile(logFile);
	  it.remove();
	}
      }
      return;
    }
  }
  /****************************************************************************
  * deleteLogFile.
  ****************************************************************************/
  private void deleteLogFile (LogFile logFile) {
    if (!logFile.getFile().delete()) {
      System.err.println("failed to delete log file: " + logFile.getFile().getAbsolutePath());
    }
  }
}
