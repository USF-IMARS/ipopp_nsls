/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
/*******************************************************************************
 *
 * Little wrapper class around standard ProgressMonitor to allow simple
 * bumping of progress
 ******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.console;
import gov.nasa.gsfc.nisgs.nsls.console.ProgressMonitor;
import java.awt.Component;
import java.awt.Dimension;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SimpleProgressMonitor extends ProgressMonitor {
    int currentState;
    Pattern progressPattern;

    /**
     * It started out as a "simple" progress monitor, then a few extras were
     * added: bumpState() which increments the progress counter,
     * and setProgressLine() which sets the note line and bumps the progress
     * counter if the line Matches the progressLineHead string.
     * This is based on our extended ProgressMonitor which allows an extra
     * boolean flag to enable/disable the Cancel button (it is replaced with
     * an "OK" button) and a setPreferredSize(Dimension) method.
     */
    public SimpleProgressMonitor (Component pc, Object msg, String note, int max, String progressPatString)
	throws Exception
    {
	super(pc, msg, note, 0, max, false);
	// Pop this up at once
	setMillisToDecideToPopup(1);
	setMillisToPopup(1);
	setPreferredSize(new Dimension(400,167));
	currentState=0;
	setProgress(currentState);
	progressPattern = Pattern.compile(progressPatString);
    }

    public void setMaximum(int max) {
	super.setMaximum(max);
	//System.err.println("SPM NEW MAX: " + max);
    }

    public int getCurrentState() { return currentState; }

    public void bumpState() {
	setProgress(++currentState);
    }

    /**
     * Set the note line with this string.  If the string matches
     * progressLineHead, also bump our progress counter
     */
    public void setProgressLine(String line)
	throws Exception

    {
	setNote(line);
	
	if(progressPattern.matcher(line).find())
	    bumpState();
    }

}
