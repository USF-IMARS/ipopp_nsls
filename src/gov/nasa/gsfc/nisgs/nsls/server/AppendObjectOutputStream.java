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
*  11-Mar-05, 	Original version.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.server;
import java.io.*;

public class AppendObjectOutputStream extends ObjectOutputStream {
  /****************************************************************************
  * AppendObjectOutputStream.
  ****************************************************************************/
  public AppendObjectOutputStream (OutputStream out) throws Exception {
    super(out);
  }
  /****************************************************************************
  * writeStreamHeader.
  ****************************************************************************/
  protected void writeStreamHeader () {
    // Don't...we're appending.
  }
}
