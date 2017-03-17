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
*  20-Jun-05, 	Original version.
*   3-Jan-06, 	Fixed javadoc comments.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.filter;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;
import java.util.*;
import java.io.*;

public class Filters implements Serializable {
  private LinkedList<Filter> filters = new LinkedList<Filter>();
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * 
  ****************************************************************************/
  public Filters () {

  }
  /****************************************************************************
  * @param filter1 1st filter to be added.
  ****************************************************************************/
  public Filters (Filter filter1) {
    filters.add(filter1);
  }
  /****************************************************************************
  * @param filter1 1st filter to be added.
  * @param filter2 2nd filter to be added.
  ****************************************************************************/
  public Filters (Filter filter1, Filter filter2) {
    filters.add(filter1);
    filters.add(filter2);
  }
  /****************************************************************************
  * @param filter1 1st filter to be added.
  * @param filter2 2nd filter to be added.
  * @param filter3 3rd filter to be added.
  ****************************************************************************/
  public Filters (Filter filter1, Filter filter2, Filter filter3) {
    filters.add(filter1);
    filters.add(filter2);
    filters.add(filter3);
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public DateTimeFilter getDateTimeFilter () {
	for (Filter filter : filters) {
    // Iterator it = filters.iterator();
    // while (it.hasNext()) {
      // Filter filter = (Filter) it.next();
      if (filter instanceof DateTimeFilter) {
      	return (DateTimeFilter) filter;
      }
    }
    return null;
  }
  /****************************************************************************
  * @param filter Filter to be added.
  ****************************************************************************/
  public void addFilter (Filter filter) {
    filters.add(filter);
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean areNoFilters () {
    return (filters.size() < 1);
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean passes (LogEvent event) {
	for (Filter filter : filters) {
    // Iterator it = filters.iterator();
    // while (it.hasNext()) {
      // Filter filter = (Filter) it.next();
      if (!filter.passes(event)) {
      	return false;
      }
    }
    return true;
  }
}
