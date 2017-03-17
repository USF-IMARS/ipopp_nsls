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
*  2014-02-24	Hack "layout manager" to force single button to low right
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.util;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;

public class LowRightLayout extends BoxLayout
{

    public LowRightLayout(Container target)
    {
	super(target, BoxLayout.X_AXIS);
    }

    /**
     * Simple layout that gets the preferredSize of the container
     * and nails the single component in the container to the
     * lower right corner.
     */
    public void layoutContainer (Container target) {
	int w = target.getWidth();
	int h = target.getHeight();

	RoundButton infoButton = (RoundButton) (target.getComponent(0));
	Dimension infoButtonDim = infoButton.getPreferredSize();
	int infoButtonHeight = (int)(infoButtonDim.getHeight());
	int infoButtonWidth = (int) (infoButtonDim.getWidth());

	// Rashly assuming the button will fit in the container at its
	// preferred size...
	infoButton.setSize(infoButtonDim);
	infoButton.setLocation(w-infoButtonWidth, h-infoButtonHeight);
    }
}
