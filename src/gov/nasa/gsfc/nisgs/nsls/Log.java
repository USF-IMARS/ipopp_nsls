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
*  26-Oct-05, 	Original version.
*  20-Feb-06, 	Fixed javadoc comments.  Handle corrupted
*				temporary event files (delete them).  Option
*				to write errors to "standard error".  Added
*				shutdown hook to wait for "to log" and "to
*				server" threads to finish.
*  23-Aug-06, 	Get host here rather than each time an event
*				occurs.
*   1-Feb-07, 	Daemon threads and timers for Windows service.
*  20-Feb-07, 	Handle non-files (eg. the .svn directory) in
*  				the temporary directory.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
import java.net.*;
import java.io.*;

/******************************************************************************
* This class is used by a client of the logging system to log events.
* <br>
* <br>
* For example:
* 
* <pre>
* <tt>
* Log log = new Log("nisds1.sci.gsfc.nasa.gov",4005,
*                   "/home/drl/ncs/tmp-logs");
* .
* .
* .
* try {
*   // do something
* } catch (Exception e) {
*   log.error(new NSLS.NCS("Station1","Group3","Job7"),
*             new NSLS.ProductParameters("Product1","Granule8","Pass5","Type4"),
*             "error doing something",e);
* }
* </tt>
* </pre>
* 
******************************************************************************/

public class Log implements AutoCloseable{
  private HostPort logServerAddr;
  private String tmpDir;
  private File[] logFiles;
  private long maxLogFileSize;
  private boolean toStdOut;
  private boolean toStdErr = false;
  private boolean doubleSpace = false;
  private Source defaultSource = null;
  private Parameters defaultParameters = null;
  private String host;
  private ObjectQueue toLog = null;
  private ObjectQueue toServer = null;
  private Thread toLogThread = null;
  private Thread toServerThread = null;
  private static final int MAX_TMP_FILES = 0;
  private boolean closed = false;
  /****************************************************************************
  * An information event.
  ****************************************************************************/
  public static final int INFO_EVENT = 1;
  /****************************************************************************
  * A warning event.
  ****************************************************************************/
  public static final int WARNING_EVENT = 2;
  /****************************************************************************
  * An error event.
  ****************************************************************************/
  public static final int ERROR_EVENT = 3;
  /****************************************************************************
  * Event source classes must be subclassed from this abstract class.
  ****************************************************************************/
  public static abstract class Source implements Serializable {
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @return A string representation of the source.
    **************************************************************************/
    public abstract String toString();
  }
  /****************************************************************************
  * Parameters classes must be subclassed from this abstract class.
  ****************************************************************************/
  public static abstract class Parameters implements Serializable {
    private static final long serialVersionUID = 1L;
    /**************************************************************************
    * @return A string representation of the parameters.
    **************************************************************************/
    public abstract String toString();
    /**************************************************************************
    * @return A string representation of the parameters as lines of text.
    **************************************************************************/
    public abstract String[] toStrings();
  }
  /****************************************************************************
  * Log events will be written to "standard output" as lines of text (if
  * desired).
  * @param toStdOut Whether or not log events are written to "standard output".
  ****************************************************************************/
  public Log (boolean toStdOut) {
    this(null,null,null,0,toStdOut);
  }
  /****************************************************************************
  * Log events will be written to a local log file as lines of text.
  * @param logFile A local log file which will be created if necessary or
  * appended to if it already exists.  The size of the log file is not limited.
  ****************************************************************************/
  public Log (File logFile) {
    this(null,null,new File[] {logFile},0,false);
  }
  /****************************************************************************
  * Log events will be written to local log files as lines of text.
  * @param logFiles Local log files which will be created as necessary.  New
  * events are always written to the first log file.  When the first log file
  * gets too big, it is renamed as the second log file, the second as the third,
  * and so on with the last log file being deleted if necessary.  The first log
  * file is then created and written to.
  * @param maxLogFileSize The limit in bytes of a log file.
  ****************************************************************************/
  public Log (File[] logFiles, long maxLogFileSize) {
    this(null,null,logFiles,maxLogFileSize,false);
  }
  /****************************************************************************
  * Log events will be sent to the NSLS server.
  * @param logServerHost The host on which the NSLS server is running.
  * @param logServerPort The port on which the NSLS server is listening.
  * @param tmpDir A local directory used for the temporary storage of log
  * events in case the NSLS server is unavailable.
  ****************************************************************************/
  public Log (String logServerHost, int logServerPort, String tmpDir) {
    this(new HostPort(logServerHost,logServerPort),tmpDir,null,0,false);
  }
  /****************************************************************************
  * Log.
  ****************************************************************************/
  private Log (HostPort logServerAddr, String tmpDir, File[] logFiles, long maxLogFileSize, boolean toStdOut)
  {
    this.logServerAddr = logServerAddr;
    this.tmpDir = tmpDir;
    this.logFiles = logFiles;
    this.maxLogFileSize = maxLogFileSize;
    this.toStdOut = toStdOut;
    try
    {
      InetAddress addr = InetAddress.getLocalHost();
      if (addr.isLoopbackAddress())
      {
        host = null;
      }
      else
      {
        host = addr.getHostName();
      }
    }
    catch (Exception e)
    {
      host = null;
    }
  }
  /****************************************************************************
  * Sets whether or not log events are written to "standard output" as lines
  * of text. 
  * @param toStdOut Whether or not log events are written to "standard output".
  ****************************************************************************/
  public void setToStdOut (boolean toStdOut) {
    this.toStdOut = toStdOut;
  }
  /****************************************************************************
  * @param toStdErr Whether or not errors are written to "standard error".
  ****************************************************************************/
  public void setToStdErr (boolean toStdErr) {
    this.toStdErr = toStdErr;
  }
  /****************************************************************************
  * Sets whether or not log events will be written to a local log file as lines
  * of text.
  * @param logFile A local log file which will be created if necessary or
  * appended to if it already exists.  The size of the log file is not limited.
  * If null, logs events are not written to a local log file.
  ****************************************************************************/
  public void setToLogFile (File logFile) {
    this.logFiles = new File[] {logFile};
  }
  /****************************************************************************
  * Sets whether or not log events will be written to local log files as lines
  * of text.
  * @param logFiles Local log files which will be created as necessary.  New
  * events are always written to the first log file.  When the first log file
  * gets too big, it is renamed as the second log file, the second as the third,
  * and so on with the last log file being deleted if necessary.  The first log
  * file is then created and written to.
  * @param maxLogFileSize The limit in bytes of a log file.
  ****************************************************************************/
  public void setToLogFile (File[] logFiles, long maxLogFileSize) {
    this.logFiles = logFiles;
    this.maxLogFileSize = maxLogFileSize;
  }
  /****************************************************************************
  * Sets the default source for subsequent log events.
  * @param defaultSource The source of the event.
  ****************************************************************************/
  public void setDefaultSource (Source defaultSource) {
    this.defaultSource = defaultSource;
  }
  /****************************************************************************
  * Gets the default source for subsequent log events.
  * @return The source of the event.
  ****************************************************************************/
  public Source getDefaultSource () {
    return defaultSource;
  }
  /****************************************************************************
  * Sets the default parameters for subsequent log events.
  * @param defaultParameters The event parameters.
  ****************************************************************************/
  public void setDefaultParameters (Parameters defaultParameters) {
    this.defaultParameters = defaultParameters;
  }
  /****************************************************************************
  * Gets the default parameters for subsequent log events.
  * @return The event parameters.
  ****************************************************************************/
  public Parameters getDefaultParameters () {
    return defaultParameters;
  }
  /****************************************************************************
  * Sets whether or not to double-space log events written to standard output
  * or a local file.
  * @param doubleSpace Whether or not to double-space.
  ****************************************************************************/
  public void setDoubleSpacing (boolean doubleSpace) {
    this.doubleSpace = doubleSpace;
  }
  /****************************************************************************
  * Logs an informational event.
  * @param text A description of the event.
  ****************************************************************************/
  public void info (String text) {
    log(INFO_EVENT,defaultSource,defaultParameters,text,null,null);
  }
  public void info (String text, String mode)
  {
    log(INFO_EVENT,defaultSource,defaultParameters,text,null,mode);
  }
  /****************************************************************************
  * Logs an informational event.
  * @param source The source of the event.
  * @param text A description of the event.
  ****************************************************************************/
  public void info (Source source, String text) {
    log(INFO_EVENT,source,defaultParameters,text,null,null);
  }
  /****************************************************************************
  * Logs an informational event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  ****************************************************************************/
  public void info (Parameters parameters, String text) {
    log(INFO_EVENT,defaultSource,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs an informational event.
  * @param source The source of the event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  ****************************************************************************/
  public void info (Source source,
  		    Parameters parameters, String text) {
    log(INFO_EVENT,source,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param text A description of the event.
  ****************************************************************************/
  public void warning (String text) {
    log(WARNING_EVENT,defaultSource,defaultParameters,text,null,null);
  }
  public void warning (String text, String mode)
  {
    log(WARNING_EVENT,defaultSource,defaultParameters,text,null,mode);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param source The source of the event.
  * @param text A description of the event.
  ****************************************************************************/
  public void warning (Source source, String text) {
    log(WARNING_EVENT,source,defaultParameters,text,null,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param text A description of the event.
  * @param parameters The event parameters.
  ****************************************************************************/
  public void warning (Parameters parameters, String text) {
    log(WARNING_EVENT,defaultSource,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param source The source of the event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  ****************************************************************************/
  public void warning (Source source,
  		       Parameters parameters, String text) {
    log(WARNING_EVENT,source,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void warning (String text, Throwable throwable) {
    log(WARNING_EVENT,defaultSource,defaultParameters,text,throwable,null);
  }
  public void warning (String text, Throwable throwable, String mode)
  {
    log(WARNING_EVENT,defaultSource,defaultParameters,text,throwable,mode);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param source The source of the event.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void warning (Source source, String text, Throwable throwable) {
    log(WARNING_EVENT,source,defaultParameters,text,throwable,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void warning (Parameters parameters,
  		     String text, Throwable throwable) {
    log(WARNING_EVENT,defaultSource,parameters,text,throwable,null);
  }
  /****************************************************************************
  * Logs a warning event.
  * @param source The source of the event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void warning (Source source, Parameters parameters,
  		     String text, Throwable throwable) {
    log(WARNING_EVENT,source,parameters,text,throwable,null);
  }

  /****************************************************************************
  * Logs an error event.
  * @param text A description of the event.
  ****************************************************************************/
  public void error (String text) {
    log(ERROR_EVENT,defaultSource,defaultParameters,text,null,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param source The source of the event.
  * @param text A description of the event.
  ****************************************************************************/
  public void error (Source source, String text) {
    log(ERROR_EVENT,source,defaultParameters,text,null,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  ****************************************************************************/
  public void error (Parameters parameters, String text) {
    log(ERROR_EVENT,defaultSource,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param source The source of the event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  ****************************************************************************/
  public void error (Source source,
  		     Parameters parameters, String text) {
    log(ERROR_EVENT,source,parameters,text,null,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void error (String text, Throwable throwable) {
    log(ERROR_EVENT,defaultSource,defaultParameters,text,throwable,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param source The source of the event.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void error (Source source, String text, Throwable throwable) {
    log(ERROR_EVENT,source,defaultParameters,text,throwable,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void error (Parameters parameters,
  		     String text, Throwable throwable) {
    log(ERROR_EVENT,defaultSource,parameters,text,throwable,null);
  }
  /****************************************************************************
  * Logs an error event.
  * @param source The source of the event.
  * @param parameters The event parameters.
  * @param text A description of the event.
  * @param throwable A {@link Throwable Throwable} (eg.
  * {@link Exception Exception}) that was caught.
  ****************************************************************************/
  public void error (Source source, Parameters parameters,
  		     String text, Throwable throwable) {
    log(ERROR_EVENT,source,parameters,text,throwable,null);
  }
  
  /****************************************************************************
  * log.
  ****************************************************************************/
  private void log (int level, Source source, Parameters parameters, String text, Throwable throwable, String mode)
  {
    String threadName = Thread.currentThread().getName();
    String[] stackTrace = getStackTrace(this.getClass().getName());
    LogEvent event = new LogEvent(level,source,parameters,threadName,stackTrace,text,throwable);
    event.setHost(host);
    
    if (toLog == null)
    {
      toLog = new ObjectQueue();
      toLogThread = Util.startThread(new HandleToLog(),true);
      
      if (logServerAddr != null)
      {
      	toServer = new ObjectQueue();
      	toServerThread = Util.startThread(new HandleToServer(),true);
      }
    }
    
    if (mode==null)
    {
      toLog.put(event);

      if (logServerAddr != null)
      {
    	toServer.put(event);
      }
    }
    else if (mode.equals("v"))
    {
      toLog.put(event);
    }
    else if (mode.equals("n"))
    {
      if (logServerAddr != null)
      {
      	toServer.put(event);
      }
    }
  }
  
  /****************************************************************************
  * getStackTrace.
  ****************************************************************************/
  private String[] getStackTrace (String excludeClassName) {
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] elements = t.getStackTrace();
    int startAt = 0;
    for (int i = 0; i < elements.length; i++) {
       if (elements[i].getClassName().equals(excludeClassName)) {
       	 startAt = i + 1;
       }
    }    
    String[] stackTrace = new String[elements.length-startAt];
    for (int i = 0; i < stackTrace.length; i++) {
       stackTrace[i] = elements[startAt+i].toString();
    }
    return stackTrace;
  }
  /****************************************************************************
  * HandleToLog.
  ****************************************************************************/
  private class HandleToLog implements Runnable 
  {
    public void run () 
    {
      LogEvent event = null;
      while (true) 
      {
      	event = (LogEvent) toLog.get();
      	if (event == null)
      	{
      	  // Shutdown...
      	  return;
      	}
      	if (logFiles != null)
      	{
      	  toFile(event);
      	}
        if (toStdOut)
        {
      	  toStdOut(event);
      	}
      }
    }
  }
  /****************************************************************************
  * toFile.
  ****************************************************************************/
  private void toFile (LogEvent event)
  {
    PrintWriter out = null;
    try
    {
      String text = event.toString();
      if (tooBig(logFiles[0],text.length()))
      {
        shiftFiles();
      }
      out = new PrintWriter(new FileOutputStream(logFiles[0],true));
      out.println(text);
      if (doubleSpace)
      {
        out.println("");
      }
      out.close();
    }
    catch (Exception e)
    {
      if (toStdErr)
      {
	System.err.println("NSLS: failed to write event (" + event.toString() + ") to local log file: " + e.toString());
      }
    }
    finally
    {
      try {out.close();} catch (Exception e) {}
    }
  }
  
  private boolean tooBig (File file, int eventSize) {
    if (maxLogFileSize > 0) {
      if (file.exists()) {
        if (file.length() + eventSize > maxLogFileSize) {
          return true;
        }
      }
    }
    return false;
  }
  private void shiftFiles () throws Exception {
    int last = logFiles.length - 1;
    if (logFiles[last].exists()) {
      if (!logFiles[last].delete()) {
        throw new Exception("failed to delete last local log file");
      }
    }
    for (int i = last - 1; i >= 0; i--) {
       if (logFiles[i].exists()) {
         if (!logFiles[i].renameTo(logFiles[i+1])) {
           throw new Exception("failed to rename local log file");
         }
       }
    }
    if (!logFiles[0].createNewFile()) {
      throw new Exception("failed to create local log file");
    }
  }
  /****************************************************************************
  * toStdOut.
  ****************************************************************************/
  private void toStdOut (LogEvent event) {
    System.out.println("NSLS: " + event.toString());
    if (doubleSpace) {
      System.out.println("");
    }
  }
  /****************************************************************************
  * HandleToServer.
  ****************************************************************************/
  private class HandleToServer implements Runnable {
    private LinkedList<File> tmpList = null;
    private int nextTmpSeq = 0;
    //-------------------------------------------------------------------------
    // run.
    //-------------------------------------------------------------------------
    public void run () {
      if (tmpDir != null) {
      	try {
      	  loadTmpList();
      	} catch (Exception e) {
	  if (toStdErr) {
	    System.err.println("NSLS: failed to load temporary events: " + e.toString());
	  }
      	  tmpDir = null;
      	}
      }
      while (true) {
      	Object o = toServer.get();
	if (o == null) {
	  // Shutdown...
	  return;
	}
      	if (o instanceof LogEvent) {
      	  LogEvent event = (LogEvent) o;
      	  if (tmpDir != null) {
      	    if (tmpList.size() > 0) {
      	      toTmp(event);
      	    } else {
      	      toServer(event);
      	    }
      	  } else {
      	    toServer(event);
      	  }
      	  continue;
      	}
      	if (o instanceof TryTmpList) {
      	  tryTmpList();
      	  continue;
      	}
      }
    }
    //-------------------------------------------------------------------------
    // loadTmpList.
    //-------------------------------------------------------------------------
    private void loadTmpList () throws Exception {
      tmpList = new LinkedList<File>();
      File dir = new File(tmpDir);
      File[] files = dir.listFiles();
      if (files == null) {
      	throw new Exception("temporary directory listing failed");
      }
      // Remove any non-files (eg. .svn directory).
      LinkedList<File> list = new LinkedList<File>();
      for (File file : files) {
	 if (file.isFile()) {
	   list.add(file);
	 }
      }
      files = list.toArray(new File[list.size()]);
      // Check for no temporary events.
      if (files.length == 0) {
      	nextTmpSeq = 0;
      	return;
      }
      // Sort by sequence number.
      Arrays.sort(files,new SeqComparator());
      for (int i = 0; i < files.length; i++) {
      	 tmpList.addLast(files[i]);
      }
      File youngestFile = (File) tmpList.getLast();
      String name = youngestFile.getName();
      nextTmpSeq = Integer.parseInt(name.substring(1,name.length()-4)) + 1;
      tryTmpList();
    }
    //-------------------------------------------------------------------------
    // tryTmpList.
    //-------------------------------------------------------------------------
    private void tryTmpList () {
      while (tmpList.size() > 0) {
      	File tmpFile = tmpList.getFirst();
      	if (!tryTmpEvent(tmpFile)) {
      	  break;
      	}
      	if (!tmpFile.delete()) {
      	  if (toStdErr) {
	    System.err.println("NSLS: failed to delete temporary event file");
      	  }
      	}
      	tmpList.removeFirst();
      }
      if (tmpList.size() > 0) {
        Timer timer = new Timer(true);
        timer.schedule(new TryTmpList(),100000);
      } else {
      	nextTmpSeq = 0;
      }
    }
    //-------------------------------------------------------------------------
    // tryTmpEvent.
    //-------------------------------------------------------------------------
    private boolean tryTmpEvent (File tmpFile) {
      LogEvent event = null;
      ObjectInputStream in = null;
      try {
      	in = new ObjectInputStream(new FileInputStream(tmpFile));
      	event = (LogEvent) in.readObject();
      	in.close();
      } catch (Exception e) {
      	// A corrupt event object - lie and say that it was handled
      	// successfully so that it will be removed and deleted.
	if (toStdErr) {
	  System.err.println("NSLS: corrupt temporary event found (discarding)");
      	}
      	return true;
      } finally {
      	try {in.close();} catch (Exception e) {}
      }
      ConnectionHandler ch = null;
      try {
        Socket s = new Socket(logServerAddr.getHost(),logServerAddr.getPort());
        ch = new ConnectionHandler(s,null,true);
        ch.send(event);
        ch.close();
      	return true;
      } catch (Exception e) {
      	return false;
      } finally {
      	try {ch.close();} catch (Exception e) {}
      }
    }
    //-------------------------------------------------------------------------
    // TryTmpList.
    //-------------------------------------------------------------------------
    private class TryTmpList extends TimerTask {
      public void run () {
      	toServer.put(this);
      }
    }
    //-------------------------------------------------------------------------
    // SeqComparator.
    //-------------------------------------------------------------------------
    private class SeqComparator implements Comparator<File> {
      public int compare (File f1, File f2) {
        // File f1 = (File) o1;
        String n1 = f1.getName();
        int s1 = Integer.parseInt(n1.substring(1,n1.length()-4));
        // File f2 = (File) o2;
        String n2 = f2.getName();
        int s2 = Integer.parseInt(n2.substring(1,n2.length()-4));
        return s1 - s2;
      }
    }
    //-------------------------------------------------------------------------
    // toServer.
    //-------------------------------------------------------------------------
    private void toServer (LogEvent event) {
      ConnectionHandler ch = null;
      try {
        Socket s = new Socket(logServerAddr.getHost(),logServerAddr.getPort());
        ch = new ConnectionHandler(s,null,true);
        ch.send(event);
        ch.close();
      } catch (Exception e) {
      	if (tmpDir != null) {
      	  toTmp(event);
      	} else {
      	  if (toStdErr) {
	    System.err.println("NSLS: failed to write event (" + event.toString() + ") to server and temporary directory is not available: " + e.toString());
      	  }
      	}
      } finally {
      	try {ch.close();} catch (Exception e) {}
      }
    }
    //-------------------------------------------------------------------------
    // toTmp.
    //-------------------------------------------------------------------------
    private void toTmp (LogEvent event) {
      if (MAX_TMP_FILES > 0) {
      	while (tmpList.size() > MAX_TMP_FILES) {
      	  File tmpFile = (File) tmpList.removeFirst();
      	  if (!tmpFile.delete()) {
      	    if (toStdErr) {
	      System.err.println("NSLS: failed to delete temporary log file");
      	    }
      	  }
      	}
      }
      File tmpFile = new File(tmpDir + File.separator + "E" +
      			      Integer.toString(nextTmpSeq++) + ".sjo");
      ObjectOutputStream out = null;
      try {
        out = new ObjectOutputStream(new FileOutputStream(tmpFile));
        out.writeObject(event);
        out.close();
        tmpList.addLast(tmpFile);
        if (tmpList.size() == 1) {
          // If the list was empty (and now has one event), start the timer...
          Timer timer = new Timer(true);
          timer.schedule(new TryTmpList(),100000);
        }
      } catch (Exception e) {
	if (toStdErr) {
	  System.err.println("NSLS: failed to write event (" + event.toString() + ") to temporary log file: " + e.toString());
	}
      } finally {
        try {out.close();} catch (Exception e) {}
      }
    }
  }

  @Override
  public void finalize() throws Throwable{
    super.finalize();
    if (!closed) {
      // To avoid call Log.close() when done or do a try-with-resources
      System.out.println(
              "Log finalizing before Log.close(). You might be leaking memory."
      );
    }
  }

  public void close(){
    if (toLog != null) {
      toLog.put(null);
    }
    if (toServer != null) {
      toServer.put(null);
    }
    closed=true;
  }
}
