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

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.filter.*;

public class Test3 {
  public static void main (String[] args) throws Exception {
    LogReader reader = new LogReader("localhost",4005);
    LogEvent[] events = reader.search(0,new Filters());
    // LogEvent[] events = reader.search(0,new Filters(new LevelFilter(false,false,true),new SourceFilter(false,false,true,false,false,false)));
    for (int i = 0; i < events.length; i++) {
       System.out.println(i+1 + " " + events[i].toString());
    }
  }
}
