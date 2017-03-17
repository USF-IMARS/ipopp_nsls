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
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.filter.*;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/*********************************************************************
 * This class connects to an SLS server and prints its log messages
 * in a tab-delimited text format, optionally filtered by date/time
 * and message type.
 *
 * Command line options are:
 *
 * -host NSLS-HOST
 *	Defaults to localhost
 * -startdate yyyy-mm-ddThh:mm:ss
 *	Defaults to now - 1 day
 * -enddate yyyy-mm-ddThh:mm:ss
 *	Defaults to now
 * -eventlevel [iwe]
 *	Selects for INFO(i), WARNING(w), ERROR(e) messages
 *	Default is print everything
 *
 *********************************************************************/

public class LogPrinter {

    public static void main (String[] args) throws Exception {
	String nslshost = "localhost";
	Date endDate = new Date();
	Date startDate = new Date(endDate.getTime() - 86400 * 1000);
	Filter levelFilter = null;
	DateFormat dtFormat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	for(int i=0; i < args.length;i++) {
	    if (args[i].equals("-host")) {
		nslshost = args[++i];
	    }
	    else if (args[i].equals("-startdate")) {
		startDate = dtFormat.parse(args[++i]);
	    }
	    else if (args[i].equals("-enddate")) {
		endDate = dtFormat.parse(args[++i]);
	    }
	    else if (args[i].equals("-eventlevel")) {
		String levels = args[++i];
		levelFilter = new LevelFilter(levels.contains("i"),
					      levels.contains("w"),
					      levels.contains("e"),
					      false);
	    }
	    else
		throw new Exception("Unrecognized option: " + args[i]);
	}

	// Sanity check
	//System.err.println("Time window: " + startDate + " : " + endDate);
	if (endDate.getTime() <= startDate.getTime())
	    throw new Exception("End date (" + endDate + ") is before start date (" + startDate + ")");

	// Connect up the LogReader
	LogReader reader = new LogReader(nslshost, 3500);
	// Create the DateTimeFilter
	Filter dtFilter = new DateTimeFilter(startDate, endDate, false);
	// And the Filters list (adding the LevelFilter if needed)
	Filters logFilters = null;
	if(levelFilter == null)
	    logFilters = new Filters(dtFilter);
	else
	    logFilters = new Filters(levelFilter, dtFilter);

	// Go get the filtered list of events
	LogEvent[] events = reader.search(0, logFilters);

	// And print them out
	for (int i = 0; i < events.length; i++) {
	    String event_level = events[i].encodeLevel();
	    if (events[i].getThrowable() != null) {
		System.out.println(event_level + "\t" + events[i].getDate() + "\t" + events[i].getHost() + "\t" + events[i].getSource() + "\t" + events[i].getText() + "\t"  + events[i].getThrowable());
	    } else {
		System.out.println(event_level + "\t" + events[i].getDate() + "\t" + events[i].getHost() + "\t" + events[i].getSource() + "\t" + events[i].getText() );
	    }
	}
    }
}
