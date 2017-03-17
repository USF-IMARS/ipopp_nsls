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
*  22-May-06, 	Handle (ignore) .svn directory.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import java.io.*;
import java.util.*;

public class LogManager implements Runnable {
  private String logDir;
  private LinkedList<LogFile> logFiles = null;
  /****************************************************************************
  * LogManager.
  ****************************************************************************/
  public LogManager (String logDir) {
    this.logDir = logDir;
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    try {
      File dirFile = new File(logDir);
      if (!dirFile.exists()) {
        dirFile.mkdirs();
        setLogFiles(new LinkedList<LogFile>());
      } else {
        setLogFiles(scanLogFiles(dirFile));
      }
    } catch (Exception e) {
      System.out.println("LogManager.run/Exception: " + e.toString());
    }
  }
  /****************************************************************************
  * scanLogFiles.
  ****************************************************************************/
  private LinkedList<LogFile> scanLogFiles (File dirFile) throws Exception {
    LinkedList<LogFile> logFiles = new LinkedList<LogFile>();
    String[] fileNames = dirFile.list();
    for (int x = 0; x < fileNames.length; x++) {
       String pathName = logDir + File.separator + fileNames[x];
       File file = new File(pathName);
       if (file.isFile()) {
         LogFile logFile = new LogFile(file);
         if (logFile.getEventCount() > 0) {
           insertBySeqNumber(logFile,logFiles);
         } else {
      	   if (!logFile.getFile().delete()) {
      	     System.err.println("failed to delete empty (or corrupt) log file: " + logFile.getFile().getAbsolutePath());
      	   }
         }
       }
    }
    return logFiles;
  }
  private void insertBySeqNumber (LogFile logFile, LinkedList<LogFile> logFiles) {
    for (int i = 0; i < logFiles.size(); i++) {
       if (logFile.getSeqNumber() < logFiles.get(i).getSeqNumber()) {
       	 logFiles.add(i,logFile);
       	 return;
       }
    }
    logFiles.add(logFile);
  }
  /****************************************************************************
  * setLogFiles.
  ****************************************************************************/
  private synchronized void setLogFiles (LinkedList<LogFile> logFiles) {
    this.logFiles = logFiles;
    notify();
  }
  /****************************************************************************
  * getLogFiles.
  ****************************************************************************/
  public synchronized LinkedList<LogFile> getLogFiles () {
    while (logFiles == null) {
      try {wait();} catch (Exception e) {}
    }
    LinkedList<LogFile> logFiles = this.logFiles;
    this.logFiles = null;
    return logFiles;
  }
  /****************************************************************************
  * returnLogFiles.
  ****************************************************************************/
  public synchronized void returnLogFiles (LinkedList<LogFile> logFiles) {
    this.logFiles = logFiles;
    notify();
  }
}
