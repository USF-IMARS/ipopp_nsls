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
*  11-Jul-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.filter;
import gov.nasa.gsfc.nisgs.nsls.*;
import java.util.*;
import java.io.*;

public class DateTimeFilter implements Filter, Serializable {
  private static final long serialVersionUID = 1L;
  private Date start;
  private Date stop;
  private boolean not;
  /****************************************************************************
  * Filters on a date/time range.
  * @param start Beginning of date/time range (inclusive).
  * @param stop End of date/time range (inclusive).
  * @param not If true, the filter passes if the event is NOT within the
  * start/stop range (inclusive).
  ****************************************************************************/
  public DateTimeFilter (Date start, Date stop, boolean not) {
    this.start = start;
    this.stop = stop;
    this.not = not;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean isNot () {
    return not;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public Date getStartDate () {
    return start;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public Date getStopDate () {
    return stop;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean passes (LogEvent event) {
    boolean isBefore = event.getDate().before(start);
    boolean isAfter = event.getDate().after(stop);
    return not ^ (!(isBefore || isAfter));
  }
}
