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
*  14-Mar-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;

public class LivePlayer implements Runnable {
  private ConsoleHandler consoleHandler;
  private ObjectQueue events = new ObjectQueue();
  /****************************************************************************
  * LivePlayer.
  ****************************************************************************/
  public LivePlayer (ConsoleHandler consoleHandler) throws Exception {
    this.consoleHandler = consoleHandler;
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    consoleHandler.send(new LiveModeStarted());
    while (true) {
      LogEvent event = (LogEvent) events.get();
      if (event == null) {
      	break;
      }
      consoleHandler.send(event);
    }
    consoleHandler.send(new LiveModeStopped());
    consoleHandler.removeLivePlayer();
  }
  /****************************************************************************
  * liveEvent.
  ****************************************************************************/
  public void liveEvent (LogEvent event) {
    events.put(event);
  }
  /****************************************************************************
  * stop.
  ****************************************************************************/
  public void stop () {
    events.clear();
    events.put(null);
  }
}
