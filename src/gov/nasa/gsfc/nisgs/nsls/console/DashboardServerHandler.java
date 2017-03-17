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
*  19-May-14,   Copied from original ServerHandler.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
import java.net.*;
import java.awt.Color;

public class DashboardServerHandler implements Runnable, ConnectionListener {
  private HostPort serverHostPort;
  private ConnectionHandler ch = null;
    protected ControlPanel controlPanel;
    private CustomLabel statusField;
  private boolean stop = false;
  private boolean liveAtConnect = true;
  private int readyCount = 0;
  private java.util.Timer timer;
  private static final long TIMEOUT_INTERVAL_MS = (5 * Util.SERVER_READY_INTERVAL_SEC) * 1000;
  private static final boolean DEBUG = false;
  private static final long serialVersionUID = 1L;
  protected static String serverVersion = null;

  /****************************************************************************
  * DashboardServerHandler.
  ****************************************************************************/
    public DashboardServerHandler (HostPort serverHostPort, ControlPanel controlPanel, CustomLabel statusField) {
    this.serverHostPort = serverHostPort;
    this.controlPanel = controlPanel;
    this.statusField = statusField;
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
	// Display "Connecting..."
	//System.err.println("Connecting...");
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
        //eventTable.connected();
	if(liveAtConnect) {
	    send(new StartLiveMode());
	}
      } catch (Exception e) {
      	try {ch.close();} catch (Exception ex) {}
      	try {s.close();} catch (Exception ex) {}
        throw e;
      }
      // Display "Connected to..."
      //System.err.println("Connected");
      GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"Connected to " + serverHostPort.toString(),Color.black));
    } catch (Exception e) {
	// Display "ERROR..."
	//System.err.println("ERROR");
	GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"ERROR - " + e.toString(),Color.red));
    }
  }
  /****************************************************************************
  * received.
  ****************************************************************************/
  public synchronized void received (Object o) {
    if (DEBUG) {
      System.out.println("DashboardServerHandler.received: " + o);
    }
    restartTimer();
    //SwingUtilities.invokeLater(new DoReceived(o));
    try {
	doReceived(o);
    }
    catch (Exception e) {
	System.err.println("DashboardServerHandler.received/Exception: " + e.toString());
	e.printStackTrace();
    }
  }
    /*
  private class DoReceived implements Runnable {
    private Object o;
    public DoReceived (Object o) {
      this.o = o;
    }
    public void run () {
      try {
	doReceived(o);
      } catch (Exception e) {
	System.out.println("DashboardServerHandler.DoReceived.run/Exception: " + e.toString());
	// e.printStackTrace();
      }
      revalidate();
      repaint();
    }
  }
    */
  private void doReceived (Object msg) throws Exception {
    if (msg instanceof PlayModeStarted) {
	//PlayModeStarted pms = (PlayModeStarted) msg;
	//eventTable.playModeStarted(pms.isSearching());
      return;
    }
    if (msg instanceof PlayModeDone) {
	//eventTable.playModeDone();
      return;
    }
    if (msg instanceof PlayModeStopped) {
	//eventTable.playModeStopped();
      return;
    }
    if (msg instanceof LiveModeStarted) {
	//eventTable.liveModeStarted();
      return;
    }
    if (msg instanceof LiveModeStopped) {
	//eventTable.liveModeStopped();
      return;
    }
    if (msg instanceof LogEvent) {
      LogEvent event = (LogEvent) msg;
      //eventTable.logEvent(event);
      controlPanel.logEvent(event);
      return;
    }
    if (msg instanceof ServerReady) {
      if (DEBUG) {
	readyCount++;
	// Display message counter update
	//GUtil.invokeAndWait(new GUtil.UpdateLabel(countField,"["+Integer.toString(readyCount)+"]",Color.black));
      }
      return;
    }
    if (msg instanceof ServerVersion) {
      ServerVersion version = (ServerVersion) msg;
      ControlPanel.serverVersion = version.getVersion();
      return;
    }
    throw new Exception("unknown message from server");
  }
  /****************************************************************************
  * send.
  ****************************************************************************/
  public synchronized void send (Object o) {
    if (DEBUG) {
      System.out.println("DashboardServerHandler.send: " + o);
    }
    if(ch != null) {
	try {
	    ch.send(o);
	} catch (Exception e) {
	    System.out.println("DashboardServerHandler.send/Exception: " + e.toString());
	}
    }
    else {
	if(o instanceof StartLiveMode)
	    liveAtConnect = true;
	else if (o instanceof StopLiveMode)
	    liveAtConnect = false;
    }
    if (DEBUG) {
      System.out.println("DashboardServerHandler.send: (sent)");
    }
  }
  /****************************************************************************
  * disconnected.
  ****************************************************************************/
  public synchronized void disconnected (Exception e) {
    if (DEBUG) {
      System.out.println("DashboardServerHandler.disconnected: " + (e != null ? e.toString() : ""));
    }
    ch = null;
    stopTimer();
    //eventTable.disconnected();
    ControlPanel.serverVersion = null;
    // Display "DISCONNECTED..."
    //System.err.println("DISCONNECTED");
    GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"DISCONNECTED " + (e != null ? e.toString() : ""),Color.red));
    readyCount = 0;
    // Display clear count field
    //GUtil.invokeAndWait(new GUtil.UpdateLabel(countField," ",Color.black));
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
    //eventTable.disconnected();
    ControlPanel.serverVersion = null;
    // Display "TIMEOUT..."
    //System.err.println("TIMEOUT");
    GUtil.invokeAndWait(new GUtil.UpdateLabel(statusField,"TIMEOUT",Color.red));
  }
}
