/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  History:
*
*  27-Sep-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.io.*;
import java.net.*;

public class ConnectionHandler implements Runnable {
  private Socket s;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ConnectionListener listener;
  private boolean closed = false;
  /****************************************************************************
  * ConnectionHandler.
  ****************************************************************************/
  public ConnectionHandler (Socket s, ConnectionListener listener, boolean isClient) throws Exception {
    this.s = s;
    if (isClient) {
      this.out = new ObjectOutputStream(s.getOutputStream());
      this.in = new ObjectInputStream(s.getInputStream());
    } else {
      this.in = new ObjectInputStream(s.getInputStream());
      this.out = new ObjectOutputStream(s.getOutputStream());
    }
    this.listener = listener;
  }
  /****************************************************************************
  * setListener.
  ****************************************************************************/
  public void setListener (ConnectionListener listener) {
    this.listener = listener;
  }
  /****************************************************************************
  * getSocket.
  ****************************************************************************/
  public Socket getSocket () {
    return s;
  }
  /****************************************************************************
  * run.
  ****************************************************************************/
  public void run () {
    try {
      while (true) {
        Object o = in.readObject();
	// System.out.println("ConnectionHandler:run: " + o.toString());
        if (listener != null) {
          listener.received(o);
        }
      }
    } catch (EOFException e) {
      // System.out.println("ConnectionHandler:run:EOFException");
      if (!closed) {
        if (listener != null) {
	  listener.disconnected(null);
        }
      }
    } catch (Exception e) {
      // System.out.println("ConnectionHandler:run:Exception: " + e.toString());
      if (!closed) {
      	if (listener != null) {
	  listener.disconnected(e);
      	}
      }
    } finally {
      close();
    }
  }
  /****************************************************************************
  * send.
  ****************************************************************************/
  public void send (Object o) throws Exception {
    out.writeObject(o);
    out.flush();
    out.reset();
  }
  /****************************************************************************
  * close.
  ****************************************************************************/
  public void close () {
    // System.out.println("ConnectionHandler:close");
    closed = true;
    try {s.shutdownOutput();} catch (Exception ex) {}
    try {s.shutdownInput();} catch (Exception ex) {}
    try {out.close();} catch (Exception ex) {}
    try {in.close();} catch (Exception ex) {}
    try {s.close();} catch (Exception ex) {}
  }
}
