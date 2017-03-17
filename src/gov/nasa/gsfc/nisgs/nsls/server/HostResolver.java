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
*  23-Aug-06, 	Original version.
*   1-Feb-07, 	Daemon threads for Windows service.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.net.*;

public class HostResolver {
  private StringMap hostsByIp = new StringMap();
  private static final boolean DEBUG = false;
  /****************************************************************************
  * HostResolver.
  ****************************************************************************/
  public HostResolver () {

  }
  /****************************************************************************
  * getHost.
  ****************************************************************************/
  public String getHost (InetAddress ia) {
    String name = (String) hostsByIp.get(ia.getHostAddress());
    if (name != null) {
      if (!name.equals("")) {
	return name;
      }
    } else {
      hostsByIp.put(ia.getHostAddress(),"");
      Util.startThread(new GetName(ia),true);
    }
    Util.sleep(3000);
    name = (String) hostsByIp.get(ia.getHostAddress());
    if (!name.equals("")) {
      return name;
    }
    return ia.getHostAddress();
  }
  /****************************************************************************
  * GetName.
  ****************************************************************************/
  private class GetName implements Runnable {
    private InetAddress ia;
    public GetName (InetAddress ia) {
      this.ia = ia;
    }
    public void run () {
      if (DEBUG) {
	System.out.println("HostResolver.GetName: resolving " + ia.getHostAddress());
      }
      try {
	String name = ia.getHostName();
        if (DEBUG) {
	  System.out.println("HostResolver.GetName: resolved " + ia.getHostAddress() + " to " + name);
        }
        hostsByIp.put(ia.getHostAddress(),name);
      } catch (Exception e) {
	Util.startThread(new GetName(ia),true);
      }
    }
  }
}
