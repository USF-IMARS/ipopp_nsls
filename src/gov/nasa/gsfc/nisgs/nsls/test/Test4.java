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

public class Test4 {
  public static void main (String[] args) throws Exception {
    Log log = new Log("localhost",3500,"./logs/tmp");
    log.setToLogFile(new File("./logs/test4.log"));
    log.setToStdOut(true);
    log.setToStdErr(true);
    log.error("test",new TestException2("test"));
  }
  private static class TestException2 extends Exception {
    private static final long serialVersionUID = 1L;
    public TestException2 (String text) {
      super(text);
    }
  }
}
