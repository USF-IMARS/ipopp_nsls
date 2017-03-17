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

public interface ConnectionListener {
  public void received (Object o);
  public void disconnected (Exception e);
}
