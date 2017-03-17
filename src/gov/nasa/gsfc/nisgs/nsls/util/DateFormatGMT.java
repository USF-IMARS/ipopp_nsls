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
import java.text.*;

public class DateFormatGMT extends SimpleDateFormat {
  private static final long serialVersionUID = 1L;
  /****************************************************************************
  * DateFormatGMT.
  ****************************************************************************/
  public DateFormatGMT (String pattern) {
    super(pattern);
    setLenient(false);
    setTimeZone(TimeZone.getTimeZone("GMT"));
  }
}
