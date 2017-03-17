/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/

package gov.nasa.gsfc.nisgs.nsls.util;
import  java.io.*;
import gov.nasa.gsfc.nisgs.nsls.console.SimpleProgressMonitor;

/**
* Read an input stream, optionally write to an output file, optionally
* log to a ProgressMonitor, using a separate thread.
* The main thread enters the constructor and starts the reader thread and then
* exits without blocking.
* <BR>
* This copier was written to read the standard and error streams from a Process.
* Copied from support code for Ncs_run and simplified.
*/

public class StreamCopier implements Runnable 
{
    InputStream instream;
    BufferedReader in = null;
    FileWriter    out = null;
    SimpleProgressMonitor pm = null;
    Exception   ioe = null;
    Thread    cthread = null;
	
	
    /**
     * Create a StreamCopier Object. The main thread exits and the object
     * begins copying from the input stream to the output file. 
     *
     * @param instream Input stream.
     * @param outputfile Output file name.  If null, output is discarded.
     * @param ProgressMonitor object; if null, no logging
     * @throws Exception if error creating input stream or output file. 
     */
	
    public StreamCopier( InputStream instream,  String outputfile, SimpleProgressMonitor pm)
	throws Exception
    {
	this.instream = instream;
	in  = new BufferedReader( new InputStreamReader(instream) );
	if (outputfile != null)
	    out = new FileWriter( outputfile );
	this.pm = pm;

	cthread = new Thread(this,"strcpy");
	cthread.start();
    }

	
    /** 
     * Thread created in constructor enters this method to copy streams.
     * Not called from outside this class.  
     */
    public void run()
    {
	try 
	    {
		String line = null;
		while( (line = in.readLine()) != null )
		    {
			if( out != null ) out.write( line + "\n" );
			if( pm != null) {
			    pm.setProgressLine(line);
			}
		    }
				
		in.close();
	    }
	catch( Exception exc )
	    {
		ioe = exc;
	    }
	finally {
	    // Strangely enough, close() on the BufferedReader is
	    // not guaranteed to close() the underlying stream.
	    try {
		if( out != null) out.close();
		instream.close();
	    }
	    catch (Exception ex2) {}
	}
    }

	
    /**
     * Wait for input stream to close.
     * @throws Exception If an exception was generated while reading the
     * input stream or writing to the file
     */
    public void close() throws Exception
    {
	cthread.join();
	if( ioe != null ) throw ioe;
    }
}
		

