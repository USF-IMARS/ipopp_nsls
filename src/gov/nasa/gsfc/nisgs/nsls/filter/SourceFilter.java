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
*   9-Jun-06, 	Added RT-STPS.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.filter;
import gov.nasa.gsfc.nisgs.nsls.*;
import java.io.*;

public class SourceFilter implements Filter, Serializable {
  private static final long serialVersionUID = 1L;
  private boolean passMMS;
  private boolean passDSM;
  private boolean passNCS;
  private boolean passIS;
  private boolean passSC;
  private boolean passRTSTPS;
  private boolean not;
  /****************************************************************************
  * Filters on the source of an event.
  * @param passMMS Whether or not events from the MMS should pass.
  * @param passDSM Whether or not events from the DSM should pass.
  * @param passNCS Whether or not events from the NCS should pass.
  * @param passIS Whether or not events from the IS should pass.
  * @param passSC Whether or not events from the SC should pass.
  * @param passRTSTPS Whether or not events from the RT-STPS should pass.
  * @param not If true, the filter passes if the source is NOT one of those
  * specified.
  ****************************************************************************/
  public SourceFilter (boolean passMMS, boolean passDSM, boolean passNCS,
  		       boolean passIS, boolean passSC, boolean passRTSTPS,
  		       boolean not) {
    this.passMMS = passMMS;
    this.passDSM = passDSM;
    this.passNCS = passNCS;
    this.passIS = passIS;
    this.passSC = passSC;
    this.passRTSTPS = passRTSTPS;
    this.not = not;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean passes (LogEvent event) {
    if (event.getSource() instanceof NSLS.MMS) {
      return not ^ passMMS;
    }
    if (event.getSource() instanceof NSLS.DSM) {
      return not ^ passDSM;
    }
    if (event.getSource() instanceof NSLS.NCS) {
      return not ^ passNCS;
    }
    if (event.getSource() instanceof NSLS.IS) {
      return not ^ passIS;
    }
    if (event.getSource() instanceof NSLS.SC) {
      return not ^ passSC;
    }
    if (event.getSource() instanceof NSLS.RTSTPS) {
      return not ^ passRTSTPS;
    }
    return not ^ false;
  }
}
