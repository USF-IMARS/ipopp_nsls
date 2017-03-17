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
*  18-Mar-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.message;
import gov.nasa.gsfc.nisgs.nsls.filter.*;
import java.io.*;

public class PlayEvents implements Serializable {
  private static final long serialVersionUID = 1L;
  private int maxCount;
  private Filters filters;
  /****************************************************************************
  * PlayEvents.
  ****************************************************************************/
  public PlayEvents (int maxCount, Filters filters) {
    this.maxCount = maxCount;
    this.filters = filters;
  }
  /****************************************************************************
  * getMaxCount.
  ****************************************************************************/
  public int getMaxCount () {
    return maxCount;
  }
  /****************************************************************************
  * getFilters.
  ****************************************************************************/
  public Filters getFilters () {
    return filters;
  }
}
