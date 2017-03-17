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
*  23-Aug-06, 	Debug tweaks.  Robust handling of connections.
*   1-Feb-07, 	Daemon threads for Windows service.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.net.*;

public class ListenForClients implements Runnable {
  private int clientPort;
  private static final boolean DEBUG = false;
  /****************************************************************************
  * ListenForClients.
  ****************************************************************************/
  public ListenForClients (int clientPort) {
    this.clientPort = clientPort;
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    while (true) {
      try {
      	accept(new ServerSocket(clientPort));
      } catch (Exception e) {
	System.out.println("ListenForClients.run/Exception: " + e.toString());
      }
      Util.sleep(1.0);
    }
  }
  /****************************************************************************
  * accept.
  ****************************************************************************/
  public void accept (ServerSocket ss) {
    while (true) {
      Socket s = null;
      try {
	s = ss.accept();
 	Read read = new Read(s);
 	read.start();
      } catch (Exception e) {
	System.out.println("ListenForClients.accept/Exception: " + e.toString());
      	try {s.close();} catch (Exception e1) {}
	try {ss.close();} catch (Exception e2) {}
	return;
      }
    }
  }
  /****************************************************************************
  * Read.
  ****************************************************************************/
  private class Read implements ConnectionListener {
    private ConnectionHandler ch;
    public Read (Socket s) throws Exception {
      ch = new ConnectionHandler(s,this,false);
    }
    public void start () {
      Util.startThread(ch,true);
    }
    public void received (Object o) {
      if (DEBUG) {
	System.out.println("ListenForClients.Read.received: " + o);
      }
      try {
      	doObject(o);
      } catch (Exception e) {
	System.out.println("ListenForClients.Read.received/Exception: " + e.toString());
      	ch.close();
      }
    }
    private void doObject (Object o) throws Exception {
      if (o instanceof LogEvent) {
      	LogEvent event = (LogEvent) o;
        if (DEBUG) {
	  System.out.println("ListenForClients.Read.doObject/LogEvent: " + event.getDate().toString());
        }
      	if (event.getHost() == null) {
      	  InetAddress ia = ch.getSocket().getInetAddress();
      	  ch.close();
      	  event.setHost(Server.hostResolver.getHost(ia));
      	} else {
	  ch.close();
      	}
      	Server.consoleManager.liveEvent(event);
      	Server.logRecorder.record(event);
        if (DEBUG) {
	  System.out.println("ListenForClients.Read.doObject/LogEvent: " + event.getDate().toString() + " (done)");
        }
      	return;
      }
      if (o instanceof ConsoleMode) {
      	Server.consoleManager.addConsole(new ConsoleHandler(ch));
      	return;
      }
      throw new Exception("unknown object");
    }
    public void disconnected (Exception e) {
      // What to do?
      if (DEBUG) {
	System.out.println("ListenForClients.Read.disconnected: " + (e != null ? e.toString() : ""));
      }
    }
  }
}
