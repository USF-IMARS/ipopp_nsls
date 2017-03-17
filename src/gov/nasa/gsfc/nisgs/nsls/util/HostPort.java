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
*  23-Jan-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.util.*;
import java.net.*;

public class HostPort {
  private String host;
  private int port;
  /****************************************************************************
  * HostPort.
  ****************************************************************************/
  public HostPort (String host, int port) {
    this.host = host;
    this.port = port;
  }
  public HostPort (String hostPort) throws Exception {
    StringTokenizer byColon = new StringTokenizer(hostPort,":");
    switch (byColon.countTokens()) {
      case 2: {
      	this.host = byColon.nextToken();
      	this.port = Integer.parseInt(byColon.nextToken());
      	break;
      }
      default: {
      	throw new Exception("illegal host:port");
      }
    }
  }
  public HostPort (ServerSocket ss) {
    this.host = ss.getInetAddress().getHostAddress();
    this.port = ss.getLocalPort();
  }
  public HostPort (Socket s) {
    this.host = s.getInetAddress().getHostAddress();
    this.port = s.getPort();
  }
  /****************************************************************************
  * getHost.
  ****************************************************************************/
  public String getHost () {
    return host;
  }
  /****************************************************************************
  * getPort.
  ****************************************************************************/
  public int getPort () {
    return port;
  }
  /****************************************************************************
  * toString.
  ****************************************************************************/
  public String toString () {
    return host + ":" + Integer.toString(port);
  }
}
