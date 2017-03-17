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
*   8-Feb-06, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.util.*;

public class ObjectQueue {
  private LinkedList<Object> list = new LinkedList<Object>();
  /****************************************************************************
  * put.
  ****************************************************************************/
  public synchronized void put (Object object) {
    list.addLast(object);
    notify();
  }
  /****************************************************************************
  * get.
  ****************************************************************************/
  public synchronized Object get () {
    while (list.size() < 1) {
      try {
	wait();
      } catch (InterruptedException e) {
	// Ignored...
      }
    }
    return list.removeFirst();
  }
  /****************************************************************************
  * clear.
  ****************************************************************************/
  public synchronized void clear () {
    list.clear();
  }
}
