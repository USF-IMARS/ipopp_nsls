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
*   8-Jul-05, 	Original version.
*  31-Jan-07, 	Thread handling.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.LogEvent;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.io.*;

public class Test2 implements Runnable {
  private String path;
  public static void main (String[] args) {
    Util.startThread(new Test2(args[0]));
  }
  public Test2 (String path) {
    this.path = path;
  }
  public void run () {
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
      while (true) {
      	LogEvent event = (LogEvent) in.readObject();
      	System.out.println(event.toString());
      }
    } catch (EOFException e) {

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
