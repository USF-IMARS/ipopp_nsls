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
*  25-Aug-06, 	Debug tweaks.  Robust connection handling.
*  20-Sep-06, 	Increase timeout interval for "server ready".
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class ServerHandler extends JPanel implements Runnable, ConnectionListener {
  private HostPort serverHostPort;
  private ConnectionHandler ch = null;
  protected EventTable eventTable;
  private CustomLabel statusField;
  private CustomLabel countField;
  private boolean stop = false;
  private boolean liveAtConnect = true;
  private int readyCount = 0;
  private java.util.Timer timer;
  private static final long TIMEOUT_INTERVAL_MS = (5 * Util.SERVER_READY_INTERVAL_SEC) * 1000;
  private static final boolean DEBUG = false;
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * ServerHandler.
  ****************************************************************************/
  public ServerHandler (HostPort serverHostPort) {
    this.serverHostPort = serverHostPort;
    setLayout(new BorderLayout());
    eventTable = new EventTable();
    add(eventTable,BorderLayout.CENTER);
    statusField = new CustomLabel(" ");
    countField = new CustomLabel(" ");
    add(new MarginPanel(5,5,new HPanel(eventTable.getStatusPanel(),GUtil.HGlue(),statusField,GUtil.HSpace(5),countField),5,5),BorderLayout.SOUTH);
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    while (true) {
      if (stop) {
      	return;
      }
      if (ch == null) {
        tryToConnect();
      }
      Util.sleep(2.0);
    }
  }
  /****************************************************************************
  * tryToConnect.
  ****************************************************************************/
  private void tryToConnect () {
    try {
	GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"Connecting" + (liveAtConnect ? " LIVE" : "") + " to " + serverHostPort.toString(),Color.black));
      Util.sleep(0.5);
      Socket s = null;
      try {
      	s = new Socket(serverHostPort.getHost(),serverHostPort.getPort());
        // s.setReceiveBufferSize(2048);
        ch = new ConnectionHandler(s,this,true);
        send(new ConsoleMode());
        Util.startThread(ch);
        startTimer();
        eventTable.connected();
	if(liveAtConnect) {
	    send(new StartLiveMode());
	}
      } catch (Exception e) {
      	try {ch.close();} catch (Exception ex) {}
      	try {s.close();} catch (Exception ex) {}
        throw e;
      }
      GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"Connected to " + serverHostPort.toString(),Color.black));
    } catch (Exception e) {
      GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"ERROR - " + e.toString(),Color.red));
    }
  }
  /****************************************************************************
  * received.
  ****************************************************************************/
  public synchronized void received (Object o) {
    if (DEBUG) {
      System.out.println("ServerHandler.received: " + o);
    }
    restartTimer();
    SwingUtilities.invokeLater(new DoReceived(o));
  }
  private class DoReceived implements Runnable {
    private Object o;
    public DoReceived (Object o) {
      this.o = o;
    }
    public void run () {
      try {
	doReceived(o);
      } catch (Exception e) {
	System.out.println("ServerHandler.DoReceived.run/Exception: " + e.toString());
	// e.printStackTrace();
      }
      revalidate();
      repaint();
    }
  }
  private void doReceived (Object msg) throws Exception {
    if (msg instanceof PlayModeStarted) {
      PlayModeStarted pms = (PlayModeStarted) msg;
      eventTable.playModeStarted(pms.isSearching());
      return;
    }
    if (msg instanceof PlayModeDone) {
      eventTable.playModeDone();
      return;
    }
    if (msg instanceof PlayModeStopped) {
      eventTable.playModeStopped();
      return;
    }
    if (msg instanceof LiveModeStarted) {
      eventTable.liveModeStarted();
      return;
    }
    if (msg instanceof LiveModeStopped) {
      eventTable.liveModeStopped();
      return;
    }
    if (msg instanceof LogEvent) {
      LogEvent event = (LogEvent) msg;
      eventTable.logEvent(event);
      return;
    }
    if (msg instanceof ServerReady) {
      if (DEBUG) {
	readyCount++;
	GUtil.invokeAndWait(new GUtil.UpdateLabel(countField,"["+Integer.toString(readyCount)+"]",Color.black));
      }
      return;
    }
    if (msg instanceof ServerVersion) {
      ServerVersion version = (ServerVersion) msg;
      Console.serverVersion = version.getVersion();
      return;
    }
    throw new Exception("unknown message from server");
  }
  /****************************************************************************
  * send.
  ****************************************************************************/
  public synchronized void send (Object o) {
    if (DEBUG) {
      System.out.println("ServerHandler.send: " + o);
    }
    if(ch != null) {
	try {
	    ch.send(o);
	} catch (Exception e) {
	    System.out.println("ServerHandler.send/Exception: " + e.toString());
	}
    }
    else {
	if(o instanceof StartLiveMode)
	    liveAtConnect = true;
	else if (o instanceof StopLiveMode)
	    liveAtConnect = false;
    }
    if (DEBUG) {
      System.out.println("ServerHandler.send: (sent)");
    }
  }
  /****************************************************************************
  * disconnected.
  ****************************************************************************/
  public synchronized void disconnected (Exception e) {
    if (DEBUG) {
      System.out.println("ServerHandler.disconnected: " + (e != null ? e.toString() : ""));
    }
    ch = null;
    stopTimer();
    eventTable.disconnected();
    Console.serverVersion = null;
    GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"DISCONNECTED " + (e != null ? e.toString() : ""),Color.red));
    readyCount = 0;
    GUtil.invokeAndWait(new GUtil.UpdateLabel(countField," ",Color.black));
  }
  /****************************************************************************
  * stop.
  ****************************************************************************/
  public synchronized void stop () {
    stop = true;
    if (ch != null) {
      ch.close();
      ch = null;
      stopTimer();
    }
  }

  /**
   * Check if we are connecting to localhost (required for ControlPanel
   * configuration checking)
   */
  public boolean isLocalhost() {
      HostPort hp = serverHostPort;
      return serverHostPort.getHost().equals("localhost");
  }
  /****************************************************************************
  * stopTimer.
  ****************************************************************************/
  private void stopTimer () {
    try {timer.cancel();} catch (Exception e) {}
  }
  /****************************************************************************
  * restartTimer.
  ****************************************************************************/
  private void restartTimer () {
    stopTimer();
    startTimer();
  }
  /****************************************************************************
  * startTimer.
  ****************************************************************************/
  private void startTimer () {
    timer = new java.util.Timer();
    timer.schedule(new TimeOut(),TIMEOUT_INTERVAL_MS);
  }
  /****************************************************************************
  * TimeOut.
  ****************************************************************************/
  private class TimeOut extends TimerTask {
    public void run () {
      timeout();
    }
  }
  private synchronized void timeout () {
    ch.close();
    ch = null;
    eventTable.disconnected();
    Console.serverVersion = null;
    GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"TIMEOUT",Color.red));
  }
}
