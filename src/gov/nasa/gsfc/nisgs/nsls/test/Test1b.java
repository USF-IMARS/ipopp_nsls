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
*  14-Dec-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.*;
import java.util.*;

public class Test1b {
  public static void main (String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: Test1 <host:port>");
      System.exit(0);
    }
    StringTokenizer byColon = new StringTokenizer(args[0],":");
    String host = byColon.nextToken();
    int port = Integer.parseInt(byColon.nextToken());
    Log log = new Log(host,port,"./logs/tmp");
    log.info((Log.Source)null,"test");
  }
}
