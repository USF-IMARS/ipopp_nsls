/*
Copyright (c) 1999-2007, United States Government, as represented by
the Administrator for The National Aeronautics and Space Administration.
All rights reserved.
*/
package gov.nasa.gsfc.nisgs.nsls;

/**
 * This class exists to hold static methods for converting a random
 * Throwable into a vanilla Throwable that can be safely serialized,
 * sent to the NSLS logger, and (this is the important part) correctly
 * deserialized.  It also insures that the stack trace from the original
 * exception is transferred (unlike the canonical Throwable(Throwable)
 * constructor, which defaults to tossing that information - *bleah*).
 */

public class LoggableThrowable {

    public static Throwable create (Throwable in)
    {
	return create("", in);
    }

    public static Throwable create (String message, Throwable in)
    {
	Throwable out = new Throwable("(really " + in.getClass().getName() + ") "
				      + in.getMessage());
	out.setStackTrace(in.getStackTrace());
	if(in.getCause() != null)
	    out.initCause(create(in.getCause()));
	return out;
    }
}
