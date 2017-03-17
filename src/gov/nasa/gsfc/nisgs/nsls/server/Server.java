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
*  14-Aug-06, 	Debug tweaks.
*  11-Dec-06, 	More tweaks for the SOCKET_WAIT condition.
*   1-Feb-07, 	Daemon threads and timers for Windows service.
*  20-Feb-07, 	More flexible limiting of log files.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;

public class Server {
  protected static LogManager logManager;
  protected static LogRecorder logRecorder;
  protected static ConsoleManager consoleManager;
  protected static HostResolver hostResolver;
  private static final long READY_INTERVAL_MS = Util.SERVER_READY_INTERVAL_SEC * 1000;
  private static boolean stop = false;
  private static final boolean DEBUG = false;
  private static final String USAGE = "Usage: Server [-limit <count>{kb|mb|gb|m|h|d}] <client-port> <log-dir>";
  /****************************************************************************
  * main.
  ****************************************************************************/
  public static void main (String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println(USAGE);
      return;
    }
    String clientPort = null;
    String logDir = null;
    String limit = "72h";
    int x = 0;
    while (x < args.length) {
      String arg = args[x++];
      if (arg.equals("-limit")) {
      	limit = args[x++];
      	continue;
      }
      if (clientPort == null) {
      	clientPort = arg;
      	continue;
      }
      if (logDir == null) {
      	logDir = arg;
      	continue;
      }
      System.out.println("unexpected command line argument: " + arg);
      System.out.println(USAGE);
      return;
    }
    if (clientPort == null) {
      System.out.println("missing client port number");
      System.out.println(USAGE);
      return;
    }
    if (logDir == null) {
      System.out.println("missing logs directory");
      System.out.println(USAGE);
      return;
    }
    logManager = new LogManager(logDir);
    logRecorder = new LogRecorder(logDir,limit);
    consoleManager = new ConsoleManager();
    hostResolver = new HostResolver();
    Util.startThread(logManager,true);
    Util.startThread(logRecorder,true);
    Util.startThread(new ListenForClients(Integer.parseInt(clientPort)),true);
    // Start timer for ServerReady messages.
    // Run as daemon thread so it won't prevent the JVM from exiting.
    Timer timer = new Timer(true);
    timer.schedule(new SendReady(),READY_INTERVAL_MS,READY_INTERVAL_MS);
    // On Windows, loop until "stop" is invoked by the executable that was
    // installed as a Windows service.  This should be the only non-daemon
    // thread running so when this thread ends the JVM should exit.
    // On Linux, not used by the JSW but doesn't hurt too bad.
    while (!stop) {
      Util.sleep(250);
    }
  }
  /****************************************************************************
  * SendReady.
  ****************************************************************************/
  private static class SendReady extends TimerTask {
    public void run () {
      if (DEBUG) {
	System.out.println("Server.SendReady:");
      }
      System.runFinalization();
      System.gc();
      consoleManager.broadcast(new ServerReady());
    }
  }
  /****************************************************************************
  * stop.
  * On Windows, invoked when the service is told to stop.
  ****************************************************************************/
  public static void stop () {
    stop = true;
  }
}
