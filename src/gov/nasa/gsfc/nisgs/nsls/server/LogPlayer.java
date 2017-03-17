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
*  25-Aug-06, 	More robust connection handling.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.io.*;
import java.util.*;

public class LogPlayer implements Runnable {
  private int maxCount;
  private Filters filters;
  private ConsoleHandler consoleHandler;
  private boolean stop = false;
  /****************************************************************************
  * LogPlayer.
  ****************************************************************************/
  public LogPlayer (int maxCount, Filters filters, ConsoleHandler consoleHandler) throws Exception {
    this.maxCount = (maxCount == 0 ? Integer.MAX_VALUE : maxCount);
    this.filters = filters;
    this.consoleHandler = consoleHandler;
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    playEvents();
    consoleHandler.removeLogPlayer();
  }
  /****************************************************************************
  * playEvents.
  ****************************************************************************/
  private void playEvents () {
    consoleHandler.send(new PlayModeStarted(true));
    LinkedList<LogFile> logFiles = Server.logManager.getLogFiles();
    if (stop) {
      Server.logManager.returnLogFiles(logFiles);
      consoleHandler.send(new PlayModeStopped());
      return;
    }
    LinkedList<LogEvent> events = (filters.areNoFilters() ? findEventsNoFilters(logFiles) : findEvents(logFiles));
    Server.logManager.returnLogFiles(logFiles);
    if (stop) {
      consoleHandler.send(new PlayModeStopped());
      return;
    }
    consoleHandler.send(new PlayModeStarted(false));
    for (LogEvent event : events) {
      consoleHandler.send(event);
      if (stop) {
	consoleHandler.send(new PlayModeStopped());
      	return;
      }
      Util.sleep(10);
    }
    consoleHandler.send(new PlayModeDone());
    return;
  }
  /****************************************************************************
  * findEvents.
  ****************************************************************************/
  private LinkedList<LogEvent> findEvents (LinkedList<LogFile> logFiles) {
    int count = 0;
    LinkedList<LinkedList<LogEvent>> lists = new LinkedList<LinkedList<LogEvent>>();
    for (int i = logFiles.size() - 1; i >= 0; i--) {
      LogFile logFile = (LogFile) logFiles.get(i);
      if (doScan(logFile.getStartDate(),logFile.getStopDate())) {
        int nNeeded = maxCount - count;
	LinkedList<LogEvent> list = findEvents(logFile,nNeeded);
	lists.addFirst(list);
	count += list.size();
	if (count == maxCount) {
	  break;
	}
      }
      if (stop) {
        break;
      }
    }
    LinkedList<LogEvent> events = new LinkedList<LogEvent>();
    for (LinkedList<LogEvent> list : lists) {
      events.addAll(list);
    }
    return events;
  }
  private LinkedList<LogEvent> findEvents (LogFile logFile, int nNeeded) {
    LinkedList<LogEvent> events = new LinkedList<LogEvent>();
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(logFile.getFile()));
      for (int x = 0; x < logFile.getEventCount(); x++) {
         LogEvent event = (LogEvent) in.readObject();
         if (filters.passes(event)) {
           if (events.size() == nNeeded) {
             events.removeFirst();
           }
           events.addLast(event);
         }
         if (stop) {
           break;
         }
      }
      in.close();
    } catch (Exception e) {
      System.out.println("LogPlayer.findEvents(LogFile,int)/Exception: " + e.toString());
    } finally {
      try {in.close();} catch (Exception e) {}
    }
    return events;
  }
  /****************************************************************************
  * findEventsNoFilters.
  ****************************************************************************/
  private LinkedList<LogEvent> findEventsNoFilters (LinkedList<LogFile> logFiles) {
    LinkedList<LogEvent> events = new LinkedList<LogEvent>();
    //-----------------------------------------------------------------------
    // Search backwards through the list of log files until enough events
    // have been reached.
    //-----------------------------------------------------------------------
    for (int x = logFiles.size() - 1, count = 0; x >= 0; x--) {
       LogFile logFile = logFiles.get(x);
       count += logFile.getEventCount();
       if (count >= maxCount) {
       	 for (int y = x; y < logFiles.size(); y++) {
            logFile = logFiles.get(y);
	    findEventsNoFilters(logFile,events);
	    if (stop) {
	      return events;
	    }
       	 }
       	 return events;
       }
    }
    //-----------------------------------------------------------------------
    // There are less events than the maximum, scan all of the log files.
    //-----------------------------------------------------------------------
    for (int x = 0; x < logFiles.size(); x++) {
       LogFile logFile = logFiles.get(x);
       findEventsNoFilters(logFile,events);
       if (stop) {
	 return events;
       }
    }
    return events;
  }
  private void findEventsNoFilters (LogFile logFile, LinkedList<LogEvent> events) {
    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(logFile.getFile()));
      for (int x = 0; x < logFile.getEventCount(); x++) {
         LogEvent event = (LogEvent) in.readObject();
         if (events.size() == maxCount) {
           events.removeFirst();
         }
         events.addLast(event);
         if (stop) {
           break;
         }
      }
      in.close();
    } catch (Exception e) {
      System.out.println("LogPlayer.findEventsNoFilters(LogFile,LinkedList)/Exception: " + e.toString());
    } finally {
      try {in.close();} catch (Exception e) {}
    }
  }
  /****************************************************************************
  * doScan.
  ****************************************************************************/
  private boolean doScan (Date startDate, Date stopDate) {
    DateTimeFilter filter = filters.getDateTimeFilter();
    if (filter != null) {
      if (startDate.after(filter.getStopDate())) {
      	return filter.isNot() ^ false;
      }
      if (stopDate.before(filter.getStartDate())) {
      	return filter.isNot() ^ false;
      }
      return filter.isNot() ^ true;
    }
    return true;
  }
  /****************************************************************************
  * stop.
  ****************************************************************************/
  public void stop () {
    stop = true;
  }
}
