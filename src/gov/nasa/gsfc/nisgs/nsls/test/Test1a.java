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
*  26-Jul-05, 	Original version.
*  31-Jan-07, 	Thread handling.
*
******************************************************************************/

package gov.nasa.gsfc.nisgs.nsls.test;
import gov.nasa.gsfc.nisgs.nsls.*;
import gov.nasa.gsfc.nisgs.nsls.util.*;
import java.util.*;
import java.io.*;

public class Test1a implements Runnable {
  private File[] logFiles;
  public static void main (String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: Test1a <file1> <file2> <file3>");
      System.exit(0);
    }
    Util.startThread(new Test1a(new File[] {new File(args[0]),new File(args[1]),new File(args[2])}));
  }
  public Test1a (File[] logFiles) {
    this.logFiles = logFiles;
  }
  public void run () {
    try {
      // Log log = new Log(true);
      Log log = new Log(logFiles,0);
      while (true) {
        log.info(new NSLS.NCS("Station4"),"started up");
        Util.sleep(1.154);
        log.warning(new NSLS.NCS("Station4","Group7","Job12"),
    	            new NSLS.ProductParameters("product1","granule2","pass3",null,null,null),
		    "I'm running out of memory!");
        Util.sleep(0.967);
        log.info(new NSLS.IS("FileCopy"),new NSLS.FileParameters("../5.pds"),"file copy: starting");
        Util.sleep(0.254);
        // log.error(new NSLS.IS("FileCopy"),new NSLS.FileParameters("../5.pds"),"file copy: failed",new Exception("oops"));
        Util.sleep(1.045);
        log.error(new NSLS.NCS("Station4","Group7","Job12"),
    	          new NSLS.ProductParameters("product1","granule2","pass3","type5",new Date(),new Date()),
		  "error while opening file: test1.dat\nsdfasdfdsfljds as dsdfasdf asd fasd fasdf asd as df asdf asdfasdfasdf asdf asdfasdfasdfa dfasdfasfdasdfasfd asdfasfdasfdasd f asdfasfdasfdasfdasdfasfdasfdasfd asdf asdfasfdasfdasdf asdf asfdasdfasfd asdfasdfasdfasdf asdfasfasfdasdfasdfasdfasfd asdfasfdasfdasfdasdf asdfasfdasfdasdfasfd asdfasdfasfdasfdasdfasfd asdf asdfasfdasdfasfdasfas asfdas fd asdf asd asd asdfas",
    	          new Exception("no such file: test1.dat",new Exception("help me")));
        log.error(new NSLS.NCS("Station4","Group7","Job12"),
    	          new NSLS.ProductParameters("product1","granule2","pass3","type5",new Date(),new Date()),
		  "error while opening file: test1.dat\nsdf ddsddddddddddddddddddddddddddddddddddddkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrnnnnnnnnnnnnnnnnnnnnnnnnnnnnnneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeooooooooooooooooooooooooooooooooooooooooxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeekkkkkkkkkkkkkkkkkkkkkkkkqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqpppppppppppppppppppppppppppppppppppppssssssssssssssssssssssss",
    	          new Exception("no such file: test1.dat",new Exception("help me")));
        Util.sleep(0.887);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
