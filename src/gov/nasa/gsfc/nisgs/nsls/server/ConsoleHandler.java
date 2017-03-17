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
*  25-Aug-06, 	Debug tweaks.  Robust handling of connections.
*   1-Feb-07, 	Daemon threads for Windows service.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;

public class ConsoleHandler implements ConnectionListener {
  private ConnectionHandler ch;
  private LogPlayer logPlayer = null;	// Non-null when playing.
  private LivePlayer livePlayer = null;	// Non-null when "live mode" enabled.
  private ObjectQueue toSend = new ObjectQueue();
  private static final boolean DEBUG = false;
  /****************************************************************************
  * ConsoleHandler.
  ****************************************************************************/
  public ConsoleHandler (ConnectionHandler ch) throws Exception {
    this.ch = ch;
    ch.setListener(this);
    // ch.getSocket().setSendBufferSize(2048);
    send(new ServerVersion(SLS.VERSION));
  }
  /****************************************************************************
  * start.
  ****************************************************************************/
  public void start () {
    Util.startThread(new Send(),true);
  }
  /****************************************************************************
  * received.
  ****************************************************************************/
  public synchronized void received (Object o) {
    if (DEBUG) {
      System.out.println("ConsoleHandler["+this.hashCode()+"].received: "+o);
    }
    try {
      doMessage(o);
    } catch (Exception e) {
      System.out.println("ConsoleHandler["+this.hashCode()+"].received/Exception: "+e.toString());
    }
  }
  /****************************************************************************
  * doMessage.
  ****************************************************************************/
  private void doMessage (Object o) throws Exception {
    if (o instanceof PlayEvents) {
      PlayEvents pe = (PlayEvents) o;
      if (logPlayer == null && livePlayer == null) {
      	logPlayer = new LogPlayer(pe.getMaxCount(),pe.getFilters(),this);
      	Util.startThread(logPlayer,true);
      }
      return;
    }
    if (o instanceof StopEvents) {
      if (logPlayer != null) {
      	logPlayer.stop();
      }
      return;
    }
    if (o instanceof StartLiveMode) {
      if (livePlayer == null && logPlayer == null) {
        livePlayer = new LivePlayer(this);
        Util.startThread(livePlayer,true);
      }
      return;
    }
    if (o instanceof StopLiveMode) {
      if (livePlayer != null) {
      	livePlayer.stop();
      }
      return;
    }
    throw new Exception("unknown object");
  }
  /****************************************************************************
  * removeLogPlayer.
  ****************************************************************************/
  public synchronized void removeLogPlayer () {
    logPlayer = null;
  }
  /****************************************************************************
  * removeLivePlayer.
  ****************************************************************************/
  public synchronized void removeLivePlayer () {
    livePlayer = null;
  }
  /****************************************************************************
  * liveEvent.
  ****************************************************************************/
  public synchronized void liveEvent (LogEvent event) {
    if (livePlayer != null) {
      livePlayer.liveEvent(event);
    }
  }
  /****************************************************************************
  * disconnected.
  ****************************************************************************/
  public synchronized void disconnected (Exception e) {
    if (DEBUG) {
      System.out.println("ConsoleHandler["+this.hashCode()+"].disconnected: " + (e != null ? e.toString() : ""));
    }
    if (logPlayer != null) {
      logPlayer.stop();
    }
    // JRB - Shouldn't we also stop any running livePlayer?
    if (livePlayer != null) {
	livePlayer.stop();
    }
    toSend.clear();
    toSend.put(null);
    Server.consoleManager.removeConsole(this);
  }
  /****************************************************************************
  * send.
  ****************************************************************************/
  public synchronized void send (Object msg) {
    if (DEBUG) {
      System.out.println("ConsoleHandler["+this.hashCode()+"].send: "+msg);
    }
    toSend.put(msg);
  }
  /****************************************************************************
  * Send.
  ****************************************************************************/
  public class Send implements Runnable {
    public void run () {
      while (true) {
	Object msg = toSend.get();
	if (msg == null) {
	  return;
	}
	if (DEBUG) {
	  System.out.println("ConsoleHandler["+this.hashCode()+"].Send.run: "+msg);
	}
	try {
	  ch.send(msg);
	} catch (Exception e) {
	  System.out.println("ConsoleHandler["+this.hashCode()+"].Send.run/Exception: "+e.toString());
	  ch.close();
	  disconnected(null);
	}
	if (DEBUG) {
	  System.out.println("ConsoleHandler["+this.hashCode()+"].Send.run: (sent)");
	}
      }
    }
  }
}
