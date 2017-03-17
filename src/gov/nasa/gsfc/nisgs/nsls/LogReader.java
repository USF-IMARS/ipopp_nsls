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
*   1-Feb-07, 	Daemon threads for Windows service.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls;
import gov.nasa.gsfc.nisgs.nsls.message.*;
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
import java.net.*;

/******************************************************************************
* This class is used by a client of the logging system to read log events.
* <br>
* <br>
* For example:
* 
* <pre>
* <tt>
* // Create log reader.
* LogReader reader = new LogReader("nisds1.sci.gsfc.nasa.gov",4005);
* 
* // Search for all error-level events.
* try {
*   LogEvent[] events = reader.search(new Filters(new LevelFilter(false,false,true)));
* } catch (Exception e) {
*   // Handle exception...
* }
* </tt>
* </pre>
* 
******************************************************************************/

public class LogReader {
  private HostPort logServerAddr;
  /****************************************************************************
  * @param logServerHost The host on which the NSLS server is running.
  * @param logServerPort The port on which the NSLS servier is listening.
  ****************************************************************************/
  public LogReader (String logServerHost, int logServerPort) {
    this.logServerAddr = new HostPort(logServerHost,logServerPort);
  }
  /****************************************************************************
  * @param maxCount Maximum number of log events to be returned.  If zero,
  * then no maximum.  If more events than the maximum are available, then
  * only the most recent are returned.
  * @param filters The filters to be applied to the search.
  ****************************************************************************/
  public LogEvent[] search (int maxCount, Filters filters) throws Exception {
    ObjectQueue reply = new ObjectQueue();
    DoSearch doSearch = new DoSearch(maxCount,filters,reply);
    doSearch.start();
    Object o = reply.get();
    if (o instanceof LogEvent[]) {
      return (LogEvent[]) o;
    }
    if (o instanceof Exception) {
      throw (Exception) o;
    }
    throw new Exception("unknown object: " + o.toString());
  }
  private class DoSearch implements ConnectionListener {
    private ObjectQueue reply;
    private ConnectionHandler ch;
    private LinkedList<LogEvent> events = new LinkedList<LogEvent>();
    public DoSearch (int maxCount, Filters filters, ObjectQueue reply) throws Exception {
      this.reply = reply;
      Socket s = null;
      try {
      	s = new Socket(logServerAddr.getHost(),logServerAddr.getPort());
        ch = new ConnectionHandler(s,this,true);
        ch.send(new ConsoleMode());
        ch.send(new PlayEvents(maxCount,filters));
      } catch (Exception e) {
        try {ch.close();} catch (Exception ex) {}
        try {s.close();} catch (Exception ex) {}
        throw e;
      }
    }
    public void start () {
      Util.startThread(ch,true);
    }
    public void received (Object o) {
      if (o instanceof LogEvent) {
        events.add((LogEvent)o);
        return;
      }
      if (o instanceof PlayModeDone) {
        ch.close();
        reply.put(events.toArray(new LogEvent[events.size()]));
      }
      if (o instanceof PlayModeStarted ||
	  o instanceof ServerReady ||
	  o instanceof ServerVersion) {
        return;
      }
      ch.close();
      reply.put(new Exception("unexpected object: " + o.toString()));
    }
    public void disconnected (Exception e) {
      reply.put(e != null ? e : new Exception("unexpected disconnect"));
    }
  }
}
