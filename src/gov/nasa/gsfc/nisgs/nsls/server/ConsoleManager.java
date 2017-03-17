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
*  23-Aug-06, 	More robust handling of socket connections.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;

import java.util.*;

public class ConsoleManager {
  private LinkedList<ConsoleHandler> handlers = new LinkedList<ConsoleHandler>();
  /****************************************************************************
  * ConsoleManager.
  ****************************************************************************/
  public ConsoleManager () {

  }
  /****************************************************************************
  * addConsole.
  ****************************************************************************/
  public synchronized void addConsole (ConsoleHandler handler) {
    handlers.add(handler);
    handler.start();
  }
  /****************************************************************************
  * removedConsole.
  ****************************************************************************/
  public synchronized void removeConsole (ConsoleHandler handler) {
    handlers.remove(handler);
  }
  /****************************************************************************
  * broadcast.
  ****************************************************************************/
  public synchronized void broadcast (Object msg) {
    for (ConsoleHandler handler : handlers) {
       handler.send(msg);
    }
  }
  /****************************************************************************
  * liveEvent.
  ****************************************************************************/
  public synchronized void liveEvent (LogEvent event) {
    for (ConsoleHandler handler : handlers) {
       handler.liveEvent(event);
    }
  }
}
