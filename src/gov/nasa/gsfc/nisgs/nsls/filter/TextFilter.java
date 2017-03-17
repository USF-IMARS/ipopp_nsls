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

public class TextFilter implements Filter, Serializable {
  private static final long serialVersionUID = 1L;
  private String text;
  private boolean textOnly;
  private boolean not;
  /****************************************************************************
  * Filters on the text of an event.
  * @param text The text string to search for.
  * @param textOnly Whether or not only the text field of an event should be
  * searched.  If false, the string representation of the entire event is
  * searched.
  * @param not If true, the filter passes if the text is NOT found.
  ****************************************************************************/
  public TextFilter (String text, boolean textOnly, boolean not) {
    this.text = text;
    this.textOnly = textOnly;
    this.not = not;
  }
  /****************************************************************************
  * 
  ****************************************************************************/
  public boolean passes (LogEvent event) {
    if (textOnly) {
      if (event.getText() != null) {
        return not ^ event.getText().contains(text);
      }
      return not ^ false;
    }
    return not ^ event.toString().contains(text);
  }
}
