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
*  11-Jul-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.filter;
import gov.nasa.gsfc.nisgs.nsls.*;
import java.io.*;

public class LevelFilter implements Filter, Serializable {
  private static final long serialVersionUID = 1L;
  private boolean passInfos;
  private boolean passWarnings;
  private boolean passErrors;
  private boolean not;
  /****************************************************************************
  * Filters on the severity level of an event.
  * @param passInfos Whether or not info events should pass.
  * @param passWarnings Whether or not warning events should pass.
  * @param passErrors Whether or not error events should pass.
  * @param not If true, the filter passes if the level is NOT one of those
  * specified.
  ****************************************************************************/
  public LevelFilter (boolean passInfos, boolean passWarnings,
          	      boolean passErrors, boolean not) {
    this.passInfos = passInfos;
    this.passWarnings = passWarnings;
    this.passErrors = passErrors;
    this.not = not;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean passes (LogEvent event) {
    switch (event.getLevel()) {
      case Log.INFO_EVENT: {
      	return not ^ passInfos;
      }
      case Log.WARNING_EVENT: {
      	return not ^ passWarnings;
      }
      case Log.ERROR_EVENT: {
      	return not ^ passErrors;
      }
    }
    return true;
  }
}
