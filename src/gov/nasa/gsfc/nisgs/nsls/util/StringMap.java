/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/******************************************************************************
*
*  SIMULCAST/UTIL
*
*  History:
*
*   7-Jan-05, 	Original version.
*   8-Feb-06, 	Java 1.5.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import java.util.*;

public class StringMap {
  private LinkedList<String> strings = new LinkedList<String>();
  private LinkedList<Object> objects = new LinkedList<Object>();
  /****************************************************************************
  * StringMap.
  ****************************************************************************/
  public StringMap () {

  }
  /****************************************************************************
  * put.
  ****************************************************************************/
  public void put (String string, Object object) {
    if (!contains(string)) {
      strings.add(string);
      objects.add(object);
    } else {
      objects.set(indexOf(string),object);
    }
  }
  /****************************************************************************
  * contains.
  ****************************************************************************/
  public boolean contains (String string) {
    for (int i = 0; i < strings.size(); i++) {
       if (string.equals(strings.get(i))) {
       	 return true;
       }
    }
    return false;
  }
  /****************************************************************************
  * get.
  ****************************************************************************/
  public Object get (int index) {
    return objects.get(index);
  }
  public Object get (String string) {
    for (int i = 0; i < strings.size(); i++) {
       if (string.equals((String)strings.get(i))) {
       	 return objects.get(i);
       }
    }
    return null;
  }
  /****************************************************************************
  * getStrings.
  ****************************************************************************/
  public String[] getStrings () {
    return (String[]) strings.toArray(new String[strings.size()]);
  }
  /****************************************************************************
  * getObjects.
  ****************************************************************************/
  public Object[] getObjects () {
    return objects.toArray();
  }
  /****************************************************************************
  * indexOf.
  ****************************************************************************/
  public int indexOf (String string) {
    for (int i = 0; i < strings.size(); i++) {
       if (string.equals(strings.get(i))) {
       	 return i;
       }
    }
    return -1;
  }
  /****************************************************************************
  * iterator.
  ****************************************************************************/
  public Iterator<Object> iterator () {
    return objects.iterator();
  }
  /****************************************************************************
  * remove.
  ****************************************************************************/
  public Object remove (String string) {
    for (int i = 0; i < strings.size(); i++) {
       if (string.equals(strings.get(i))) {
         Object o = objects.get(i);
	 strings.remove(i);
	 objects.remove(i);
	 return o;
       }
    }
    return null;
  }
  /****************************************************************************
  * size.
  ****************************************************************************/
  public int size () {
    return strings.size();
  }
  /****************************************************************************
  * clear.
  ****************************************************************************/
  public void clear () {
    strings.clear();
    objects.clear();
  }
}
