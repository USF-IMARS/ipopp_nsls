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
*   7-Feb-07, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.*;
import java.io.*;

public class ETest {
    public static void main (String[] args) throws Exception {
	Log log = new Log("localhost",3500,"./logs/tmp");
	try {
	    a();
	}
	catch (Exception e) {
	    log.error("Top level", LoggableThrowable.create(e));
	}
    }
    
    static void a() throws Exception
    {
	try {
	    b();
	}
	catch (Exception e) {
	    throw new Exception("Middle level", e);
	}
    }
    static void b() throws Exception
    {
	throw new Exception("Bottom level");
    }
}
